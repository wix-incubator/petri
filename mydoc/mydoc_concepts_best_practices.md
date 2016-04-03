---
title: Basic Concepts and Best Practices
keywords: concepts, best practices
last_updated: March 31, 2016
sidebar: mydoc_basic_concepts_best_practices
permalink: /mydoc_basic_concepts_best_practices/
---

## Experiments

- Experiment : A method of modifying system’s behaviour based on context.
- Feature Toggle / Feature Flag is a stateless experiment.
- A/B test is a stateful experiment where we keep track of the participants.

Multiple experiments can be created with the same key - for example running 2 experiments of the same type:

- One experiment on 50% of users in Japan & one experiment on 20% of users in USA
- One FT for all company employees & one A/B test for the rest of the world

At Wix, we created a Backoffice application for creating and editing experiments. Learn how to setup yours [here]({{site.data.urls.mydoc_creating_a_petri_backoffice_app.url}}).

## Experiment Specs

An experiment spec is a template for creating experiments. It defines the experiment key, test groups (possible outcomes/results), product(s) the experiment belongs to, and an owner.

Our best practice is to allow creating experiments based on existing specs. This best practice emerged as a lesson learned from cases of hunting down experiments that did not seem to be working. 

Read more about experiments and experiment specs [here]({{site.data.urls.mydoc_experiments.url}}).

## Filters

Filters may be used to determine what users will be included in your defined experiments. Our built in filters include language, geo, user-agent, individual user IDs (usually used for QA) and more. 

You can also create your own custom filters, read more about that [here]({{site.data.urls.mydoc_custom_filters.url}}).


## How Petri Persists User Experience

Cookies are used to provide stickiness for users i.e , when running an A/B test and a user is given a choice between A or B, once a user receives B they should continue to do so on to the next visit.

Read more about how Petri persists user experience [here]({{site.data.urls.my_doc_user_experience_persistence.url}}).


