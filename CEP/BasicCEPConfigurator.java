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
package ceptraj.CEP;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import ceptraj.event.context.areaOfInterest.AreaOfInterestEvent;
import ceptraj.event.itinerary.ItineraryFinishesStartsEvent;
import ceptraj.event.location.FilteredLocationEvent;
import ceptraj.event.location.RawLocationEvent;
import ceptraj.event.location.SemiFilteredLocationEvent;
import ceptraj.event.relationship.RelationEvent;
import ceptraj.event.relationship.adaptation.dynamic.RelationWinSizeOpEvent;
import ceptraj.event.trajectory.TrajectoryEvent;
import ceptraj.event.trajectory.change.BearingTrajectoryChangeEvent;
import ceptraj.event.trajectory.change.SpeedTrajectoryChangeEvent;
import ceptraj.event.trajectory.change.TrajectoryChangeEvent;
import ceptraj.event.trajectory.change.TrajectoryMultiChangeEvent;
import ceptraj.tool.Bearing;
import ceptraj.tool.Constants;
import ceptraj.tool.aggretationFunction.AggregateMiddlePointFunctionFactory;
import ceptraj.tool.supportFunction.LocationFunction;
import ceptraj.tool.filterAdaption.FirstFilterMechanism;
import ceptraj.tool.filterAdaption.SecondFilterMechanism;
import ceptraj.tool.supportFunction.SpeedFunction;

/**
 *
 * @author fernando
 */
public class BasicCEPConfigurator implements CEPConfigurator{

    @Override
    public EPServiceProvider configureCEPEngine() {
        
        Configuration configuration = new Configuration();
        configuration.getEngineDefaults().getExecution().setPrioritized(true);
        configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
        
        registerEvents(configuration);
        registerVariables(configuration);
        registerAggregationFunctions(configuration);
        registerSingleRowFunctions(configuration);
        
        EPServiceProvider epServiceProveedor = EPServiceProviderManager.getProvider("CEPTraj", configuration);
        epServiceProveedor.initialize();
        
        return epServiceProveedor;
    }
    
    protected void registerEvents(Configuration configuration){
        
        /* Trajectory event */
        configuration.addImport("ceptraj.event.trajectory.change.*");
        configuration.addImport("ceptraj.event.relationship.*");
        configuration.addImport("ceptraj.event.alarm.behavior.*");
        configuration.addImport("ceptraj.tool.filterAdaption.*");

        configuration.addImport("ceptraj.tool.supportFunction.*");
        configuration.addImport("ceptraj.tool.areaOfInterest.*");
        
        configuration.addEventType(AreaOfInterestEvent.class);           
        
        configuration.addEventType(SemiFilteredLocationEvent.class);             
        configuration.addEventType(FilteredLocationEvent.class);             
        configuration.addEventType(RawLocationEvent.class);
        
        configuration.addEventType(TrajectoryEvent.class);
        
        configuration.addEventType(ItineraryFinishesStartsEvent.class);
        
        configuration.addEventType(BearingTrajectoryChangeEvent.class);   
        configuration.addEventType(SpeedTrajectoryChangeEvent.class);                
        configuration.addEventType(TrajectoryMultiChangeEvent.class);  
        configuration.addEventType(TrajectoryChangeEvent.class);      

        configuration.addEventType(RelationEvent.class);  
        configuration.addEventType(RelationWinSizeOpEvent.class);   

    }
    
