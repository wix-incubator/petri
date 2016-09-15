## Wix Angular Storage: Getting Started

### Overview
Wix Angular Storage is the part of wixAngular utility belt that goes with every new wix-angular application. 
It is used for temporary storing data (caching) using browser's `local storage` or wix's `user preferences` service.
Due to multiple subdomains nature of wix, the local storage also supports the write and read operation through out different origins, e.g. `http://www.wix.com` and `http://shoutout.wix.com`.

### Structure
`wixAngularStorage` module consists of 3 services:
* `wixCache` is intended for operations with local storage
* `wixStorage` is intended for operation with user preferences services with optional caching to local storage
* `wixAngularStorageErrors` contains the `enum` of possible reject errors.

### Usage

#### API
This utility is intended for storing for a short period of time relatively small chunks of data, not bigger than 4KB, under provided key, which no longer than 100 chars. It provides the following capabilities:

* `wixCache.set(key, data, [options])`: returns a promise resolved with key
* `wixCache.setWithGUID(data, [options])`: returns a promise that gets resolved with key
* `wixCache.get(key, [options])`: returns a promise that gets resolved with data
* `wixCache.getAll([options])`: returns promise that gets resolved with key/value object of all keys that match given options
* `wixCache.remove(key, [options])`: returns a promise with the removed key

* `wixStorage.set(key, data, [options])`: returns promise
* `wixStorage.get(key, [options])`: returns promise that gets resolved with data
* `wixStorage.getAll([options])`: returns promise that gets resolved with key/value object of all keys that match given options
* `wixStorage.remove(key, [options])`: returns a promise with the removed key

Available options are:

* `noCache [bool]`: defaults to false - allows to get/set remote key without using local cache
* `expiration [number]`: provides TTL of the data in seconds (default for local - 1h, no more than 2 days)
* `siteId [guid]`: allows to save stuff for specific site
* `namespace [string]`: namespace to save the key under. Must provide namespace if did not set namespace on wixStorage and wixCache providers.

In case any promise is rejected, it provides on the following reasons that are defined as constants in `wixAngularStorageErrors`:

* `wixAngularStorageErrors.LOGGED_OUT`: request is performed without logged in user (missing wixSession cookie)
* `wixAngularStorageErrors.NOT_FOUND`: requested record with the given key is not found
* `wixAngularStorageErrors.RUNTIME_EXCEPTION`: thrown error, e.g. exception at `JSON.stringify` on invalid object
* `wixAngularStorageErrors.SERVER_ERROR`: server returned error
* `wixAngularStorageErrors.QUOTA_EXCEEDED`: no available space at local storage

#### Setting namespace on providers (if set on providers, not need to provide inside options)
```js
wixCacheProvider.setNamespace('test-namespace');
wixStorageProvider.setNamespace('test-namespace');
```

#### Example
```js
function myFactory(wixCache, wixAngularStorageErrors) {
  var data, message;
  // example of set
  wixCache.set('myKey', { test: 'mytestdata' }, { 
    expiration: 3, 
    siteId: '1234-1234' 
  });
  
  // example of get
  wixCache.get('myAwesomeKey')
    .then(function (res) {
      data = res;
    })
    .catch(function (reason) {
      // check the reason
      if (reason === wixAngularStorageErrors.NOT_FOUND) {
        message = 'The record is not found';
      } else {
        message = 'WTF?';
      }
    });
}
```
