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

import ceptraj.event.MapElement;
import ceptraj.event.MapElementType;
import ceptraj.event.alarm.AlarmEvent;
import ceptraj.event.relationship.RelationEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import ceptraj.tool.Point;
import ceptraj.tool.Templates;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public abstract class BehaviorAlarmEvent extends AlarmEvent{    
    
    static Logger LOG = Logger.getLogger(BehaviorAlarmEvent.class); 
    
    long initialTimestamp;
    long finalTimestamp;
    Point location;
    
    BehaviorAlarmType alarmType;
    
    MapElement[] underlayingEvents =null;       

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public long getFinalTimestamp() {
        return finalTimestamp;
    }

    public void setFinalTimestamp(long finalTimestamp) {
        this.finalTimestamp = finalTimestamp;
    }

    public long getInitialTimestamp() {
        return initialTimestamp;
    }

    public void setInitialTimestamp(long initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }

    public MapElement[] getUnderlayingEvents() {
        return underlayingEvents;
    }

    public void setUnderlayingEvents(MapElement[] underlayingEvents) {
        this.underlayingEvents = underlayingEvents;
    }

    public MapElement getUnderlayingEvent(){
        if(underlayingEvents != null){
            return underlayingEvents[0];
        }
        return null;
   }
    
    public void setUnderlayingEvent(MapElement alert){
        if(underlayingEvents == null){
            underlayingEvents = new RelationEvent[1];
        }
        this.underlayingEvents[0] = alert;
    }

    public BehaviorAlarmType getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(BehaviorAlarmType alarmType) {
        this.alarmType = alarmType;
    }
    
    @Override
    public MapElementType getType() {
        return MapElementType.BEHAVIOR_ALARM;
    }
    
    @Override
    public long getTimestampGap() {
        return timestamp - underlayingEvents[underlayingEvents.length-1].getTimestamp();
    }
    
    public String getTimestampDescription() {
        String result = "";
        DateFormat format = new SimpleDateFormat(Templates.KML_DATE_FORMAT);

        if(underlayingEvents.length > 1){
            StringBuilder sb = new StringBuilder();
            sb.append(format.format(underlayingEvents[0].getTimestamp()));
            sb.append("->");
            sb.append(format.format(underlayingEvents[underlayingEvents.length-1].getTimestamp()));
            
            result = sb.toString();
        }else{
            result = format.format(getTimestamp());
                     
        }
        
        return result;
    }

    public abstract String getAlarmTitle();
    public abstract String getAlarmDescription();

}
