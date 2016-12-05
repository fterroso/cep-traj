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

import org.apache.log4j.Logger;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class SecondFilterMechanism {
    
    static final Logger LOG = Logger.getLogger(SecondFilterMechanism.class);
    
    // Min-dist filter params.
    public static double MAX_RATE_CAPABILITY = Double.MAX_VALUE; //events per second.
    private static final double MAX_DIST = Double.MAX_VALUE;//  10000;
    private static final double DEFAULT_MIN_DIST = 10; // in meters.
    private static final double DEFAULT_MIN_DIST_INC = 1.25;
    private static final double DEFAULT_MIN_DIST_DEC = 0.8;
    
    private static final double modFactor = 0.05;
    private static double minDistInc = DEFAULT_MIN_DIST_INC;
    private static double minDistDec = DEFAULT_MIN_DIST_DEC;
    
    private static double numConsecutiveInc = 4;
    private static double numConsecutiveDec = 2;
    
    private static final double maxConsecutiveInc = 0;
    private static final double maxConsecutiveDec = 0;
    
    private static double minDist = DEFAULT_MIN_DIST;
    private static double prevRate;
    
    private static double maxRate = Double.MIN_VALUE;
    
    public static void newRateMeasurement(Double rate){
        
        if(rate != null){
            if(minDist > DEFAULT_MIN_DIST && rate < MAX_RATE_CAPABILITY){
                numConsecutiveInc = 0;            

                if(prevRate > rate){
                    minDistInc = DEFAULT_MIN_DIST_INC;
                    if(++numConsecutiveDec > maxConsecutiveDec){
                        minDistDec -= modFactor;
                    }
                    minDist= (minDist * minDistDec < DEFAULT_MIN_DIST)? DEFAULT_MIN_DIST : minDist*minDistDec;   
                }

            }else if(rate > MAX_RATE_CAPABILITY){
                numConsecutiveDec = 0;            

                if(prevRate < rate){
                    minDistDec = DEFAULT_MIN_DIST_DEC;
                    if(++numConsecutiveInc > maxConsecutiveInc){
                        minDistInc += modFactor;
                    }
                }

                minDist *= minDistInc;  
                if(minDist > MAX_DIST){
                    minDist = MAX_DIST;                    
                }
            }
            
            prevRate = rate; 
        }
    }

    public static double getMinDist() {
        return minDist;
    }
    
}
