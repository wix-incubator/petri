Work Done So Far
================

1. Introduced guineapig-webapp-os module (copied from wix-petri-webapp)
2. Removed all wix dependencies from guineapig-webapp-os
    - FW DispatcherServlet has been replaced with Spring's
    - ERB conf system has been removed and therefore ERB values are no longer loaded.
    - FW test-kits of all kinds 
3. Created a new SPI (Service Provider Interface) package for user provided implementations _com.wixpress.guineapig.spi_ (we probably need to provide default noop style implementations) 
5. We created a new IT environment, which is based on an embedded Jetty server _com.wixpress.embeddedjetty.JettyServer_, which is free from wix framework magics. The wix IT environment injects server beans into the test classes themselves and now this capability is gone, which is why all the IT tests (all except ExperimentsControllerIT which is new), are failing.
    - All new IT tests are supposed to extend _SpecificationWithEnvSupport_ in order to get a running guineapig server and then use an HTTP client to test controller endpoints.
6. Class _GuineapigResult_ is a stripped down version of the framework _WixResult_. This structure cannot change unless the UI changes - BE CAREFUL!!!
7. Authentication and authorization features from wix's guineapig have been completely removed.
      
**Look for the string "todo guineapig-os" in the code for inline comments related to guineapig open sourcing. You may use intelliJ TODO filtering for that.**      

Architecture Notes
==================

Guineapig is supposed to be able to be deployed as a part of the petri app and on the same server, or on a separate server. We should provide a server that is wired to run them both (see module _runnable-petri-server_ in the open source repo), while keeping the ability to separate them.

Configuring Guineapig Server
----------------------------

1. SPI implementations are currently designed to be injected using Spring. Take _com.wixpress.guineapig.spi.UserAppConfig_ as an example.
2. _EditableMetaDataService_ implementation is also supposed to be provided/exposed by the user/configuration
  
   
Todo List for GuineaPig Open Sourcing
=====================================

1. see if the JettyServer used for the test can be replaced with (some flavor of) ServerRunner that exists in the petri-test-utils module in the OS project
2. Finish the implementation of _ExperimentsControllerIT_
3. Replace all IT tests except _ExperimentsControllerIT_ with controller tests. 
4. **Copy petri-backoffice-core and move guineapig-webapp-os to the open-source repo**
5. work on the pom (and make sure mvn runs test + cleanup everything that can be!)
6. Integrate guineapig-webapp-os with the UI module
    - Remove wix traces from the UI (based on the 'isStaging' flag - might be nasty or easy, not sure in advance)
    - Package the UI with guineapig-webapp-os
    - Introduce a replacement to the ERB conf system that has already been removed. (if good enough can use .porperties file like we did for the other relevant modules)
7. Make _runnable-petri-server_ run both petri and guineapig (including the UI) 
    - this is where THE e2e should be (seleunium and all ;) - note there is alrady a selenium test in this project under the amplitude-testapp module so setup should be easy.
    - Make sure velocity is configured properly - there were some changes in the area and since there was no functional UI and UI automation it was never tested.
    - provide default noop style implementations for the spi classes (or extract the sample ones in UserAppConfig so they are reusable)

---------------------------

8. Determine whether the functionality tested in PetriAppIT is required - seems like it was previously supported by framework code (the FW DispatcherServlet has been replaced with Spring's DispatcherServlet)
9. exposing the _EditableMetaDataService_ mgmt endpoints (which will cause the MetaDataIT to actually pass)
10. Sounds like a good idea to introduce an API module that includes all the formal APIs the user can potentially interact with (currently mainly the SPIs).
11. both of these spi’s need to be thought of : HardCodedScopesProvider & FilterAdapterExtender
they were (painfully) broken out of the god object FilterAdapter/ExperimentConverter, and at least are now interfaces!
but they are sucky interfaces (are they? i can’t see straight anymore) + need to look at the default implementations and see where they should sit
not really such a big deal, but since these are api’s user supposedly should extend better not change them in the future..
12. make the mailing service supported in guineapig-webapp (so that if a user has a mail provider it can be wired, same as the mails sent by petri-server in the os version)
13. make the ui support custom filter (i.e dropdown list of custom filter names, then json value? or similar...) 
Specifically in wix this mean the MetaSiteIds filter needs to be passed in the new format. see WixFilterAdapterExtender for how the server is already prepared for this.
14.Authentication! (hopefully GP2.0 will happen by then. if not this is a big missing part)





