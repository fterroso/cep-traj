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
package ceptraj.event.trajectory.change;

import ceptraj.event.MapElement;
import ceptraj.event.MapElementType;
import ceptraj.event.location.LocationEvent;
import ceptraj.event.trajectory.change.value.TrajectoryChangeValue;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import ceptraj.tool.Color;
import ceptraj.tool.Point;
import ceptraj.tool.areaOfInterest.AreaOfInterestRelationshipType;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public abstract class TrajectoryChangeEvent extends MapElement {
       
    static Logger LOG = Logger.getLogger(TrajectoryChangeEvent.class);    
    
    long initialTimestamp;
    long finalTimestamp;    
    
    MapElementType typeElement = MapElementType.TRAJECTORY_CHANGE;    
    TrajectoryChangeType type;

    public TrajectoryChangeType getChangeType() {
        return type;
    }
    
    public abstract boolean isBearingChange();
    public abstract boolean isSpeedChange();

    public void setChangeType(TrajectoryChangeType type) {
        this.type = type;
    }        

    public long getInitialTimestamp() {
        return initialTimestamp;
    }

    public void setInitialTimestamp(long initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }

    public long getFinalTimestamp() {
        return finalTimestamp;
    }

    public void setFinalTimestamp(long finalTimestamp) {
        this.finalTimestamp = finalTimestamp;
    }
    
    @Override
    public MapElementType getType() {
        return typeElement;
    }       
    
    public String toGNUPlotRect(long timestampZero, int yCoord){
        
        DateFormat format = new SimpleDateFormat("Hmmss");
        
        long initialCoordinate = (((initialTimestamp)/1000) - timestampZero)/60;
        long finalCoordinate = ((finalTimestamp/1000) - timestampZero)/60;
        
        StringBuilder GNUPlotRect = new StringBuilder();
        GNUPlotRect.append("set object ");
        GNUPlotRect.append(format.format(timestamp));
        GNUPlotRect.append(" rect behind from ");
        if(initialCoordinate != finalCoordinate){
            GNUPlotRect.append(initialCoordinate);
            GNUPlotRect.append(",0 to ");
            GNUPlotRect.append(finalCoordinate);
        }else{
            GNUPlotRect.append(initialCoordinate - 0.5);
            GNUPlotRect.append(",0 to ");
            GNUPlotRect.append(initialCoordinate + 0.5);
        }
        GNUPlotRect.append(", ");
        GNUPlotRect.append(yCoord);
        GNUPlotRect.append("\n");
        
        return GNUPlotRect.toString();
        
    }
    
    protected String getColorCodeFromLevel(){        
        return Color.getColorForLevel(level).getName();
    }
    
    @Override
    public String toPlainTextFormat(){

        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();

        sb.append(type.getDescriptor());
        sb.append("[");
        sb.append("id=");
        sb.append(id);
        sb.append(", ");
        sb.append(level); 
        sb.append(", ");
        sb.append(format.format(timestamp));
        sb.append(", {");
        sb.append(format.format(initialTimestamp));
        sb.append("->");
        sb.append(format.format(finalTimestamp));
        sb.append("}, ");
        sb.append(getPlainTextDescription());
        
        if(!AreaOfInterestRelationshipType.NONE.equals(areaRelationship)){
            sb.append(", {");
            sb.append(areaRelationship);
            sb.append(" with ");
            sb.append(involvedAreaOfInterestName);
            sb.append("}");
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    
    @Override
    protected String toKMLFormat(){
        
        StringBuilder sb = new StringBuilder();
      
        //Header   
        sb.append("<Placemark>\n");
        sb.append("\t<name>");    
        sb.append(id);
        sb.append("_");
        sb.append(type);
        sb.append("</name>\n");
        
        sb.append("\t<styleUrl>#changestyle");
        sb.append(type.getDescriptor());
        sb.append("</styleUrl>\n");
        
        //Description
        sb.append("\t<description>");
        sb.append(getKMLDescription());
        sb.append("</description>\n");
               
        sb.append("<gx:Track>\n");
        for(Point p : getInvolvedPath()){
            sb.append(p.toGxTrack());
        }
        sb.append("</gx:Track>\n");             
        sb.append("</Placemark>\n");
        
        return sb.toString();
    }
    
    public LocationEvent toLocationEvent(){
        LocationEvent locationEvent = new LocationEvent();
        locationEvent.setId(id);
        locationEvent.setTimestamp(timestamp);
        locationEvent.setLocation(getMiddlePoint());
        locationEvent.setLevel(level+1);
        
        return locationEvent;
    }
   
    protected abstract String getPlainTextDescription();
    protected abstract String getKMLDescription();
    public abstract Collection<Point> getInvolvedPath();
    public abstract List<TrajectoryChangeType> getUnderlayingTypes();
    public abstract TrajectoryChangeValue getChangeValue();
    public abstract boolean isDifferentChange(List<TrajectoryChangeType> types);
    public abstract Point getHeadPoint();
    public abstract Point getMiddlePoint();
    public abstract Point getTailPoint();

}
