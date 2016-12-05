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
package ceptraj.output.visualizer;

import ceptraj.output.BasicEventConsumer;
import ceptraj.config.ConfigProvider;
import ceptraj.event.MapElement;
import ceptraj.event.alarm.AlarmEvent;
import ceptraj.event.itinerary.ItineraryFinishesStartsEvent;
import ceptraj.event.location.LocationEvent;
import ceptraj.event.relationship.RelationEvent;
import ceptraj.event.trajectory.TrajectoryEvent;
import ceptraj.event.trajectory.change.TrajectoryChangeEvent;
import ceptraj.event.trajectory.change.TrajectoryMultiChangeEvent;
import ceptraj.event.trajectory.change.TrajectorySingleChangeEvent;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import ceptraj.output.EventCounter.EventRecord;
import ceptraj.tool.Constants;
import ceptraj.tool.Templates;
import ceptraj.tool.supportFunction.SpeedFunction;
import ceptraj.tool.supportFunction.SpeedFunction.StopThresholdItem;

/**
 * Class that registers the different events created by the system and makes
 * up the output files.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class Visualizer extends BasicEventConsumer{

    static long AVG_COUNTER_MAX = 2;//10,5

    protected PrintWriter generalWriter; 
    protected int nFilteredLocations = 0;

    public int getNumFilteredLocations(){
        return nFilteredLocations;
    }
    
    private void registerEvent(
            MapElement newElement, 
            Map<String, Map<Integer, List<MapElement>>> listOfElements){
        
        List<MapElement> eventsForDeviceAndLevel;
        Map<Integer, List<MapElement>> elementsForDevice;
        
        if(listOfElements.containsKey(newElement.getId())){
            elementsForDevice = listOfElements.get(newElement.getId());
            if(elementsForDevice.containsKey(newElement.getLevel())){
                eventsForDeviceAndLevel = elementsForDevice.get(newElement.getLevel());
            }else{
                eventsForDeviceAndLevel = new LinkedList<MapElement>();
            }
        }else{
            elementsForDevice = new HashMap<Integer, List<MapElement>>();
            eventsForDeviceAndLevel = new LinkedList<MapElement>();
        }
        
        eventsForDeviceAndLevel.add(newElement);        
        elementsForDevice.put(newElement.getLevel(), eventsForDeviceAndLevel);
        
        listOfElements.put(newElement.getId(), elementsForDevice); 
    }
    
    @Override
    protected void processLocationEvent(LocationEvent event) {
        if(event.getLevel()==1){
            nFilteredLocations++;
        }else{
            registerEvent(event, bearingLocationEvents);
        }
     
    }
    
    protected void registerSpeedLocationEvent(LocationEvent event) {
        registerEvent(event, speedLocationEvents);
     
    }
        
    protected void registerMultiLocationEvent(LocationEvent event) {
        registerEvent(event, multiLocationEvents);
     
    }
    
    @Override
    protected void processTrajectoryChangeEvent(TrajectoryChangeEvent event) {
        
        switch(event.getChangeType()){
            case MULTI_CHANGE:
                registerMultiTrajectoryChangeEvent((TrajectoryMultiChangeEvent) event);
                registerMultiLocationEvent(event.toLocationEvent());
                break;
            case BEARING_INCREASING:
            case BEARING_DECREASING:
                registerBearingTrajectoryChangeEvent((TrajectorySingleChangeEvent)event);
                break;
            case SPEED_INCREASING:
            case SPEED_DECREASING:
                registerSpeedTrajectoryChangeEvent((TrajectorySingleChangeEvent)event);
                registerSpeedLocationEvent(event.toLocationEvent());
                break;                
        } 
                
    }
    
    protected void registerMultiTrajectoryChangeEvent(TrajectoryMultiChangeEvent event){
        
        registerEvent(event, multiTrajectoryChangeEvents);
 
    }
    
    protected void registerBearingTrajectoryChangeEvent(TrajectorySingleChangeEvent event){
        
        registerEvent(event, bearingTrajectoryChangeEvents);
        
    }
    
    protected void registerSpeedTrajectoryChangeEvent(TrajectorySingleChangeEvent event){
        
        registerEvent(event, speedTrajectoryChangeEvents);

    }

    @Override
    protected void processTrajectoryEvent(TrajectoryEvent event) {        
        registerEvent(event, trajectoryEvents);         
    }   

    @Override
    protected void processRelationshipEvent(RelationEvent event) {
        registerEvent(event, relationEvents);
          
    }
    
    @Override
    protected void processAlarmEvent(AlarmEvent event) {
        
        registerEvent(event, alarmEvents);
    }
    
    @Override
    protected void processItineraryEvent(ItineraryFinishesStartsEvent event) {
        registerEvent(event, itineraryEvents);
    }
    
    @Override
    public void serializeTraceInFilePath(String path, OutputType outputType) {                          
        
        super.serializeTraceInFilePath(path, outputType);
        
        printEventRegister(path);

    }
    
    
    protected void printEventRegister(String path){
    
        try{
            PrintWriter dataWriter = new PrintWriter(path + File.separator + "event_register.txt");
            
            long smoothTimeInterval = 30000;
            long initialTimeInterval = counter.getEventRecords().get(0).getTimestamp();                                    
                
            long avgCounter = 0;
            int n = 0;
            for(EventRecord r : counter.getEventRecords()){
                dataWriter.print(r.getTimestamp()/1000 + "\t" + r.getNumEvents() + "\n");               
            }
            
            dataWriter.close();
        }catch(Exception e){
            LOG.error("Error", e);
        }
    }   
            
    
    protected void printSpeedPlot(String path){
        
        try{

            LOG.debug("Serializing speed plot...");
            
            path= path + File.separator + "speed_plot" + File.separator;
            
            Set<String> ids = bearingLocationEvents.keySet();

            for(String id : ids){
                
                String gnuPlotContentFile = Templates.GNUPLOT_SPEED_CHANGE_HEAD;
                
                Map<Integer, List<MapElement>> bearingElementsForId =  bearingLocationEvents.get(id);
                Map<Integer, List<MapElement>> trajectoryElementsForId =  trajectoryEvents.get(id);
                
                String speedFileName = id+ "_speed_"+  ConfigProvider.getNumObjs() + "_" + ConfigProvider.getTimeGap() + "_" + ConfigProvider.getNumIterations()+"_" + ConfigProvider.getRelationWinAdaptMechanism();
                
                gnuPlotContentFile = gnuPlotContentFile.replace("<OUTPUT_FILE_NAME>", speedFileName);
                
                String speedDataFileName = speedFileName + ".txt";
                String speedDataFilePath = path + speedDataFileName;
                
                String stopThresholdDataFileName = speedFileName + "_stop_threshold.txt";
                
                PrintWriter dataWriter = new PrintWriter(speedDataFilePath);
                
                List<MapElement> locations = bearingElementsForId.get(0);
                List<MapElement> trajectories = trajectoryElementsForId.get(1);
                long initialTimestamp = locations.get(0).getTimestamp();
                int j = 0;
                double maxSpeed = 0;
                //Average variables used to smooth the output
                double smoothedSpeed = 0;
                long avgTimestamp =0;
                long smoothCounter = 0;
                for(int i = 0; i< trajectories.size() && j< locations.size(); i++){
                    
                    LocationEvent l = (LocationEvent) locations.get(j);
                    TrajectoryEvent trajectory = (TrajectoryEvent) trajectories.get(i);

                    while((l.getTimestamp() <= trajectory.getTimestamp()) && j < (locations.size()-1)){
                                   
                        double instantSpeed = trajectory.getAvgInfSpeed();//l.getLocation().getSpeed()*10)/36;  
                        long coarseGrainedTimestamp = ((l.getTimestamp() - initialTimestamp)/1000)/60;
                        
                        //Non-smoothed display
//                        dataWriter.print(coarseGrainedTimestamp);
//                        dataWriter.print("\t");
//                        dataWriter.println(instantSpeed);
//                        if(instantSpeed > maxSpeed){
//                            maxSpeed = instantSpeed;
//                         }
                        
                        //Smoothed display                        
                        if(coarseGrainedTimestamp == avgTimestamp){
                            smoothedSpeed += instantSpeed;
                            smoothCounter++;
                            
                        }else{
                            dataWriter.print(avgTimestamp);
                            dataWriter.print("\t");
                            dataWriter.println(smoothedSpeed/smoothCounter);
                            
                            if(smoothedSpeed/smoothCounter > maxSpeed){
                                maxSpeed =  smoothedSpeed/smoothCounter;
                            }
                            
                            smoothedSpeed = smoothCounter = 0;
                            avgTimestamp = coarseGrainedTimestamp; 
                        } 
                        //End smoothed display
                                               
                        l = (LocationEvent) locations.get(++j);
 
                    }                    
                }
               
                long lastTimestamp = locations.get(locations.size()-1).getTimestamp();                               
                long timestampGap = ((lastTimestamp-initialTimestamp)/1000)/60;
                
                long xtics = Math.round(timestampGap / 12);
                
                // Here we replace the general params of the file tempalte with
                // the specific values for the moving entity id.
                gnuPlotContentFile = gnuPlotContentFile.replace("<ID>", id);
                gnuPlotContentFile = gnuPlotContentFile.replace("<X_RANGE>", String.valueOf(timestampGap));
                gnuPlotContentFile = gnuPlotContentFile.replace("<Y_RANGE>", String.valueOf(maxSpeed+1));
                gnuPlotContentFile = gnuPlotContentFile.replace("<X_TIC>", String.valueOf(xtics));
                if(maxSpeed > 20){
                    gnuPlotContentFile = gnuPlotContentFile.replace("<Y_TIC>", String.valueOf(Math.round(maxSpeed / 20)));                
                }else{
                    gnuPlotContentFile = gnuPlotContentFile.replace("<Y_TIC>", "1");                
                }
                gnuPlotContentFile = gnuPlotContentFile.replace("<DATA_FILE_NAME>", speedDataFileName);
                gnuPlotContentFile = gnuPlotContentFile.replace("<STOP_FILE_NAME>", stopThresholdDataFileName);

                
                if(speedTrajectoryChangeEvents.containsKey(id)){
                     
                    bearingElementsForId = speedTrajectoryChangeEvents.get(id);
                    List<MapElement> mapElementsForIdAndLevel = bearingElementsForId.get(1); 
                    StringBuilder trajectoryChangesForScript = new StringBuilder();
                    for(MapElement mapElementForIdAndLevel : mapElementsForIdAndLevel){
                        TrajectoryChangeEvent trajChangeEvent = (TrajectoryChangeEvent) mapElementForIdAndLevel;
                        trajectoryChangesForScript.append(trajChangeEvent.toGNUPlotRect(initialTimestamp/1000, (int) Math.round(maxSpeed)));
                    }
                    gnuPlotContentFile = gnuPlotContentFile.replace("<RECTANGLE_SECTION>", trajectoryChangesForScript.toString());
 
                }else{
                    gnuPlotContentFile = gnuPlotContentFile.replace("<RECTANGLE_SECTION>", "");
                }
                
                String speedGapScriptFileName = speedFileName + ".gp";
                String speedGapScriptFilePath = path + speedGapScriptFileName;
                
                PrintWriter scriptWriter = new PrintWriter(speedGapScriptFilePath);
                scriptWriter.println(gnuPlotContentFile);
                
                dataWriter.close();
                scriptWriter.close();
                
                //We create the file comprising the log of stop thresholds.
                String stopThresholdDataFilePath = path + stopThresholdDataFileName;
                PrintWriter writer = new PrintWriter(stopThresholdDataFilePath);

                List<StopThresholdItem> stopThresholds = SpeedFunction.getStopThresholdLog(id);
                           
                smoothedSpeed = 0;
                avgTimestamp =0;
                
                if(stopThresholds != null){
                for(StopThresholdItem item : stopThresholds){
                    
                        long coarseGrainedTimestamp= ((item.getTimestamp() - initialTimestamp)/1000)/60;

                        //Non-averaged display
    //                    writer.println(coarseGrainedTimestamp + "\t" + item.getValue());

                        //Averaged display
                        if(coarseGrainedTimestamp == avgTimestamp){
                            if(item.getValue() > smoothedSpeed){
                                smoothedSpeed = item.getValue();
                            }

                        }else{
                            dataWriter.print(avgTimestamp);
                            dataWriter.print("\t");
                            dataWriter.println(smoothedSpeed);

                            smoothedSpeed = 0;
                            avgTimestamp = coarseGrainedTimestamp;
                        } 
                        writer.println(avgTimestamp + "\t" + item.getValue());
                        //End Averaged display

                    }
                }
                writer.close();
                
                // Now we generate the plot by means of GNUPlot
                String OS = System.getProperty("os.name").toLowerCase(); 
                if(OS.contains(Constants.WINDOWS_NAME)){
                    Runtime.getRuntime().exec("cmd /c cmd.exe /K \"cd "+ path +" && gnuplot "+ speedGapScriptFileName + "\""); 
                }else if(OS.contains(Constants.LINUX_NAME)){                                            
                    List<String> commands = new ArrayList<String>();
                    commands.add("gnuplot");
                    //Add arguments
                    commands.add(speedGapScriptFileName);

                    //Run macro on target
                    ProcessBuilder pb = new ProcessBuilder(commands);
                    pb.directory(new File(path));
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    if(process.waitFor() != 0){
                        LOG.error("Error when creating plot " +speedGapScriptFileName);
                    }                    
                } 
            }
 
        }catch(Exception e){
            LOG.error("Error serializating speed gap", e);
        }
    }
    
    
    protected void printBearingPlot(String path){
        
        try{

            LOG.debug("Serializing bearing plot...");
            
            path = path + File.separator + "bearing_plot" + File.separator;

            DecimalFormat twoDForm = new DecimalFormat("#.##");
            
            Set<String> ids = trajectoryEvents.keySet();

            for(String id : ids){
                
                String gnuPlotContentFile = Templates.GNUPLOT_BEARING_CHANGE_HEAD;

                Map<Integer, List<MapElement>> mapElementsForId = trajectoryEvents.get(id);
                String bearingGapFileName =  id+ "_bearing_gap_"+ ConfigProvider.getNumObjs() + "_" + ConfigProvider.getTimeGap() + "_" + ConfigProvider.getNumIterations()+ "_"  + ConfigProvider.getRelationWinAdaptMechanism();
                
                gnuPlotContentFile = gnuPlotContentFile.replace("<OUTPUT_FILE_NAME>", bearingGapFileName);

                
                String bearingGapDataFileName = bearingGapFileName + ".txt";
                String bearingGapDataFilePath = path + bearingGapDataFileName;
                
                PrintWriter dataWriter = new PrintWriter(bearingGapDataFilePath);
                
                List<MapElement> trajectories = mapElementsForId.get(1);
                long initialTimestamp = trajectories.get(0).getTimestamp();
                double avgBearing = 0;
                long avgTimestamp = 0;
                long bearingCounter = 0;
                for(int i = 0; i < trajectories.size(); i++){
                    TrajectoryEvent l = (TrajectoryEvent) trajectories.get(i);

                    avgBearing += l.getStraightBearing();
                    avgTimestamp += (l.getTimestamp() - initialTimestamp);
                    bearingCounter++;
                    
                    if(bearingCounter >= AVG_COUNTER_MAX){
                        avgTimestamp /= bearingCounter;
                        avgBearing /= bearingCounter;
                        
                        dataWriter.print((avgTimestamp/1000)/60);
                        dataWriter.print("\t");
                        dataWriter.println(twoDForm.format(avgBearing).replace(",", "."));
                        
                        avgBearing = 0;
                        avgTimestamp = 0;
                        bearingCounter = 0;
                    }
                }
                long lastTimestamp = trajectories.get(trajectories.size()-1).getTimestamp();
                
                long timestampGap = ((lastTimestamp-initialTimestamp)/1000) /60;
                
                long xtics = Math.round(timestampGap / 12);
                
                gnuPlotContentFile = gnuPlotContentFile.replace("<ID>", id);
                gnuPlotContentFile = gnuPlotContentFile.replace("<X_RANGE>", String.valueOf(timestampGap));
                gnuPlotContentFile = gnuPlotContentFile.replace("<X_TIC>", String.valueOf(xtics));
                gnuPlotContentFile = gnuPlotContentFile.replace("<Y_TIC>", "20");                

                gnuPlotContentFile = gnuPlotContentFile.replace("<DATA_FILE_NAME>", bearingGapDataFileName);
                
                if(bearingTrajectoryChangeEvents.containsKey(id)){
                     
                    mapElementsForId = bearingTrajectoryChangeEvents.get(id);
                    List<MapElement> mapElementsForIdAndLevel = mapElementsForId.get(1); 
                    StringBuilder trajectoryChangesForScript = new StringBuilder();
                    for(MapElement mapElementForIdAndLevel : mapElementsForIdAndLevel){
                        TrajectoryChangeEvent trajChangeEvent = (TrajectoryChangeEvent) mapElementForIdAndLevel;
                        trajectoryChangesForScript.append(trajChangeEvent.toGNUPlotRect(initialTimestamp / 1000, 360));
                    }
                    gnuPlotContentFile = gnuPlotContentFile.replace("<RECTANGLE_SECTION>", trajectoryChangesForScript.toString());
 
                }else{
                    gnuPlotContentFile = gnuPlotContentFile.replace("<RECTANGLE_SECTION>", "");
                }
                
                String bearingGapScriptFileName = bearingGapFileName + ".gp";
                String bearingGapScriptFilePath = path + bearingGapScriptFileName;
                
                PrintWriter scriptWriter = new PrintWriter(bearingGapScriptFilePath);
                scriptWriter.println(gnuPlotContentFile);
                
                dataWriter.close();
                scriptWriter.close();
                
                String OS = System.getProperty("os.name").toLowerCase();  
                if(OS.contains(Constants.WINDOWS_NAME)){
                    Runtime.getRuntime().exec("cmd /c cmd.exe /K \"cd "+ path +" && gnuplot "+ bearingGapScriptFileName + "\""); 
                }else if(OS.contains(Constants.LINUX_NAME)){                                            
                    List<String> commands = new ArrayList<String>();
                    commands.add("gnuplot");
                    //Add arguments
                    commands.add(bearingGapScriptFileName);

                    //Run macro on target
                    ProcessBuilder pb = new ProcessBuilder(commands);
                    pb.directory(new File(path));
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    if(process.waitFor() != 0){
                        LOG.error("Error when creating plot " +bearingGapScriptFileName);
                    }   
                }
            }
        }catch(Exception e){
            LOG.error("Error serializating bearing gap", e);
        }
    }
        
    protected void printTimeGap(  
            String head,            
            String path,
            String typeOfEvent,
            Map<Integer, List<MapElement>> mapElements){
        
        if(!mapElements.isEmpty()){
            try{                                
                LOG.debug("Serializing gap times for event "+typeOfEvent);
                                                
                String traceOutputFileName = head + "_" + typeOfEvent +"_all";

                String traceOutputDateFileName = traceOutputFileName + ".dat";
                
                PrintWriter writerAll = new PrintWriter(path + traceOutputDateFileName);
                
                Set<Integer> levels = mapElements.keySet();
                
                List<Integer> noShowedElements = new LinkedList<Integer>();
                noShowedElements.add(new Integer(0));
                noShowedElements.add(new Integer(1));
//                noShowedElements.add(new Integer(2));
                noShowedElements.add(new Integer(6));
                
                levels.removeAll(noShowedElements);
                
                Map<Integer, String> values = new HashMap<Integer, String>();
                double maxY = 0;
                for(int level : levels){  
                    try{
                        List<MapElement> mapElementsForLevel = mapElements.get(level);                       
                        
                        int index = 0;
                        for(MapElement element : mapElementsForLevel){
                            String st = "";
                            double value = element.getTimestampGap()/1000;
                            
                            if(value > maxY){
                                maxY = value;
                            }
                            
                            if(values.containsKey(index)){
                                st = values.get(index);
                                st = st + "\t"+ value;
                            }else{
                                st = String.valueOf(value);
                            }
                            
                            values.put(index, st);
                            index++;
                        }
                        
                    }catch(Exception e){
                        LOG.error("Error serializating delays for level "+level, e);
                    }
                }
                
                //Create GNUPlot script
                
                PrintWriter writerGNUPlot = new PrintWriter(path + traceOutputFileName+".gp");
                String header = Templates.GNUPLOT_HEAD;
          
                StringBuilder xticsLine = new StringBuilder();
                
                int xCoords = (levels.size()/2)+1;
                
                ArrayList<Integer> orderedKeys = new ArrayList(values.keySet());    
                Collections.sort(orderedKeys);
                
                for(int i : orderedKeys){
                    
                    if(xticsLine.length() > 0){
                        xticsLine.append(", ");
                    }else{
                        xticsLine.append("set xtics (");
                    }
                    
                    String value = values.get(i);                    
                    writerAll.println(xCoords + "\t" + value);
                    
                    xticsLine.append("\"");
                    xticsLine.append(i);
                    xticsLine.append("\" ");
                    xticsLine.append(xCoords);
                    
                    xCoords += levels.size();                    
                    xCoords++;
                }
                        
                xticsLine.append(") border out nomirror");
                
                writerAll.close();
                
                header = header.replace("<maxy>", String.valueOf(maxY));
                header = header.replace("<maxx>", String.valueOf(xCoords));
                                              
                writerGNUPlot.print(header);
                writerGNUPlot.println(xticsLine.toString());
                
                StringBuilder plotPartSt = new StringBuilder();
                String beginPlotPart = "plot ";
                plotPartSt.append(beginPlotPart);
                
                StringBuilder plotPartTemplate = new StringBuilder();
                plotPartTemplate.append("'");
                plotPartTemplate.append(traceOutputDateFileName);
                plotPartTemplate.append("' ");
                plotPartTemplate.append("using ($1<val1>):($<val2>):(1) w boxes fs pattern 3 lw 1 title \"Level <val3>\"");
                
                int numLevels = levels.size();
                double inc = 0.5;
                if((numLevels % 2) != 0){
                    inc = 1;
                    numLevels--;
                }
                
                double column1Value = 0 - (inc * (numLevels/2));
                
                for(int i : levels){
                   
                    if(plotPartSt.length()> beginPlotPart.length()){
                        plotPartSt.append(", ");
                    }
                    
                    String plotPart = plotPartTemplate.toString();
                    int newIndex = i-noShowedElements.size()+2;
                    
                    String column1String = "";
                    
                    if(column1Value > 0){
                        column1String = "+" + String.valueOf(column1Value);
                    }else if(column1Value < 0){
                        column1String = String.valueOf(column1Value);                        
                    }
                    
                    plotPart = plotPart.replace("<val1>", column1String);
                    plotPart = plotPart.replace("<val2>", String.valueOf(newIndex+1));
                    plotPart = plotPart.replace("<val3>", String.valueOf(i));
                                        
                    plotPartSt.append(plotPart);
                    
                    column1Value += inc;
                }
                
                writerGNUPlot.println(plotPartSt.toString());
                writerGNUPlot.close();

            }catch(Exception e){
                LOG.error("Error serializating delays ", e);
            }  
        }        
    }
}
