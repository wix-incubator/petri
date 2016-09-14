# wix-angular

An helper library for wix-angular applications

## Installation

No installation needed in case you scaffold using `yo wix-angular`

Otherwise - `bower install --save wix-angular`

## Usage

**Note:** If you generated your project using wix-angular generator, you already have all of this.

In your .html file add the following script tags:

```html
<script src="bower_components/uri.js/src/URI.js"></script>
<script src="bower_components/wix-angular/dist/wix-angular.js"></script>
```

Add `wixAngular` as module dependency for your app: 

```js
angular.module('myApp', [..., 'wixAngular']);
```

## What you get?

 * [wixAngularInterceptor](docs/wix-angular-interceptor.md)
 * [wixAngularStorage](docs/wix-angular-storage.md)
 * [translation utils](docs/translation-utils.md)
 * [wixAngularExperiments](docs/wix-angular-experiments.md)
 * [wixAngularPermissions](docs/wix-angular-permissions.md)
 * [wixAngularInterceptor](docs/wix-mail-validator.md)
 * [wixDebounce](docs/wix-debounce.md)
 * [wixHeightChangedReporter and wixTpaHeightWatcher](docs/wix-height-reporter-and-watcher.md)
 * [Components test kit](docs/components-test-kit.md)
