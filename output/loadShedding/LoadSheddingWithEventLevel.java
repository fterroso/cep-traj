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
package ceptraj.output.loadShedding;

import ceptraj.event.MapElement;
import org.apache.log4j.Logger;
import ceptraj.output.EventCounter;

/**
 * Class that implements the proposed mechanism of load shedding to adjust
 * the delivery of events of an event consumer in case of high delivery.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class LoadSheddingWithEventLevel implements LoadShedding{

     static Logger LOG = Logger.getLogger(LoadSheddingWithEventLevel.class); 
    
    private static final int MAX_NUM_EVENTS = 250;
    
    private long initTimestamp = 0;
    
    EventCounter counter = new EventCounter();
    int currentLevel = 1;
    
    int prevCount = 0;
    
    @Override
    public boolean discart(MapElement event) {

        if(initTimestamp == 0){
            initTimestamp = event.getTimestamp();
        }
        
        boolean discart = false;
        
        int currentCount = 0;
        if(event.getLevel() < currentLevel){
            currentCount = counter.updateWindow(event.getTimestamp());
            discart = true;
        }else{                
            currentCount = counter.newEvent(event);
        }

        if(currentCount!= 0){
            if(currentCount > MAX_NUM_EVENTS && currentCount > prevCount){
                currentLevel++;
            }else if(currentCount < MAX_NUM_EVENTS && currentLevel > 1){
                currentLevel--;
            }                
        }
        
        System.out.println((event.getTimestamp() - initTimestamp)/1000+"\t"+ (Math.round(currentLevel/10)+1));
        
        prevCount = currentCount;
        
        return discart;        
    }
    
}
