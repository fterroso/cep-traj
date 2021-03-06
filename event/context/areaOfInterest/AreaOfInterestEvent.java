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
package ceptraj.event.context.areaOfInterest;

import ceptraj.tool.Polygon2D;
import ceptraj.tool.areaOfInterest.AreaOfInterestType;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class AreaOfInterestEvent {
    
    String name;
    Polygon2D area;
    AreaOfInterestType type;

    public Polygon2D getArea() {
        return area;
    }

    public void setArea(Polygon2D area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AreaOfInterestType getType() {
        return type;
    }

    public void setType(AreaOfInterestType type) {
        this.type = type;
    } 
    
    
    @Override
    public String toString(){
        StringBuilder des = new StringBuilder();
        
        des.append("[");
        des.append(name);
        des.append(", ");
        des.append(type);       
        des.append("]");
               
        
        return des.toString();
    }
}
