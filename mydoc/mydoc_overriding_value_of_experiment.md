---
title: Overriding Experiment Values
keywords: QA, override, experiment, support, testing
last_updated: March 31, 2016
sidebar: mydoc_overriding_value_of_experiment
permalink: /mydoc_overriding_value_of_experiment/
---

Use the `petri_ovr=The-Key:The-Value` query parameter to override the value returned from Laboratory's conduction to 'The-Value'. The end url should look something like this: 
`http://yourUrl?petri_ovr=The-Key:The-Value`

The value will be overridden regardless of the current settings. For example, even if the experiment has 100% for the 'off' option, and even if the given user does not pass any filters you may have defined on that experiment.

To override multiple experiments concatenate them with a semicolon as a delimiter: `?petri_ovr=The-Key:The-Value;Other-Key:Other-Value`

Note: The value must be one of the testgroups defined on the experiment