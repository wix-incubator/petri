---
title: Managing Experiments And Specs
keywords: managing, experiments, specs
last_updated: March 31, 2016
sidebar: managing_experiments_specs
permalink: /managing_experiments_specs/
---

## Using A Java Client

If you're using a Java client, you can use our builder classes to create objects, then call `FullPetriClient` methods with them, as described [here]({{site.data.urls.creating_a_petri_backoffice_app.url}})

## Using A JSON-RPC Client

Issuing an HTTP request to `http://ip:port/petri/full_api` (following the JSON RPC protocol)
(all times are UTC timedates)

### Creating A New Experiment

To create a new experiment, issue an HTTP request to the `insertExperiment` method, which accepts the following JSON:

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


### Editing An Experiment

To edit an experiment, issue an HTTP request to the `updateExperiment` method, which accepts the same JSON as before, only wrapped in the following JSON:

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
    

# Creating and Updating Experiment Specs

To create or update an experiment spec, issue a call to the `void addSpecs(List<ExperimentSpec> expectedSpecs);` method or JSON with an array of the following objects:

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

