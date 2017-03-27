---
title: User Experience Persistence
keywords: persistence, experience, UX
last_updated: March 31, 2016
sidebar: user_experience_persistence
permalink: /user_experience_persistence/
---

\* "user agent" - a machine/browser

#### Cookies
  - Cookies are used to provide stickiness for users
i.e , when running an A/B test and a user is given a choice between A or B, once a user receives B they should continue to do so on to the next visit.

  - For anonymous (non-registered) users, the conduction is random so the best we can do is save cookies.
If the user changes user agent we have no means of identifying the same person (browser/machine) and stickiness will be lost.

  - For registered users, we use the UID as a seed for conduction, thereby providing consistent results regardless of the user agent.
Despite the seed and results being consistent, we must still save a cookie as it is needed for the 'pause' feature.

  - Cookies are saved separately.
One cookie for anonymous experiments (shared by all anonymous users / experiments) on a given user agent, and another cookie per UID. 
    - The reason for this 'per UID cookie' is so every user receives their very own unique experience, regardless of other users sharing this user agent (think shared a household where there are two people with different accounts).

    - The reason the anonymous must be saved separately even when a user is logged in is because if and when the user logs out we still want the 'anonymous experiments' to retain stickiness (and without the UID we wouldn't know which cookie to read)


#### The Problem
When an experiment is paused, stickiness may be lost (if a user changed user agents).
In order to solve this you can enable server side state.

#### Cookies VS Server Side State: The Tradeoff
  - Cookies: no overhead, but you may loose stickiness sometimes (when an experiment is paused and a registered user changes the user agent).
  - Server side state: overhead of another hop (for read. if conduction occurs then another for write), but you gain correctness. Here's [how to config your app to use server side state]({{site.data.urls.integrating_petri_into_your_app.url}}). Default is 'off'.

  - Therefore, you probably want to configure the server side state only for appropriate services (probably in your 'registered users' segment, thus allowing any 'public' segment to retain its SLA and not pay the overhead).


  - Petri's implementation tries to save overhead wherever possible:
    - Cookies override DB state so if a cookie exists the extra hop is avoided.
    - If a conduction was made write state back to DB as an async call so user request returns immediately.
    - State is read from server iff conduction is on registered scope and it is an A/B test (not feature toggle) and if a user exists in context etc.
    - When using the 'conductAllByScope' API state is brought once for all experiments.
