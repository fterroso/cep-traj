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
package ceptraj.EPA.trajectory.listener;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import ceptraj.event.trajectory.TrajectoryEvent;
import org.apache.log4j.Logger;
import ceptraj.output.EventConsumer;
import ceptraj.tool.filterAdaption.FirstFilterMechanism;
import ceptraj.tool.supportFunction.SpeedFunction;

/**
 *
 * @author fernando
 */
public class TrajectoryListener implements UpdateListener{

    static Logger LOG = Logger.getLogger(TrajectoryListener.class);    
    
    EventConsumer eventConsumer;

    public TrajectoryListener(EventConsumer eventConsumer) {
        this.eventConsumer = eventConsumer;
    }
    
    @Override
    public void update(EventBean[] ebs, EventBean[] ebs1) {
        TrajectoryEvent te = (TrajectoryEvent)ebs[0].getUnderlying();
        eventConsumer.processEvent(te);
        if(te.getLevel()== 1){
            SpeedFunction.newCurrentSpeed(te.getId(), te.getTimestamp(), te.getAvgInfSpeed());
            FirstFilterMechanism.setReferenceBearing(te.getStraightBearing());
            FirstFilterMechanism.setReferenceSpeed(te.getAvgInfSpeed());
        }
    }
}
