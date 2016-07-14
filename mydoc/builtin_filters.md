---
title: Built-In Filters
keywords: filters, users, custom
last_updated: July 14, 2016
sidebar: builtin_filters
permalink: /builtin_filters/
---

* [Filters concept](http://wix.github.io/petri/basic_concepts_best_practices/#filters) 

## Customize a built-in filter data extraction

You can view the various builtin filters in here under the [package filters](https://github.com/wix/petri/tree/master/wix-petri-core/src/main/java/com/wixpress/petri/experiments/domain)

Currently three filters support custom parameters:
1. CountryFilter
2. LanguageFilter
3. IncludeUserIdsFilter

We can take the `CountryFilter` as an example, this filter uses the CountryResolver in order to get the calue of the country from the Http request
you can view the [CountryResolver]()'s, default resolution of this value by looking at the `defaultResolution` function.
If you want to customize the way country param is bein extracted from the Http request You can configure it by setting ``

