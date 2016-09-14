#Wix Debounce

[AngularJS](http://www.angularjs.org) debounce service

## What it does

Provides a service that creates and returns a new debounced version of the passed function which will postpone its execution until after **wait** milliseconds have elapsed since the last time it was invoked. Useful for implementing behavior that should only happen *after* the input has stopped arriving. For example: recalculating a layout after the window has stopped being resized.

It is basically a port (copy and paste) of the service from [ngDebounce](https://github.com/shahata/angular-debounce) created by **shahata** (without the directive which is deprecated since AngularJS 1.3) that implements [underscorejs debounce](http://underscorejs.org/#debounce) as an AngularJS service.

## Usage
Make sure wixAngular dependency is included in your app
```javascript
angular.module('myApp', ['wixAngular']);
```

Use the service by injecting wixDebounce into your component and use:

`wixDebounce(func, wait, [immediate], [invokeApply])`

### Arguments

|Param|Type|Details|
|---|---|---|
|func|function|The function we want to **debounce**|
|wait|number|Number of miliseconds to **wait** before invoking the debounced function|
|immediate (optional)|boolean|Pass `true` for the **immediate** parameter to cause **wixDebounce** to trigger the function on the leading instead of the trailing edge of the **wait** interval. Useful in circumstances like preventing accidental double-clicks on a "submit" button from firing a second time.|
|invokeApply (optional)|boolean|`invokeApply` param passed to `$timeout` service (defines whether apply should be called in order to trigger a digest cycle at the end of the `func` call) - see [$timeout](https://docs.angularjs.org/api/ng/service/$timeout) service for more details|

### Returns

A debounced version of the passed function. Any arguments passed to this function will be also passed to the passed function in case this invocation will trigger the function.

The returned function also has a `cancel()` method which can be used in case you what to reset the current debounce state. This will prevent the function from being triggered even after **wait** miliseconds have passed from last input. In case **immediate** is `true`, this means that the next user input will trigger the debounce.

## References

The debounce service is based on the debounce implementation in uderscorejs
* underscorejs: http://underscorejs.org/#debounce
* ngDebounce: https://github.com/shahata/angular-debounce
