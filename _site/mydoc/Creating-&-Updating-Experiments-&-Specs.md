# For creating/updating experiments
* if you're using a java client, you can use the builder classes to create the objects, then call FullPetriClient methods with these objects, as described [here](https://github.com/wix/petri/wiki/Creating-a-Petri-BackOffice-app)

* If you're using some json-rpc client, you can issue http requests to 'http://ip:port/petri/full_api' (following json rpc protocol)
(all times are utc timedates)

    * method 'insertExperiment' accepts this json:

    ```json
    {    
      "key" : "experimentKey",
      "fromSpec" : true,
      "creationDate" : "2014-11-05T15:32:29.451Z", 
      "description" : "some description",
      "startDate" : "2014-11-05T15:37:00.000Z",
      "endDate" : "2015-11-05T15:34:00.000Z",
      "groups" : [ {
        "id" : 1,
        "chunk" : 0,
        "value" : "false"
      }, {
        "id" : 2,
        "chunk" : 100,
        "value" : "true"
      } ],
      "scope" : "sub-product-tag",
      "paused" : false,
      "name" : "some human readable name",
      "creator" : "someone@wix.com",
      "featureToggle" : true,
      "originalId" : 0,
      "linkedId" : 0,
      "persistent" : true,
      "filters" : [ ],
      "onlyForLoggedInUsers" : false,
      "comment" : "",
      "updater" : "someone@wix.com",
      "conductLimit" : 0
}
```

    * method 'updateExperiment' receives the same, but wrapped in:


    ```json
    {
"id" : 6221,
    "lastUpdated" : "2014-11-05T15:37:00.000Z",
    "experimentSnapshot" : {
             "key" : "experimentKey",
             "fromSpec" : true,
             "etc" : "etc",
             "originalId" : 6221,
     }
}
```

    

# For creating/updating specs:
call ```void addSpecs(List<ExperimentSpec> expectedSpecs);```

or json with an array of these objects:

```json   
    {
    "creationDate" : "2014-01-09T13:11:26.846Z",
    "updateDate" : "2014-01-09T13:11:26.846Z",
    "key" : "experimentKey",
    "owner" : "someone@wix.com",
    "persistent" : true,
    "testGroups" : [ "old", "new" ],
    "scopes" : [ {
      "name" : "your-product",
      "onlyForLoggedInUsers" : true
    }]
}
```

