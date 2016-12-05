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
package ceptraj.tool;

import ceptraj.config.ConfigProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * Class that represents the bearing of a moving device.
 *
 * @author Fernando Terroso-Saenz<fterroso@um.es>
 */
public enum Bearing {
       
    UNSP (new int[]{-1}), //Unspecified
    NNE (new int[]{1}),
    NE (new int[]{2}),
    ENE (new int[]{3}),
    E (new int[]{4}),
    ESE (new int[]{5}),
    SE (new int[]{6}),
    SSE (new int[]{7}),
    S (new int[]{8}),
    SSW (new int[]{9}),
    SW (new int[]{10}),
    WSW (new int[]{11}),
    W (new int[]{12}),
    WNW (new int[]{13}),
    NW (new int[]{14}),
    NNW (new int[]{15}),
    N  (new int[]{16,0});
    
    protected static Logger LOG = Logger.getLogger(Bearing.class); 

    private final int[] bearingValue;    
    
    private Bearing(int[] value){
        bearingValue = value;
    }

    public int getBearingValue() {
        return bearingValue[0];
    }
        
    public int[] getAllBearingValues(){
        return bearingValue;
    }
    
    public static Bearing getFineGrainBearingFromValue(double value){
        
        int tmp = (int) Math.round(value / 22.5);      
        
        for (Bearing b : values()) {
            if(ArrayUtils.contains(b.getAllBearingValues(), tmp)){
//            if (Arrays.asList(b.getAllBearingValues()).contains(tmp)) {
                return b;
            }
        }
        
        return UNSP;
    }
    
    public static Bearing getCoarseGrainBearingFromValue(double value){
        int tmp = (int) Math.round(value / 22.5);
        
        switch(tmp){
            case 0:
            case 1:
            case 15:
            case 16:
                return N;
            case 2:
                return NE;
            case 3:
            case 4:
            case 5:
                return E;
            case 6:
                return SE;
            case 7:
            case 8:
            case 9:
                return S;
            case 10:
                return SW;
            case 11:
            case 12:
            case 13:
                return W;
            case 14:
                return NW;

        }
        
        return UNSP;
    }
    
    public static Bearing getCardinalPointFromValue(double value){
            
        if(value > 315 || value <=45){
            return N;
        }else if(value > 45 && value <= 135){
            return E;
        }else if(value > 135 && value <= 225){
            return S;
        }
        
        return UNSP;
    }
    
    public static double bearing(Point p1, Point p2){
        double bearing = 0;
        if(p1 != null && p2 != null){   
            
            switch(ConfigProvider.getSpaceType()){
                case lat_lon:
                    //In case of lat-lon space
                    double p1LatRad = Math.toRadians(p1.getLat());
                    double p2LatRad = Math.toRadians(p2.getLat());

                    double p1LonRad = Math.toRadians(p1.getLon());
                    double p2LonRad = Math.toRadians(p2.getLon());

                    bearing = (Math.toDegrees((Math.atan2(Math.sin(p2LonRad - p1LonRad) * Math.cos(p2LatRad), Math.cos(p1LatRad) * Math.sin(p2LatRad) - Math.sin(p1LatRad) * Math.cos(p2LatRad) * Math.cos(p2LonRad - p1LonRad)))) + 360) % 360;
                    break;
                case cartesian:
                    //In case of cartesian space
                    double dy = p2.y - p1.y;
                    double dx = p2.x - p1.x;
                    bearing = 90 - (180/Math.PI)*Math.atan2(dy, dx);
                    if(bearing < 0){
                        bearing += 360;
                    }
                    break;                    
            }
        }
        
        return bearing;
    } 
    
    public static double avgBearing(Point[] points){
        double bearing = 0;
        
        if(points.length > 1 ){
            for(int i = 0; i<= points.length-2; i++){
                double aux = bearing(points[i], points[i+1]);
                bearing += aux;
            }
            bearing /= points.length;
        }
        
        return bearing;    
    } 
    
   public static double modifyBearing(double bearing, double modifyVal){
       bearing += modifyVal;
       
       if(bearing >= 0){
           if(bearing <= Constants.TOTAL_DEGREES){
               return bearing;
           }else if(bearing < (Constants.TOTAL_DEGREES + Constants.TOTAL_DEGREES)){
               return bearing - Constants.TOTAL_DEGREES;               
           }
       }else{
           if(bearing > -Constants.TOTAL_DEGREES){
               return bearing + Constants.TOTAL_DEGREES;
           }
       }
       
       return bearing - 360 * Math.floor(bearing/360);
   }
    
    
    /* Method which indicates whether the beraring bearingV is contained in the 
     * range defined by lowLimit and upperLimit
     */
    public static boolean contains(
            double bearingV, 
            double lowLimit, 
            double upperLimit){
        
        if(lowLimit < upperLimit){
            return (bearingV >= lowLimit && bearingV <= upperLimit);            
        }else{
            return ((bearingV >= lowLimit && bearingV <= 360) || (bearingV >= 0 && bearingV <= upperLimit));            
        }
        
    }
    
    public static double bearingDifference(Double b1, Double b2){
        
        if(b1 != null && b2 != null){
            double diff = Math.abs(b1 - b2);        
            return Math.min(diff, Constants.TOTAL_DEGREES-diff);
        }
        
        return -1;
                
    }
    
    /*
     * This functions returns which of b1 and b2 is the closest bearing to 
     * targetBearing.
     */
    public static double closestBearing(
            Double b1, 
            Double b2, 
            Double targetBearing){
        
        if(bearingDifference(b1,targetBearing) < bearingDifference(b2,targetBearing)){
            return b1;
        }
        
        return b2;
        
    }
    
    public static boolean isIncreasingBearing(double newBearing, double oldBearing){
        
        boolean result = false;
        
        double dist1 = Math.abs(newBearing - oldBearing);
        double dist2 = Constants.TOTAL_DEGREES - dist1;
        
        if(newBearing > oldBearing){
            if(dist1 < dist2) result = true;
        }else{
            if(dist1 > dist2) result = true;
        }
        
        return result;
    }
        
}
