# CEP-traj

CEP-traj is a project to process spatio-temporal data by means of the Complex Event Processing (CEP) paradigm. 

The key feature of CEP-traj are:

* Detection of sudden acceleration and deceleration of the moving objects.
* Detection of different interactions between couples of moving objects (e.g. moving in parallel, converge, diverge, etc.)

In its current version the application is able to process trajectories stored in different file formats (.kml, .csv, etc.). 

For more info about the application features, go to the reference section.

## Third-party Library Dependencies
* Esper v. 4.9
* Log4J v. >=1.2.16
* Commons-lang3 v.3.3.2
* jCoord v. 1.0
* jDOM
* JTS Topology Suite v. 1.8


## Reference
Please, if your are going to use CEP-traj then reference it as

Terroso-Saenz, F., Valdes-Vela, M., den Breejen, E., Hanckmann, P., Dekker, R., & Skarmeta-Gomez, A. F. (2015). CEP-traj: An event-based solution to process trajectory data. Information Systems, 52, 34-54.
