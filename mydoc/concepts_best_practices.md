---
title: Basic Concepts and Best Practices
keywords: concepts, best practices
last_updated: March 31, 2016
sidebar: basic_concepts_best_practices
permalink: /basic_concepts_best_practices/
---

## Experiments

> An **experiment** is a method of modifying system’s behavior based on context.

There are two types of experiments, each used for different purposes.

> A **Feature Toggle / Feature Flag** is a stateless experiment.

At Wix, developers use Feature Toggles (FT) to gradually roll out different implementations, where product changes are not intended. Examples include changing of databases and refactoring code.

> An **A/B test** is a stateful experiment where data is collected on participants behavior.

At Wix, we use A/B tests to gradually affect our user's experience. This feature helps our Product Managers collect data on user behavior before making decisions. Examples include changing landing pages and product behaviour.

Multiple experiments can be created with the same key. For example:

- One experiment can be open to 50% of users in Japan and one experiment open to 20% of users in the USA.
- One FT open to all company employees & an A/B test for the rest of the population.

At Wix, we created a Backoffice application for creating and editing experiments. Learn how to setup yours [here]({{site.data.urls.creating_a_petri_backoffice_app.url}}).

## Experiment Specs

> An **experiment spec** is a template for creating experiments. It defines the experiment key, test groups (possible outcomes/results), product(s) the experiment belongs to, and an owner.

Our best practice is to allow creating experiments based on existing specs. This best practice emerged as a lesson learned from cases of hunting down experiments that did not seem to be working. 

Read more about experiments and experiment specs [here]({{site.data.urls.experiments.url}}).

## Filters

> A **filter** is a condition for eligibility to participate in an experiment.

Filters may be used to determine what users will be included in your defined experiments. Our built in filters include language, geo, user-agent, individual user IDs (usually used for QA) and [more]({{site.data.urls.builtin_filters.url}}). 

Read more about filters [here]({{site.data.urls.filters.url}}).


## How Petri Persists User Experience

Cookies are used to provide stickiness for users i.e , when running an A/B test and a user is given a choice between A or B, once a user receives B they should continue to do so on to the next visit.

Read more about how Petri persists user experience [here]({{site.data.urls.user_experience_persistence.url}}).


