---
title: Managing Experiments And Specs
keywords: managing, experiments, specs
last_updated: March 31, 2016
sidebar: mydoc_managing_experiments_specs
permalink: /mydoc_managing_experiments_specs/
---

If you're using a Java client, you can use the builder classes to create the objects, then call FullPetriClient methods with these objects, as described [here]({{site.data.urls.mydoc_creating_a_petri_backoffice_app.url}})

* If you're using a json-rpc client, you can issue HTTP requests to 'http://ip:port/petri/full_api' (following JSON RPC protocol)
(all times are UTC timedates)

    * method 'insertExperiment' accepts this JSON:

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

