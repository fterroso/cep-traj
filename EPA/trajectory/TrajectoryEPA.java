
/*
 * Copyright 2014 University of Murcia (Fernando Terroso-Saenz (fterroso@um.es), Mercedes Valdes-Vela, Antonio F. Skarmeta)
 * 
 * This file is part of CEP-traj.
 * 
 * CEP-traj is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CEP-traj is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see http://www.gnu.org/licenses/.
 * 
 */
package ceptraj.EPA.trajectory;

import ceptraj.EPA.EPA;
import ceptraj.EPA.trajectory.listener.NewLevelPointListener;
import ceptraj.EPA.trajectory.listener.TrajectoryAvgSpeedListener;
import ceptraj.EPA.trajectory.listener.TrajectoryListener;
import ceptraj.EPA.trajectory.listener.TrajectoryChangeListener;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import org.apache.log4j.Logger;
import ceptraj.output.EventConsumer;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class TrajectoryEPA extends EPA{
    
    static Logger LOG = Logger.getLogger(TrajectoryEPA.class);   
    
    private static final String CONTEXT_ID_LEVEL_EPL = "CREATE CONTEXT segmentedByIdAndLevel "+
                                                 "PARTITION BY id, level "+
                                                 "FROM FilteredLocationEvent";       

    private static final String TRAJECTORY_EPL =   "CONTEXT segmentedByIdAndLevel "+
                                                 "INSERT INTO TrajectoryEvent "+
                                                 "SELECT  id as id, "+   
                                                 "        current_timestamp as timestamp,"+
                                                 "        A.level as level, "+       
                                                 "        first(A.location.timestamp) as initialTimestamp, "+
                                                 "        last(A.location.timestamp) as finalTimestamp, "+
                                                 "        avgBearing(window(A.location)) as avgBearing, "+
                                                 "        bearing(first(A.location), last(A.location)) as straightBearing, "+ 
                                                 "        bearing(last(A.location), first(A.location)) as revStraightBearing, "+ 
                                                 "        window(A.location) as locations, "+
                                                 "        avg(A.location.speed) as avgSpeed, "+
                                                 "        euclideanDist(last(A.location), first(A.location))/((last(A.timestamp) - first(A.timestamp))/1000) as avgInfSpeed, "+
                                                 "        last(A.location.speed) as currentSpeed "+
                                                 "FROM FilteredLocationEvent.win:length(2) A ";//+
    
    private static final String CONTEXT_TRAJ_LEVEL_1_EPL =    "CREATE CONTEXT segmentedByIdAndLevel1 "+
                                                        "PARTITION BY id, level "+
                                                        "FROM TrajectoryEvent(level=1)";
    
    
    private static final String AVG_TRAJECTORY_SPEED_EPL =    "CONTEXT segmentedByIdAndLevel1 "+
                                                        "SELECT  id, last(timestamp) as timestamp, avg(avgSpeed)/3.6 as speed "+
                                                        "FROM TrajectoryEvent.win:ext_timed(timestamp, 60 seconds)";                                                            
    
    private static final String BEARING_TRAJECTORY_INC_CHANGE_EPL =  "INSERT INTO BearingTrajectoryChangeEvent "+
                                                               "SELECT TrajectoryChangeType.BEARING_INCREASING as changeType, "+
                                                               "       id,"+
                                                               "       timestamp,"+
                                                               "       level,"+
                                                               "       initialBearing,"+
                                                               "       finalBearing,"+
                                                               "       initialTimestamp,"+
                                                               "       finalTimestamp, "+
                                                               "       tail,"+
                                                               "       head,"+            
                                                               "       middle "+                                               
                                                               "FROM TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE, avgInfSpeed > stopThreshold(id)) "+
                                                               "MATCH_RECOGNIZE ( "+
                                                               "    PARTITION BY id,level "+
                                                               "    MEASURES current_timestamp as timestamp, "+
                                                               "             A.id as id, "+
                                                               "             A.level as level, "+
                                                               "             A.straightBearing as initialBearing, "+
                                                               "             C.straightBearing as finalBearing, "+
                                                               "             A.initialTimestamp as initialTimestamp, "+
                                                               "             C.finalTimestamp as finalTimestamp, "+
                                                               "             A.locations as tail, "+
                                                               "             aggMiddlePoint(B.locations) as middle, "+
                                                               "             C.locations as head "+
                                                               "    PATTERN (A B* C)  "+
                                                               "    DEFINE "+
                                                               "           B as "+
                                                               "                isIncreasingBearing(B.straightBearing, A.straightBearing) and "+
                                                               "                (isIncreasingBearing(B.straightBearing, prev(B.straightBearing,1)) or"+
                                                               "                bearingDifference(B.straightBearing, prev(B.straightBearing,1)) <= (prev(B.straightBearing,1) * 0.01)), " +
                                                               "           C as "+
                                                               "                bearingDifference(C.straightBearing, A.straightBearing) >= bearingMinChange and "+
                                                               "                isIncreasingBearing(C.straightBearing, A.straightBearing) "+
                                                               " )";
    
    private static final String BEARING_TRAJECTORY_DEC_CHANGE_EPL =  "INSERT INTO BearingTrajectoryChangeEvent "+
                                                               "SELECT TrajectoryChangeType.BEARING_DECREASING as changeType, "+
                                                               "       id,"+
                                                               "       timestamp,"+
                                                               "       level,"+            
                                                               "       initialBearing,"+
                                                               "       finalBearing,"+
                                                               "       initialTimestamp,"+
                                                               "       finalTimestamp, "+            
                                                               "       tail,"+
                                                               "       head,"+            
                                                               "       middle "+                                               
                                                               "FROM TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE, avgInfSpeed > stopThreshold(id)) "+
                                                               "MATCH_RECOGNIZE ( "+
                                                               "    PARTITION BY id,level "+
                                                               "    MEASURES current_timestamp as timestamp, "+
                                                               "             A.id as id, "+
                                                               "             A.level as level, "+            
                                                               "             A.straightBearing as initialBearing, "+
                                                               "             C.straightBearing as finalBearing, "+
                                                               "             A.initialTimestamp as initialTimestamp, "+
                                                               "             C.finalTimestamp as finalTimestamp, "+      
                                                               "             A.locations as tail, "+
                                                               "             aggMiddlePoint(B.locations) as middle, "+
                                                               "             C.locations as head "+            
                                                               "    PATTERN (A B* C)  "+
                                                               "    DEFINE "+
                                                               "            B as "+
                                                               "                not isIncreasingBearing(B.straightBearing, A.straightBearing) and "+
                                                               "                (not (isIncreasingBearing(B.straightBearing, prev(B.straightBearing,1))) or"+
                                                               "                bearingDifference(B.straightBearing, prev(B.straightBearing,1)) <= (prev(B.straightBearing,1) * 0.01)), " +
                                                               "            C as "+ 
                                                               "                bearingDifference(C.straightBearing, A.straightBearing) >= bearingMinChange and "+
                                                               "                not isIncreasingBearing(C.straightBearing, A.straightBearing) " +            
                                                               " )";    
    
    
    private static final String SPEED_TRAJECTORY_INC_CHANGE_EPL = "INSERT INTO SpeedTrajectoryChangeEvent "+
                                                               "SELECT TrajectoryChangeType.SPEED_INCREASING as changeType, "+
                                                               "       id,"+
                                                               "       timestamp,"+
                                                               "       level,"+
                                                               "       initialSpeed,"+
                                                               "       finalSpeed,"+
                                                               "       initialTimestamp,"+
                                                               "       finalTimestamp, "+
                                                               "       tail,"+
                                                               "       head,"+            
                                                               "       middle "+                                               
                                                               "FROM TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) "+
                                                               "MATCH_RECOGNIZE ( "+
                                                               "    PARTITION BY id,level "+
                                                               "    MEASURES current_timestamp as timestamp, "+
                                                               "             A.id as id, "+
                                                               "             A.level as level, "+
                                                               "             A.avgInfSpeed as initialSpeed, "+
                                                               "             C.avgInfSpeed as finalSpeed, "+
                                                               "             A.initialTimestamp as initialTimestamp, "+
                                                               "             C.finalTimestamp as finalTimestamp, "+
                                                               "             A.locations as tail, "+
                                                               "             aggMiddlePoint(B.locations) as middle, "+
                                                               "             C.locations as head "+            
                                                               "    PATTERN (A B* C)  "+
                                                               "    DEFINE "+ 
                                                               "           B as B.avgInfSpeed > stopThreshold(B.id) and "+ 
                                                               "                B.avgInfSpeed > A.avgInfSpeed and "+
                                                               "                B.avgInfSpeed >= prev(B.avgInfSpeed,1), " +
                                                               "           C as (C.avgInfSpeed - A.avgInfSpeed) >= (A.avgInfSpeed * speedMinChangePerc) and "+ 
                                                               "                C.avgInfSpeed > stopThreshold(A.id) "+
                                                               " )";
    
    private static final String SPEED_TRAJECTORY_DEC_CHANGE_EPL =  "INSERT INTO SpeedTrajectoryChangeEvent "+
                                                               "SELECT TrajectoryChangeType.SPEED_DECREASING as changeType, "+
                                                               "       id,"+
                                                               "       timestamp,"+
                                                               "       level,"+
                                                               "       initialSpeed,"+
                                                               "       finalSpeed,"+
                                                               "       initialTimestamp,"+
                                                               "       finalTimestamp, "+
                                                               "       tail,"+
                                                               "       head,"+            
                                                               "       middle "+                                              
                                                               "FROM TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) "+
                                                               "MATCH_RECOGNIZE ( "+
                                                               "    PARTITION BY id,level "+
                                                               "    MEASURES current_timestamp as timestamp, "+
                                                               "             A.id as id, "+
                                                               "             A.level as level, "+
                                                               "             A.avgInfSpeed as initialSpeed, "+
                                                               "             C.avgInfSpeed as finalSpeed, "+
                                                               "             A.initialTimestamp as initialTimestamp, "+
                                                               "             C.finalTimestamp as finalTimestamp, "+
                                                               "             A.locations as tail, "+
                                                               "             aggMiddlePoint(B.locations) as middle, "+
                                                               "             C.locations as head "+            
                                                               "    PATTERN (A B* C)  "+
                                                               "    DEFINE "+
                                                               "           A as A.avgInfSpeed > stopThreshold(A.id), "+
                                                               "           B as B.avgInfSpeed > stopThreshold(A.id) and "+
                                                               "                B.avgInfSpeed <= prev(B.avgInfSpeed,1) and " +
                                                               "                B.avgInfSpeed < A.avgInfSpeed, "+
                                                               "           C as (A.avgInfSpeed - C.avgInfSpeed) >= (A.avgInfSpeed * speedMinChangePerc) "+
                                                               " )";
    
    private static final String MULTI_TRAJECTORY_CHANGE_EPL = "INSERT INTO TrajectoryMultiChangeEvent "+
                                                        "SELECT "+
                                                        "       A.id as id, "+
                                                        "       A.level as level, "+
                                                        "       current_timestamp as timestamp, "+
                                                        "       TrajectoryChangeType.MULTI_CHANGE as changeType, "+
                                                        "       max(A.initialTimestamp, B.initialTimestamp) as initialTimestamp, "+
                                                        "       min(A.finalTimestamp, B.finalTimestamp) as finalTimestamp,"+
                                                        "       A.involvedPath.intersect(B.involvedPath) as involvedPath, "+
                                                        "       A.changeValue as value1, "+
                                                        "       B.changeValue as value2, "+
                                                        "       A.underlayingTypes as type1, "+
                                                        "       B.underlayingTypes as type2 "+
                                                        "FROM "+
                                                        "   TrajectoryChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                        "   TrajectoryChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE).std:groupwin(id,level,changeType).win:length(2) B "+
                                                        "WHERE "+
                                                        "   A.id = B.id and  "+
                                                        "   A.level = B.level and "+
                                                        "   A.initialTimestamp < B.finalTimestamp and "+
                                                        "   A.isDifferentChange(B.underlayingTypes)";
     
        
    EPStatement contextStatement;
    EPStatement trajectoryStatement;
    
    EPStatement contextForLevel1Statement;
    EPStatement trajectoryAvgSpeedStatement;    
    
    EPStatement bearingIncStatement;
    EPStatement bearingDecStatement;
    
    EPStatement newLevelPointStatement;
    
    EPStatement speedIncStatement;
    EPStatement speedDecStatement;
    
    EPStatement multiChangeStatement;
    
    @Override
    public void start(EPServiceProvider CEPProvider, EventConsumer eventConsumer) {
        CEPEngine = CEPProvider;
        
        contextStatement = CEPEngine.getEPAdministrator().createEPL(CONTEXT_ID_LEVEL_EPL);
        contextForLevel1Statement = CEPEngine.getEPAdministrator().createEPL(CONTEXT_TRAJ_LEVEL_1_EPL);
        
        trajectoryStatement = CEPEngine.getEPAdministrator().createEPL(TRAJECTORY_EPL);
        TrajectoryListener trajList = new TrajectoryListener(eventConsumer);
        trajectoryStatement.addListener(trajList); 
        
        trajectoryAvgSpeedStatement = CEPEngine.getEPAdministrator().createEPL(AVG_TRAJECTORY_SPEED_EPL);
               
        bearingIncStatement = CEPEngine.getEPAdministrator().createEPL(BEARING_TRAJECTORY_INC_CHANGE_EPL);
        TrajectoryChangeListener trajChangeList = new TrajectoryChangeListener(eventConsumer);
        bearingIncStatement.addListener(trajChangeList);

        bearingDecStatement = CEPEngine.getEPAdministrator().createEPL(BEARING_TRAJECTORY_DEC_CHANGE_EPL);
        bearingDecStatement.addListener(trajChangeList);
        


    }
    
}
