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

import ceptraj.config.ConfigProvider;
import static ceptraj.config.ConfigProvider.SpaceType.cartesian;
import static ceptraj.config.ConfigProvider.SpaceType.lat_lon;
import ceptraj.event.MapElement;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.Logger;
import ceptraj.tool.Constants;
import ceptraj.tool.Point;
import java.util.List;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

/**
 * Class with the global functions related to the management of the locations
 * comprised in the different events of the framework.
 *
 * @author Fernando Terroso-Saenz.
 */
public class LocationFunction {
    
    static Logger LOG = Logger.getLogger(LocationFunction.class);    
    
    public static double euclideanDist(Point p1, Point p2){
        
        double a = Math.pow(p1.getX()-p2.getX(), 2);
        double b = Math.pow(p1.getY()-p2.getY(), 2);
                
        return Math.sqrt(a+b);
    }    
    
    public static double euclideanDist2(String id,Point p1, Point p2){
        
        double a = Math.pow(p1.getX()-p2.getX(), 2);
        double b = Math.pow(p1.getY()-p2.getY(), 2);
                
        return Math.sqrt(a+b);
    }
    
    public static double haversineDistance(Point p1, Point p2) {
        float lat1 = (float) p1.getLat();
        float lat2 = (float) p2.getLat();
        float lng1 = (float) p1.getLon();
        float lng2 = (float) p2.getLon();
        
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng/2) * Math.sin(dLng/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = Constants.EARTH_RADIUS * c;        
                    
        return dist * 1000;
    
    }
    
    public static Point getCentroidFromPoints(List<Point> points){
        
        double xCentroid = 0;
        double yCentroid = 0;
        for(Point p : points){
            xCentroid += p.getX();
            yCentroid += p.getY();
        }
        
        xCentroid /= points.size();
        yCentroid /= points.size();
        
        Point centroid = new Point();
        centroid.setX(xCentroid);
        centroid.setY(yCentroid);
        
        UTMRef aux = new UTMRef(xCentroid,xCentroid, points.get(0).getLatZone(),points.get(0).getLngZone());
        LatLng aux2 = aux.toLatLng();
        centroid.setLat(aux2.getLat());
        centroid.setLon(aux2.getLng());
        
        return centroid;
    }
    
    public static double dist(Point p1, Point p2) {
        switch(ConfigProvider.getSpaceType()){
            case lat_lon:
                return haversineDistance(p1, p2);
            case cartesian:
                return euclideanDist(p1,p2);
        }
        
        return Double.MAX_VALUE;
    }

    
    public static Point translatePoint(
            Point origin,
            double bearing,
            double dist){
        
        float originLat = (float) Math.toRadians(origin.getLat());
        float originLon = (float) Math.toRadians(origin.getLon());      
        bearing = Math.toRadians(bearing);
        
        double lat2 = Math.asin( Math.sin(originLat)*Math.cos(dist/Constants.EARTH_RADIUS) + 
              Math.cos(originLat)*Math.sin(dist/Constants.EARTH_RADIUS)*Math.cos(bearing));
        
        double lon2 = originLon + Math.atan2(Math.sin(bearing)*Math.sin(dist/Constants.EARTH_RADIUS)*Math.cos(originLat), 
                     Math.cos(dist/Constants.EARTH_RADIUS)-Math.sin(originLat)*Math.sin(lat2));
        
        Point p2 = new Point(Math.toDegrees(lat2),Math.toDegrees(lon2));
        
        return p2;
    }
    
    /* Function which calculates the hevasine distance between p2 and the point
     * which is "speed*(finaltimestamp-initialTimestamp)" meters from origin
     * heading the direction defined by the param bearing.
     * 
     */
    public static double dist2(
            Point origin, 
            double bearing, 
            double speed,
            long finalTimestamp,
            long initialTimestamp,
            Point p2){

        switch(ConfigProvider.getSpaceType()){
            case lat_lon:
                double dist = speed * ((double)(finalTimestamp-initialTimestamp)/(double)1000);                
                dist /=1000;
                return haversineDistance(translatePoint(origin,bearing,dist), p2);
            case cartesian:
                return euclideanDist(origin,p2);
        }
        
        return Double.MAX_VALUE;
               
    }
    
    
    public static MapElement[] mergeElements(
            MapElement e1, 
            MapElement[] e2, 
            MapElement e3, 
            MapElement e4){
        
        ArrayList<MapElement> result  = new ArrayList<MapElement>();
        
        if(e1 != null){
            result.add(e1);
        }
        
        if(e2 != null){
            result.addAll(Arrays.asList(e2));
        }      
       
        result.add(e3);
        result.add(e4);
        
        return result.toArray(new MapElement[result.size()]);
    }          
    
    public static MapElement[] mergeElements(
            MapElement e1, 
            MapElement e2, 
            MapElement e3, 
            MapElement e4){            
        
        return new MapElement[]{e1,e2,e3,e4};        
    }  
    
    public static MapElement[] mergeElements(
            MapElement e1, 
            MapElement e2, 
            MapElement e3){
        
        return new MapElement[]{e1,e2,e3};  
    } 
       
}
