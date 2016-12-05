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
package ceptraj.event.relationship;

import ceptraj.event.MapElement;
import ceptraj.event.MapElementType;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import ceptraj.tool.Point;
import ceptraj.tool.areaOfInterest.AreaOfInterestRelationshipType;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class RelationEvent extends MapElement{
    
    long initialTimestamp;
    long finalTimestamp;
    
    RelationType relationshipType;
    
    Point[] locations1 = new Point[20];
    Point[] locations2 = new Point[20];
    
    double bearing1;
    double bearing2;
    
    double speed1;
    double speed2;
    
    Point head1;
    Point head2;
    
    Point tail1;
    Point tail2;
    
        double auxBear;

    public double getAuxBear() {
        return auxBear;
    }

    public void setAuxBear(double auxBear) {
        this.auxBear = auxBear;
    }

    public RelationType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationType type) {
        this.relationshipType = type;
    }

    public Point[] getLocations1() {    
        return locations1;
    }

    public void setLocations1(Point[] locations1) {
        head1 = locations1[locations1.length-1];
        tail1 = locations1[0];
        this.locations1 = locations1;
    }

    public Point[] getLocations2() {
        return locations2;
    }

    public void setLocations2(Point[] locations2) {
        head2 = locations2[locations2.length-1];
        tail2 = locations2[0];
        this.locations2 = locations2;
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

    public double getBearing1() {
        return bearing1;
    }

    public void setBearing1(double avgBearing1) {
        this.bearing1 = avgBearing1;
    }

    public double getBearing2() {
        return bearing2;
    }

    public void setBearing2(double avgBearing2) {
        this.bearing2 = avgBearing2;
    }

    public double getSpeed1() {
        return speed1;
    }

    public void setSpeed1(double speed1) {
        this.speed1 = speed1;
    }

    public double getSpeed2() {
        return speed2;
    }

    public void setSpeed2(double speed2) {
        this.speed2 = speed2;
    }

    public Point getHead1() {
        return head1;
    }

    public Point getHead2() {
        return head2;
    }

    public Point getTail1() {
        return tail1;
    }

    public Point getTail2() {
        return tail2;
    }        

    @Override
    public MapElementType getType() {
        return MapElementType.RELATIONSHIP;
    }

    @Override
    public long getTimestampGap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String toGPXFormat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String toPlainTextFormat() {
       
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        DecimalFormat twoDForm = new DecimalFormat("#.##");

        StringBuilder sb = new StringBuilder();
        sb.append(relationshipType);
        sb.append("[id=");
        sb.append(id);
        sb.append(", ");  
        sb.append(level);
        
        sb.append(", b:{");          
        sb.append(twoDForm.format(bearing1).replace(",", "."));
        sb.append(", ");  
        sb.append(twoDForm.format(bearing2).replace(",", "."));
        sb.append("}");
        
        sb.append(", s:{");          
        sb.append(twoDForm.format(speed1).replace(",", "."));
        sb.append(", ");  
        sb.append(twoDForm.format(speed2).replace(",", "."));
        sb.append("}");
        
        sb.append(", t:{");        
        sb.append(dateFormat.format(initialTimestamp));
        sb.append(", ");
        sb.append(dateFormat.format(finalTimestamp));        
        sb.append("}");
        
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
    protected String toKMLFormat() {
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        
        StringBuilder sb = new StringBuilder();
        sb.append("<Placemark>");
        
        sb.append("\t<name>");    
        sb.append(id);
        sb.append("_");
        sb.append(relationshipType);
        sb.append("</name>\n");
        
        sb.append("\t<styleUrl>#relation_");
        sb.append(relationshipType);
        sb.append("</styleUrl>\n");
        
        sb.append("<description>");
        sb.append("Level:" );
        sb.append(level);
        sb.append("; Bearing1: ");
        sb.append(twoDForm.format(bearing1).replace(",", "."));
        sb.append("; Bearing2: ");
        sb.append(twoDForm.format(bearing2).replace(",", "."));
        
        sb.append("; Initial timestamp: ");
        sb.append(dateFormat.format(initialTimestamp));
        sb.append("; Final timestamp: ");
        sb.append(dateFormat.format(finalTimestamp));
 
        sb.append("; Aux bearing: ");
        sb.append(twoDForm.format(this.auxBear).replace(",", "."));
        
        sb.append("</description>");
        
        sb.append("<gx:MultiTrack id=\"ID\">\n");
        
        sb.append("<gx:Track>\n");
        sb.append("\t<extrude>1</extrude>\n");
        sb.append("\t<altitudeMode>absolute</altitudeMode>\n");
        for(Point p : locations1){
            sb.append(p.toGxTrack());
        }
        sb.append("</gx:Track>\n");
        
        sb.append("<gx:Track>\n");
        sb.append("\t<extrude>1</extrude>\n");
        sb.append("\t<altitudeMode>absolute</altitudeMode>\n");
        for(Point p : locations2){
            sb.append(p.toKML());
        }
        sb.append("</gx:Track>\n");
        
        sb.append("</gx:MultiTrack>\n");

        sb.append("</Placemark>\n");
                
        return sb.toString();
    }
    
    
}
