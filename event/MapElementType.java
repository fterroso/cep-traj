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
package ceptraj.event;

/**
 * The different event types defined in the present system.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public enum MapElementType {
    
    LOCATION,
    MOVEMENT,
    TRAJECTORY, // moving point
    TRAJECTORY_CHANGE, // meaningfull change in the trajectory of an object
    RELATIONSHIP, // relationship between two different trajectories.
    ITINERARY,
    BEHAVIOR_ALARM;
    
}
