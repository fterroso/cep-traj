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
package ceptraj.event.location;

import ceptraj.event.MapElement;
import ceptraj.event.MapElementType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import ceptraj.tool.Color;
import ceptraj.tool.Point;
import ceptraj.tool.Templates;
import ceptraj.output.visualizer.OutputType;

/**
 * Class that represents a single point in the trajectory of an object.
 *
 * @author calcifer
 */
public class LocationEvent extends MapElement{
      
    long sourceTimestamp;    
    MapElementType type = MapElementType.LOCATION;
    
    boolean isLast = false;    
    Point location;

    public boolean getIsLast() {
        return isLast;
    }

    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }        
    
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public long getSourceTimestamp() {
        return sourceTimestamp;
    }

    public void setSourceTimestamp(long sourceTimestamp) {
        this.sourceTimestamp = sourceTimestamp;
    }           
    
    @Override
    public long getTimestampGap(){
        
        return timestamp - location.getTimestamp();
    }
  
    @Override
    public String serialize(OutputType format, MapElement... next){
        String result = "";
        switch(format){
            case GPX:
                result = toGPXFormat();
                break;
            case PLAIN_TEXT:
                result = toPlainTextFormat();
                break;
            case KML:
                if(next.length > 0){
                    result = toKMLFormat((LocationEvent) next[0]);                
                }else{
                    result = toKMLFormat();
                }
                break;
        }
        
        return result;
    }
    
    @Override
    protected String toPlainTextFormat(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.getId());
        sb.append(",");
        sb.append(this.location.getTimestamp());
        sb.append(",");
        sb.append(this.location.getLat());
        sb.append(",");
        sb.append(this.location.getLon());       
        
        return sb.toString();
        
    }
        
    @Override
    protected String toGPXFormat(){
        StringBuilder sb = new StringBuilder();
        sb.append("\t<trkpt lat=\"");
        sb.append(location.getLat());
        sb.append("\"  lon=\"");
        sb.append(location.getLon());
        sb.append("\">\n\t\t<time>");
        
        Date date = new Date(getTimestamp());
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sb.append(format.format(date));        
        
        sb.append("</time>\n");
        sb.append("\t\t<desc>");
        sb.append(location.getNumSeq());
        sb.append(", ");
        sb.append(format.format(location.getTimestamp()));
        sb.append("</desc>\n\t</trkpt>\n");
                
        return sb.toString();
        
    }
    
    protected String toKMLFormat(LocationEvent next){
        
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<Placemark>\n");
        
        sb.append("\t<name>");
        sb.append(format.format(getLocation().getTimestamp()));
        sb.append("</name>\n");
        
        sb.append("\t<styleUrl>#linestyle");
        sb.append(getLevel());
        sb.append("</styleUrl>\n");
        
        sb.append("\t<TimeSpan>\n");
        sb.append("\t\t<begin>"); 
        format = new SimpleDateFormat(Templates.KML_DATE_FORMAT);
        sb.append(format.format(next.getTimestamp()));         
        sb.append("</begin>\n");
        sb.append("\t</TimeSpan>\n");
        
        sb.append("\t<LineString>\n");
        sb.append("\t\t<extrude>1</extrude>\n");
        sb.append("\t\t<tessellate>1</tessellate>\n");
        sb.append("\t\t<coordinates>");
        sb.append(getLocation().getLon());
        sb.append(",");
        sb.append(getLocation().getLat());
        sb.append(",0,");
        sb.append(next.getLocation().getLon());
        sb.append(",");
        sb.append(next.getLocation().getLat());
        sb.append(",0");        
        sb.append("</coordinates>\n");
        sb.append("\t</LineString>\n");
        
        sb.append("</Placemark>\n");
                
        return sb.toString();
    }
    
    @Override
    protected String toKMLFormat(){
        StringBuilder sb= new StringBuilder();
        sb.append("<when>");
        DateFormat format = new SimpleDateFormat(Templates.KML_DATE_FORMAT);
        sb.append(format.format(timestamp));
        sb.append("</when>\n");
        sb.append("<gx:coord>");
        sb.append(location.getLon());
        sb.append(" ");
        sb.append(location.getLat());
        sb.append(" 0");
        sb.append("</gx:coord>\n");
        sb.append("<gx:angles>0  0  0</gx:angles>\n");
        
        return sb.toString();

    }
    
    @Override
    public String toString(){
        
        StringBuilder des = new StringBuilder();
        des.append("[");
        des.append("id=");
        des.append(id);
        des.append(", ");        
        des.append(level);
        des.append(", "); 
        Date date = new Date(timestamp);
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        des.append(format.format(date));  
        des.append(", ");
        des.append(location);
        des.append("]");
        
        return des.toString();
        
    }

    @Override
    public MapElementType getType() {
        return type;
    }
    
}
