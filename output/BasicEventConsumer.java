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
package ceptraj.output;

import ceptraj.config.ConfigProvider;
import ceptraj.event.MapElement;
import ceptraj.event.MapElementType;
import static ceptraj.event.MapElementType.RELATIONSHIP;
import static ceptraj.event.MapElementType.TRAJECTORY;
import static ceptraj.event.MapElementType.TRAJECTORY_CHANGE;
import ceptraj.event.alarm.AlarmEvent;
import ceptraj.event.alarm.behavior.BehaviorAlarmEvent;
import ceptraj.event.alarm.behavior.BehaviorAlarmType;
import ceptraj.event.itinerary.ItineraryFinishesStartsEvent;
import ceptraj.event.location.LocationEvent;
import ceptraj.event.relationship.RelationEvent;
import ceptraj.event.relationship.RelationType;
import ceptraj.event.trajectory.TrajectoryEvent;
import ceptraj.event.trajectory.change.TrajectoryChangeEvent;
import ceptraj.event.trajectory.change.TrajectoryChangeType;
import ceptraj.output.visualizer.OutputType;
import static ceptraj.output.visualizer.OutputType.GPX;
import static ceptraj.output.visualizer.OutputType.KML;
import static ceptraj.output.visualizer.OutputType.PLAIN_TEXT;
import ceptraj.tool.Color;
import ceptraj.tool.Templates;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author fernando
 */
public abstract class BasicEventConsumer implements EventConsumer {
    
    protected static Logger LOG = Logger.getLogger(BasicEventConsumer.class); 

    protected EventCounter counter = new EventCounter();
    
    protected Map<String, Map<Integer, List<MapElement>>> speedLocationEvents;
    protected Map<String, Map<Integer, List<MapElement>>> multiLocationEvents;
    protected Map<String, Map<Integer, List<MapElement>>> bearingLocationEvents;
    
    protected Map<String, Map<Integer, List<MapElement>>> trajectoryEvents;
    protected Map<String, Map<Integer, List<MapElement>>> bearingTrajectoryChangeEvents;
    protected Map<String, Map<Integer, List<MapElement>>> speedTrajectoryChangeEvents;
    protected Map<String, Map<Integer, List<MapElement>>> multiTrajectoryChangeEvents;
    
    protected Map<String, Map<Integer, List<MapElement>>> relationEvents;
    
    protected Map<String, Map<Integer, List<MapElement>>> alarmEvents;
   
    protected Map<String, Map<Integer, List<MapElement>>> itineraryEvents;

    
    private String dateStr = "";
    
    public BasicEventConsumer() {
        
        bearingLocationEvents = new HashMap<String, Map<Integer, List<MapElement>>>();    
        speedLocationEvents = new HashMap<String, Map<Integer, List<MapElement>>>();    
        multiLocationEvents = new HashMap<String, Map<Integer, List<MapElement>>>();    
        
        trajectoryEvents = new HashMap<String, Map<Integer, List<MapElement>>>();
        
        bearingTrajectoryChangeEvents = new HashMap<String, Map<Integer, List<MapElement>>>();  
        speedTrajectoryChangeEvents = new HashMap<String, Map<Integer, List<MapElement>>>();     
        multiTrajectoryChangeEvents = new HashMap<String, Map<Integer, List<MapElement>>>(); 
        
        relationEvents = new HashMap<String, Map<Integer, List<MapElement>>>();    
       
        alarmEvents = new HashMap<String, Map<Integer, List<MapElement>>>();        

        itineraryEvents = new HashMap<String, Map<Integer, List<MapElement>>>();        

    } 
    
