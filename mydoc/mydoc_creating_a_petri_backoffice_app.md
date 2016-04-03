---
title: Creating a Petri Backoffice Application
keywords: backoffice, management, application
last_updated: March 31, 2016
sidebar: mydoc_creating_a_petri_backoffice_app
permalink: /mydoc_creating_a_petri_backoffice_app/
---


In order to create and modify experiments and specs you will need to create a backoffice app  
(if you prefer to write it in a non-JVM language, see [the jsons here](https://github.com/wix/petri/wiki/Creating-&-Updating-Experiments-&-Specs)

* add petri-core dependency to your pom

```
<dependency>
	<groupId>com.wixpress.common</groupId>
    <artifactId>wix-petri-core</artifactId>
</dependency>
 ```
* add laboratory-servlet-api-integration dependency to your pom
    

```
<dependency>
 	<groupId>com.wixpress.common</groupId>
    <artifactId>laboratory-servlet-api-integration</artifactId>   
</dependency>
```

* Create an instance of FullPetriClient pointing to your Petri Server 

```java
FullPetriClient  petriClient = PetriRPCClient.makeFullClientFor("http://localhost:9901/petri");
```

* Create a spec definition for your experiment (Or better yet, read [this](https://github.com/wix/petri/wiki/Experiment-Specs), then add the specs to your code and trigger spec scanning when needed)

```java
private  void createSpec(String specKey, List<String> testGroupValues, String scopeName) {
        petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec(specKey).
                        withTestGroups(testGroupValues).
                        withScopes(aScopeDefinitionOnlyForLoggedInUsers(scopeName)).
                        build()));
}
```

* Create an experiment 

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
