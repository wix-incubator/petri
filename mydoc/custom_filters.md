---
title: Custom Filters
keywords: filters, users
last_updated: March 31, 2016
sidebar: custom_filters
permalink: /custom_filters/
---

## Add a filter with your own custom logic 

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