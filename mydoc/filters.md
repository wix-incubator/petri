---
title: Filters
keywords: filters, users, custom, built in
last_updated: March 31, 2016
sidebar: filters
permalink: /filters/
---

> A **filter** is a condition for eligibility to participate in an experiment.

Filters may be used to determine what users will be included in your defined experiments. Our built in filters include language, geo, user-agent, individual user IDs (usually used for QA) and more. 

## Built-In Filters

Petri comes with various built-in filters, these filters extract and use parts of an HTTP request to make decisions about which users will be included in a defined experiment. 
A full list of built-in servers can be found [here](https://github.com/wix/petri/tree/master/wix-petri-core/src/main/java/com/wixpress/petri/experiments/domain).

Some built-in filters let you choose which parts of an HTTP request to extract relevant data from (and then make decisions by). These filters are:

 1. `GeoFilter` (By Country)
 2. `LanguageFilter`
 3. `IncludeUserIdsFilter` (Filters using user IDs, this helps us open features to specific users)

### Customizing data extraction for a filter

A filter may extract data from an HTTP header, a cookie, a query parameter, or a an HTTP header. Priority may also be set. Both are configured by adding configuration to (or creating) a `filters.yaml` file in your application `WEB-INF` folder.

The following is configuration for the `CountryFilter`:

```yaml
    configs:
      Country:
      - - "Header"
        - "SOME_HEADER_NAME"
      - - "Cookie"
        - "SOME_COOKIE_NAME"
      - - "Param"
        - "SOME_PARAM_NAME"
      - - "Header"
        - "SOME_OTHER_HEADER_NAME"
      Language:
        ...
```

The configuration above means that the country value for the filter will be extracted using the priority set by the list: First, the reslover will try to find a header on the request with the name `SOME_HEADER_NAME`. Then, it will search by the cookie and so on. 

*Note:* You may specify multiple header/cookie/param names and they will each be resolved by the order of the list. When the resolver finds a value, the solution process completes. In case the value is not resolved, the  reslover defaults to the default behaviour.

Setting default behaviour for a resolver is done using its `defaultResolution` function, as can be seen in [CountryResolver's defaultResolution function](https://github.com/wix/petri/blob/master/laboratory-servlet-api-integration/src/main/java/com/wixpress/petri/laboratory/Resolvers.scala#L44)


## Custom Filters

### Add a custom filter with your own custom logic 

Any filter on the classpath that answers the following criteria will be available:

  - The class should be under the root package "filters" (similar to the "specs" package)
  - The class should implement the [Filter](https://github.com/wix/petri/blob/master/wix-petri-core/src/main/java/com/wixpress/petri/experiments/domain/Filter.java) interface 
  - The class should define the '@FilterTypeName("id")' annotation

see examples:

1. [Additional Filter example](https://github.com/wix/petri/blob/master/wix-petri-core/src/test/java/filters/AdditionalFilter.java)
2. [Custom User Type Filter example](https://github.com/wix/petri/blob/2c31c03a47dcf00466fc812834b5c7abdc3271ae/sample-extended-filters/src/main/java/filters/CustomUserTypeFilter.java)



Another option is to add a jar containing your custom filters. This option is convenient if you are using the [Laboratory as a Service]({{site.data.urls.using_laboratory_as_a_service.url}}).

1. Create a directory called `petri-plugins` in the same location as you [installed the server]({{site.data.urls.quickstart.url}}#install-petri-server)

	- Any jar containing 'extended-filters' in it's name will be scanned for filters
	- Filters should follow the same guidelines as in the above section

2. Do the same in the location where you installed the 'Laboratory Service'
