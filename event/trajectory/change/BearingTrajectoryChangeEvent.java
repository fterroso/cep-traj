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

import ceptraj.event.trajectory.change.value.BearingTrajectoryChangeValue;
import ceptraj.event.trajectory.change.value.TrajectoryChangeValue;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import ceptraj.tool.Bearing;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class BearingTrajectoryChangeEvent extends TrajectorySingleChangeEvent{
    
    double initialBearing = Double.NaN;
    double finalBearing = Double.NaN;
    
    boolean isSharpTurn;
    
    public double getFinalBearing() {
        return finalBearing;
    }

    public void setFinalBearing(double finalBearingValue) {
        this.finalBearing = finalBearingValue;
        setSharpTurn();
    }

    public double getInitialBearing() {
        return initialBearing;
    }

    public void setInitialBearing(double initialBearingValue) {
        this.initialBearing = initialBearingValue;
        setSharpTurn();
    }  

    public boolean getSharpTurn() {
        return isSharpTurn;
    }

    public void setSharpTurn() {
        if(initialBearing != Double.NaN &&
                finalBearing != Double.NaN){
            isSharpTurn = false;
            double diff = Bearing.bearingDifference(initialBearing, finalBearing);
            if(diff < 180 && diff > 150){
                isSharpTurn = true;
            }
        }
    }

    
    @Override
    public TrajectoryChangeValue getChangeValue(){
        
        BearingTrajectoryChangeValue bearingChange = new BearingTrajectoryChangeValue();
        bearingChange.setInitialBearingValue(initialBearing);
        bearingChange.setFinalBearingValue(finalBearing);
        
        return bearingChange;
    }    
  
    @Override
    protected String getPlainTextDescription(){
        
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Bearing: {");
        sb.append(Bearing.getFineGrainBearingFromValue(initialBearing));
        sb.append("(");
        sb.append(twoDForm.format(initialBearing).replace(",", "."));
        sb.append((") -> "));
        sb.append(Bearing.getFineGrainBearingFromValue(finalBearing));
        sb.append("(");
        sb.append(twoDForm.format(finalBearing).replace(",", "."));
        sb.append(")");
        sb.append("}");
        
        return sb.toString();
    }

    @Override
    protected String getKMLDescription(){
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("ID: ");
        sb.append(getId());
        sb.append("; Start time: ");
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        sb.append(format.format(initialTimestamp));
        sb.append("; End time: ");
        sb.append(format.format(finalTimestamp)); 
        sb.append("; Initial bearing: ");
        sb.append(Bearing.getFineGrainBearingFromValue(initialBearing));
        sb.append("(");
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        sb.append(twoDForm.format(initialBearing).replace(",", "."));
        sb.append(("); Final bearing: "));
        sb.append(Bearing.getFineGrainBearingFromValue(finalBearing));
        sb.append("(");
        sb.append(twoDForm.format(finalBearing).replace(",", "."));
        sb.append(")");
       
        return sb.toString();
    }

    @Override
    public boolean isBearingChange() {
        return true;
    }

    @Override
    public boolean isSpeedChange() {
        return false;
    }
}
