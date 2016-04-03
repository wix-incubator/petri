Read [this](https://github.com/wix/petri/wiki/PETRI-System-Components) for a general overview of the system's architecture.

If you don't run a JVM application you can call PETRI server to perform the experiment for you and get the resulting test group/s via REST api.
This can be achieved by simply deploying the [sample app] (https://github.com/wix/petri/tree/master/sample-petri-app)  
(url is defined in laboratory.properties under the WEB-INF folder)

##### If you use this setup, it is important that you understand how laboratory reads and writes context:  

- relevant context is read from the http request (user id, language etc). See the [relevant code](https://github.com/wix/petri/blob/master/petri-spring-integration/src/main/java/com/wixpress/petri/laboratory/HttpRequestUserInfoExtractor.java) for exact header/cookie/param names
- previous conduction values are read from Petri's cookie - so consistent experience is maintained per user
- conduction values (where relevant) may be written back to the cookies.

##### This means that you have two options of using this service:
- for issuing calls directly from your client code / the browser
- from your non-JVM app, and then you might want to pass some of the context to the request via one of these 2 options:
  - add the userId param for example, or the previous conduction value from the cookie
  - pass your own custom context and call the 'conductExperimentWithCustomContext' method on the [SampleAppController](https://github.com/wix/petri/blob/2c31c03a47dcf00466fc812834b5c7abdc3271ae/sample-petri-app/src/main/java/com/wixpress/common/petri/SampleAppController.java). 
Combine this with writing your own [custom filters](https://github.com/wix/petri/wiki/Custom-Filters)
.  

If you <b>are</b> using a JVM app, go [this way](https://github.com/wix/petri/wiki/Integrating-Petri-into-your-app)  
You 