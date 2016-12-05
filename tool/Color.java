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
package ceptraj.tool;

/**
 * Different colors to visualize the elements in the output files.
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public enum Color {
    
    RED ("red", "ff1400FF", 0), 
    ORANGE ("orange", "ffff8000", 1),
    YELLOW("yellow", "ffffff00", 2),     
    GREEN("green", "ff2EFE2E", 3),
    LIME ("lime", "ff00ffff", 4),
    AQUA("aqua", "ffA9E2F3", 5),
    BLUE ("blue", "ff0040ff", 6),
    VIOLET("violet","fff520ff", 7),
    LIGHT_GREEN("light_green","ff14F096", 8),
    ROSE("rose","ff7800F0", 9),
    DARK_BLUE("dark_blue","ffF00014", 10),
    LIGHT_BLUE("light_blue","ffFF7800", 11),
    LIGHT_VIOLET("light_violet","ffAA78B4", 12),
    BLUE_GREEN("blue_green","ff5A7800", 13),
    PURPLE("purple","ff140050", 14);

    
    String name;
    String hexCode;
    int level;

    private Color(String name, String hexCode, int level) {
        this.name = name;
        this.hexCode = hexCode;
        this.level = level;
    }

    public String getHexCode() {
        return hexCode;
    }

    public String getName() {
        return name;
    }      

    public int getLevel() {
        return level;
    }

    public static Color getColorForLevel(int level){
        level = level % Color.values().length;
        for(Color c : Color.values()){
            if(c.level == level){
                return c;
            }
        }
        return RED;
    }
}
