---
title: Laboratory As A Service
keywords: SAAS, laboratory, service, cloud
last_updated: March 31, 2016
sidebar: using_laboratory_as_a_service
permalink: /using_laboratory_as_a_service/
---

## Prerequisites

Read an [overview on Petri's architecture]({{site.data.urls.architecture_overview.url}}}) for a general understanding before moving forward.

## Integration For JVM Applications

If you are using a JVM app, please [read more here]({{site.data.urls.integrating_petri_into_your_app.url}})  

## Integration For Non-JVM Applications

If you don't run a JVM application you may call Petri server to perform the experiment for you and get the resulting testgroups via a REST API.
This can be achieved by deploying the [sample app](https://github.com/wix/petri/tree/master/sample-petri-app). Note: The URL is defined in laboratory.properties under the WEB-INF folder.

When you use this setup, it is important you understand how Laboratory reads and writes context:  

- Relevant context is read from the HTTP request (user id, language etc). See the [relevant code](https://github.com/wix/petri/blob/master/laboratory-servlet-api-integration/src/main/java/com/wixpress/petri/laboratory/HttpRequestUserInfoExtractor.java) for exact header/cookie/param names
- Previous conduction values are read from Petri's cookie, so consistent experience is maintained per user.
- Conduction values (where relevant) may be written back to the cookies.

This means there are two options for using this service:

- For issuing calls directly from your client code / the browser
- From your non-JVM application, and then you may want to pass some of the context to the request via one of these 2 options:

  - Add the userId param for example, or the previous conduction value from the cookie
  - Pass your own custom context and call the `conductExperimentWithCustomContext` method on the [SampleAppController](https://github.com/wix/petri/blob/2c31c03a47dcf00466fc812834b5c7abdc3271ae/sample-petri-app/src/main/java/com/wixpress/common/petri/SampleAppController.java). Combine this with writing your own [custom filters]({{site.data.urls.filters.url}}})
.  

