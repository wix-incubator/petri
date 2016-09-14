# AngularJS NewRelic API

- [Report errors to new relic](#report-errors-to-new-relic).
- [Report the real application loading time](#report-the-real-application-loading-time).
- [Tag a session with extra data](#tag-a-session-with-extra-data).
- [Add a custom page action](#add-a-custom-page-action).
- [Report ui.router state changes timing](#report-uirouter-state-changes-timing).
- [Recommendations](#recommendations).
- You can take a look at the [wiki](https://github.com/wix/new-relic-angular-client/wiki)! There is a lot more to know.

Using NewRelic **[JavaScript API](https://docs.newrelic.com/docs/browser/new-relic-browser/browser-agent-apis/reporting-data-events-browser-agent-api#methods)**

# Requirements
- [AngularJS](https://angularjs.org/) version 1.x.x.
- [NewRelic browser monitoring](http://newrelic.com/browser-monitoring) installed.

# Usage
[bower](http://bower.io/) for dependency management.  Install and save to bower.json by running:
```
$ bower install new-relic-angular-client --save
```
First, Add the module to your application:
```javascript
<script src="bower_components/new-relic-angular-client/dist/nr-ng-client.min.js"></script>
var myAppModule = angular.module('MyApp', ['nr-ng-client']);
```
- In order to report ui.router state changes timing, you need to have **[angular-ui/ui-router](https://github.com/angular-ui/ui-router)** installed. 

## Report errors to new relic
- The library decorate the **[$exceptionHandler](https://docs.angularjs.org/api/ng/service/$exceptionHandler)** in order to report angular errors to new relic.
- The library will not decorate $exceptionHandler if newrelic is not installed, or disabled.
- To disable the automatic errors report:
```javascript
angular.module('myApp')
  .config(function (nrNgClientProvider) {
    nrNgClientProvider.reportErrorsEnabled(false);
  });
```
- To report errors manually:
```javascript
angular.module('myApp')
  // report in the config phase
  .config(function (nrNgClientProvider) {
    nrNgClientProvider.reportError(new Error('something when wrong!'));
  })
  // report in the run phase
  .run(function (nrNgClient) {
    nrNgClient.reportError(new Error('something when wrong!'));
  });
```

## Report the real application loading time
- The library waits for no outstanding requests and reports the finished event.
- To disable the automatic finished report:
```javascript
angular.module('myApp')
  .config(function (nrNgClientProvider) {
    nrNgClientProvider.reportFinishedEnabled(false);
  });
```
- To report the finished event manually:
```javascript
angular.module('myApp')
  .run(function (loadService, nrNgClient) {
    loadService.load().then(function () {
      nrNgClient.reportFinishLoading();
    });
  });
```

## Tag a session with extra data
- To add tags to identify the session (e.g. app version):
```javascript
angular.module('myApp')
  .config(function (nrNgClientProvider) {
    nrNgClientProvider.tag('version', 'v1.0.0');
  });
```

## Add a custom page action
- To add a page action (for example user clicked on 'buy' button):
```javascript
angular.module('myApp')
  .run(function (nrNgClient) {
    nrNgClient.addPageAction('buy clicked', {product: 'beautiful product'});
  });
```

## Report ui.router state changes timing
- The library checks if angular-ui/ui-router installed.
- Tracks ui.router state changes and report timing to new relic Browser and Insights.
- The extra column in Insights in "duration". The Value is in seconds.
- The default threshold to report is 300 milliseconds.
- To change the threshold:
```javascript
angular.module('myApp')
  .config(function (nrNgClientProvider) {
    nrNgClientProvider.threshold(500); // custom number value
  });
```
- To disable the state loaded auto report:
```javascript
angular.module('myApp')
  .config(function (nrNgClientProvider) {
    nrNgClientProvider.stateChangedReportEnabled(false);
  });
```
- To report state loaded manually:
```javascript
angular.module('myApp')
  .controller('someCtrl', function (loadService, nrNgClient) {
    loadService.load().then(function () {
      nrNgClient.reportStateDataLoaded();
    });
  });
```
# Recommendations
- Include the following script after the  new relic startup script inside the `<head>`.
```javascript
<script>
!function(n){var o=window.onerror;window.onerror=function(r){n.addPageAction("error",{message:r}),o&&o.apply(window,arguments)}}(NREUM);
</script>
```
The script catchs errors that happened outside the angular "world" and report them also to new relic Insights using the window.onerror api.
