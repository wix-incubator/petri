Petri
=====

[![Build Status](https://travis-ci.org/wix/petri.svg?branch=master)](https://travis-ci.org/wix/petri)

Wix's holistic experiment system - covers the lifecycle of product experimenting (A/B tests and feature toggles).

Petri is used at large scale at [Wix](http://www.wix.com), supporting dozens of daily changes (instantly affecting production, no deployment needed).  

### Main features
* A/B tests can be used to affect the experience users receive - thus enabling product managers to know what users prefer.
* Feature toggles can be used to control inner implementation details - thus enabling developers to gradually roll out features.  
* Petri allows you to define filters that determine what users will be included in the experiment (by language, geo, user-agent, for individual users and more). Custom filters can also be added.
* Conduction values are periodically reported back to the server to enable quick triaging as well as automatic pausing if limit is specified.

### Getting started
* [Run](https://github.com/wix/petri/wiki/Running-Petri-Server) (or [Install](https://github.com/wix/petri/wiki/Installing-Petri-Server)) Petri Server
* [Integrate Laboratory (petri client) into your app](https://github.com/wix/petri/wiki/Integrating-Petri-into-your-app)


### Want to learn more? 
Have a look at the [quickstart guide](https://github.com/wix/petri/wiki/Quickstart-Guide) or browse our [wiki](https://github.com/wix/petri/wiki) for more [concepts & best practices] (https://github.com/wix/petri/wiki/Concepts-&-Best-Practices).

</br>

### License

Copyright (c) 2014 Wix.com Ltd. All Rights Reserved. Use of this source code is governed by a BSD-style license that can be found in the LICENSE file in the root of the source tree.