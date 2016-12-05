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
package ceptraj.tool.supportFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import ceptraj.tool.Constants;

/**
 * Class that comprises common speed-based functions used by different EPAs.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class SpeedFunction {
        
    static Logger LOG = Logger.getLogger(SpeedFunction.class); 
    
    private static final double INC_FACTOR_FOR_NEW_STAGE= 1.3;
    private static final double MIN_SPEED_BELOW_THRESHOLD_FOR_NEW_STAGE = 5;
    
    static Map<String,Double> weightedAvgSpeeds = new HashMap<String,Double>();
    static Map<String,Double> maxSpeeds = new HashMap<String,Double>();
    static Map<String,Double> thresholds = new HashMap<String,Double>();   
    
    static Map<String,Double> weights = new HashMap<String,Double>();
    static Map<String,Long> initialTimestamp = new HashMap<String,Long>();
    static Map<String,Boolean> newStage = new HashMap<String,Boolean>();
    
    
    static Map<String,List<StopThresholdItem>> thresholdLog = new HashMap<String,List<StopThresholdItem>>();
    
    static int numSpeedBelowThreshold = 0;
    
    public static void newInitialTimestamp(
            String id,
            long timestamp){
        initialTimestamp.put(id, timestamp);
    }
    
    public static void newCurrentSpeed(
            String id,
            long timestamp,
            double newSpeed){                
           
        if(!Double.isNaN(newSpeed)){
        
            double newWeight = timestamp - initialTimestamp.get(id);
            double weight;
            try{
                weight = weights.get(id);            
            }catch(NullPointerException e){ 
                weight = 1;
            }
            weight += newWeight;

            double weighthedAvgSpeed;
            try{
                weighthedAvgSpeed = weightedAvgSpeeds.get(id);
            }catch(NullPointerException e){
                weighthedAvgSpeed = 0;
            }

            weighthedAvgSpeed = weighthedAvgSpeed + ((newWeight/weight)*(newSpeed-weighthedAvgSpeed));

            weightedAvgSpeeds.put(id, weighthedAvgSpeed);
            weights.put(id, weight);  

            if(!maxSpeeds.containsKey(id)){
                newMaxSpeed(id, newSpeed);            
            }else if(newSpeed > maxSpeeds.get(id)){
                newMaxSpeed(id,newSpeed);
            }

            if(weighthedAvgSpeed != 0){

                double threshold =  (weighthedAvgSpeed * (1 - ((weighthedAvgSpeed/maxSpeeds.get(id)) * Constants.STOP_DECREASING_FACTOR)))+1; 

                thresholds.put(id, threshold);
                               
                StopThresholdItem logEntry = new StopThresholdItem();
                logEntry.timestamp = timestamp;
                logEntry.value = threshold;

                List<StopThresholdItem> log = thresholdLog.get(id);

                if(log == null){
                    log = new ArrayList<StopThresholdItem>();
                }

                log.add(logEntry);
                thresholdLog.put(id, log);
            }
        }
    }
                
        
    private static void newMaxSpeed(String id, double maxSpeed){
        maxSpeeds.put(id, maxSpeed);
        newStage.put(id, false);
    }
    
    public static double stopThreshold(String id){
        
        try{            
            return 1;
        }catch(NullPointerException e){
            return 1;
        }                        
    }
    
    public static List<StopThresholdItem> getStopThresholdLog(String id){
        return thresholdLog.get(id);
    }
    
    public static class StopThresholdItem{
        
        protected long timestamp;
        protected double value;

        public long getTimestamp() {
            return timestamp;
        }

        public double getValue() {
            return value;
        }     
        
    }
    
}
