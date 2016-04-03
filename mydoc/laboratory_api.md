---
title: Conducting An Experiment
keywords: laboratory, conduct, API, experiment
last_updated: March 31, 2016
sidebar: laboratory_api
permalink: /laboratory_api/
---

For the full updated API set see [the code](https://github.com/wix/petri/blob/master/wix-petri-core/src/main/java/com/wixpress/petri/laboratory/Laboratory.java)

### Concepts
- Conduction key has 3 options:
  * Class<? extends SpecDefinition> experimentKey - always favor this over the next option (read [this]({{site.data.urls.mydocs_experiments.url}}}) to understand why)
  * String key
  * String scope - when using 'conductAllInScope'

- fallback value - used as return value when user does not pass the experiment's filters.
Usually will be the same as the 1st testgroup's value - i.e 'off'.

- TestResultConverter<T> - can be used to receive a value of type T from the laboratory call - default is StringConverter, other options are BooleanConverter and IntegerConverter. More can be easily added.

- ConductionContext - for special cases where you want to pass some custom context. Read [here]({{site.data.urls.mydocs_custom_filters.url}}}) for more.