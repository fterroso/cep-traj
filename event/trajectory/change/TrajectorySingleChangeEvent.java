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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import ceptraj.tool.Point;

/**
 *
 * @author calcifer
 */
public abstract class TrajectorySingleChangeEvent extends TrajectoryChangeEvent{
  
    Point[] tail;
    Point[] head;
    Point[] middle;

    private List<Point> involvedPath = null;
    private List<TrajectoryChangeType> underlayingTypes = null;
    
    //temporal (for tests)
    double counter = Integer.MIN_VALUE;

    public Point[] getHead() {
        return head;
    }

    public void setHead(Point[] head) {
        this.head = head;
    }

    public Point[] getMiddle() {
        return middle;
    }

    public void setMiddle(Point[] middle) {
        this.middle = middle;
    }
    
    public Point[] getTail() {
        return tail;
    }

    public void setTail(Point[] tail) {
        this.tail = tail;
    }
    
    @Override
    public Point getHeadPoint(){
        return head[head.length-1];
    }
    
    @Override
    public Point getTailPoint(){
        return tail[0];
    }

    @Override
    public Point getMiddlePoint(){
        if(middle != null){
            return middle[middle.length/2];
        }else{
            return tail[tail.length/2];
        }
    }

    public void setInvolvedPath(Collection<Point> involvedPath) {
        this.involvedPath =  new LinkedList(involvedPath);
    }
        
  
    @Override
    public Collection<Point> getInvolvedPath(){
        if(involvedPath == null){
            involvedPath = new LinkedList<Point>();
            involvedPath.addAll(Arrays.asList(tail));
            if(middle != null){
                involvedPath.addAll(Arrays.asList(middle));
            }
            involvedPath.addAll(Arrays.asList(head));
        }        
        return involvedPath;
    }

    public double getCounter() {
        if(counter == Integer.MIN_VALUE){
            Collection<Point> points = getInvolvedPath();
            List<Point>  l = new ArrayList(points);
            double i = l.get(0).getNumSeq();
            double f = l.get(l.size()-1).getNumSeq();
            counter = f-i;
        }
        return counter;
    }

    public void setCounter(double counter) {
        this.counter = counter;
    }
    
    

    @Override
    public long getTimestampGap(){
        
        return timestamp - getMiddlePoint().getTimestamp();
    }
    
    @Override
    protected String toGPXFormat() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("<trkseg>\n");
        
        sb.append("\t<trkpt lat=\"");
        sb.append(getTailPoint().getLat());
        sb.append("\"  lon=\"");
        sb.append(getTailPoint().getLon());
        sb.append("\">\n\t\t<time>");
        sb.append(timestamp);
        sb.append("</time>\n\t\t<ele>0</ele>\n\t</trkpt>\n");    
        
        sb.append("\t<trkpt lat=\"");
        sb.append(getMiddlePoint().getLat());
        sb.append("\"  lon=\"");
        sb.append(getMiddlePoint().getLon());
        sb.append("\">\n\t\t<time>");
        
        Date date = new Date(timestamp);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        sb.append(format.format(date)); 
        sb.append("</time>\n\t\t<ele>0</ele>\n\t</trkpt>\n"); 
        
        sb.append("\t<trkpt lat=\"");
        sb.append(getHeadPoint().getLat());
        sb.append("\"  lon=\"");
        sb.append(getHeadPoint().getLon());
        sb.append("\">\n\t\t<time>");
        sb.append(timestamp);
        sb.append("</time>\n\t\t<ele>0</ele>\n\t</trkpt>\n");         
        
        sb.append("</trkseg>\n");

        return sb.toString();  
    }
        
    @Override
    public List<TrajectoryChangeType> getUnderlayingTypes(){
        if(underlayingTypes == null){
            underlayingTypes = new LinkedList<TrajectoryChangeType>();
            underlayingTypes.add(type);
        }
        
        return underlayingTypes;
    }
    
    @Override
    public boolean isDifferentChange(List<TrajectoryChangeType> types){              
        switch(type){
            case BEARING_INCREASING:
            case BEARING_DECREASING:
                for(TrajectoryChangeType t : types){
                   switch(t){
                       case BEARING_INCREASING:
                       case BEARING_DECREASING:
                           return false;
                   }    
                }
                break;  
            case SPEED_INCREASING:
            case SPEED_DECREASING:
                for(TrajectoryChangeType t : types){
                   switch(t){
                       case SPEED_INCREASING:
                       case SPEED_DECREASING:
                           return false;
                   }    
                }
                break;                  
        }
        return true;
    }       
}
