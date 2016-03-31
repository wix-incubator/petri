---
title: Introduction
tags: [getting_started, introduction]
keywords: introduction
last_updated: March 31, 2016
summary: "Introduction"
sidebar: mydoc_introduction
permalink: /mydoc_introduction/
---

![Wix PETRI](http://static.wix.com/media/1a2c40_947e5f0e64d175a4534898a64d1a67d2.jpg)

# Product Experiments Toggles & Reporting Infrastructure

Perti is a holistic experiment infrastructure that covers the lifecycle of product experimenting.

### Terminology

> **Experiment** - A method of modifying a system’s behaviour based on context.

> **Feature Toggle / Feature Flag** - A stateless experiment.

> **A/B test** - A stateful experiment where we keep track of the participants.

### How an experiment begins its life
There are many types of experiments on a system and they may vary based on the context. 

We must understand what we are testing, what the context of the experiment is, what the prerequisites or limitations to the experiment are.

For instance, is the experiment being conducted on a section of the system that a user needs to be authenticated for? 
It is being run on landing pages when visitors are anonymous?
Are we conducting an experiment on bots when there is no human interaction?
Is the experiment on a part of the system that is executed by a scheduler?
Are we testing human behavior, protecting the system by doing gradual exposure to users, or trying to improve performance?

While some of these questions can be answered by a product manager who wants to do a simple A/B test, the person who can answer all these question is the developer who implements the experiment logic and knows exactly what the context of the experiment is. 

> An **experiment spec** is the basic template of an experiment. It has all the mandatory limitation and condition on an experiment.

For example: if an experiment is being done on an area of your application where a user has to be signed in then the spec will define a mandatory “filter” on sign-in users

> **Filter** - A condition for eligibility to participate in an experiment.

### How an experiment is defined
Once a developer creates the experiment spec in the code, a product manager can create an instance of the experiment spec and employ conditions (filters) on the test groups and who is eligible to participate in the experiment.

> **Test Group** - A possible value of an experiment, for instance: for an experiment that has two possible values of “true” and “false”, the “users” of test group A will get the value “true” and test group B will get the value “false”.

An experiment is defined by an instance of a spec that is either stateful (A/B test) or stateless(Feature Toggle), that is enriched by a set of filters that define the eligibility of participating in the experiment, and the test group ratio (the percentage of users who will get to be in each test group)

While the experiment spec defines the mandatory filters by the developer, a product manager can also add a set of optional filters to determine the eligibility. Some of the filters can be geo IP, language, user agent or anything else that is known in the context of the running experiment. 
