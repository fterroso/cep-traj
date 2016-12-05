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
package ceptraj.EPA.filter;

import ceptraj.EPA.EPA;
import ceptraj.EPA.filter.listener.SecondFilterListener;
import ceptraj.EPA.filter.listener.FirstEventListener;
import ceptraj.EPA.filter.listener.FirstFilterListener;
import ceptraj.EPA.filter.listener.MinDistAdjListener;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import org.apache.log4j.Logger;
import ceptraj.output.EventConsumer;

/**
 * Class that filters the incoming locations on the basis of their distance with 
 * the previous one and a time threashold.
 * 
 * It also creates the new hierarchy levels of points.
 * @author fterroso<fterroso@um.es>
 */
public class BasicFilterEPA extends EPA{

    static Logger LOG = Logger.getLogger(BasicFilterEPA.class);    
    
    private static final String FILTER_LOCATION_RATE_EPL = 
                                                           "SELECT "+
                                                           " SecondFilterMechanism.newRateMeasurement(rate(1)) "+
                                                           "FROM FilteredLocationEvent "+
                                                           "OUTPUT SNAPSHOT EVERY 3 sec";
        
    private static String CONTEXT_ID_EPL =      "CREATE CONTEXT segmentedById "+
                                                 "PARTITION BY id "+
                                                 "FROM RawLocationEvent"; 
    
    private static final String FIRST_FILTER_EPL =   "@Priority(9) "+
                                                      "INSERT INTO SemiFilteredLocationEvent "+
                                                      "SELECT current_timestamp as timestamp, "+
                                                      "       id, "+
                                                      "       location, "+
                                                      "       1 as level "+  
                                                      "FROM RawLocationEvent "+
                                                      "MATCH_RECOGNIZE ( "+
                                                      "    PARTITION BY id "+
                                                      "    MEASURES "+
                                                      "         B.id as id,"+
                                                      "         B.location as location"+
                                                      "     AFTER MATCH SKIP TO NEXT ROW "+
                                                      "     PATTERN (A B) "+
                                                      "     DEFINE B as not isOutlier(A,B) "+
                                                      ")";                                            
    
    private static final String SECOND_FILTER_EPL =  "@Priority(8) "+
                                                      "INSERT INTO FilteredLocationEvent "+
                                                      "SELECT current_timestamp as timestamp, "+
                                                      "       B.id as id, "+
                                                      "       B.location as location, "+
                                                      "       1 as level "+
                                                      "FROM SemiFilteredLocationEvent B unidirectional, "+
                                                      "     FilteredLocationEvent(level=1).std:unique(id) A "+
                                                      "WHERE "+
                                                      "     A.id = B.id AND "+
                                                      "     euclideanDist(A.location, B.location) > minDist()";
                                                        
    private static final String LOWER_LEVEL_FIRST_EVENT_EPL =   "@Priority(10) "+
                                                                "INSERT INTO FilteredLocationEvent "+
                                                                "SELECT current_timestamp as timestamp, "+
                                                                "       A.id as id, "+
                                                                "       A.location as location, "+
                                                                "       1 as level "+
                                                                "FROM pattern[every-distinct(A.id) A=RawLocationEvent]"; 
    
    private static final String NEW_LEVEL_FIRST_EVENT_EPL =     "INSERT INTO FilteredLocationEvent "+
                                                                "SELECT current_timestamp as timestamp, "+
                                                                "       A.id as id, "+
                                                                "       A.location as location, "+
                                                                "       B.level+1 as level "+
                                                                "FROM pattern[every-distinct(A.id) A=RawLocationEvent -> every-distinct(B.level, B.id) B=FilteredLocationEvent(level = EventHierarchy.getLevelForId(B.id), id = A.id)]";
    
    private static final String NEW_LEVEL_EPL = "SELECT EventHierarchy.incrementLevelForId(A.id) "+
                                                "FROM pattern[ every-distinct(A.level, A.id) A=FilteredLocationEvent(level = EventHierarchy.getLevelForId(A.id)) -> B= FilteredLocationEvent(level = A.level, id = A.id)]";
   
   
    EPStatement contextStatement;
    
    EPStatement maxDistAdjStatement;
    EPStatement filLocEventRateStatement;
    
    EPStatement firstFilterStatement;
    EPStatement secondFilterStatement;
    
    EPStatement lowerLevelFirstEventStatement;
    EPStatement newLevelFirstEventStatement;
    EPStatement newLevelStatement;
    
    @Override
    public void start(EPServiceProvider pCEPEngine, EventConsumer eventConsumer) {
                
        CEPEngine = pCEPEngine;
        
        contextStatement = CEPEngine.getEPAdministrator().createEPL(CONTEXT_ID_EPL);
      
        filLocEventRateStatement = CEPEngine.getEPAdministrator().createEPL(FILTER_LOCATION_RATE_EPL);
        MinDistAdjListener minDistAdjList = new MinDistAdjListener();
        filLocEventRateStatement.addListener(minDistAdjList);
     
        firstFilterStatement = CEPEngine.getEPAdministrator().createEPL(FIRST_FILTER_EPL);
        FirstFilterListener firstFilterList = new FirstFilterListener(eventConsumer);
       firstFilterStatement.addListener(firstFilterList);
        
        secondFilterStatement = CEPEngine.getEPAdministrator().createEPL(SECOND_FILTER_EPL);
        SecondFilterListener secondFilterList = new SecondFilterListener(eventConsumer);
        secondFilterStatement.addListener(secondFilterList);
        
        lowerLevelFirstEventStatement = CEPEngine.getEPAdministrator().createEPL(LOWER_LEVEL_FIRST_EVENT_EPL);
        FirstEventListener  firstEventList = new FirstEventListener(eventConsumer);
        lowerLevelFirstEventStatement.addListener(firstEventList);
        
        newLevelFirstEventStatement = CEPEngine.getEPAdministrator().createEPL(NEW_LEVEL_FIRST_EVENT_EPL);
        NewLevelFirstEventListener newLevelEventList = new NewLevelFirstEventListener(visualizer);
        newLevelFirstEventStatement.addListener(newLevelEventList);
        
        newLevelStatement = CEPEngine.getEPAdministrator().createEPL(NEW_LEVEL_EPL);
       NewLevelEventListener newLevelList = new NewLevelEventListener();
        newLevelStatement.addListener(newLevelList);
               
    }
    
}
