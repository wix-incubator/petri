---
title: Integrate Laboratory (Petri client) Into Your Application
keywords: application, laboratory, client, petri, integration
last_updated: March 31, 2016
sidebar: integrating_petri_into_your_app
permalink: /integrating_petri_into_your_app/
---

* Laboratory is supported for any servlet-api based application:

    (If you want to use it from a different type of app please read this : [Laboratory as a Service]({{site.data.urls.using_laboratory_as_a_service.url}}))  

* Laboratory is published to the Maven Central Repository, so you simply have to add the appropriate dependency to your POM:

```
<dependency>
    <groupId>com.wix</groupId>
    <artifactId>laboratory-servlet-api-integration</artifactId>   
    <version>0.6</version>
</dependency>
```

Or, for the spring flavored version:

```
<dependency>
    <groupId>com.wix</groupId>
    <artifactId>laboratory-spring-integration</artifactId>   
    <version>0.6</version>
</dependency>
```

 * Create a 'laboratory.properties' file under you WEB-INF folder, with values matching your installed Petri server:
 
```
    petri.url=http://<sever>:<port>/petri  
```

 * Optionally add this to the properties file - (if you want this. read [here]({{site.data.urls.user_experience_persistence.url}}) for more info)
 
```
    petri.writeStateToServer = true 
```

* Add the following filters to your web.xml file 

```
 <filter>
        <filter-name>laboratoryFilter</filter-name>
        <filter-class>com.wixpress.petri.laboratory.http.LaboratoryFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>laboratoryFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <context-param>
        <param-name>laboratoryConfig</param-name>
        <param-value>/WEB-INF/laboratory.properties</param-value>
    </context-param>
```

* If you are using Spring add the LaboratoryConfig spring configuration to your applicationContext

```
 <bean class="com.wixpress.petri.laboratory.LaboratoryConfig"/>
```

* To conduct - `laboratory.conductExperiment(key, fallback)`
[spring example](https://github.com/wix/petri/blob/master/laboratory-spring-integration/src/it/java/com/wixpress/petri/TestAppController.java), [servlet-filter-example](https://github.com/wix/petri/blob/master/laboratory-servlet-api-integration/src/it/java/com/wixpress/petri/ConductExperimentServlet.java)

* Take a look at the [sample-petri-app](https://github.com/wix/petri/tree/master/sample-petri-app) project for a full example






