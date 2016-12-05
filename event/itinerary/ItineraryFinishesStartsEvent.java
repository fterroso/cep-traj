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
package ceptraj.event.itinerary;

import ceptraj.event.MapElement;
import ceptraj.event.MapElementType;
import ceptraj.tool.Point;

/**
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class ItineraryFinishesStartsEvent extends MapElement {
        
    String finishedItineraryId;
    String startedItineraryId;
    
    Point lastLocation;
    Point startLocation;

    public String getFinishedItineraryId() {
        return finishedItineraryId;
    }

    public void setFinishedItineraryId(String finishedItineraryId) {
        this.finishedItineraryId = finishedItineraryId;
    }

    public String getStartedItineraryId() {
        return startedItineraryId;
    }

    public void setStartedItineraryId(String startedItineraryId) {
        this.startedItineraryId = startedItineraryId;
    }

    public Point getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Point lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Point getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Point startLocation) {
        this.startLocation = startLocation;
    }

    @Override
    public MapElementType getType() {
        return MapElementType.ITINERARY;
    }

    @Override
    public long getTimestampGap() {
        return 0;
    }

    @Override
    protected String toGPXFormat() {
        return "";
    }

    @Override
    protected String toPlainTextFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("Finish:");
        sb.append(this.finishedItineraryId);
        sb.append("; Start:");
        sb.append(this.startedItineraryId);
        
        return sb.toString();
    }

    @Override
    protected String toKMLFormat() {
        return "";
    }
    
}
