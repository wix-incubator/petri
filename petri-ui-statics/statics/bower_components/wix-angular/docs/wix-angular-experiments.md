## Wix Angular Experiments
==========================

1. [Overview](#overview)
2. [Installation](#installation)
3. [API](#api)
4. [Directives](#directives)
4. [Usage](#usage)
5. [Test-Kit](#test-kit)

## Overview

Manage your experiments!
`wixAngularExperiments` module consists of:
* `experimentManagerProvider` + `experimentManager`
* `wixExperimentIf`: “ngIf based” directive to show/hide elements in according to experiment value.
* `wixExperimentDisabled`: “ngDisabled based” directive to enable/disable elements in according to experiment value.
* `wixExperimentClass`: add classes to your choice by experiment values.

## Installation

```html
<script>
  angular.module('myApp').config(function (experimentManagerProvider) {
    experimentManagerProvider.setExperiments(${experiments});
  });
</script>
```

What if you do not have velocity? This snippet will load experiments from a specific scope using jsonp:

```html
<script>
  loadExperimentScopeSync('my-account');
</script>
```

## API

* `experimentManager.getExperimentValue(experimentName)`:
        Returns a string
* `experimentManager.isExperimentEnabled(experimentName)`:
        Returns true/false if experiment value is ‘true’/’false’ accordingly.
* `experimentManager.loadScope(scope)`:
        Returns a promise resolved with an object of experiments key-value.
        This result will also be added to the manager’s experiments.
* `experimentManager.loadExperiment(experimentName)`:
        Returns a promise resolved with the experiment value.
        The experiment key-value will also be added to the manager’s experiments.

## Directives

```html
<div wix-experiment-if=”NAME-OF-CONSTANT or NAME-OF-EXPERIMENT”></div> //the value of the constant should equal to the experiment name
<button wix-experiment-disabled=”NAME-OF-CONSTANT or NAME-OF-EXPERIMENT”></button>
<div wix-experiment-class=”NAME-OF-EXPERIMENT” experiment-values=”{FISRT-VALUE: class1, SECOND-VALUE: class2}”></div>
```  

`wix-experiment-if` && `wix-experiment-disabled` supports negation:  
```html
<div wix-experiment-if=”!NAME-OF-CONSTANT  or !NAME-OF-EXPERIMENT”></div>
<div wix-experiment-disabled=”!NAME-OF-CONSTANT or !NAME-OF-EXPERIMENT”></div>
```
Inner div data is shown when `NAME-OF-EXPERIMENT` is false or does not exists (undefined). Similar to javascript negation.
You can use as many of negation sign you'd like
## Usage

## Test-Kit
`experimentManagerMock` is an angular decorator over the original service which gives us additional functionality
needed in our unit & E2E tests.

#### UT
Add this file to your karma.conf.js:
'app/bower_components/wix-angular/dist/wix-angular.js'

```js
/* myService.js */
this.f = function () {
    experimentManager.getExperimentValue('first');
    experimentManager.isExperimentEnabled('forth'); 
}

this.g = function () {
    experimentManager.loadScope('my-account').then(function () {
        // do something
    });
}
```

```js
/* myService.spec.js */

beforeEach(function () {
    module('wixAngularExperiments');
    module('experimentManagerMock');
});

function setExperiments(experiments) {
    module(function (experimentManagerProvider) {
        experimentManagerProvider.setExperiments(experiments);
    });
}

it('should be able to get experiment value', function () {
    setExperiments({first: '1', second: '2', third: 'true'});
    inject(function (experimentManager) {
        myService.f();
        expect(function () {
            experimentManager.verifyNoUnusedExperiments();
        }).toThrow('unused experiments: second, third');
       expect(function () {
            experimentManager.verifyNoUnexpectedExperiments();
       }).toThrow('unexpected experiments: forth');
    });
});

it("should load Scope's experiments", inject(
    function (experimentManager, $rootScope, myService) {
        experimentManager.setScopeExperiments('my-account', {experimentA: 'true', experimentB: 'val'});
        myService.func();
        expect(experimentManager.isExperimentEnabled('experimentA')).toBe(true);
        expect(experimentManager.getExperimentValue('experimentB')).toEqual('val');
    }
));
```
#### E2E

```html
<div class="back" wix-experiment-class="background-color" experiment-values="{gray: 'gray-background', white: 'white-background'}">
    <span class="app-icon" wix-experiment-if="big-ugly-icon"></span>
</div>
```

 ```js
 var experimentManager = require('../../../app/bower_components/wix-angular/test/lib/experiment-manager-test-kit.js');

experimentManager.setExperiments({'big-ugly-icon': 'true', 'background-color': 'gray'});
browser.get('/');
expect($('.app-background')).toHaveClass('gray-background');
expect($('.app-icon').isPresent()).toBe(true);

//DON'T FORGET TO ADD THIS CODE AS WELL
afterEach(function () {
  browser.clearMockModules();
});

```
