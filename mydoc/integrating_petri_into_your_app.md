---
title: Integrate Laboratory (Petri client) Into Your Application
keywords: application, laboratory, client, petri, integration
last_updated: March 31, 2016
sidebar: integrating_petri_into_your_app
permalink: /integrating_petri_into_your_app/
---

* Laboratory is supported for any servlet-api based application:

    (If you want to use it from a different type of app please read this : [Laboratory as a Service](https://github.com/wix/petri/wiki/Using-Laboratory-as-a-Service))  

* add laboratory-servlet-api-integration dependency to your pom

```
<dependency>
    <groupId>com.wixpress.common</groupId>
    <artifactId>laboratory-servlet-api-integration</artifactId>   
</dependency>
```

 * Create a 'laboratory.properties' file under you WEB-INF folder, with values matching your installed Petri server:
 
```
    petri.url=http://<sever>:<port>/petri  
```

 * Optionally add this to the properties file - (if you want this. read [here](https://github.com/wix/petri/wiki/How-Petri-Persists-Experience-For-Users) for more info)
 
```
    petri.writeStateToServer = true 
```

* Add the following filters to your web.xml file 
```
    <filter>
        <filter-name>laboratoryFilter</filter-name>
        <filter-class>com.wixpress.petri.laboratory.http.LaboratoryFilter</filter-class>
        <init-param>
            <param-name>laboratoryConfig</param-name>
            <param-value>/WEB-INF/laboratory.properties</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>laboratoryFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

* Add the LaboratoryConfig spring configuration to your applicationContext

```
 <bean class="com.wixpress.petri.laboratory.LaboratoryConfig"/>
```


* Take a look at the [sample-petri-app](https://github.com/wix/petri/tree/master/sample-petri-app) project for a full example





</br>