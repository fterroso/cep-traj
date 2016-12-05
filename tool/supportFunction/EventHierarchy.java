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

import ceptraj.event.relationship.adaptation.dynamic.RelationWinSizeOpType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import ceptraj.tool.Constants;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class EventHierarchy implements Serializable {
    
    static Logger LOG = Logger.getLogger(EventHierarchy.class);    
        
    private static Map<String, Integer> levelPerId = new HashMap<String, Integer>();
    public static Map<Integer, Long> relationWinSizePerLevel = new HashMap<Integer, Long>();  
    public static Map<Integer, Long> incrementForWinSizePerLevel = new HashMap<Integer, Long>();    

    //For the avg. approach
    private static Map<Integer, Integer> numOfRelationsPerLevel = new HashMap<Integer, Integer>();        
    
    public static int getLevelForId(String id){
        if(!levelPerId.containsKey(id)){
            levelPerId.put(id, Constants.INITIAL_CURRENT_LEVEL);
        }
                
        return levelPerId.get(id);
    }
    
    public static void incrementLevelForId(String id){
        int currentLevel = getLevelForId(id);        
        levelPerId.put(id, ++currentLevel);
    }
    
    public static long getWinSizeForRelationLevel(int level){
        if(!relationWinSizePerLevel.containsKey(level)){
            if(level == 1){
                
                relationWinSizePerLevel.put(level, Constants.INITIAL_RELATION_WIN_SIZE_LEVEL_1);
            }else{
                relationWinSizePerLevel.put(level, Constants.INITIAL_RELATION_WIN_SIZE_OTHER_LEVELS);
            }
        } 
        return relationWinSizePerLevel.get(level);
        
    }
    
    public static void setWinSizeForRelationLevel(int level, long winSize){
        relationWinSizePerLevel.put(level, winSize);
    }
    
    public static void addNewWinSizeForRelationLevel(int level, long winSize){
        if(winSize > 0){
            int numOfRelations = 0;
            if(numOfRelationsPerLevel.containsKey(level)){

               numOfRelations = numOfRelationsPerLevel.get(level); 
               long currentWinSize = getWinSizeForRelationLevel(level);

               currentWinSize = ((currentWinSize * numOfRelations) + winSize) / (numOfRelations+1);

               setWinSizeForRelationLevel(level,currentWinSize);
                numOfRelationsPerLevel.put(level, numOfRelations+1);

            }else{        
                setWinSizeForRelationLevel(level,winSize);
                numOfRelationsPerLevel.put(level, 1);
            }
        }
    }
    
    //Average mechanism methods
    private static long getModifyValueForLevel(int level){
        if(!incrementForWinSizePerLevel.containsKey(level)){
            if(level == 1){
                incrementForWinSizePerLevel.put(level, Constants.INITIAL_WIN_SIZE_MODIFIER);
            }else{
                incrementForWinSizePerLevel.put(level, Constants.INITIAL_WIN_SIZE_MODIFIER * level * 1000);
            }
        }                
        return incrementForWinSizePerLevel.get(level);
    }
    
    public static RelationWinSizeOpType increaseWinSizeForRelationLevel(int level){
        long winSize = getWinSizeForRelationLevel(level) + getModifyValueForLevel(level);
        setWinSizeForRelationLevel(level,winSize);
        
        return RelationWinSizeOpType.INC;
    }
    
    public static RelationWinSizeOpType decreaseWinSizeForRelationLevel(int level){
        long winSize = getWinSizeForRelationLevel(level) - getModifyValueForLevel(level);
        setWinSizeForRelationLevel(level,winSize);
        
        return RelationWinSizeOpType.DEC;
    }
    
    public static void incementModifyValueForLevel(int level){      
        long modifyVal = getModifyValueForLevel(level);
        long modifyVal2 = (long) ((1+Constants.WIN_SIZE_MODIFIER_INC_RATE) * modifyVal);
        incrementForWinSizePerLevel.put(level, modifyVal2);
    }
    
    public static void decrementModifyValueForLevel(int level){
        long modifyVal = getModifyValueForLevel(level);
        long modifyVal2 = (long) ((1-Constants.WIN_SIZE_MODIFIER_DEC_RATE) * modifyVal);
        
        if(modifyVal2 >= (Constants.INITIAL_WIN_SIZE_MODIFIER * 0.2)){
            incrementForWinSizePerLevel.put(level, modifyVal2);
        }
    }
}
