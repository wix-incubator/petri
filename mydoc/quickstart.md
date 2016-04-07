---
title: Quickstart
keywords: questions, quickstart, installation, server, integration
last_updated: March 31, 2016
sidebar: quickstart
permalink: /quickstart/
---

## Step 1: Setting Up Petri Server

![Setting Up Petri Server](https://raw.githubusercontent.com/wix/petri/gh-pages/images/quickstart_architecture1.png)

Use our runnable jar, which use H2 with disk persistency. However, if you're looking for a more scalable solution you can [configure Petri server to run with MySql]({{site.data.urls.configure_petri_server.url}}).

Download the [runnable-petri-server](https://github.com/wix/petri/releases/download/1.0/runnable-petri-server.jar). 

Run the server:
```
java -jar runnable-petri-server.jar
```

## Step 2: [Create a Petri BackOffice app]({{site.data.urls.creating_a_petri_backoffice_app.url}}) 

We created a BackOffice application (soon to be open sourced as well!) to manage our experiments and specs. Our Product Managers have full access to it so they can create any user experience they like. 

If you're looking to see if Petri is for you, you can [issue a few HTTP requests]({{site.data.urls.managing_experiments_specs.url}}) to create specs and experiments programatically to get the ball rolling. This is essentially what our BackOffice does behind the scenes. 


## Step 3: [Integrate Laboratory (Petri's client) into your application]({{site.data.urls.integrating_petri_into_your_app.url}}) 

![Setting Up Petri Server](https://raw.githubusercontent.com/wix/petri/gh-pages/images/quickstart_architecture3.png)

Integrating Laboratory into your application is very easy if you're writing a JVM based server. It saves our servers extra hops as all the logic of experiemnt conduction is done within the Laboratory library, within the server who needs it. 

If you're not using a JVM based language you can [run Laboratory as a service]({{site.data.urls.using_laboratory_as_a_service.url}}). This means that you will be making that extra hop when you need to conduct an experiment. We use this method for some of our client code. 
