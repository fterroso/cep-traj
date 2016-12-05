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
package ceptraj.config;

import ceptraj.EPA.relationship.RelationWinAdaptationMechanismType;
import ceptraj.context.provider.ContextProvider;
import java.io.File;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class ConfigProvider {
    
    static RelationWinAdaptationMechanismType relationWinAdaptMechanism;
    
    static long timeGap = -1;
    static int numIterations = -1;
    static int numDeviceSetRepetitions = -1;
    
    static ContextProvider contextProvider;
    
    static String outputPath;
    
    static SpaceType spaceType;
    
    public static ContextProvider getContextProvider(){
        
        return contextProvider;
        
    }
    
    public static void setContextProvider(ContextProvider p){
        contextProvider = p;
    }

    public static RelationWinAdaptationMechanismType getRelationWinAdaptMechanism() {
        return relationWinAdaptMechanism;
    }

    public static void setRelationWinAdaptMechanism(
            RelationWinAdaptationMechanismType pRelationWinAdaptMechanism) {
        relationWinAdaptMechanism = pRelationWinAdaptMechanism;
    }

    public static long getTimeGap() {
        return timeGap;
    }

    public static void setTimeGap(long timeGap) {
        ConfigProvider.timeGap = timeGap;
    }

    public static int getNumIterations() {
        return numIterations;
    }

    public static void setNumIterations(int numIterations) {
        ConfigProvider.numIterations = numIterations;
    }

    public static int getNumObjs() {
        return numDeviceSetRepetitions;
    }

    public static void setNumObjs(int numDeviceSetRepetitions) {
        ConfigProvider.numDeviceSetRepetitions = numDeviceSetRepetitions;
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static void setOutputPath(String outputPath) {
        
        if(!outputPath.endsWith(File.separator)){
            outputPath += File.separator;
        }
        
        ConfigProvider.outputPath = outputPath;
    }

    public static SpaceType getSpaceType() {
        return spaceType;
    }

    public static void setSpaceType(SpaceType pSpaceType) {
        spaceType = pSpaceType;
    }
    
    
    
    public enum SpaceType{
        lat_lon, 
        cartesian;                
    }
    
    
}
