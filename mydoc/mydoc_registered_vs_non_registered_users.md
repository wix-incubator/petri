---
title: User Experience Persistence
keywords: persistence, experience, UX
last_updated: March 31, 2016
sidebar: mydoc_registered_vs_non_registered_users
permalink: /mydoc_registered_vs_non_registered_users/
---

# Registered VS. Non-Registered Users

One of the important fields the ExperimentSnapshotBuilder requires you to define is the ```withOnlyForLoggedInUsers```.

The ```withOnlyForLoggedInUsers``` field is needed for writing to the correct cookie and using server side state wisely. Read more about it [here](https://github.com/wix/petri/wiki/How-Petri-Persists-Experience-For-Users). 

**Note:** Trying to call the 'build' method without setting this field will result in an exception:  
```IllegalArgumentException("an experiment cannot be created without specifying the onlyForLoggedInUsers field");```


### Setting the withOnlyForLoggedInUsers field
Use ```true``` if the experiment is conducted only for logged in users, i.e in an area in your product where it is guaranteed that the user has been identified (for example, the user's dashboard or account area). 

Use ```false``` if the experiment can be conducted for non-logged in users (for example, landing pages)

### Specifying correct values via experiment specs
When creating a spec you have the option of calling the ```withScopes(ScopeDefinition... scopes)``` method.
A scope definition defines the name/tag of the product and what type of users this spec can run on.
Examples: 

* ScopeDefinition.aScopeDefinitionOnlyForLoggedInUsers("dashboard")
* ScopeDefinition.aScopeDefinitionForAllUserTypes("landingPages")

You then use these scope definitions to enforce correct creation of experiments, as explained here (link here). So if, for example, a spec has only 'aScopeDefinitionForAllUserTypes', the corresponding experiment should be built with ```withOnlyForLoggedInUsers(false)```

The name/tag of the scope can be used for tagging of specs and easy filtering in your UI and also in the laboratory.conductByScope API (link here)