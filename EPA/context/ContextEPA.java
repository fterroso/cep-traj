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
package ceptraj.EPA.context;

import ceptraj.EPA.EPA;
import ceptraj.EPA.context.listener.RelationshipWithAreaListener;
import ceptraj.EPA.context.listener.TrajectoryChangeWithAreaListener;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import ceptraj.output.EventConsumer;
import ceptraj.output.BasicEventConsumer;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class ContextEPA extends EPA{

    
    //RELATIONSHIPS
    private static final String RELATIONSHIP_TOTALLY_CONTAINS = "INSERT INTO RelationShipEvent "+
                                                                "SELECT "+
                                                                "       A.id as id, "+
                                                                "       A.timestamp as timestamp, "+
                                                                "       A.locations1 as locations1, "+
                                                                "       A.locations2 as locations2, "+
                                                                "       A.level as level, "+
                                                                "       A.bearing1 as bearing1, "+
                                                                "       A.bearing2 as bearing2, "+
                                                                "       A.relationshipType as relationshipType, "+
                                                                "       A.finalTimestamp as finalTimestamp, "+
                                                                "       A.initialTimestamp as initialTimestamp, "+
                                                                "       B.name as involvedAreaOfInterestName, "+
                                                                "       AreaOfInterestRelationshipType.TOTALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   RelationShipEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.contains(A.locations1) AND "+
                                                                "   B.area.contains(A.locations2) ";
    
    private static final String RELATIONSHIP_PARTIALLY_CONTAINS = "INSERT INTO RelationShipEvent "+
                                                                "SELECT "+
                                                                "       A.id as id, "+
                                                                "       A.timestamp as timestamp, "+
                                                                "       A.locations1 as locations1, "+
                                                                "       A.locations2 as locations2, "+
                                                                "       A.level as level, "+
                                                                "       A.bearing1 as bearing1, "+
                                                                "       A.bearing2 as bearing2, "+
                                                                "       A.relationshipType as relationshipType, "+
                                                                "       A.finalTimestamp as finalTimestamp, "+
                                                                "       A.initialTimestamp as initialTimestamp, "+
                                                                "       B.name as involvedAreaOfInterestName, "+
                                                                "       AreaOfInterestRelationshipType.PARTIALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   RelationShipEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   (B.area.contains(A.locations1) AND "+            
                                                                "   not B.area.contains(A.locations2)) OR "+
                                                                "   (not B.area.contains(A.locations1) AND "+
                                                                "   B.area.contains(A.locations2))";            
    
    private static final String RELATIONSHIP_CLOSE_TO =         "INSERT INTO RelationShipEvent "+
                                                                "SELECT "+
                                                                "       A.id as id, "+
                                                                "       A.timestamp as timestamp, "+
                                                                "       A.locations1 as locations1, "+
                                                                "       A.locations2 as locations2, "+
                                                                "       A.level as level, "+
                                                                "       A.bearing1 as bearing1, "+
                                                                "       A.bearing2 as bearing2, "+
                                                                "       A.relationshipType as relationshipType, "+
                                                                "       A.finalTimestamp as finalTimestamp, "+
                                                                "       A.initialTimestamp as initialTimestamp, "+
                                                                "       B.name as involvedAreaOfInterestName, "+
                                                                "       AreaOfInterestRelationshipType.CLOSE_TO as areaRelationship "+
                                                                "FROM "+
                                                                "   RelationShipEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.isWithinDistance(A.locations1, minDistToClose) AND "+            
                                                                "   B.area.isWithinDistance(A.locations2, minDistToClose)";  
    
    private static final String RELATIONSHIP_OUT =            "INSERT INTO RelationShipEvent "+
                                                                "SELECT "+
                                                                "       A.id as id, "+
                                                                "       A.timestamp as timestamp, "+
                                                                "       A.locations1 as locations1, "+
                                                                "       A.locations2 as locations2, "+
                                                                "       A.level as level, "+
                                                                "       A.bearing1 as bearing1, "+
                                                                "       A.bearing2 as bearing2, "+
                                                                "       A.relationshipType as relationshipType, "+
                                                                "       A.finalTimestamp as finalTimestamp, "+
                                                                "       A.initialTimestamp as initialTimestamp, "+
                                                                "       B.name as involvedAreaOfInterestName, "+
                                                                "       AreaOfInterestRelationshipType.OUT as areaRelationship "+
                                                                "FROM "+
                                                                "   RelationShipEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   not B.area.contains(A.locations1) AND "+            
                                                                "   not B.area.contains(A.locations2)";
    
    //SPEED CHANGE    
    private static final String SPEED_TOTALLY_CONTAINS =   "INSERT INTO SpeedTrajectoryChangeEvent "+
                                                               "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialSpeed as initialSpeed,"+
                                                               "       A.finalSpeed as finalSpeed,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.TOTALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   SpeedTrajectoryChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.contains(A.headPoint) AND "+            
                                                                "   B.area.contains(A.middlePoint) AND "+
                                                                "   B.area.contains(A.tailPoint)";
    
    
    private static final String SPEED_PARTIALLY_CONTAINS =   "INSERT INTO SpeedTrajectoryChangeEvent "+
                                                               "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialSpeed as initialSpeed,"+
                                                               "       A.finalSpeed as finalSpeed,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.PARTIALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   SpeedTrajectoryChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   (B.area.contains(A.headPoint) AND "+            
                                                                "   B.area.contains(A.middlePoint) AND "+
                                                                "   not B.area.contains(A.tailPoint)) OR "+
                                                                "   (B.area.contains(A.headPoint) AND "+            
                                                                "   not B.area.contains(A.middlePoint) AND "+
                                                                "   B.area.contains(A.tailPoint)) OR "+
                                                                "   (not B.area.contains(A.headPoint) AND "+            
                                                                "   B.area.contains(A.middlePoint) AND "+
                                                                "   B.area.contains(A.tailPoint))";

    
    private static final String SPEED_CLOSE_TO =            "INSERT INTO SpeedTrajectoryChangeEvent "+
                                                                "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialSpeed as initialSpeed,"+
                                                               "       A.finalSpeed as finalSpeed,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.CLOSE_TO as areaRelationship "+
                                                                "FROM "+
                                                                "   SpeedTrajectoryChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.isWithinDistance(A.headPoint, minDistToClose) OR "+            
                                                                "   B.area.isWithinDistance(A.middlePoint, minDistToClose) OR "+
                                                                "   B.area.isWithinDistance(A.tailPoint, minDistToClose) ";
    
    private static final String SPEED_CHANGE_OUT =            "INSERT INTO SpeedTrajectoryChangeEvent "+
                                                                "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialSpeed as initialSpeed,"+
                                                               "       A.finalSpeed as finalSpeed,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.OUT as areaRelationship "+
                                                                "FROM "+
                                                                "   SpeedTrajectoryChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   not B.area.contains(A.headPoint) AND "+            
                                                                "   not B.area.contains(A.middlePoint) AND "+
                                                                "   not B.area.contains(A.tailPoint) ";
    
    //BEARING CHANGE    
    private static final String BEARING_TOTALLY_CONTAINS =   "INSERT INTO BearingTrajectoryChangeEvent "+
                                                               "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialBearing as initialBearing,"+
                                                               "       A.finalBearing as finalBearing,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.TOTALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   BearingTrajectoryChangeEvent( areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.contains(A.headPoint) AND "+            
                                                                "   B.area.contains(A.middlePoint) AND "+
                                                                "   B.area.contains(A.tailPoint)";
    
    
    private static final String BEARING_PARTIALLY_CONTAINS =   "INSERT INTO BearingTrajectoryChangeEvent "+
                                                               "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialBearing as initialBearing,"+
                                                               "       A.finalBearing as finalBearing,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.PARTIALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   BearingTrajectoryChangeEvent( areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   (B.area.contains(A.headPoint) AND "+            
                                                                "   B.area.contains(A.middlePoint) AND "+
                                                                "   not B.area.contains(A.tailPoint)) OR "+
                                                                "   (B.area.contains(A.headPoint) AND "+            
                                                                "   not B.area.contains(A.middlePoint) AND "+
                                                                "   B.area.contains(A.tailPoint)) OR "+
                                                                "   (not B.area.contains(A.headPoint) AND "+            
                                                                "   B.area.contains(A.middlePoint) AND "+
                                                                "   B.area.contains(A.tailPoint))";

    
    private static final String BEARING_CLOSE_TO =            "INSERT INTO BearingTrajectoryChangeEvent "+
                                                                "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialBearing as initialBearing,"+
                                                               "       A.finalBearing as finalBearing,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.CLOSE_TO as areaRelationship "+
                                                                "FROM "+
                                                                "   BearingTrajectoryChangeEvent( areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.isWithinDistance(A.headPoint, minDistToClose) OR "+            
                                                                "   B.area.isWithinDistance(A.middlePoint, minDistToClose) OR "+
                                                                "   B.area.isWithinDistance(A.tailPoint, minDistToClose) ";
    
    private static final String BEARING_CHANGE_OUT =            "INSERT INTO BearingTrajectoryChangeEvent "+
                                                                "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.initialBearing as initialBearing,"+
                                                               "       A.finalBearing as finalBearing,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+            
                                                               "       A. tailNumSeq as tailNumSeq,"+            
                                                               "       A.headNumSeq as headNumSeq, "+  
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.OUT as areaRelationship "+
                                                                "FROM "+
                                                                "   BearingTrajectoryChangeEvent( areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   not B.area.contains(A.headPoint) AND "+            
                                                                "   not B.area.contains(A.middlePoint) AND "+
                                                                "   not B.area.contains(A.tailPoint) ";
    
    // Multi
    private static final String MULTI_TOTALLY_CONTAINS =   "INSERT INTO TrajectoryMultiChangeEvent "+
                                                               "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.changeValue as changeValue, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+             
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.TOTALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   TrajectoryMultiChangeEvent( areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.contains(A.involvedPath)";
    
    private static final String MULTI_PARTIALLY_CONTAINS =   "INSERT INTO TrajectoryMultiChangeEvent "+
                                                               "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.changeValue as changeValue, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+             
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.PARTIALLY_CONTAINS as areaRelationship "+
                                                                "FROM "+
                                                                "   TrajectoryMultiChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   (B.area.contains(A.headPoint) AND "+            
                                                                "   not B.area.contains(A.tailPoint)) OR "+
                                                                "   (not B.area.contains(A.headPoint) AND "+            
                                                                "   B.area.contains(A.tailPoint))";
    
    private static final String MULTI_CLOSE_TO =   "INSERT INTO TrajectoryMultiChangeEvent "+
                                                               "SELECT "+
                                                               "       A.id as id,"+
                                                               "       A.timestamp as timestamp,"+
                                                               "       A.level as level,"+
                                                               "       A.changeType as changeType, "+
                                                               "       A.changeValue as changeValue, "+
                                                               "       A.initialTimestamp as initialTimestamp,"+
                                                               "       A.finalTimestamp as finalTimestamp, "+
                                                               "       A.involvedPath as involvedPath, "+             
                                                               "       B.name as involvedAreaOfInterestName, "+
                                                               "       AreaOfInterestRelationshipType.CLOSE_TO as areaRelationship "+
                                                                "FROM "+
                                                                "   TrajectoryMultiChangeEvent( areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                                "   AreaOfInterestEvent.std:unique(name) B "+
                                                                "WHERE "+
                                                                "   B.area.isWithinDistance(A.involvedPath, minDistToClose)";
    
    private static final String MULTI_OUT =   "INSERT INTO TrajectoryMultiChangeEvent "+
                                                   "SELECT "+
                                                   "       A.id as id,"+
                                                   "       A.timestamp as timestamp,"+
                                                   "       A.level as level,"+
                                                   "       A.changeType as changeType, "+
                                                   "       A.changeValue as changeValue, "+
                                                   "       A.initialTimestamp as initialTimestamp,"+
                                                   "       A.finalTimestamp as finalTimestamp, "+
                                                   "       A.involvedPath as involvedPath, "+             
                                                   "       B.name as involvedAreaOfInterestName, "+
                                                   "       AreaOfInterestRelationshipType.OUT as areaRelationship "+
                                                    "FROM "+
                                                    "   TrajectoryMultiChangeEvent(areaRelationship = AreaOfInterestRelationshipType.NONE) A unidirectional, "+
                                                    "   AreaOfInterestEvent.std:unique(name) B "+
                                                    "WHERE "+
                                                    "   not B.area.contains(A.involvedPath)";
    
    
    EPStatement relationshipTotallyContains;
    EPStatement relationshipPartiallyContains;
    EPStatement relationshipCloseTo;
    EPStatement relationshipOut;
    
    EPStatement speedChangeTotallyContains;
    EPStatement speedChangePartiallyContains;
    EPStatement speedChangeCloseTo;
    EPStatement speedChangeOut;

    EPStatement bearingChangeTotallyContains;
    EPStatement bearingChangePartiallyContains;
    EPStatement bearingChangeCloseTo;
    EPStatement bearingChangeOut; 
    
    EPStatement multiChangeTotallyContains;
    EPStatement multiChangePartiallyContains;
    EPStatement multiChangeCloseTo;
    EPStatement multiChangeOut;
    
    @Override
    public void start(EPServiceProvider CEPProvider, EventConsumer eventConsumer) {
        CEPEngine = CEPProvider;
        
        relationshipTotallyContains = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_TOTALLY_CONTAINS);
        RelationshipWithAreaListener list = new RelationshipWithAreaListener(eventConsumer);
        relationshipTotallyContains.addListener(list);    

        relationshipPartiallyContains = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_PARTIALLY_CONTAINS);
        relationshipPartiallyContains.addListener(list);  
        
        relationshipCloseTo = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_CLOSE_TO);
        relationshipCloseTo.addListener(list);

        relationshipOut = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_OUT);
        relationshipOut.addListener(list);
        
        speedChangeTotallyContains = CEPEngine.getEPAdministrator().createEPL(SPEED_TOTALLY_CONTAINS);
        TrajectoryChangeWithAreaListener tjCL = new TrajectoryChangeWithAreaListener(eventConsumer);
        speedChangeTotallyContains.addListener(tjCL);
        
        speedChangePartiallyContains = CEPEngine.getEPAdministrator().createEPL(SPEED_PARTIALLY_CONTAINS);
        speedChangePartiallyContains.addListener(tjCL);
        
        speedChangeCloseTo = CEPEngine.getEPAdministrator().createEPL(SPEED_CLOSE_TO);
        speedChangeCloseTo.addListener(tjCL);
        
        speedChangeOut = CEPEngine.getEPAdministrator().createEPL(SPEED_CHANGE_OUT);
        speedChangeOut.addListener(tjCL);
        
        //Bearing changes
        bearingChangeTotallyContains = CEPEngine.getEPAdministrator().createEPL(BEARING_TOTALLY_CONTAINS);
        bearingChangeTotallyContains.addListener(tjCL);
        
        bearingChangePartiallyContains = CEPEngine.getEPAdministrator().createEPL(BEARING_PARTIALLY_CONTAINS);
        bearingChangePartiallyContains.addListener(tjCL);
        
        bearingChangeCloseTo = CEPEngine.getEPAdministrator().createEPL(BEARING_CLOSE_TO);
        bearingChangeCloseTo.addListener(tjCL);
        
        bearingChangeOut = CEPEngine.getEPAdministrator().createEPL(BEARING_CHANGE_OUT);
        bearingChangeOut.addListener(tjCL);
        
        //Multi changes
        multiChangeTotallyContains = CEPEngine.getEPAdministrator().createEPL(MULTI_TOTALLY_CONTAINS);
        multiChangeTotallyContains.addListener(tjCL);
        
        multiChangePartiallyContains = CEPEngine.getEPAdministrator().createEPL(MULTI_PARTIALLY_CONTAINS);
        multiChangePartiallyContains.addListener(tjCL);
        
        multiChangeCloseTo = CEPEngine.getEPAdministrator().createEPL(MULTI_CLOSE_TO);
        multiChangeCloseTo.addListener(tjCL);
        
        multiChangeOut = CEPEngine.getEPAdministrator().createEPL(MULTI_OUT);
        multiChangeOut.addListener(tjCL);        
      
    }    
}
