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
package ceptraj.tool.filterAdaption;

import ceptraj.event.location.LocationEvent;
import ceptraj.tool.Bearing;
import ceptraj.tool.supportFunction.LocationFunction;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Class that comprises the dynamic mechanism to adjust the max distance used
 * to identify outliers in the raw GPS trace.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class FirstFilterMechanism {
    
    // Max-dist filter params.
    public static final double DEFAULT_MAX_DIST = 5000; // in meters
    public static final double LOCATION_MAX_DEVIATION_FACTOR = 4;
    
    private static final int MAX_NUM_LOCATIONS_OVER_THRESHOLD = 2;
    private static int numLocationsOverThreshold =0;
    
    private static final int MAX_NUM_SEQ_OF_INITIAL_LOCATIONS = 4;
    private static final double MAX_BEARING_DIFFERENCE = 120;//meters
    private static final double MAX_SPEED_DIFFERENCE = 40; // m/s
    
    private static double referenceBearing;
    private static double referenceSpeed;
    
    static Logger LOG = Logger.getLogger(FirstFilterMechanism.class);   
    
    static HashMap<String,Double> avgDistances = new HashMap<String,Double>();
    static HashMap<String,Double> weights = new HashMap<String,Double>();
    static HashMap<String,Long> initialTimestamp = new HashMap<String,Long>();
    
    public static void newInitialTimestamp(
            String id,
            long timestamp){
        initialTimestamp.put(id, timestamp);
    }
    
    public static boolean isOutlier(LocationEvent le1, LocationEvent le2){
        try{
            
            double dist = LocationFunction.euclideanDist(le1.getLocation(), le2.getLocation());
            double speed = dist /((le2.getLocation().getTimestamp()-le1.getLocation().getTimestamp())/1000);
            
            double bearing = Bearing.bearing(le1.getLocation(), le2.getLocation());            
            double bearingDiff = Bearing.bearingDifference(bearing, referenceBearing);
            
            boolean bearingCriteria = bearingDiff > MAX_BEARING_DIFFERENCE;
            
            double maxSpeed = referenceSpeed + MAX_SPEED_DIFFERENCE;
            boolean speedCriteria = (speed > 0) && (speed > maxSpeed);// || (speed < minSpeed)); 
            boolean notInitCriteria = !isInitialLocation(le2);

            if(bearingCriteria && speedCriteria && notInitCriteria){
                if(++numLocationsOverThreshold > MAX_NUM_LOCATIONS_OVER_THRESHOLD){
                    referenceBearing = bearing;
                    referenceSpeed = speed; 
                    numLocationsOverThreshold = 0;
                }
            }else{
                numLocationsOverThreshold = 0;
            }
           
            return bearingCriteria && speedCriteria && notInitCriteria;
            
        }catch(Exception e){
            LOG.error("Error in isOutlier ("+le1.getLocation().getNumSeq()+", "+le2.getLocation().getNumSeq()+")", e);
            return true;
        }
        
    }

    public static void setReferenceBearing(double referenceBearing) {
        FirstFilterMechanism.referenceBearing = referenceBearing;
    }

    public static void setReferenceSpeed(double referenceSpeed) {
        FirstFilterMechanism.referenceSpeed = referenceSpeed;
    }
    
    private static boolean isInitialLocation(LocationEvent le){
        return le.getLocation().getNumSeq() <= MAX_NUM_SEQ_OF_INITIAL_LOCATIONS;
    }
}
