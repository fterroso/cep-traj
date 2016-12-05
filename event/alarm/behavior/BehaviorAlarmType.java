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
package ceptraj.event.alarm.behavior;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public enum BehaviorAlarmType {
    
    UNSPECIFIED (-1, "Unspecified", BehaviorAlarmLevel.HIGH, "U"),
        
    POSSIBLE_SMUGGLING (1, "Smugling", BehaviorAlarmLevel.HIGH, "SM"),
    FISHING_BEHAVIOR (1, "Fishing", BehaviorAlarmLevel.HIGH, "FB");

    int intValue;   
    String description;
    String acronym;
    BehaviorAlarmLevel level;    

    public int getIntValue() {
        return intValue;
    }

    public String getDescription() {
        return description;
    }

    public BehaviorAlarmLevel getLevel() {
        return level;
    }

    public String getAcronym() {
        return acronym;
    }
      
    
    private BehaviorAlarmType(
            int intValue, 
            String description, 
            BehaviorAlarmLevel level,
            String acronym) {
        this.intValue = intValue;
        this.description = description;
        this.level = level;
        this.acronym = acronym;

    }
    
    public static BehaviorAlarmType getAlarmTypeFromInt(int i){
        
        BehaviorAlarmType type = null;
        
        for(BehaviorAlarmType alarm: BehaviorAlarmType.values()){
            if(alarm.getIntValue() == i){
                type = alarm;
                break;
            }
        }
        
        return type;
    }
     
    
}
