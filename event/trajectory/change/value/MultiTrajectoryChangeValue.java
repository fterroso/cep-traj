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
package ceptraj.event.trajectory.change.value;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class MultiTrajectoryChangeValue implements TrajectoryChangeValue{
    
    List<TrajectoryChangeValue> changes = new LinkedList<TrajectoryChangeValue>();

    public List<TrajectoryChangeValue> getChanges() {
        return changes;
    }

    public void setChanges(List<TrajectoryChangeValue> changes) {
        this.changes = changes;
    }
    
    public void addChange(TrajectoryChangeValue change){
        changes.add(change);
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        for(TrajectoryChangeValue change : changes){
            sb.append(change);
        }
        
        return sb.toString();
    }    
    
}
