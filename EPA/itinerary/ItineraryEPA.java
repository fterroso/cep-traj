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
package ceptraj.EPA.itinerary;

import ceptraj.EPA.EPA;
import ceptraj.EPA.itinerary.listener.ItineraryBoundariesListener;
import ceptraj.output.EventConsumer;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

/**
 * Class that comprises the processing rules to slide a GPS trace to detect
 * the itineraries' boundaries (start, end).
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class ItineraryEPA extends EPA{
    
    
    private static final String ITINERARY_BOUNDARIES_BY_TIME_EPL =  
                                                          "INSERT INTO ItineraryFinishesStartsEvent "+
                                                          "SELECT current_timestamp as timestamp, "+
                                                          "       finishedItineraryId, "+
                                                          "       startedItineraryId, "+
                                                          "       lastLocation, "+
                                                          "       startLocation "+             
                                                          "FROM RawLocationEvent "+
                                                          "MATCH_RECOGNIZE ( "+
                                                          "    MEASURES "+
                                                          "         A.id as finishedItineraryId,"+
                                                          "         A.location as lastLocation,"+          
                                                          "         B.id as startedItineraryId,"+
                                                          "         B.location as startLocation "+
                                                          "     PATTERN (A B) "+
                                                          "     DEFINE B as B.location.timestamp - A.location.timestamp  >= minTimeBetweenItineraries "+
                                                          ")";  

    private static final String ITINERARY_BOUNDARIES_BY_ID_EPL =  
                                                          "INSERT INTO ItineraryFinishesStartsEvent "+
                                                          "SELECT current_timestamp as timestamp, "+
                                                          "       finishedItineraryId, "+
                                                          "       startedItineraryId, "+
                                                          "       lastLocation, "+
                                                          "       startLocation "+             
                                                          "FROM RawLocationEvent "+
                                                          "MATCH_RECOGNIZE ( "+
                                                          "    MEASURES "+
                                                          "         A.id as finishedItineraryId,"+
                                                          "         A.location as lastLocation,"+          
                                                          "         B.id as startedItineraryId,"+
                                                          "         B.location as startLocation "+
                                                          "     PATTERN (A B) "+
                                                          "     DEFINE B as B.id != A.id "+
                                                          ")";
    
    EPStatement itinerariBoundariesQuery;
    
    @Override        
    public void start(EPServiceProvider epsp, EventConsumer ec) {

        CEPEngine = epsp;
        
        itinerariBoundariesQuery = CEPEngine.getEPAdministrator().createEPL(ITINERARY_BOUNDARIES_BY_ID_EPL);        
        ItineraryBoundariesListener ibl = new ItineraryBoundariesListener(ec);
        itinerariBoundariesQuery.addListener(ibl);
        
    }
}
