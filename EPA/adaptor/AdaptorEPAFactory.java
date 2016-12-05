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
package ceptraj.EPA.adaptor;

import java.io.File;
import org.apache.log4j.Logger;
import ceptraj.tool.Constants;

/**
 *
 * @author Fernando Terroso-Saenz
 */
public class AdaptorEPAFactory {
    
    static Logger LOG = Logger.getLogger(AdaptorEPAFactory.class);    
        
    /* FILES */
    
    protected static final String GPX_FILE_NAME= "";     
    protected static final String KML_FILE_NAME= "";  

  
    public static AdaptorEPA getAdaptorEPA(LocationSourceFormat type) throws Exception{
        
        AdaptorEPA targetEPA = null;
                     
        String path = Constants.getPath();
               
        boolean modify = false;
        LOG.info("Loading "+type.getDescriptor()+" adaptor...");
        switch (type){                          
            case GPX:
                targetEPA = new GPXAdaptorEPA(new File(path+GPX_FILE_NAME),modify);
                break;               
            case KML:
                targetEPA = new KMLAdaptorEPA(new File(path + KML_FILE_NAME));
                break;

        }
        
        return targetEPA;
    }
    
    public static AdaptorEPA getAdaptorEPA(LocationSourceFormat type, String path) throws Exception{
        
        AdaptorEPA targetEPA = null;
                                    
        boolean modify = false;
        switch (type){               
            case GPX:
                targetEPA = new GPXAdaptorEPA(new File(path),modify);
                break;
            case KML:
                targetEPA = new KMLAdaptorEPA(new File(path));
                break;
        }
        
        return targetEPA;
    }
    
}
