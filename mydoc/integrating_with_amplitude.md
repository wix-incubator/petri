---
title: Integrating Laboratory with Amplitude
keywords: amplitude, bi, event
last_updated: August 4, 2016
sidebar: integrating_with_amplitude
permalink: /integrating_with_amplitude/
---

> Petri logs its experiments' results to a log file. If you don't have a BI system in place, you can optionally report them on Amplitude.com for faster results. Amplitude.com is a simple BI as a service which you can use to easily analyze experiment results via funnels.

### Reporting Petri Experiment Results to Amplitude

Add the following properties to your 'laboratory.properties' file:

```
amplitude.url=https://api.amplitude.com/httpapi
amplitude.api.key=<your-api-key>
amplitude.timeout.ms=500
```

### Reporting Custom BI Events to Amplitude

Sending the experiment results is not enough for creating a funnel - you must also report your business events to Amplitude as well.

For instructions on how to integrate and send events to Amplitude read [this](https://amplitude.zendesk.com/hc/en-us/categories/200409887-Installation-and-Integration).

An example can be found in the class AmplitudeAdapter in the laboratory-servlet-api-integration module.