    protected void registerVariables(Configuration configuration){
        
        //Filtering params.
        configuration.addVariable("locationMaxDev", Double.class, FirstFilterMechanism.LOCATION_MAX_DEVIATION_FACTOR); 
        
        configuration.addVariable("bearingMinChange", Double.class, Constants.DEFAULT_BEARING_MIN_CHANGE); // radians     
        configuration.addVariable("speedMinChangePerc", Double.class, Constants.DEFAULT_SPEED_MIN_CHANGE); // 20%      

        configuration.addVariable("bearingChangeWin", Double.class, 60); // seconds    
        configuration.addVariable("speedChangeWin", Double.class, 60); // seconds      
        configuration.addVariable("multiChangeWin", Double.class, 60); // seconds  
        
        configuration.addVariable("maxTimeBetweenLocations", Long.class, Constants.MAX_TIME_BETWEEN_LOCATIONS); // milliseconds   

        configuration.addVariable("minLocationsPerTrajectory", Integer.class, Constants.INITIAL_POINTS_PER_LEVEL);
        configuration.addVariable("minLocationsPerLongTrajectory", Integer.class, Constants.INITIAL_POINTS_PER_LEVEL*2);
                        
        configuration.addVariable("minTimeBetweenItineraries", Long.class, 900000); // milliseconds

        
        //Parallel relationship params
        configuration.addVariable("parallelMaxDist", Double.class, 100); // meters     
        configuration.addVariable("parallelMaxBearingDiff", Double.class, 2); // radians
        
        //Perpendicular relationship params
        configuration.addVariable("perpendicularMaxDist", Double.class, 100); // meters

        //Converge relationship params
        configuration.addVariable("convergeMaxDist", Double.class, 100); // meters
        configuration.addVariable("minBearingDiffForConvergence", Double.class, 20); // radians
        
        //Diverge relationship params
        configuration.addVariable("divergeMaxDist", Double.class, 100); // meters
        configuration.addVariable("minBearingDiffForDivergence", Double.class, 20); // radians
        
        //Arrive relationship params
        configuration.addVariable("arriveMaxDist", Double.class, 100);
        
        //Depart relationship params
        configuration.addVariable("departMaxDist", Double.class, 100);
        
        //Close relationship params
        configuration.addVariable("closeMaxDist", Double.class, 50);


        //Smuggling activity params
        configuration.addVariable("maxDistForSmuggling", Double.class, 75); // meters
        
        //Fishing activity params
        configuration.addVariable("fishingSlidingWin", Long.class, 3600); // in seconds (2 h)
        configuration.addVariable("fishingWaitingTime", Long.class, 900000); // in seconds (15 min)
        configuration.addVariable("minFishingTurningDist", Double.class, 200); // in meters
        configuration.addVariable("minFishingMovementDist", Double.class, 100); // in meters
        
        //Context
        configuration.addVariable("minDistToClose", Double.class, 200); // meters
    }
    
    protected void registerAggregationFunctions(Configuration configuration){
        configuration.addPlugInAggregationFunctionFactory("aggMiddlePoint", AggregateMiddlePointFunctionFactory.class.getName());
    }
    
    protected void registerSingleRowFunctions(Configuration configuration){

        //Bearing functions
        configuration.addPlugInSingleRowFunction("bearing", Bearing.class.getName(), "bearing");
        configuration.addPlugInSingleRowFunction("avgBearing", Bearing.class.getName(), "avgBearing");
        configuration.addPlugInSingleRowFunction("modifyBearing", Bearing.class.getName(), "modifyBearing");
        configuration.addPlugInSingleRowFunction("contains", Bearing.class.getName(), "contains");
        configuration.addPlugInSingleRowFunction("isIncreasingBearing", Bearing.class.getName(), "isIncreasingBearing");
        configuration.addPlugInSingleRowFunction("bearingDifference", Bearing.class.getName(), "bearingDifference"); 
        configuration.addPlugInSingleRowFunction("closestBearing", Bearing.class.getName(), "closestBearing"); 

        configuration.addPlugInSingleRowFunction("euclideanDist", LocationFunction.class.getName(), "euclideanDist"); 
        configuration.addPlugInSingleRowFunction("euclideanDist2", LocationFunction.class.getName(), "euclideanDist2");    
        configuration.addPlugInSingleRowFunction("haversineDistance", LocationFunction.class.getName(), "dist");
        configuration.addPlugInSingleRowFunction("haversineDistance2", LocationFunction.class.getName(), "dist2");
        configuration.addPlugInSingleRowFunction("getCentroidFromPoints", LocationFunction.class.getName(), "getCentroidFromPoints");

        configuration.addPlugInSingleRowFunction("mergeElements", LocationFunction.class.getName(), "mergeElements");   
        
        configuration.addPlugInSingleRowFunction("isOutlier", FirstFilterMechanism.class.getName(), "isOutlier");   
        configuration.addPlugInSingleRowFunction("minDist", SecondFilterMechanism.class.getName(), "getMinDist");   

        configuration.addPlugInSingleRowFunction("stopThreshold", SpeedFunction.class.getName(), "stopThreshold");   
        
    }
    
}
