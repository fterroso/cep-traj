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
 * File templates to be used as system's output.
 *
 * @author Fernando Terroso-Saenz <fterroso@um.es>
 */
public class Templates {
    
    //KML   
    public static final String KML_GENERAL_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\">\n<Document>\n\t<name>ELEMENT_NAME</name>\n";
    public static final String KML_GENERAL_TAIL = "</Document>\n</kml>";
       
    public static final String KML_SPECIFIC_HEAD = "<Folder id=\"ELEMENT_NAME\">\n"+
                                                               "<name>ELEMENT_NAME</name>\n";
    
    public static final String KML_SPECIFIC_TAIL = "</Folder>\n"; 
  
    public static final String KML_TRACK_STYLE = "<Style id=\"linestyleNUM_LEVEL\">\n"+
                                                        "\t<LineStyle>\n"+
                                                            "\t\t<color>COLOR_CODE</color>\n"+
                                                            "\t\t<width>2</width>\n"+
                                                        "\t</LineStyle>\n"+
                                                    "</Style>\n";
    
    public static final String KML_CHANGE_STYLE = "<Style id=\"changestyleCHANGE_TYPE\">\n"+
                                                  "\t<IconStyle>\n"+
                                                  "\t\t<color>COLOR_CODE</color>\n"+
                                                  "\t\t<scale>0.7</scale>\n"+
                                                  "\t\t<Icon>\n"+
                                                  "\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/track.png</href>\n"+
                                                  "\t\t</Icon>\n"+
                                                  "\t\t<hotSpot x=\"32\" xunits=\"pixels\" y=\"1\" yunits=\"pixels\"/>\n"+
                                                  "\t</IconStyle>\n"+
                                                  "\t<LabelStyle>\n"+
                                                  "\t\t<color>COLOR_CODE</color>\n"+
                                                  "\t\t<scale>0.7</scale>\n"+
                                                  "\t</LabelStyle>\n"+
                                                  "\t<LineStyle>\n"+
                                                  "\t\t<color>COLOR_CODE</color>\n"+
                                                  "\t\t<width>5</width>\n"+
                                                  "\t</LineStyle>\n"+
                                                  "\t<PolyStyle>\n"+
                                                  "\t\t<color>COLOR_CODE</color>\n"+
                                                  "\t</PolyStyle>\n"+
                                                  "</Style>\n";
    
    //KML Style element for the relationships.
    public static final String KML_RELATION_STYLE = "<StyleMap id=\"relation_RELATION\">\n"+
                                                             "<Pair>\n"+
                                                                "<key>normal</key>\n"+
                                                                "<styleUrl>#relation_RELATIONn</styleUrl>\n"+
                                                             "</Pair>\n"+
                                                             "<Pair>\n"+
                                                                "<key>highlight</key>\n"+
                                                                "<styleUrl>#relation_RELATIONh</styleUrl>\n"+
                                                             "</Pair>\n"+
                                                          "</StyleMap>\n"+
                                                          "<Style id=\"relation_RELATIONn\">\n"+
                                                             "<IconStyle>\n"+
                                                                "<Icon><href>http://maps.google.com/mapfiles/kml/shapes/track.png</href></Icon>\n"+
                                                             "</IconStyle>\n"+
                                                             "<LabelStyle>\n"+
                                                                "<color>LINE_COLOR</color>\n"+
                                                                "<scale>0.7</scale>\n"+
                                                             "</LabelStyle>\n"+
                                                             "<LineStyle>\n"+
                                                                "<color>LINE_COLOR</color>\n"+
                                                                "<width>2</width>\n"+
                                                             "</LineStyle>\n"+
                                                             "<PolyStyle>\n"+
                                                                "<color>ff009900</color>\n"+
                                                             "</PolyStyle>\n"+
                                                          "</Style>\n"+
                                                          "<Style id=\"relation_RELATIONh\">\n"+
                                                             "<IconStyle>\n"+
                                                                "<color>7f00ffff</color>\n"+
                                                                "<Icon><href>http://maps.google.com/mapfiles/kml/shapes/track.png</href></Icon>\n"+
                                                             "</IconStyle>\n"+
                                                             "<LabelStyle>\n"+
                                                                "<color>7f00ffff</color>\n"+
                                                                "<scale>0.7</scale>\n"+
                                                             "</LabelStyle>\n"+
                                                             "<LineStyle>\n"+
                                                                "<color>LINE_COLOR</color>\n"+
                                                                "<width>6</width>\n"+
                                                             "</LineStyle>\n"+
                                                             "<PolyStyle>\n"+
                                                                "<color>7f00ffff</color>\n"+
                                                             "</PolyStyle>\n"+
                                                          "</Style>\n";
        
