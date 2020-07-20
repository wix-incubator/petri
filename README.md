[Petri](http://wix.github.io/petri/)
======

[![Build Status](https://travis-ci.org/wix/petri.svg?branch=master)](https://travis-ci.org/wix/petri)

Petri is [Wix](http://www.wix.com)’s holistic experiment system, it covers the lifecycle of product experimenting including A/B tests and feature toggles.

Petri is used at large scale at [Wix](http://www.wix.com), supporting dozens of daily changes (instantly affecting production, no deployment needed).


### Main features
* Create and gradually open A/B tests to affect your user’s experience. This feature helps our Product Managers collect data on user behavior before making decisions.
* Create Feature Toggles to control inner implementation details. This feature helps our developers gradually roll out features.
* Define filters to determine what users will be included in your defined experiments. Our filters include language, geo, user-agent, individual user IDs (usually used for QA) and more. Custom filters can also be added.
* Conduction values are periodically reported back to the server to enable quick triaging as well as automatic pausing if a limit to the number of users is specified.



### License

Copyright (c) 2014 Wix.com Ltd. All Rights Reserved. Use of this source code is governed by a BSD-style license that can be found in the LICENSE file in the root of the source tree.
