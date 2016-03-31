### How to add a filter with your own custom logic 
#### any filter on the classpath that answers the following criteria will be available:
  - the class should be under the root package "filters" (similar to the "specs" package)
  - the class should implement the [Filter](https://github.com/wix/petri/blob/master/wix-petri-core/src/main/java/com/wixpress/petri/experiments/domain/Filter.java) interface 
  - the class should define the '@FilterTypeName("id")' annotation

* see examples [here](https://github.com/wix/petri/blob/master/wix-petri-core/src/test/java/filters/AdditionalFilter.java) and [here](https://github.com/wix/petri/blob/2c31c03a47dcf00466fc812834b5c7abdc3271ae/sample-extended-filters/src/main/java/filters/CustomUserTypeFilter.java)

#### another option is to add a jar containing your custom filters - This option is convenient if you are using the [Laboratory as a Service] (https://github.com/wix/petri/wiki/Using-Laboratory-as-a-Service).
  - create a directory called ' petri-plugins' in the same location as you [installed the server](https://github.com/wix/petri/wiki/Installing-Petri-Server)
    - any jar containing 'extended-filters' in it's name will be scanned for filters
    - filters should follow the same guidelines as in the above section
  - do the same in the location where you installed the 'Laboratory Service'