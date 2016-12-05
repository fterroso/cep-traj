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

/**
 * Global constants of the system
 * @author Fernando Terroso-Saenz
 */
public class Constants {
    
    public static final String GEOLIFE_DATE_FORMAT = "yyyy-MM-dd,HH:mm:ss";
    //Lines to skip in a geolife file
    public static final int GEOLIFE_FILE_HEAD_NUM_LINES = 6;
    public static final String GEOLIFE_ID_PREFIX = "geolife";
    
    public static final String IMIS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    public static final double DEFAULT_BEARING_MIN_CHANGE = 45;
    public static final double DEFAULT_SPEED_MIN_CHANGE = 0.2; //20 % 
           
    public static final double MAX_LAT_VARIATION = 0.00005;
    public static final double MAX_LON_VARIATION = 0.00005;
    
    public static final long MAX_TIME_BETWEEN_LOCATIONS = 900000;
    
    public static final int INITIAL_POINTS_PER_LEVEL = 2;
    public static final int INITIAL_CURRENT_LEVEL = 1;
    
    public static final double STOP_DECREASING_FACTOR = 0.99;
    
    public static final long INITIAL_RELATION_WIN_SIZE_LEVEL_1 = 100;//300000; // milliseconds
    public static final long INITIAL_RELATION_WIN_SIZE_OTHER_LEVELS = 100;//450000; //milliseconds 
    public static final long INITIAL_WIN_SIZE_MODIFIER = 4000;

    public static final double WIN_SIZE_MODIFIER_INC_RATE = 0.25;
    public static final double WIN_SIZE_MODIFIER_DEC_RATE = 0.20;
    
    public static final long WAITING_TIME_TO_FINISH = 4000;
    
    public static final double EARTH_RADIUS = 6371;
    public static final double TOTAL_DEGREES = 360; // in degrees.
        
    /*PATHS*/
    protected static String LINUX_PATH = "";
    protected static String MAC_OS_PATH = "";
    protected static String WINDOWS_PATH ="";
    
    public static String LINUX_NAME = "linux";
    public static String MAC_OS_NAME = "mac";
    public static String WINDOWS_NAME = "windows";
    
    public static final String COMMON_ROUTES_DIRECTORY_NAME = "common_routes";
   
    public static String getPath(){
        
        String path = "";
        
        String OS = System.getProperty("os.name").toLowerCase();
        if(OS.contains(LINUX_NAME)){
            path = LINUX_PATH;
        }else if(OS.contains(MAC_OS_NAME)){
            path = MAC_OS_PATH;
        }else if(OS.contains(WINDOWS_NAME)){
            path = WINDOWS_PATH;
        }
        
        return path;        
    }
    
}
