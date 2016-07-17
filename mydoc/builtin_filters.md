---
title: Built-In Filters
keywords: filters, users, custom
last_updated: July 14, 2016
sidebar: builtin_filters
permalink: /builtin_filters/
---

* [Filters concept](http://wix.github.io/petri/basic_concepts_best_practices/#filters) 

## Customize a built-in filter data extraction

You can view the various builtin filters in  [here](https://github.com/wix/petri/tree/master/wix-petri-core/src/main/java/com/wixpress/petri/experiments/domain)

Currently three filters support custom parameters:

 1. `GeoFilter` (By Country)
 2. `LanguageFilter`
 3. `IncludeUserIdsFilter`

We can take the `GeoFilter` as an example, this filter uses the `CountryResolver` in order to get the value of the country from the Http request
you can view the [CountryResolver](https://github.com/wix/petri/blob/master/laboratory-servlet-api-integration/src/main/java/com/wixpress/petri/laboratory/Resolvers.scala#L44)'s, default resolution by looking at the `defaultResolution` function.
If you want to customize the way the country parameter is being extracted from the Http request, you can configure it by setting `FilterParametersExtractorsConfig`. This can also be achieved by putting a `filters.yaml` file in your application `WEB-INF` folder.
the file should look like this:

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

This configuration means that the country value for the the filter will be extracted by the order of the list. Meanning first the reslover will try to find a header on the request with the name `SOME_HEADER_NAME`, then the cookie and so on. Notice that you can specify multpile header/cookie/param names and they will each be resolved by the order of the list.
The first time the resolver finds a value the resolution process terminates.
If no custom value was extracted the reslover defalts to `defaultResolution`'s value
