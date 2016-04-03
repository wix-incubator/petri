---
title: Creating a Petri Backoffice Application
keywords: backoffice, management, application
last_updated: March 31, 2016
sidebar: mydoc_creating_a_petri_backoffice_app
permalink: /mydoc_creating_a_petri_backoffice_app/
---

> At Wix, we created a BackOffice application to manage our experiments and specs. Our Product Managers have full access to this BackOffice and can create any user experience they like.

## Creating Your BackOffice Application

In this section, we will show you how to link your BackOffice application to Petri. 

**Note:** If you prefer to write your BackOffice in a non-JVM language, [read more here]({{site.data.urls.mydoc_managing_experiments_specs.url}}).


### Add petri-core dependency to your POM.xml

```
<dependency>
	<groupId>com.wixpress.common</groupId>
    <artifactId>wix-petri-core</artifactId>
</dependency>
```

### Add laboratory-servlet-api-integration dependency to your POM.xml
    
```
<dependency>
 	<groupId>com.wixpress.common</groupId>
    <artifactId>laboratory-servlet-api-integration</artifactId>   
</dependency>
```

### Create an instance of FullPetriClient pointing to your Petri Server 

```java
FullPetriClient  petriClient = PetriRPCClient.makeFullClientFor("http://localhost:9901/petri");
```

### Create a spec definition for your experiment

Better yet, read [this]({{site.data.urls.mydoc_experiments.url}}#what-are-experiment-specs), then add the specs to your code and trigger spec scanning when needed.

```java
private  void createSpec(String specKey, List<String> testGroupValues, String scopeName) {
        petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec(specKey).
                        withTestGroups(testGroupValues).
                        withScopes(aScopeDefinitionOnlyForLoggedInUsers(scopeName)).
                        build()));
}
```

### Create an experiment 

```java
private  void createExperiment(String specKey, List<TestGroup> testGroups, boolean onlyForLoggedIn) {
        DateTime now = new DateTime();
        petriClient.insertExperiment(
                anExperimentSnapshot().
                        withStartDate(now.minusMinutes(1)).
                        withEndDate(now.plusYears(1)).
                        withKey(specKey).
                        withGroups(testGroups).
                        withOnlyForLoggedInUsers(onlyForLoggedIn).
                        build());
    }
```
