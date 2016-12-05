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
package ceptraj.event;

import ceptraj.tool.areaOfInterest.AreaOfInterestRelationshipType;
import ceptraj.output.visualizer.OutputType;
import ceptraj.tool.Constants;
import java.sql.Timestamp;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public abstract class MapElement implements Comparable{
    
    protected String id;
    protected long timestamp;
    protected int level;
    
    protected AreaOfInterestRelationshipType areaRelationship = AreaOfInterestRelationshipType.NONE;
    protected String involvedAreaOfInterestName;

    //Only for Microsoft Geolife datasets
    protected String movingObjId;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if(id.startsWith(Constants.GEOLIFE_ID_PREFIX)){
            String parts[] = id.split("_");
            movingObjId = parts[1].trim();
        }
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public AreaOfInterestRelationshipType getAreaRelationship() {
        return areaRelationship;
    }

    public void setAreaRelationship(AreaOfInterestRelationshipType areaRelationship) {
        this.areaRelationship = areaRelationship;
    }

    
    public String getInvolvedAreaOfInterestName() {
        return involvedAreaOfInterestName;
    }

    public void setInvolvedAreaOfInterestName(String involvedAreaOfInterestName) {
        this.involvedAreaOfInterestName = involvedAreaOfInterestName;
    }

    public String getMovingObjId() {
        return movingObjId;
    }

    public String serialize(OutputType format, MapElement... aux){
        
        String result = "";
        switch(format){
            case GPX:
                result = toGPXFormat();
                break;
            case PLAIN_TEXT:
                result = toPlainTextFormat();
                break;
            case KML:
                result = toKMLFormat();
                break;                    
        }
        
        return result;
    }

    @Override
    public int compareTo(Object o) {
        MapElement l = (MapElement)o;
        
        Timestamp t=  new Timestamp(getTimestamp());
        return t.compareTo(new Timestamp(l.getTimestamp()));
                
    }
    
    @Override
    public String toString(){
        return toPlainTextFormat();
    }
      
    public abstract MapElementType getType();        
    public abstract long getTimestampGap();    
    protected abstract String toGPXFormat();    
    protected abstract String toPlainTextFormat();    
    protected abstract String toKMLFormat();

}