    @Override
    public void processEvent(MapElement event) {
               
        switch(event.getType()){
            case LOCATION:
                processLocationEvent((LocationEvent) event);
                break;
            case BEHAVIOR_ALARM:
                processAlarmEvent((AlarmEvent) event); 
                break;  
            case TRAJECTORY:
                processTrajectoryEvent((TrajectoryEvent) event);
                counter.newEvent(event);
                break;
            case TRAJECTORY_CHANGE:
                processTrajectoryChangeEvent((TrajectoryChangeEvent) event);
                counter.newEvent(event);
                break;
            case ITINERARY:
                processItineraryEvent((ItineraryFinishesStartsEvent) event);
                break;
            case RELATIONSHIP:
                processRelationshipEvent((RelationEvent) event);  
                counter.newEvent(event);
        }
        
    }
    
    @Override
    public void postProcessAllEvents(){
        serializeTraceInFilePath(ConfigProvider.getOutputPath(), OutputType.KML);
    }  
        
    public void serializeTraceInFilePath(String path, OutputType outputType) {                          
        
        printMapElements(path, "trajectoryChange_multi", outputType, multiTrajectoryChangeEvents);    
        printMapElements(path, "trajectoryChange_bearing", outputType, bearingTrajectoryChangeEvents);    
        printMapElements(path, "trajectoryChange_speed", outputType, speedTrajectoryChangeEvents);        

        printMapElements(path, "location", outputType, bearingLocationEvents);    
        printMapElements(path, "location_speed", outputType, speedLocationEvents);    
        printMapElements(path, "location_multi", outputType, multiLocationEvents);    
        printMapElements(path, "relation", outputType, relationEvents);    
        
        printMapElements(path, "alarmEvents", outputType, alarmEvents);   
        printAllMapElements(path, "alarmEvents", outputType, alarmEvents, MapElementType.BEHAVIOR_ALARM);

    }
    
    protected String getGeneralHeaderForOutputType(OutputType type, String id){
        String header = "";
        
        switch(type){
            case PLAIN_TEXT:
                header = Templates.GPSVISUALIZER_WEB_HEAD;
                break;
            case GPX:
                header = Templates.GPX_HEAD;
                break;
            case KML:
                header = Templates.KML_GENERAL_HEAD;
                header = header.replace("ELEMENT_NAME", id);
                for(Color c : Color.values()){
                    String aux = Templates.KML_TRACK_STYLE;
                    aux = aux.replace("NUM_LEVEL", String.valueOf(c.getLevel()));
                    aux = aux.replace("COLOR_CODE", c.getHexCode());
                    header += aux;
                }
                
                for(RelationType rType : RelationType.values()){
                    String rTypeKMLStyle = Templates.KML_RELATION_STYLE;
                    rTypeKMLStyle = rTypeKMLStyle.replace("RELATION", rType.toString());
                    rTypeKMLStyle = rTypeKMLStyle.replace("LINE_COLOR", rType.getKMLParams().getLineColor().getHexCode());
                    
                    header += rTypeKMLStyle;
                    
                }
               
               header += Templates.KML_ACTIVITY_SMUGLING_STYLE;
               header += Templates.KML_ACTIVITY_FISHING_STYLE;               
               
                int i = 0;
                for(TrajectoryChangeType changeType : TrajectoryChangeType.values()){
                    String aux = Templates.KML_CHANGE_STYLE;
                    aux = aux.replace("CHANGE_TYPE", changeType.getDescriptor());
                    aux = aux.replace("COLOR_CODE", Color.values()[i++].getHexCode());
                    
                    header += aux;
                }
                
                break;
        }
                
        return header;
    }
    
    protected String getSpecificHeaderForOutputType(
            OutputType typeOfOutput, 
            MapElementType typeOfEvent,
            String eventName,
            int level){
        
        String header = "";
        
        switch(typeOfOutput){
            case GPX:
                header = "<trk><name>"+eventName+"_"+dateStr+"_"+level+"</name>\n";
                if(MapElementType.LOCATION.equals(typeOfEvent)){
                    header +=  "\t<trkseg>\n";
                }
                break;
            case KML:
                header = Templates.KML_SPECIFIC_HEAD;
                String elementName = eventName + "_" + level;
                header = header.replace("ELEMENT_NAME", elementName);
                break;                

        }
        
        return header;
    }
    
