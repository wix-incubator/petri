---
title: Integrating Laboratory with Amplitude
keywords: amplitude, bi, event
last_updated: August 4, 2016
sidebar: integrating_with_amplitude
permalink: /integrating_with_amplitude/
---

> Amplitude.com is a simple BI-as-a-service which lets you easily analyze experiment results via funnels. 

![simple funnel example](https://raw.githubusercontent.com/wix/petri/gh-pages/images/button_clicks_by_its_color_funnel.png)

Petri logs its experiments results to a log file. If you don't have a BI system in place, you can optionally report these results to Amplitude.com.

### Reporting Petri Experiment Results to Amplitude

Add the following properties to your `laboratory.properties` file:

```
amplitude.url=https://api.amplitude.com/httpapi
amplitude.api.key=<your-api-key>
amplitude.timeout.ms=500
```

### Reporting Custom BI Events to Amplitude

To create a funnel (as seen in the picture above) on Amplitude both experiment results and business events must be reported. 

For instructions on how to integrate and send events to Amplitude read [this](https://amplitude.zendesk.com/hc/en-us/categories/200409887-Installation-and-Integration). An example can be found in the class [AmplitudeAdapter](petri/laboratory-servlet-api-integration/src/main/scala/com/wixpress/petri/amplitude/AmplitudeAdapter.scala).

### Live Examples

#### A live demo with Petri and Amplitude

1. Edit [laboratory.properties](https://github.com/wix/petri/blob/master/petri-bi-integration-testapp/src/main/webapp/WEB-INF/laboratory.properties) and uncomment the amplitude.url parameter.
2. Run [MainBiSampleApp](https://github.com/wix/petri/blob/master/petri-bi-integration-testapp/src/main/scala/com/wixpress/common/petri/MainBiSampleApp.scala)
3. Using your browser, go to `http://localhost:9811/testAmplitude`. As Petri uses cookies to track users you may do one of the following to mimic multiple users coming into this site: 
	- Open the same url from different browsers
	- Open the url in incognito mode several times
	- Delete your browser's cookies after each visit
4. See the funnel created from your reported events on [Amplitude.com](https://amplitude.com/app/151746/funnels?fid=20206&cg=User&range=Last%2030%20Days&i=1&dets=0)

#### A Petri + Amplitude integrated application 

An application that is integrated with both Petri and Amplitude, and sends both Petri BI events and relevant application BI events can be found in [AmplitudeTestappIT](petri/petri-bi-integration-testapp/src/it/scala/com/wixpress/common/petri/AmplitudeTestappIT.scala) 

