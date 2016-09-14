Angular Debounce [![Build Status](https://travis-ci.org/shahata/angular-debounce.svg?branch=master)](https://travis-ci.org/shahata/angular-debounce) [![Coverage Status](https://coveralls.io/repos/shahata/angular-debounce/badge.png?branch=master)](https://coveralls.io/r/shahata/angular-debounce?branch=master)
================

[AngularJS](http://www.angularjs.org) debounce service and directive

## What it does

1. Service - Creates and returns a new debounced version of the passed function which will postpone its execution until after **wait** milliseconds have elapsed since the last time it was invoked. Useful for implementing behavior that should only happen *after* the input has stopped arriving. For example: recalculating a layout after the window has stopped being resized.

2. Directive - Can be added to any element with an ng-model attribute and postpone model updates by user input until after **wait** milliseconds have elapsed since the last user input. Useful for watchers that should only be invoked *after* the input has stopped arriving. For example: rendering a preview of a Markdown comment.


## Installation

Install using bower

`bower install --save ng-debounce`

Include script tag in your html document.

```html
<script type="text/javascript" src="bower_components/ng-debounce/angular-debounce.js"></script>
```

Add a dependency to your application module.

```javascript
angular.module('myApp', ['debounce']);
```

## Service Usage

`debounce(func, wait, [immediate], [invokeApply])`

### Arguments

|Param|Type|Details|
|---|---|---|
|func|function|The function we want to **debounce**|
|wait|number|Number of miliseconds to **wait** before invoking the debounced function|
|immediate (optional)|boolean|Pass `true` for the **immediate** parameter to cause **debounce** to trigger the function on the leading instead of the trailing edge of the **wait** interval. Useful in circumstances like preventing accidental double-clicks on a "submit" button from firing a second time.|
|invokeApply (optional)|boolean|`invokeApply` param passed to `$timeout` service (defines whether apply should be called in order to trigger a digest cycle at the end of the `func` call) - see [$timeout](https://docs.angularjs.org/api/ng/service/$timeout) service for more details|

### Returns

A debounced version of the passed function. Any arguments passed to this function will be also passed to the passed function in case this invocation will trigger the function.

The returned function also has a `cancel()` method which can be used in case you what to reset the current debounce state. This will prevent the function from being triggered even after **wait** miliseconds have passed from last input. In case **immediate** is `true`, this means that the next user input will trigger the debounce.

## Directive Usage

```html
<input type="text" ng-model="blah" debounce="500"></input>
<input type="checkbox" ng-model="blah" debounce="500" immediate="true"></input>
etc.
```

### Arguments

|Param|Type|Details|
|---|---|---|
|debounce|number|Number of miliseconds to **wait** before triggering the model update|
|immediate (optional)|boolean|Pass `true` for the **immediate** parameter to cause **debounce** to trigger model update on the leading instead of the trailing edge of the **wait** interval.|

## Reference

The debounce service is based on the debounce implementation in uderscorejs
* underscorejs: http://underscorejs.org/#debounce

## License

The MIT License.

See [LICENSE](https://github.com/shahata/angular-debounce/blob/master/LICENSE)