    protected String getGeneralTailForOutputType(
            OutputType typeOfOutput,
            MapElementType typeOfEvent){
        
        String header = "";
        
        switch(typeOfOutput){
            case GPX:
                header = Templates.GPX_TAIL;
                break;
            case KML:
                header = Templates.KML_GENERAL_TAIL;
                break;
        }
        
        return header;
    }
    
    protected String getSpecificTailForOutputType(
            OutputType typeOfOutput,
            MapElementType typeOfEvent){
        
        String header = "";
        
        switch(typeOfOutput){
            case GPX:
                if(MapElementType.LOCATION.equals(typeOfEvent)){
                    header += "\t</trkseg>\n";
                }
                header += "</trk>\n";
                break;
            case KML:
                header = Templates.KML_SPECIFIC_TAIL;
        }
        return header;
    }
    
    protected void printMapElements(
            String path,             
            String typeOfEvent,
            OutputType traceOutputType,
            Map<String, Map<Integer, List<MapElement>>> mapElements){
        
        if(!mapElements.isEmpty()){
            try{
                MapElementType type = null;
                
                LOG.debug("Serializing "+typeOfEvent +" in "+traceOutputType+" format...");
                
                Set<String> ids = mapElements.keySet();
                
                for(String id : ids){
                    
                    Map<Integer, List<MapElement>> mapElementsForId = mapElements.get(id);
                
                    String oFilePath = path + id+ "_" + typeOfEvent;

                    PrintWriter writerAll = new PrintWriter(oFilePath+"."+traceOutputType.getFileExtension());

                    String generalHeader = getGeneralHeaderForOutputType(traceOutputType, id);
                    writerAll.print(generalHeader);

                    Set<Integer> levels = mapElementsForId.keySet();
                    
                    for(int level : levels){                   
                        try{

                            List<MapElement> mapElementsForLevel = mapElementsForId.get(level);                             

                                type = mapElementsForLevel.get(0).getType();

                                String levelHeader = getSpecificHeaderForOutputType(traceOutputType,type,type.toString(),level);
                                writerAll.print(levelHeader);

                                int i = 0;
                                while(i < mapElementsForLevel.size()){
                                    MapElement mapElement = mapElementsForLevel.get(i++);

                                    switch(traceOutputType){
                                        case PLAIN_TEXT:
                                        case GPX:
                                            writerAll.print(mapElement.serialize(traceOutputType));
                                            break;
                                        case KML:
                                            MapElement aux = null;
                                            if(typeOfEvent.contains("location")){
                                                if(i< mapElementsForLevel.size()){
                                                    aux = mapElementsForLevel.get(i);
                                                    writerAll.print(mapElement.serialize(traceOutputType, aux));
                                                }
                                            }else{
                                                writerAll.print(mapElement.serialize(traceOutputType, aux));
                                            }                                                
                                            break;
                                    }                                                                
                                }

                                writerAll.print(getSpecificTailForOutputType(traceOutputType, type));

                        }catch(Exception e){
                            LOG.error("Error serializating event trace for level "+level, e);
                        }
                    }

                    writerAll.print(getGeneralTailForOutputType(traceOutputType,type));
                    writerAll.close();
                }

            }catch(Exception e){
                LOG.error("Error serializating "+ typeOfEvent +" trace ", e);
            }
        }        
    }
    
    private List<Integer> getLevels(Map<String, Map<Integer, List<MapElement>>> mapElements){
        List<Integer> levels = new ArrayList<Integer>();
        Set<String> ids = mapElements.keySet();
                
        for(String id : ids){
            Map<Integer, List<MapElement>> mapElementsForId = mapElements.get(id);
            Set<Integer> levelsForId = mapElementsForId.keySet();
            for(int l : levelsForId){
                if(!levels.contains(l)){
                    levels.add(l);
                }
            }
        }
        
        Collections.sort(levels);
        
        return levels;
    }
    
