---
title: Architecture Overview
keywords: architecture
last_updated: March 31, 2016
sidebar: mydoc_architecture_overview
permalink: /mydoc_architecture_overview/
---

PETRI consists of 2 parts:

PETRI server, which manages and stores all the experiments;
and a PETRI client library called Laboratory.

![Petri architecture](http://static.wixstatic.com/media/1a2c40_1f81fb9df4b64b0fad220841f31b1de3.png)

Laboratory retrieves the active experiments from PETRI server and conducts the experiments. All the business logic of the tossing algorithm and all the filters logic is run from the laboratory which is embedded in your application. 

If you don't run a JVM application you can call [a separate server](https://github.com/wix/petri/wiki/Using-Laboratory-as-a-Service) to perform the conduction for you and get the resulting test group/s via REST api.

PETRI is designed to be very flexible so if you don't want to use PETRI server you can simply use PETRI Laboratory and configure it to load the active experiments from the local file system (since you don't have PETRI server it is up to you to provide the experiment definition file)

