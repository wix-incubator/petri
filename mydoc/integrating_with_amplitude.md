---
title: Integrating Laboratory with Amplitude
keywords: amplitude, bi, event
last_updated: August 4, 2016
sidebar: integrating_with_amplitude
permalink: /integrating_with_amplitude/
---

> Petri logs its experiments' results to a log file. If you don't have a BI system in place, you can optionally report them on Amplitude.com for faster results. Amplitude.com is a simple BI-as-a-service which you can use to easily analyze experiment results via funnels.

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

An example can be found in the class [AmplitudeAdapter](petri/laboratory-servlet-api-integration/src/main/scala/com/wixpress/petri/amplitude/AmplitudeAdapter.scala).

### Live Examples

- Take a look on [AmplitudeTestappIT](petri/petri-amplitude-testapp/src/it/scala/com/wixpress/common/petri/AmplitudeTestappIT.scala) for an application that is integrated with petri and amplitude, and sends both petri BI events and the relevant application BI events.
- For a live demo with petri and amplitude:
    - Run [MainAmplitudeSampleApp](petri/petri-amplitude-testapp/src/main/scala/com/wixpress/common/petri/MainAmplitudeSampleApp.scala),
    - Enter from couple of browsers, or incognito mode several times, or delete cookies after each visit in **http://localhost:9811/test**,
    - See the funnel created from the events on [Amplitude.com](https://amplitude.com/app/151746/funnels?fid=20206&cg=User&range=Last%2030%20Days&i=1&dets=0) [user:nimrodl@wix.com,password:GNK5OdwkZzh5Qw7f9qPB].
- Funnel example on Amplitude:
![Setting Up Petri Server](https://raw.githubusercontent.com/wix/petri/gh-pages/images/button_clicks_by_its_color_funnel.png)