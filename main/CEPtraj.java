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
package ceptraj.main;

import ceptraj.CEP.BasicCEPConfigurator;
import ceptraj.CEP.CEPConfigurator;
import ceptraj.EPA.EPA;
import ceptraj.EPA.adaptor.AdaptorEPA;
import ceptraj.EPA.adaptor.AdaptorEPAFactory;
import ceptraj.EPA.adaptor.LocationSourceFormat;
import ceptraj.EPA.adaptor.deliver.BasicLocationDeliverer;
import ceptraj.EPA.filter.FilterEPAFactory;
import ceptraj.EPA.itinerary.ItineraryEPA;
import ceptraj.EPA.relationship.RelationEPA;
import ceptraj.EPA.relationship.RelationWinAdaptationMechanismType;
import ceptraj.EPA.trajectory.TrajectoryEPAFactory;
import ceptraj.config.ConfigProvider;
import ceptraj.config.ConfigProvider.SpaceType;
import ceptraj.output.EventConsumer;
import ceptraj.output.visualizer.Visualizer;
import com.espertech.esper.client.EPServiceProvider;
import java.io.File;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Starting point of the system.
 *
 * @author fernando
 */
public class CEPtraj {

    static Logger LOG = Logger.getLogger(CEPtraj.class);  
    
    private static EPServiceProvider cepProvider;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
           
        try{      
          
            
            //Disable Esper log messagges. Too verbose.
            Logger.getLogger("com.espertech.esper").setLevel(Level.ERROR);
            LOG.info("----------------");
            LOG.info("Start CEP-traj execution");
                       
            //Parameters reading
            LocationSourceFormat type = LocationSourceFormat.findByDesc(args[0].toLowerCase());
            
            String outputPath = "." + File.separator + "output";

            SpaceType spaceType = SpaceType.lat_lon;
            EventConsumer eventConsumer = new Visualizer();

            start(RelationWinAdaptationMechanismType.AVERAGE, 
                    type, 
                    eventConsumer, 
                    "", 
                    outputPath, 
                    spaceType,
                    null);
                   
             
                                    
            LOG.info("End of the simulation.");
        
        }catch(Exception e){
            LOG.error("Error en main, ", e);
        }

    }
    
    public static void start(
            LocationSourceFormat type,
            EventConsumer eventConsumer,
            String inputPath,
            String ouputPath,
            EPA extraEPA,
            SpaceType spaceType,
            CEPConfigurator... cepConfigurator) throws Exception{
        
        start(1,1000,1,
                RelationWinAdaptationMechanismType.MAX,
                type,
                eventConsumer,
                inputPath,ouputPath,
                spaceType,
                extraEPA,
                cepConfigurator);
        
    }
    
    public static void start(
            LocationSourceFormat type,
            EventConsumer eventConsumer,
            String inputPath,
            String ouputPath) throws Exception{
        
        start(1,1000,1,
                RelationWinAdaptationMechanismType.MAX,
                type,
                eventConsumer,
                inputPath,ouputPath,
                SpaceType.lat_lon,
                null);
        
    }
    
    protected static void start(
            int numObjsInScenario, 
            long timeGap, 
            int iterationNumber,
            RelationWinAdaptationMechanismType adaptionMechanismType,
            LocationSourceFormat type,
            EventConsumer eventConsumer,
            String inputPath,
            String outputPath,
            SpaceType spaceType,
            EPA extraEPA,
            CEPConfigurator... cepConfigurator) throws Exception{
                
        ////////////TEMPORAL////////////////////////
        ConfigProvider.setNumIterations(iterationNumber);
        ConfigProvider.setNumObjs(numObjsInScenario);
        ConfigProvider.setTimeGap(timeGap);
        ////////////TEMPORAL////////////////////////
        
        start(adaptionMechanismType, 
                type, 
                eventConsumer, 
                inputPath, 
                outputPath, 
                spaceType, 
                extraEPA,
                cepConfigurator);

    }
    
    protected static void start(
            RelationWinAdaptationMechanismType adaptionMechanismType,
            LocationSourceFormat type,
            EventConsumer eventConsumer,
            String inputPath,
            String outputPath,
            SpaceType spaceType,
            EPA extraEPA,
            CEPConfigurator... cepConfigurator) throws Exception{
        
        ////////////TEMPORAL////////////////////////
        ConfigProvider.setRelationWinAdaptMechanism(adaptionMechanismType);
        ConfigProvider.setOutputPath(outputPath);
        ConfigProvider.setSpaceType(spaceType);
        ////////////TEMPORAL////////////////////////

        CEPConfigurator configurator = new BasicCEPConfigurator(); 
        if(cepConfigurator != null && cepConfigurator.length > 0){
            configurator = cepConfigurator[0];   
        }
        cepProvider = configurator.configureCEPEngine();
        
        if(extraEPA != null){
            extraEPA.start(cepProvider, eventConsumer);
        }
        
        EPA itineraryEPA = new ItineraryEPA();
        itineraryEPA.start(cepProvider, eventConsumer);       

        EPA relationship = new RelationEPA();
        relationship.start(cepProvider, eventConsumer);

        EPA trajectory = TrajectoryEPAFactory.getTrajectoryEPA();
        trajectory.start(cepProvider, eventConsumer);

        EPA filter = FilterEPAFactory.getFilterEPAFactory();
        filter.start(cepProvider, eventConsumer);       

        AdaptorEPA adaptor;
        if(inputPath.length() > 0){
            adaptor = AdaptorEPAFactory.getAdaptorEPA(type, inputPath);  
        }else{
            adaptor = AdaptorEPAFactory.getAdaptorEPA(type);  
        }
        
        BasicLocationDeliverer deliverer = new BasicLocationDeliverer(cepProvider, eventConsumer);
        adaptor.start(deliverer, eventConsumer, false);
    }

    public static EPServiceProvider getCepProvider() {
        return cepProvider;
    }
   
}
