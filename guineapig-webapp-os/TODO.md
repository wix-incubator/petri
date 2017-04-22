Architecture Notes
==================

Guineapig is supposed to be able to be deployed as a part of the petri app and on the same server, or on a separate server. We should provide a server that is wired to run them both (see module _runnable-petri-server_ in the open source repo), while keeping the ability to separate them.

Configuring Guineapig Server
----------------------------

1. SPI implementations are currently designed to be injected using Spring. Take _com.wixpress.guineapig.spi.UserAppConfig_ as an example.
2. _EditableMetaDataService_ implementation is also supposed to be provided/exposed by the user/configuration
  
   
Todo List for GuineaPig Open Sourcing
=====================================

1. Determine whether the functionality tested in PetriAppIT is required - seems like it was previously supported by framework code (the FW DispatcherServlet has been replaced with Spring's DispatcherServlet)
2. exposing the _EditableMetaDataService_ mgmt endpoints (which will cause the MetaDataIT to actually pass)
3. Sounds like a good idea to introduce an API module that includes all the formal APIs the user can potentially interact with (currently mainly the SPIs).
4. both of these spi’s need to be thought of : HardCodedScopesProvider & FilterAdapterExtender
they were (painfully) broken out of the god object FilterAdapter/ExperimentConverter, and at least are now interfaces!
but they are sucky interfaces (are they? i can’t see straight anymore) + need to look at the default implementations and see where they should sit
not really such a big deal, but since these are api’s user supposedly should extend better not change them in the future..
5. make the mailing service supported in guineapig-webapp (so that if a user has a mail provider it can be wired, same as the mails sent by petri-server in the os version)
6. make the ui support custom filter (i.e dropdown list of custom filter names, then json value? or similar...) 
Specifically in wix this mean the MetaSiteIds filter needs to be passed in the new format. see WixFilterAdapterExtender for how the server is already prepared for this.
7. Authentication! (hopefully GP2.0 will happen by then. if not this is a big missing part)





