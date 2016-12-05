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
package ceptraj.EPA.adaptor;

import ceptraj.EPA.adaptor.deliver.LocationDeliverer;
import ceptraj.config.ConfigProvider;
import static ceptraj.config.ConfigProvider.SpaceType.cartesian;
import static ceptraj.config.ConfigProvider.SpaceType.lat_lon;
import ceptraj.event.MapElement;
import ceptraj.event.location.RawLocationEvent;
import ceptraj.output.EventConsumer;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import ceptraj.tool.Point;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public abstract class AdaptorEPA{
    
    static protected Logger LOG = Logger.getLogger(AdaptorEPA.class); 
    
    Connection connection = null;
    Statement statement = null;
 
    public void start(LocationDeliverer producer, boolean includeAdaptor) {
        try{
            
            if(includeAdaptor){
                producer.setAdaptor(this);
            }else{
                List<MapElement> incomingEvents = generateTargetEvents();
                
            try{
                if(connection != null)
                  connection.close();
              }catch(SQLException e){
                // connection close failed.
                LOG.error("Error while closing database ",e);
              }
                
                producer.setLocations(incomingEvents);
            }
                                   
            Thread t = new Thread(producer);
            t.start();     
            
            t.join();
            
        }catch(Exception e){
            LOG.error("Error in AdaptorEPA ", e);
        }
    }
    
    
    public void start(LocationDeliverer producer, EventConsumer eventConsumer, boolean... mode) {
        try{
            if(mode.length > 0){
                start(producer,mode[0]);
            }else{
                start(producer,false);
            }            
                 
            eventConsumer.postProcessAllEvents();

        }catch(Exception e){
            LOG.error("Error in AdaptorEPA ", e);
        }
    }
    
    public abstract List<MapElement> generateTargetEvents();
    
    public abstract MapElement getNextEvent();
            
    protected RawLocationEvent makeUpLocationEvent(
            String id, 
            double lat, 
            double lon, 
            Date d, 
            int counter,
            double... speed){
        

        LatLng latLn = new LatLng(lat, lon);
        UTMRef utm = latLn.toUTMRef();                

        RawLocationEvent event = new RawLocationEvent();
        Point p = new Point();

        
        switch(ConfigProvider.getSpaceType()){
            case lat_lon:
                //UTM coords
                p.setY(utm.getNorthing());
                p.setX(utm.getEasting());
                p.setLatZone(utm.getLatZone());
                p.setLngZone(utm.getLngZone());
                //lat long coords
                p.setLat(lat);
                p.setLon(lon);
                break;
            case cartesian:
                p.setX(lon);
                p.setY(lat);
                break;                
        }
        
        p.setTimestamp(d.getTime());
        p.setNumSeq(counter);
        
        if(speed.length > 0){
            p.setSpeed(speed[0]);
        }

        event.setId(id);
        event.setLocation(p);
        event.setTimestamp(d.getTime());
        event.setLevel(0);
        
        String[] parts = id.split("_");
        
        
        if(statement != null && Integer.valueOf(parts[2]) < 502){
            try{
                StringBuilder sqlCode = new StringBuilder();
                sqlCode.append("insert into BBFOldTest ('id','seq','t','x','y','speed','nextX','nextY') ");
                sqlCode.append("values(");
                String[] idParts = id.split("_");
                sqlCode.append(Integer.valueOf(idParts[2]));
                sqlCode.append(",");
                sqlCode.append(p.getNumSeq());
                sqlCode.append(",");
                sqlCode.append(d.getTime());
                sqlCode.append(",");
                sqlCode.append(lon);
                sqlCode.append(",");
                sqlCode.append(lat);
                sqlCode.append(",0.0,-1.0,-1.0)");          

//                LOG.debug(sqlCode);
                statement.executeUpdate(sqlCode.toString());
            }catch(Exception e){
              // if the error message is "out of memory", 
              // it probably means no database file is found
              LOG.error("Error while executing SQL statement ",e);
            }      
        }
        return event;
    }
    
    protected void configMySQLiteDDBB(){
        
        
        try{
           Class.forName("org.sqlite.JDBC");
          // create a database connection
          connection = DriverManager.getConnection("jdbc:sqlite:geolife_163_60.db");
          statement = connection.createStatement();
//          statement.setQueryTimeout(30);  // set timeout to 30 sec.

          statement.executeUpdate("drop table if exists BBFOldTest");
          statement.executeUpdate("CREATE TABLE BBFOldTest(type varchar(20), id INTEGER, seq INTEGER, class INTEGER, t INTEGER, x FLOAT, y FLOAT, speed FLOAT, nextX INTEGER, nextY INTEGER)");

          LOG.info("SQLite database configured.");
        }catch(Exception e){
          // if the error message is "out of memory", 
          // it probably means no database file is found
          LOG.error("Error while creating database ",e);
        }    
    }

}
