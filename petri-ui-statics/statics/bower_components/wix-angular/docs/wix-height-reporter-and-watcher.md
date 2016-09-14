#Wix Height Reporter and Watcher

## What it does

Provides two directives and a service that allows an application to implement a behaviour for document height changes.
It is needed when the angular application is contained within some external container and should report height changes since it does not control the document height (e.g. in an iframe).
The Wix viewer and editor for example are wrapping the TPA in an iFrame and expects the TPA to report height changes using SDK method (Wix.setHeight()).

* wixHeightWatcher - a service that allows subscribing for height changes, a callback is provided in order to define what method should be invoked when the height is changed (invoked with the new height value)
* wixTpaHeightChanged - a directive that implements a default listener for the above service, and informs the Wix SDK about the height changes (invokes **Wix.setHeight()** method) - useful for page typed TPAs that should inform the Wix Editor/Wix Viewer about page height changes
* wixHeightChanged - a directive that allows a custom callback implementation in case the TPA implementation is not enough or when used with a different container

**Important:** wixTpaHeightChanged requires Wix SDK to be loaded (not included in wixAngular), see [Wix SDK](http://dev.wix.com/docs/sdk/introduction) for installation details
**Important 2:** The directives will no longer listen to height changes when their scope is destroyed, so they should **not** be used on HTML elements which are later removed by the application (unless removing them means that height changed subscription is no longer needed)

## Directives Usage
Make sure wixAngular dependency is included in your app
```javascript
angular.module('myApp', ['wixAngular']);
```

In your main html template:
```html
<div wix-height-changed="callback(height)" />
```
Or in case default behavior is needed (TPA):
```html
<div wix-tpa-height-changed />
```

## Service Usage
A listener component can be implemented by using the service:
```javascript
   var unsubscribeFunc = wixHeightWatcher.subscribe(function (height) {
    //use height
   });

   //stop listening to height changes
   unsubscribeFunc();
```

### wixHeightWatcher Service

#### subscribe
|Param|Type|Details
|---|---|---|
|func|function|Function which will be invoked with height (number) argument when height is modified|

##### Returns
A function which un-subscribe from height changed events (useful for directive destroy method for example)

### Directives' arguments

#### wixTpaHeightChanged
|Param|Type|Details|
|---|---|---|
|wix-tpa-height-changed|N/A|The directive that will trigger height change listener which will use Wix SDK [setHeight()](http://dev.wix.com/docs/sdk/wix#setheight) in order to inform the viewer/editor that the window size should be changed|

#### wixHeightChanged
|Param|Type|Details|
|---|---|---|
|wix-height-changed|callback|The callback that will be called when height is modified (can specify `height` argument which will contain the new height)|


### Notes
* Height change is only reported if a digest cycle is involved, if the document height change is caused by a non AngularJS component, the height change will only be reported on the next digest cycle
* The height calculation and update will not be called more than once in every 50 milliseconds (last height update is reported), this is achieved by using [wixDebounce](docs/wix-debounce.md) service,

### Refernce
* Wix SDK setHeight - http://dev.wix.com/docs/sdk/wix#setheight
