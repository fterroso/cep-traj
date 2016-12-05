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

import ceptraj.event.trajectory.change.value.MultiTrajectoryChangeValue;
import ceptraj.event.trajectory.change.value.TrajectoryChangeValue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import ceptraj.tool.Point;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class TrajectoryMultiChangeEvent extends TrajectoryChangeEvent{
    
    private List<Point> involvedPath = null;
    private List<TrajectoryChangeType> underlayingTypes = new LinkedList<TrajectoryChangeType>();
    MultiTrajectoryChangeValue changeValue = new MultiTrajectoryChangeValue();

    @Override
    public Collection<Point> getInvolvedPath() {
        return involvedPath;
    }

    public void setInvolvedPath(Collection<Point> involvedPath) {
        this.involvedPath = new LinkedList(involvedPath);
    }

    @Override
    public TrajectoryChangeValue getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(TrajectoryChangeValue changeValue) {
        this.changeValue = (MultiTrajectoryChangeValue) changeValue;
    }

    @Override
    public List<TrajectoryChangeType> getUnderlayingTypes() {
        return underlayingTypes;
    }
    
    @Override
    public Point getMiddlePoint(){
        return involvedPath.get(involvedPath.size()/2);
    }
    
    @Override
    public Point getHeadPoint(){
        return involvedPath.get(involvedPath.size()-1);
    }
     
    @Override
    public Point getTailPoint(){
        return involvedPath.get(0);
    }
    
    public void setType1(List<TrajectoryChangeType> type){
        underlayingTypes.addAll(type);
    }

    public void setType2(List<TrajectoryChangeType> type){
        underlayingTypes.addAll(type);
    }
    
    public void setValue1(TrajectoryChangeValue value){
        changeValue.addChange(value);
    }
    
    public void setValue2(TrajectoryChangeValue value){
        changeValue.addChange(value);
    }
    
    @Override
    public boolean isDifferentChange(List<TrajectoryChangeType> types){
               
        for(TrajectoryChangeType underlayingType : getUnderlayingTypes()){
            switch(underlayingType){
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
        }
        
        return true;
    }    
    
    @Override
    protected String getPlainTextDescription(){
        StringBuilder sb = new StringBuilder();        
        for(TrajectoryChangeValue value : changeValue.getChanges()){
            sb.append(value);
            sb.append(", ");
        }        
        return sb.toString();
    }
    
    @Override
    protected String getKMLDescription() {
        return getPlainTextDescription();
    }

    @Override
    public long getTimestampGap() {
        return timestamp - involvedPath.get(involvedPath.size()/2).getTimestamp();
    }

    @Override
    protected String toGPXFormat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }   

    @Override
    public boolean isBearingChange() {
        return false;
    }

    @Override
    public boolean isSpeedChange() {
        return false;
    }
}
