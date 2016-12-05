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
package ceptraj.output;

import ceptraj.event.MapElement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class EventCounter {
    
    static Logger LOG = Logger.getLogger(EventCounter.class);    
    
    long firstTimestamp = 0;
    long intervalInitialTimestamp = 0;
    long timestampInterval = 10000; //in milliseconds
    long windowLength = 30000; // in milliseconds
    
    List<EventRecord> eventRecords = new ArrayList<EventRecord>();
    List<Long>counterSlidingWindow = new ArrayList<Long>();
        
    public synchronized int newEvent(MapElement event){           
        
        if(intervalInitialTimestamp == 0){
            firstTimestamp = intervalInitialTimestamp = event.getTimestamp();
        }

        appendToWindow(event.getTimestamp());
        
        EventRecord record = new EventRecord();
        record.setTimestamp(event.getTimestamp()- intervalInitialTimestamp); 
        record.setNumEvents(counterSlidingWindow.size());
        eventRecords.add(record);               
        
        return counterSlidingWindow.size();
    }   
    
    public synchronized int updateWindow(long timestamp){
        
        for(int i= counterSlidingWindow.size()-1; i > 0; i--){
            try{
            if(timestamp - counterSlidingWindow.get(i) > windowLength){
                counterSlidingWindow.remove(i);                
            }
            }catch(Exception e){
                LOG.error("Null con "+i+" "+timestamp + " "+counterSlidingWindow);
            }
        }
        
        return counterSlidingWindow.size();                                
    }

    private void appendToWindow(long timestamp){
        
        counterSlidingWindow.add(0, timestamp);
        updateWindow(timestamp);

    }
        
    public List<EventRecord> getEventRecords() {
        return eventRecords;
    }
    
    public class EventRecord{
        
        long timestamp;
        int numEvents;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getNumEvents() {
            return numEvents;
        }

        public void setNumEvents(int numEvents) {
            this.numEvents = numEvents;
        }
        
        @Override
        public String toString(){
            return timestamp/1000 + "\t" + numEvents;
        }

    }
}
