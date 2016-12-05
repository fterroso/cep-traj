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
package ceptraj.event.relationship;

import ceptraj.tool.Color;
import ceptraj.output.visualizer.KMLVisualParams;

/**
 * Types of relationships supported by CEP-traj to date.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public enum RelationType {
    
    PARALLEL (0, new KMLVisualParams(Color.LIGHT_GREEN, Color.LIGHT_GREEN)),
    PERPENDICULAR (1, new KMLVisualParams(Color.ROSE, Color.ROSE)),
    CONVERGE (2, new KMLVisualParams(Color.DARK_BLUE, Color.DARK_BLUE)),
    DIVERGE (3, new KMLVisualParams(Color.LIGHT_BLUE, Color.LIGHT_BLUE)),
    ARRIVE (4, new KMLVisualParams(Color.LIGHT_VIOLET, Color.LIGHT_VIOLET)),
    DEPART (5, new KMLVisualParams(Color.ROSE, Color.ROSE)),
    CLOSE (6, new KMLVisualParams(Color.BLUE_GREEN, Color.BLUE_GREEN)),
    FAR (7, new KMLVisualParams(Color.PURPLE, Color.PURPLE));
    
    int intValue;   
    KMLVisualParams KMLParams; //Visual params for its serialization in a KML file

    private RelationType(int intValue, KMLVisualParams KMLParams) {
        this.intValue = intValue;
        this.KMLParams = KMLParams;
    }

    public KMLVisualParams getKMLParams() {
        return KMLParams;
    }
    
    
    
}
