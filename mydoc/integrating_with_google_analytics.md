---
title: Integrating Laboratory with Google Analytics
keywords: google analytics, bi, event
last_updated: December 21, 2016
sidebar: integrating_with_google_analytics
permalink: /integrating_with_google_analytics/
---

> Google Analytics is a simple BI-as-a-service which lets you easily analyze experiment results via funnels. 

![simple funnel example](https://raw.githubusercontent.com/wix/petri/gh-pages/images/ga_button_clicks_by_its_color_funnel.png)

Petri logs its experiments results to a log file. If you don't have a BI system in place, you can optionally report these results to Google Analytics.

### Reporting Petri Experiment Results to Google Analytics

Add the following properties to your `laboratory.properties` file:

```
google.analytics.url=https://www.google-analytics.com/collect
google.analytics.tracking.id=<your-tracking-id>
google.analytics.timeout.ms=1000
```

### Reporting Custom BI Events to Google Analytics

To create a funnel (as seen in the picture above) on Google Analytics both experiment results and business events must be reported. 

For instructions on how to integrate and send events to Google Analytics read [this](https://developers.google.com/analytics/devguides/collection/protocol/v1/). An example can be found in the class [GoogleAnalyticsAdapter](petri/laboratory-servlet-api-integration/src/main/scala/com/wixpress/petri/google_analytics/GoogleAnalyticsAdapter.scala).

### Live Examples

#### A live demo with Petri and Google Analytics
1. Edit [laboratory.properties](https://github.com/wix/petri/blob/master/petri-bi-integration-testapp/src/main/webapp/WEB-INF/laboratory.properties) and uncomment the google.analytics.url parameter.
2. Run [MainBiSampleApp](https://github.com/wix/petri/blob/master/petri-bi-integration-testapp/src/main/scala/com/wixpress/common/petri/MainBiSampleApp.scala)
3. Using your browser, go to `http://localhost:9811/testGoogleAnalytics`. As Petri uses cookies to track users you may do one of the following to mimic multiple users coming into this site: 
	- Open the same url from different browsers
	- Open the url in incognito mode several times
	- Delete your browser's cookies after each visit
4. See the funnel created from your reported events on [Google Analytics](https://analytics.google.com/analytics/web/?authuser=0#realtime/rt-event/a89204848w132385051p136346283/)

#### A Petri + Google Analytics integrated application 

An application that is integrated with both Petri and Google Analytics, and sends both Petri BI events and relevant application BI events can be found in [GoogleAnalyticsTestappIT](https://github.com/wix/petri/blob/master/petri-bi-integration-testapp/src/it/scala/com/wixpress/common/petri/GoogleAnalyticsTestappIT.scala) 

