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
package ceptraj.EPA.relationship;

import ceptraj.EPA.EPA;
import ceptraj.EPA.relationship.listener.RelationListener;
import ceptraj.EPA.relationship.listener.RelationWinAdaptorListener;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import ceptraj.config.ConfigProvider;
import ceptraj.output.EventConsumer;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class RelationEPA extends EPA{
    
    // Adaptation mechanism rules
    private static final String RELATIONSHIP_WIN_SIZE_MAX_ADAPTOR_EPL = "SELECT "+
                                                                        " EventHierarchy.setWinSizeForRelationLevel(A.level, A.timestamp-A.initialTimestamp) as a "+
                                                                        "FROM TrajectoryEvent A "+
                                                                        "WHERE "+
                                                                        "   (A.timestamp-A.initialTimestamp) > EventHierarchy.getWinSizeForRelationLevel(A.level) ";
    
    private static final String RELATIONSHIP_WIN_SIZE_AVG_ADAPTOR_EPL = "SELECT "+
                                                                        " EventHierarchy.addNewWinSizeForRelationLevel(A.level, A.timestamp-A.initialTimestamp) as a "+
                                                                        "FROM TrajectoryEvent A ";
    
    private static final String RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_1 =   "INSERT INTO RelationWinSizeOpEvent "+
                                                                            "SELECT "+
                                                                            " EventHierarchy.increaseWinSizeForRelationLevel(A.level) as type, "+
                                                                            " A.level as level "+
                                                                            "FROM TrajectoryEvent A "+
                                                                            "WHERE "+
                                                                            "   (A.timestamp-A.initialTimestamp) > EventHierarchy.getWinSizeForRelationLevel(A.level) ";
    
    private static final String RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_2 =   "INSERT INTO RelationWinSizeOpEvent "+
                                                                            "SELECT "+
                                                                            " EventHierarchy.decreaseWinSizeForRelationLevel(A.level) as type, "+
                                                                            " A.level as level "+
                                                                            "FROM TrajectoryEvent A "+
                                                                            "WHERE "+
                                                                            "   (A.timestamp-A.initialTimestamp) < EventHierarchy.getWinSizeForRelationLevel(A.level) ";
    
    private static final String RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_3 = "SELECT "+
                                                                          " EventHierarchy.incementModifyValueForLevel(a.level) "+
                                                                          "FROM "+
                                                                          " PATTERN ["+
                                                                          "     every ( a=RelationWinSizeOpEvent -> (([3] b=RelationWinSizeOpEvent(level= a.level, type = a.type)) and not RelationWinSizeOpEvent(level= a.level, type != a.type)))"+
                                                                          "]";
    
    private static final String RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_4 = "SELECT "+
                                                                          " EventHierarchy.decrementModifyValueForLevel(a.level) "+
                                                                          "FROM "+
                                                                          " PATTERN ["+
                                                                          "     every (a=RelationWinSizeOpEvent -> RelationWinSizeOpEvent(level= a.level, type != a.type))"+
                                                                          "]";
    
    private static final String CONTEXT_TRAJECTORY_LEVEL_EPL = "CREATE CONTEXT segmentedByLevel "+
                                                               "PARTITION BY level "+
                                                               "FROM TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE, avgInfSpeed > stopThreshold(id))";
    
    //Relationships detection rules
    private static final String PARALLEL_RELATIONSHIP_EPL = "CONTEXT segmentedByLevel "+
                                                            "INSERT INTO RelationEvent "+
                                                            "SELECT "+
                                                            "        CASE WHEN A.id.compareTo(B.id) < 0 THEN A.id || \"_\" || B.id ELSE B.id || \"_\" || A.id END as id, "+ 
                                                            "       current_timestamp as timestamp, "+
                                                            "       A.locations as locations1, "+
                                                            "       B.locations as locations2, "+
                                                            "       A.level as level, "+
                                                            "       A.straightBearing as bearing1, "+
                                                            "       B.straightBearing as bearing2, "+
                                                            "       A.avgInfSpeed as speed1,"+
                                                            "       B.avgInfSpeed as speed2,"+
                                                            "       ceptraj.event.relationship.RelationType.PARALLEL as relationshipType, "+
                                                            "       min(A.finalTimestamp, B.finalTimestamp) as finalTimestamp, "+
                                                            "       max(A.initialTimestamp, B.initialTimestamp) as initialTimestamp "+
                                                            "FROM "+
                                                            "   TrajectoryEvent A unidirectional, "+
                                                            "   TrajectoryEvent.win:expr(oldest_timestamp > newest_timestamp - EventHierarchy.getWinSizeForRelationLevel(level)) B "+
                                                            "WHERE "+
                                                            "      A.id != B.id and "+
                                                            "      A.avgInfSpeed > stopThreshold(A.id) and "+            
                                                            "      B.avgInfSpeed > stopThreshold(B.id) and "+            
                                                            "      B.finalTimestamp.between(A.initialTimestamp, A.finalTimestamp) and "+
                                                            "      haversineDistance2(A.tail, A.straightBearing, A.avgInfSpeed, B.finalTimestamp, A.initialTimestamp, B.head) <= parallelMaxDist and "+
                                                            "      bearingDifference(A.straightBearing, B.straightBearing) <= parallelMaxBearingDiff";
    
    
    private static final String PERPENDICULAR_RELATIONSHIP_EPL = 
                                                            "CONTEXT segmentedByLevel "+
                                                            "INSERT INTO RelationEvent "+
                                                            "SELECT "+
                                                            "        CASE WHEN A.id.compareTo(B.id) < 0 THEN A.id || \"_\" || B.id ELSE B.id || \"_\" || A.id END as id, "+ 
                                                            "       current_timestamp as timestamp, "+
                                                            "       A.locations as locations1, "+
                                                            "       B.locations as locations2, "+
                                                            "       A.level as level, "+
                                                            "       A.straightBearing as bearing1, "+
                                                            "       B.straightBearing as bearing2, "+
                                                            "       A.avgInfSpeed as speed1,"+
                                                            "       B.avgInfSpeed as speed2,"+            
                                                            "       ceptraj.event.relationship.RelationType.PERPENDICULAR as relationshipType, "+
                                                            "       max(A.finalTimestamp, B.finalTimestamp) as finalTimestamp, "+
                                                            "       min(A.initialTimestamp, B.initialTimestamp) as initialTimestamp "+
                                                            "FROM "+
                                                            "   TrajectoryEvent A unidirectional, "+
                                                            "   TrajectoryEvent.win:expr(oldest_timestamp > newest_timestamp - EventHierarchy.getWinSizeForRelationLevel(level)) B "+
                                                            "WHERE "+
                                                            "      A.id != B.id and "+
                                                            "      A.avgInfSpeed > stopThreshold(A.id) and "+            
                                                            "      B.avgInfSpeed > stopThreshold(B.id) and "+
                                                            "      B.finalTimestamp.between(A.initialTimestamp, A.finalTimestamp) and "+
                                                            "      haversineDistance2(A.tail, A.straightBearing, A.avgInfSpeed, B.finalTimestamp, A.initialTimestamp, B.head) <= perpendicularMaxDist and "+
                                                            "      bearingDifference(A.straightBearing, B.straightBearing) in [85:95]";
                                                 

    private static final String CONVERGE_RELATIONSHIP_EPL = "CONTEXT segmentedByLevel "+
                                                            "INSERT INTO RelationEvent "+
                                                            "SELECT "+
                                                            "       CASE WHEN A.id.compareTo(B.id) < 0 THEN A.id || \"_\" || B.id ELSE B.id || \"_\" || A.id END as id, "+ 
                                                            "       current_timestamp as timestamp, "+
                                                            "       A.locations as locations1, "+
                                                            "       B.locations as locations2, "+
                                                            "       A.level as level, "+
                                                            "       A.straightBearing as bearing1, "+
                                                            "       B.straightBearing as bearing2, "+
                                                            "       A.avgInfSpeed as speed1,"+
                                                            "       B.avgInfSpeed as speed2,"+            
                                                            "       CASE WHEN B.straightBearing > A.straightBearing THEN bearing(B.head, A.head) ELSE bearing(A.head, B.head) END as auxBear, "+ 
                                                            "       ceptraj.event.relationship.RelationType.CONVERGE as relationshipType, "+
                                                            "       max(A.finalTimestamp, B.finalTimestamp) as finalTimestamp, "+
                                                            "       min(A.initialTimestamp, B.initialTimestamp) as initialTimestamp "+
                                                            "FROM "+
                                                            "   TrajectoryEvent A unidirectional, "+
                                                            "   TrajectoryEvent.win:expr(oldest_timestamp > newest_timestamp - EventHierarchy.getWinSizeForRelationLevel(level)) B "+
                                                            "WHERE "+
                                                            "   A.id != B.id and "+
                                                            "   A.avgInfSpeed > stopThreshold(A.id) and "+            
                                                            "   B.avgInfSpeed > stopThreshold(B.id) and "+
                                                            "   B.finalTimestamp between A.initialTimestamp and A.finalTimestamp and "+
                                                            "   haversineDistance2(A.tail, A.straightBearing, A.avgInfSpeed, B.finalTimestamp, A.initialTimestamp, B.head) <= convergeMaxDist and "+

                                                            "   Math.abs(B.straightBearing - A.straightBearing) > minBearingDiffForConvergence and "+            
                                                            // Case 1
                                                            "   ((B.straightBearing > A.straightBearing and "+
                                                            "   ((A.straightBearing in (180:360] and "+
                                                            "    contains(bearing(B.head, A.head), B.straightBearing, A.revStraightBearing)) or "+
                                                            
                                                            "   (not A.straightBearing in (180:360] and "+
                                                            "    (B.straightBearing > A.revStraightBearing and "+
                                                            "    contains(bearing(B.head, A.head), A.revStraightBearing, B.straightBearing)) or "+
                                                            "    (B.straightBearing < A.revStraightBearing and "+
                                                            "    contains(bearing(B.head, A.head), B.straightBearing, A.revStraightBearing))) or "+
                      
                                                            "   (bearingDifference(B.straightBearing, A.revStraightBearing) < 10 and "+
                                                            "    contains(bearing(B.head, A.head), modifyBearing(B.straightBearing, -90), modifyBearing(B.straightBearing, 90))))) or "+
                                                            //Case 2
                                                            "   (A.straightBearing > B.straightBearing and "+
                                                            "   ((B.straightBearing in (180:360] and "+
                                                            "    contains(bearing(A.head, B.head), A.straightBearing, B.revStraightBearing)) or "+
                                                            
                                                            "   (not B.straightBearing in (180:360] and "+
                                                            "    (A.straightBearing > B.revStraightBearing and "+
                                                            "    contains(bearing(A.head, B.head), B.revStraightBearing, A.straightBearing)) or "+
                                                            "    (A.straightBearing < B.revStraightBearing and "+
                                                            "    contains(bearing(A.head, B.head), A.straightBearing, B.revStraightBearing))) or "+
                      
                                                            "   (bearingDifference(A.straightBearing, B.revStraightBearing) < 10 and "+
                                                            "    contains(bearing(A.head, B.head), modifyBearing(A.straightBearing, -90), modifyBearing(A.straightBearing, 90)))))) ";
                       

    private static final String DIVERGE_RELATIONSHIP_EPL =  "CONTEXT segmentedByLevel "+
                                                            "INSERT INTO RelationEvent "+
                                                            "SELECT "+
                                                            "       CASE WHEN A.id.compareTo(B.id) < 0 THEN A.id || \"_\" || B.id ELSE B.id || \"_\" || A.id END as id, "+ 
                                                            "       current_timestamp as timestamp, "+
                                                            "       A.locations as locations1, "+
                                                            "       B.locations as locations2, "+
                                                            "       A.level as level, "+
                                                            "       A.straightBearing as bearing1, "+
                                                            "       B.straightBearing as bearing2, "+
                                                            "       A.avgInfSpeed as speed1,"+
                                                            "       B.avgInfSpeed as speed2,"+            
                                                            "       ceptraj.event.relationship.RelationType.DIVERGE as relationshipType, "+
                                                            "       max(A.finalTimestamp, B.finalTimestamp) as finalTimestamp, "+
                                                            "       min(A.initialTimestamp, B.initialTimestamp) as initialTimestamp "+
                                                            "FROM "+
                                                            "   TrajectoryEvent A unidirectional, "+
                                                            "   TrajectoryEvent.win:expr(oldest_timestamp > newest_timestamp - EventHierarchy.getWinSizeForRelationLevel(level)) B "+
                                                            "WHERE "+
                                                            "   A.id != B.id and "+
                                                            "   A.avgInfSpeed > stopThreshold(A.id) and "+            
                                                            "   B.avgInfSpeed > stopThreshold(B.id) and "+            
                                                            "   B.finalTimestamp between A.initialTimestamp and A.finalTimestamp and "+
                                                            "   haversineDistance2(A.tail, A.straightBearing, A.avgInfSpeed, B.finalTimestamp, A.initialTimestamp, B.head) <= divergeMaxDist and "+
            
                                                            "   Math.abs(B.straightBearing - A.straightBearing) > minBearingDiffForDivergence and "+
                                                            // Case 1
                                                            "   ((B.revStraightBearing > A.revStraightBearing and "+
                                                            "   ((A.revStraightBearing in (180:360] and "+
                                                            "    contains(bearing(B.tail, A.tail), B.revStraightBearing, A.straightBearing)) or "+
                                                            
                                                            "   (not A.revStraightBearing in (180:360] and "+
                                                            "    (B.revStraightBearing > A.straightBearing and "+
                                                            "    contains(bearing(B.tail, A.tail), A.straightBearing, B.revStraightBearing)) or "+
                                                            "    (B.revStraightBearing < A.straightBearing and "+
                                                            "    contains(bearing(B.tail, A.tail), B.revStraightBearing, A.straightBearing))) or "+
                      
                                                            "   (bearingDifference(B.revStraightBearing, A.straightBearing) < 10 and "+
                                                            "    contains(bearing(B.tail, A.tail), modifyBearing(B.revStraightBearing, -90), modifyBearing(B.revStraightBearing, 90))))) or "+
                                                            //Case 2
                                                            "   (A.revStraightBearing > B.revStraightBearing and "+
                                                            "   ((B.revStraightBearing in (180:360] and "+
                                                            "    contains(bearing(A.tail, B.tail), A.revStraightBearing, B.straightBearing)) or "+
                                                            
                                                            "   (not B.revStraightBearing in (180:360] and "+
                                                            "    (A.revStraightBearing > B.straightBearing and "+
                                                            "    contains(bearing(A.tail, B.tail), B.straightBearing, A.revStraightBearing)) or "+
                                                            "    (A.revStraightBearing < B.straightBearing and "+
                                                            "    contains(bearing(A.tail, B.tail), A.revStraightBearing, B.straightBearing))) or "+
                      
                                                            "   (bearingDifference(A.revStraightBearing, B.straightBearing) < 10 and "+
                                                            "    contains(bearing(A.tail, B.tail), modifyBearing(A.revStraightBearing, -90), modifyBearing(A.revStraightBearing, 90)))))) ";
            
    
    private static final String DEPART_RELATIONSHIP_EPL =   "INSERT INTO RelationEvent "+
                                                            "SELECT "+
                                                            "        CASE WHEN A.id.compareTo(B.id) < 0 THEN A.id || \"_\" || B.id ELSE B.id || \"_\" || A.id END as id, "+             
                                                            "       current_timestamp as timestamp, "+
                                                            "       A.locations as locations1, "+
                                                            "       B.locations as locations2, "+
                                                            "       A.level as level, "+
                                                            "       A.straightBearing as bearing1, "+
                                                            "       B.straightBearing as bearing2, "+
                                                            "       A.avgInfSpeed as speed1,"+
                                                            "       B.avgInfSpeed as speed2,"+            
                                                            "       ceptraj.event.relationship.RelationType.DEPART as relationshipType, "+
                                                            "       max(A.finalTimestamp, B.finalTimestamp) as finalTimestamp, "+
                                                            "       min(A.initialTimestamp, B.initialTimestamp) as initialTimestamp "+
                                                            "FROM "+
                                                            "   TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE, avgInfSpeed > stopThreshold(id)) A unidirectional, "+
                                                            "   TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE).std:groupwin(level).win:expr(oldest_timestamp > newest_timestamp - EventHierarchy.getWinSizeForRelationLevel(level)) B "+
                                                            "WHERE "+
                                                            "      B.avgInfSpeed <= stopThreshold(B.id) and "+
                                                            "      A.id != B.id and "+
                                                            "      A.level = B.level and "+
                                                            "      (A.timestamp-B.timestamp) <= EventHierarchy.getWinSizeForRelationLevel(B.level) and "+
                                                            "      haversineDistance(A.tail, B.head) <= departMaxDist and "+
                                                            "      haversineDistance(A.tail, B.head) <  haversineDistance(A.head, B.head) ";
    
    
    private static final String ARRIVE_RELATIONSHIP_EPL =   "INSERT INTO RelationEvent "+
                                                            "SELECT "+
                                                            "        CASE WHEN A.id.compareTo(B.id) < 0 THEN A.id || \"_\" || B.id ELSE B.id || \"_\" || A.id END as id, "+             
                                                            "       current_timestamp as timestamp, "+
                                                            "       A.locations as locations1, "+
                                                            "       B.locations as locations2, "+
                                                            "       A.level as level, "+
                                                            "       A.straightBearing as bearing1, "+
                                                            "       B.straightBearing as bearing2, "+
                                                            "       A.avgInfSpeed as speed1,"+
                                                            "       B.avgInfSpeed as speed2,"+            
                                                            "       ceptraj.event.relationship.RelationType.ARRIVE as relationshipType, "+
                                                            "       max(A.finalTimestamp, B.finalTimestamp) as finalTimestamp, "+
                                                            "       min(A.initialTimestamp, B.initialTimestamp) as initialTimestamp "+
                                                            "FROM "+
                                                            "   TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE, avgInfSpeed > stopThreshold(id)) A unidirectional, "+
                                                            "   TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE).std:groupwin(level).win:expr(oldest_timestamp > newest_timestamp - EventHierarchy.getWinSizeForRelationLevel(level)) B "+
                                                            "WHERE "+
                                                            "      B.avgInfSpeed <= stopThreshold(B.id) and "+
                                                            "      A.id != B.id and "+
                                                            "      A.level = B.level and "+
                                                            "      (A.timestamp-B.timestamp) <= EventHierarchy.getWinSizeForRelationLevel(A.level) and "+
                                                            "      haversineDistance(A.head, B.head) <= arriveMaxDist and "+
                                                            "      haversineDistance(A.head, B.head) <  haversineDistance(A.tail, B.head) ";
    
    
    private static final String CLOSE_RELATIONSHIP_EPL =    "INSERT INTO RelationEvent "+
                                                            "SELECT "+
                                                            "        CASE WHEN A.id.compareTo(B.id) < 0 THEN A.id || \"_\" || B.id ELSE B.id || \"_\" || A.id END as id, "+             
                                                            "       current_timestamp as timestamp, "+
                                                            "       A.locations as locations1, "+
                                                            "       B.locations as locations2, "+
                                                            "       A.level as level, "+
                                                            "       A.straightBearing as bearing1, "+
                                                            "       B.straightBearing as bearing2, "+
                                                            "       A.avgInfSpeed as speed1,"+
                                                            "       B.avgInfSpeed as speed2,"+            
                                                            "       ceptraj.event.relationship.RelationType.CLOSE as relationshipType, "+
                                                            "       max(A.finalTimestamp, B.finalTimestamp) as finalTimestamp, "+
                                                            "       min(A.initialTimestamp, B.initialTimestamp) as initialTimestamp "+
                                                            "FROM "+
                                                            "   TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE, avgInfSpeed < stopThreshold(id)) A unidirectional, "+
                                                            "   TrajectoryEvent(areaRelationship = AreaOfInterestRelationshipType.NONE).std:unique(id, level) B "+
                                                            "WHERE "+
                                                            "      B.avgInfSpeed <= stopThreshold(B.id) and "+
                                                            "      A.id != B.id and "+
                                                            "      A.level = B.level and "+
                                                            "      (A.timestamp-B.timestamp) <= EventHierarchy.getWinSizeForRelationLevel(A.level) and "+
                                                            "      haversineDistance(A.head, B.head) <= closeMaxDist";            
    
    EPStatement contextTrajectorySt;
    
    EPStatement parallelRelationSt;
    EPStatement perpendicularRelationSt;
   
    EPStatement convergeRelationSt;
    EPStatement divergeRelationSt;

    EPStatement arriveRelationSt;
    EPStatement departRelationSt;
    
    EPStatement closeRelationSt;
    
    EPStatement maxWinAdaptorSt;
    EPStatement avgWinAdaptorSt;

    EPStatement dynamicWinAdaptorSt1;
    EPStatement dynamicWinAdaptorSt2;
    EPStatement dynamicWinAdaptorSt3;
    EPStatement dynamicWinAdaptorSt4;

    @Override
    public void start(EPServiceProvider CEPProvider, EventConsumer eventConsumer) {
        
        CEPEngine = CEPProvider;
        
        contextTrajectorySt = CEPEngine.getEPAdministrator().createEPL(CONTEXT_TRAJECTORY_LEVEL_EPL);
        
        parallelRelationSt = CEPEngine.getEPAdministrator().createEPL(PARALLEL_RELATIONSHIP_EPL);
        RelationListener list = new RelationListener(eventConsumer);
        parallelRelationSt.addListener(list);
       
        perpendicularRelationSt = CEPEngine.getEPAdministrator().createEPL(PERPENDICULAR_RELATIONSHIP_EPL);
        perpendicularRelationSt.addListener(list);

        convergeRelationSt = CEPEngine.getEPAdministrator().createEPL(CONVERGE_RELATIONSHIP_EPL);
        convergeRelationSt.addListener(list);
   
        divergeRelationSt = CEPEngine.getEPAdministrator().createEPL(DIVERGE_RELATIONSHIP_EPL);
        divergeRelationSt.addListener(list);
       
        departRelationSt = CEPEngine.getEPAdministrator().createEPL(DEPART_RELATIONSHIP_EPL);
        departRelationSt.addListener(list);
        
        arriveRelationSt = CEPEngine.getEPAdministrator().createEPL(ARRIVE_RELATIONSHIP_EPL);
        arriveRelationSt.addListener(list);
        
        closeRelationSt = CEPEngine.getEPAdministrator().createEPL(CLOSE_RELATIONSHIP_EPL);
        closeRelationSt.addListener(list);
        
        RelationWinAdaptorListener winAdaptorList = new RelationWinAdaptorListener();
        switch(ConfigProvider.getRelationWinAdaptMechanism()){
            case MAX:
                maxWinAdaptorSt = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_WIN_SIZE_MAX_ADAPTOR_EPL);
                maxWinAdaptorSt.addListener(winAdaptorList);
                break;
            case AVERAGE:
                avgWinAdaptorSt = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_WIN_SIZE_AVG_ADAPTOR_EPL);
                avgWinAdaptorSt.addListener(winAdaptorList);
                 break;
            case DYNAMIC:
                dynamicWinAdaptorSt1 = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_1);
                dynamicWinAdaptorSt1.addListener(winAdaptorList);
                dynamicWinAdaptorSt2 = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_2);
                dynamicWinAdaptorSt2.addListener(winAdaptorList);
                dynamicWinAdaptorSt3 = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_3);
                dynamicWinAdaptorSt3.addListener(winAdaptorList);
                dynamicWinAdaptorSt4 = CEPEngine.getEPAdministrator().createEPL(RELATIONSHIP_WIN_SIZE_DYN_ADAPTOR_EPL_4);
                dynamicWinAdaptorSt4.addListener(winAdaptorList);
                break;
        }
    }
    
}
