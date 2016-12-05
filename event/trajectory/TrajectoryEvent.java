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
package ceptraj.event.trajectory;

import ceptraj.event.MapElement;
import ceptraj.event.MapElementType;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import ceptraj.tool.Bearing;
import ceptraj.tool.Point;
import ceptraj.tool.supportFunction.SpeedFunction;

/**
 * Class that represents the event of a trajectory. That is, a moving point.
 *
 * @author calcifer
 */
public class TrajectoryEvent extends MapElement{
    
    //Time interval during which this trajectory happened.
    long initialTimestamp;
    long finalTimestamp;
    
    MapElementType type = MapElementType.TRAJECTORY;
     
    Point[] locations = new Point[20];
    
    //Speed info about the trajectory
    double avgSpeed;
    double currentSpeed;
    double avgInfSpeed; // Speed infered by the system.
    
    double avgBearing;    
    double straightBearing;
    double revStraightBearing;
   
    public double getAvgBearing() {
        return avgBearing;
    }

    public void setAvgBearing(double avgBearing) {
        this.avgBearing = avgBearing;
    }

    public double getStraightBearing() {
        return straightBearing;
    }

    public void setStraightBearing(double straightBearing) {
        this.straightBearing = straightBearing;
    }

    public double getRevStraightBearing() {
        return revStraightBearing;
    }

    public void setRevStraightBearing(double revStraightBearing) {
        this.revStraightBearing = revStraightBearing;
    }  

    public Point[] getLocations() {
        return locations;
    }

    public void setLocations(Point[] locations) {
        this.locations = locations;
    }
    
    public Point getHead(){
        return locations[locations.length-1];
    }
    
    public void setHead(){}
    
    public Point getTail(){
        return locations[0];
    }
    
    public void setTail(Point p){}

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
    
    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public double getAvgInfSpeed() {
        return avgInfSpeed;
    }

    public void setAvgInfSpeed(double avgInfSpeed) {
        this.avgInfSpeed = avgInfSpeed;
    }
 
    @Override
    public MapElementType getType() {
        return type;
    }

    @Override
    public long getTimestampGap(){
        return timestamp - finalTimestamp;
    }
    
    @Override
    protected String toPlainTextFormat(){
        
        StringBuilder des = new StringBuilder();
        des.append("[");
        des.append("id=");
        des.append(id);
        des.append(", ");
        des.append(level);
        des.append(", ");
        
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        des.append(format.format(timestamp));                 
        
        des.append(", time:{");
        des.append(format.format(initialTimestamp));
        des.append(",");
        des.append(format.format(finalTimestamp));
        des.append("}, "); 

        DecimalFormat twoDForm = new DecimalFormat("#.##");
        
        des.append("speed:{");
        des.append("inf: "); 
        des.append(twoDForm.format(avgInfSpeed).replace(",", "."));
        des.append(", thres: ");
        des.append(twoDForm.format(SpeedFunction.stopThreshold(id)).replace(",", "."));
        des.append("}");       
        
        des.append(", bearing:{str: ");
        des.append(Bearing.getFineGrainBearingFromValue(straightBearing));
        des.append("(");
        des.append(twoDForm.format(straightBearing).replace(",", "."));
        des.append(")");
        des.append("}");
        
        des.append("]");
        return des.toString();
                
    } 

    @Override
    protected String toGPXFormat(){
        StringBuilder sb = new StringBuilder();
        sb.append("<trkseg>\n");
        
        sb.append("\t<trkpt lat=\"");
        sb.append(locations[0].getLat());
        sb.append("\"  lon=\"");
        sb.append(locations[0].getLon());
        sb.append("\">\n\t\t<time>");
        
        Date date = new Date(timestamp);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        sb.append(format.format(date)); 
        sb.append("</time>\n\t\t<ele>0</ele>\n\t</trkpt>\n");            
        sb.append("\t<trkpt lat=\"");
        sb.append(locations[locations.length-1].getLat());
        sb.append("\"  lon=\"");
        sb.append(locations[locations.length-1].getLon());
        sb.append("\">\n\t\t<time>");
        sb.append(timestamp);
        sb.append("</time>\n\t\t<ele>0</ele>\n\t</trkpt>\n"); 
        
        sb.append("</trkseg>\n");

        return sb.toString();        
    }

    
    @Override
    public String toString(){
        
        return toPlainTextFormat();
        
    }

    @Override
    protected String toKMLFormat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
