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

import java.text.DecimalFormat;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class SpeedTrajectoryChangeValue implements TrajectoryChangeValue{
    double initialSpeed;
    double finalSpeed;

    public double getInitialSpeed() {
        return initialSpeed;
    }

    public void setInitialSpeed(double initialSpeed) {
        this.initialSpeed = initialSpeed;
    }

    public double getFinalSpeed() {
        return finalSpeed;
    }

    public void setFinalSpeed(double finalSpeed) {
        this.finalSpeed = finalSpeed;
    }
    
    @Override
    public String toString(){
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Speed: {");
        sb.append(twoDForm.format(initialSpeed).replace(",", "."));
        sb.append("->");
        sb.append(twoDForm.format(finalSpeed).replace(",", "."));
        sb.append("}");
        
        return sb.toString();
    }
}
