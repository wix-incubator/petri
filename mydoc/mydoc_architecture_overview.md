---
title: Architecture Overview
keywords: architecture
last_updated: March 31, 2016
sidebar: mydoc_architecture_overview
permalink: /mydoc_architecture_overview/
---

Petri consists of 2 parts:

- Petri server, which manages and stores all experiments
- Laboratory, Petri's client

![Petri architecture](http://static.wixstatic.com/media/1a2c40_1f81fb9df4b64b0fad220841f31b1de3.png)

Laboratory retrieves the active experiments from Petri server and conducts the experiments. All business logic of the tossing algorithm and all filter logic is run from the laboratory which is embedded in the client application. 

**Note:** Clients that don't run a JVM application can call [a separate server]({{site.data.urls.mydoc_using_laboratory_as_a_service.url}}) to perform the conduction for them, and get the resulting testgroups via HTTP API.

Petri is designed to be very flexible, so if you don't want to use Petri server you can simply use Petri Laboratory and configure it to load the active experiments from the local file system (since you don't have Petri server, it is up to you to provide the experiment definition file).

