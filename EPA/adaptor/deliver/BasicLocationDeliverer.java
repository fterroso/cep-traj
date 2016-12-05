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
package ceptraj.EPA.adaptor.deliver;

import ceptraj.EPA.adaptor.AdaptorEPA;
import ceptraj.EPA.filter.listener.SecondFilterListener;
import ceptraj.config.ConfigProvider;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.CurrentTimeSpanEvent;
import ceptraj.event.MapElement;
import ceptraj.event.location.LocationEvent;
import ceptraj.event.location.RawLocationEvent;
import ceptraj.event.trajectory.EndTrajectoryEvent;
import ceptraj.output.EventConsumer;
import ceptraj.output.visualizer.OutputType;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import ceptraj.tool.Constants;
import ceptraj.tool.Point;
import java.io.File;
import java.io.PrintWriter;

/**
 * Class that sends all the MapElement events (alerts, alarms and locations) to 
 * the CEP engine.
 *
 * @author fernando
 */
public class BasicLocationDeliverer implements LocationDeliverer{

    static Logger LOG = Logger.getLogger(BasicLocationDeliverer.class);    
    
    private List<MapElement> locations;
    EPServiceProvider CEPProvider;
    EventConsumer consumer;
    AdaptorEPA adaptor = null;
    
    long prevTimestamp = 0;
    MapElement prevEvent = null;
    int nSentEvents = 0;

    public BasicLocationDeliverer(
            EPServiceProvider CEPProvider,
            EventConsumer consumer){

        this.CEPProvider = CEPProvider;
        this.consumer = consumer;
    }

    @Override
    public void setLocations(List<MapElement> locations) {
        this.locations = locations;
    }
    
    @Override
    public void setAdaptor(AdaptorEPA adaptor) {
        this.adaptor = adaptor;
    }
    
    
    @Override
    public void run() {
        try{
            String id = "";            
            if(adaptor == null){
                LOG.info("First-read-all-then-send mode...");
                Collections.sort(locations);
                

                long startTime = locations.get(0).getTimestamp();
                CEPProvider.getEPRuntime().sendEvent(new CurrentTimeEvent(startTime));
                
                LOG.info("Creating CSV");
                PrintWriter w = new PrintWriter("geolife_constrained.csv");
                for(MapElement e : locations){                              
                    LocationEvent le = (LocationEvent)e;
                    w.println(le.serialize(OutputType.PLAIN_TEXT));
                }
                w.flush();
                w.close();
                LOG.info("CSV created");
                
                
                for(MapElement e : locations){
                    sendEvent(e);
                    prevEvent = e;
                }

               
            }else{
                LOG.info("Little-by-little mode...");
                MapElement e = adaptor.getNextEvent();
                id = e.getId();
                long startTime = e.getTimestamp();
                CEPProvider.getEPRuntime().sendEvent(new CurrentTimeEvent(startTime));
                
                while(e!=null){
                    sendEvent(e);
                    prevEvent = e;
                    e = adaptor.getNextEvent();
                }
            }
            
            long lastTimestamp = prevEvent.getTimestamp()+1;
            CEPProvider.getEPRuntime().sendEvent(new CurrentTimeEvent(lastTimestamp));
            
            EndTrajectoryEvent endItinerary = new EndTrajectoryEvent();
            endItinerary.setTimestamp(lastTimestamp);
            endItinerary.setLevel(1);
            endItinerary.setId(id);

            CEPProvider.getEPRuntime().sendEvent(endItinerary);
            CEPProvider.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(lastTimestamp + 10*60*1000, 100));
            
            TimeUnit.MILLISECONDS.sleep(Constants.WAITING_TIME_TO_FINISH);
        }catch(Exception e){
            LOG.error("Something wrong happend in sender thread ", e);
        }
    }   
    
    private void sendEvent(MapElement e){
        try{
            nSentEvents++;
            if(e.getTimestamp() != prevTimestamp){
                CEPProvider.getEPRuntime().sendEvent(new CurrentTimeEvent(e.getTimestamp()));
            }
            RawLocationEvent rle = (RawLocationEvent) e;
            Point p = rle.getLocation();
            p.setRealTimestamp(System.currentTimeMillis());
            rle.setLocation(p);
            e = rle;
            CEPProvider.getEPRuntime().sendEvent(e); 
            consumer.processEvent(e);
            prevTimestamp = e.getTimestamp();
        }catch(Exception err){
            LOG.error("Error while sending event " +e, err);
        }
    }


        
}