    public static final String KML_ACTIVITY_SMUGLING_STYLE = 
                                                            "<Style id=\"alarm_POSSIBLE_SMUGGLING\">\n"+
                                                            "<IconStyle>\n" +
                                                            "	<color>ff00ff00</color>\n" +
                                                            "	<scale>1.1</scale>\n" +
                                                            "	<Icon>\n" +
                                                            "		<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>\n" +
                                                            "	</Icon>\n" +
                                                            "  </IconStyle>"+
                                                            "</Style>\n";
            
    
    public static final String KML_ACTIVITY_FISHING_STYLE = "<Style id=\"alarm_FISHING_BEHAVIOR\">\n"+
                                                            "<IconStyle>\n" +
                                                            "	<color>ff00ffff</color>\n" +
                                                            "	<scale>1.1</scale>\n" +
                                                            "	<Icon>\n" +
                                                            "		<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>\n" +
                                                            "	</Icon>\n" +
                                                            "  </IconStyle>"+
                                                            "</Style>\n";
                     
    
    public static final String KML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"; 
    public static final String KML_COORDINATES_FORMAT = "(-?\\d+\\.?\\d*)(\\s+)(-?\\d+\\.?\\d*)\\s+";

    //GNUPlot
    public static final String GNUPLOT_HEAD =   "set term postscript eps enhanced color\n"+
                                                    "set output 'gap_times.eps'\n"+
                                                    "set bmargin 6\n"+
                                                    "set border 3\n"+
                                                    "set yrange [0:<maxy>]\n"+
                                                    "set xrange [0:<maxx>]\n"+
                                                    "set key top center\n"+
                                                    "set key box\n"+
                                                    "set key horiz samplen 2\n"+
                                                    "set ylabel \"Delay (s)\"\n"+
                                                    "set xlabel \"Point number\"\n"+
                                                    "set grid ytics\n"+
                                                    "set xtics rotate by-45\n"+
                                                    "set ytics 500 nomirror\n";
    
    public static final String GNUPLOT_SPEED_CHANGE_HEAD = "unset log\n"+
                                                                "unset label\n"+
                                                                "set term postscript eps \"Helvetica\" 18\n"+
                                                                "set output '<OUTPUT_FILE_NAME>.eps'\n"+
                                                                "set size 1,1\n"+
                                                                "set lmargin 8\n"+
                                                                "set rmargin 8\n"+
                                                                "set xr [0:<X_RANGE>]\n"+
                                                                "set xlabel \"Time (m)\"\n"+
                                                                "set xtic <X_TIC>\n"+
                                                                "set yr [0:<Y_RANGE>]\n"+
                                                                "set ytic <Y_TIC>\n"+
                                                                "set ytics rotate by 0\n"+                                                                
                                                                "set style line 1 lt 1 lw 3 pt 3 lc rgb \"blue\"\n"+
                                                                "set style line 2 lt 2 lw 3 pt 3 lc rgb \"red\"\n"+
                                                                "set ylabel \"Speed (m/s)\"\n"+
                                                                "set style rect fc lt -1 fs solid 0.45 noborder\n"+
                                                                "set grid ytics\n"+
                                                                "<RECTANGLE_SECTION>"+
                                                                "set key box\n"+
                                                                "set key top\n"+
                                                                "plot \"<DATA_FILE_NAME>\" using 1:2 title 'Real speed' with lines ls 1 axis x1y1,"+
                                                                "\"<STOP_FILE_NAME>\" using 1:2 title 'Stop threshold' with lines ls 2 axis x1y1\n";
    
    public static final String GNUPLOT_BEARING_CHANGE_HEAD = "unset log\n"+
                                                                    "unset label\n"+
                                                                    "set term postscript eps \"Helvetica\" 18\n"+
                                                                    "set output '<OUTPUT_FILE_NAME>.eps'\n"+
                                                                    "set size 1,1\n"+
                                                                    "set lmargin 8\n"+
                                                                    "set rmargin 8\n"+
                                                                    "set xr [0:<X_RANGE>]\n"+
                                                                    "set xlabel \"Time (m)\"\n"+
                                                                    "set xtic <X_TIC>\n"+
                                                                    "set yr [0:360]\n"+
                                                                    "set ytic 20\n"+
                                                                    "set ytics rotate by 0\n"+
                                                                    "set ytics <Y_TIC> \n"+
                                                                    "set style line 1 lt 1 lw 3 pt 3 lc rgb \"blue\"\n"+
                                                                    "set style line 2 lt 2 lw 3 pt 3 lc rgb \"red\"\n"+
                                                                    "set ylabel \"Bearing (radians)\"\n"+
                                                                    "set style rect fc lt -1 fs solid 0.45 noborder\n"+
                                                                    "<RECTANGLE_SECTION>"+
                                                                    "set grid ytics xtics\n"+
                                                                    "set key box\n"+
                                                                    "set key top\n"+
                                                                    "plot \"<DATA_FILE_NAME>\" using 1:2 title 'Real bearing' with lines ls 1 axis x1y1\n";
    
    
    //PLAIN TEXT
    public static final String GPSVISUALIZER_WEB_HEAD = "name,desc,color,opacity,symbol,latitude,longitude\n"; 
    
    //GPX.
    public static final String GPX_HEAD = "<?xml version='1.0' encoding='UTF-8'?>\n<gpx version=\"1.1\" creator=\"JOSM GPX export\" xmlns=\"http://www.topografix.com/GPX/1/1\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n";
    public static final String GPX_TAIL = "</gpx>\n";    
    
}
