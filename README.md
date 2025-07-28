## Region Matcher

This application reads two json files, one which contains a list of regions defined by one or more polygons. And the other a list of locations. The coordinates for both are in [longitude(-180,180), latitude(-90,90)]

It then finds which locations are contained in which regions if any and provides the result in a json file.

#### Limitations
There are two solving algorithms available.
 1. A custom implementation of a ray cast algorithm. It assumes 2D geometry, so for large distances it won't be accurate. The current implementation can handle polygons that cross the antimeridian.
 2. And one that uses the Spatial4j library. It isn't able to work with a Polygon that encircles a pole. 

The default solver is Spatial4j, but can be changed with an optional CLI argument

Additionally the current implementation is not able to evaluate holes in polygons.
 
### Usage

1. ### SBT

    The simplest option is to just run it with sbt 
    ```sbt "run [[--locations | -l <location-path>]] [[--regions | -r <region-path>]] [[--output | -o <output-path>]] [[--solver | -s  <RayCast2D | Spatial4j>]]"```
   
3. ### Assembly .Jar
    the sbt-assembly plugin is configured, as such you can run ```sbt assembly``` to create a single .jar file with all dependencies. it can then be either with the java interpreter. 
    ```java -jar <application-name> [[--locations | -l <location-path>]] [[--regions | -r <region-path>]] [[--output | -o <output-path>]] [[--solver | -s  <RayCast2D | Spatial4j>]]```


### File format

  Sample input and output files are included in the the respective folders. They are formated as follows:

  Locations: 
  ```
  [
    {
      "name": "<unique identifier>",
      "coordinates": [<longitude>, <latitude>]
    },
    ... // more locations
  ]
  ```
  Regions: 
  ```[
  {
      "name": "<unique identifier>",
      "coordinates": [
        [[<longitude>, <latitude>], [<longitude>, <latitude>]], 
          ... // more polygons    
      ] - array of polygons, where each polygon is an array of coordinates.
    },
    ... // more regions
  ]
  ```
  Output: 
  ```
  [
    {
      "region": "<unique identifier>",
      "matched_locations": [<location identifier>,
        ... // more location identifiers
      ]
    },
    ... // more regions
  ]
  ```
