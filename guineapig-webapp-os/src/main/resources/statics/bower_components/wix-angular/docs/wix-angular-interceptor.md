#Wix Angular Interceptor

## How to add it?

Add configuration block for `wixAngular` in your .vm file (optional - by default it will be the URL in your base tag):

```html
<script>
  angular.module('myApp').config(function (wixAngularProvider) {
    wixAngularTopologyProvider.setStaticsUrl('${staticsUrl}');
  });
</script>
```

## What you get?

Well, mainly you get stuff to work around CORS issues and the fact static files are downloaded from CDN.
Although all scripts and styles are downloaded from the CDN, we still want to download HTML partials
from a /_partials mapping on the same origin.
On the other hand, we want IMG tags to download from the CDN while not having to use an absolute URL.

So, the following cool things will solve most of those issues:

## Navigation Utils:

 1. If you set `templateUrl` or use `ngInclude` or whatever using `views/my-view.html`,
 the wix-angular interceptor will make sure the request goes to the partials url (calculated from the statics url you set)
 so you can keep using `views/my-view.html` and it will just work.
 2. Have an image you want you want to be downloaded from the static url you defined?
 Instead of `ngSrc` use the `relativeSrc` directive. You can use `<img relative-src="images/my-image.png">`
 and it will be downloaded from the statics url you set.

## WixResult handling:

 When Wix servers return a response to some api call, the HTTP status code is 200 even if the response is an error.
 This is the source of really ugly code that needs to handle a communication error and an application error differently.
 Since `WixResult` is a wrapper, the actual response is instead stored inside the `payload` field of the response.

 The wix-angular interceptor detects errors in `WixResult` and makes sure they will be treated as errors by `$http`
 and that you won't ever need to handle errors in a `success` callback again :).
 Also, if the `WixResult` is successful, only the `payload` field contents will be sent to your callback
 so you won't need to repeat the pattern of stripping it away everywhere.