    protected void printAllMapElements(
            String path,             
            String typeOfEvent,
            OutputType traceOutputType,
            Map<String, Map<Integer, List<MapElement>>> mapElements,
            MapElementType type){
        
        int nFB = 0;
        int nSM = 0;
        
        if(!mapElements.isEmpty()){
            try{                
                
                LOG.debug("Serializing all "+typeOfEvent +" in "+traceOutputType+" format...");
 
               String oFilePath = path + "all_" + typeOfEvent;

                PrintWriter writerAll = new PrintWriter(oFilePath+"."+traceOutputType.getFileExtension());

                String generalHeader = getGeneralHeaderForOutputType(traceOutputType, "all");
                writerAll.print(generalHeader);
                
                Set<String> ids = mapElements.keySet();
                List<Integer> levels = getLevels(mapElements);
                
                for(int level : levels){
    
                    String levelHeader = getSpecificHeaderForOutputType(traceOutputType,type,type.toString(),level);
                    writerAll.print(levelHeader);
                    
                    for(String id : ids){

                        Map<Integer, List<MapElement>> mapElementsForId = mapElements.get(id);

                            try{
                                List<MapElement> mapElementsForLevel = mapElementsForId.get(level);                             
                                if(mapElementsForLevel!= null && !mapElementsForLevel.isEmpty()){
                                    int i = 0;
                                    while(i < mapElementsForLevel.size()){
                                        MapElement mapElement = mapElementsForLevel.get(i++);

                                        if(type.equals(MapElementType.BEHAVIOR_ALARM)){
                                            BehaviorAlarmType aType = ((BehaviorAlarmEvent) mapElement).getAlarmType();
                                            switch(aType){
                                                case POSSIBLE_SMUGGLING:
                                                    nSM++;
                                                    break;
                                                case FISHING_BEHAVIOR:
                                                    nFB++;
                                                    break;
                                            }
                                        }
                                        
                                        switch(traceOutputType){
                                            case PLAIN_TEXT:
                                            case GPX:
                                                writerAll.print(mapElement.serialize(traceOutputType));
                                                break;
                                            case KML:
                                                MapElement aux = null;
                                                if(typeOfEvent.contains("location")){
                                                    if(i< mapElementsForLevel.size()){
                                                        aux = mapElementsForLevel.get(i);
                                                        writerAll.print(mapElement.serialize(traceOutputType, aux));
                                                    }
                                                }else{
                                                    writerAll.print(mapElement.serialize(traceOutputType, aux));
                                                }                                                
                                                break;
                                        }                                                                
                                    }
                                }

                            }catch(Exception e){
                                LOG.error("Error serializating event trace for level "+level, e);
                            }                        
                    }
                    writerAll.print(getSpecificTailForOutputType(traceOutputType, type));

                }
                writerAll.print(getGeneralTailForOutputType(traceOutputType,type));
                writerAll.flush();
                writerAll.close();

            }catch(Exception e){
                LOG.error("Error serializating "+ typeOfEvent +" trace ", e);
            }
            LOG.info("Num FB:"+nFB);
            LOG.info("Num SM:"+nSM);
        }        
    }
    
    protected String getCurrentDateString(){
        if(dateStr.length() == 0){
            Date date = new Date();
        
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            dateStr = format.format(date);
        }
        
        return dateStr;
        
    }
            
    protected abstract void processTrajectoryChangeEvent(TrajectoryChangeEvent event);   
    
    protected abstract void processTrajectoryEvent(TrajectoryEvent event);
            
    protected abstract void processRelationshipEvent(RelationEvent event);
            
    protected abstract void processLocationEvent(LocationEvent event);  
    
    protected abstract void processItineraryEvent(ItineraryFinishesStartsEvent event);
                
    protected abstract void processAlarmEvent(AlarmEvent event);
      
}
