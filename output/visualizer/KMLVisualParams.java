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

import ceptraj.tool.Color;

/**
 * Class which comprises the visual parameters to tune the visualization of
 * the elements in a KML file
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class KMLVisualParams {
    
    Color normalLabelColor;
    Color normalLineColor;

    public KMLVisualParams(Color normalLabelColor, Color normalLineColor) {
        this.normalLineColor = normalLineColor;
        this.normalLabelColor = normalLabelColor;
    }

    public Color getLineColor() {
        return normalLineColor;
    }
}
