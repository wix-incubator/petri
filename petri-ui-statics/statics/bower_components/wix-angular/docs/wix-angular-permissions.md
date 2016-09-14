## Wix Angular Permissions
==========================

1. [Overview](#overview)
2. [Installation](#installation)
3. [BI Integration](#bi-integration)
4. [API](#api)
5. [Directives](#directives)
6. [Filters](#filters)
7. [Usage](#usage)
8. [Test-Kit](#test-kit)

## Overview

Manage Your Permissions!
`wixAngularPermissions` module consists of:
* `permissionsManagerProvider` + `permissionsManager`
* `wixPermissionIf`: “ngIf based” directive to show/hide elements in according to whether or not the have the right permission.
* `wixPermissionDisabled`: “ngDisabled based” directive to enable/disable elements in according to permission.
* `wixPermissionClass`: add classes to your choice by permissions values.
* 'isPermitted': a filter. If no permissions it will pass the fallback text. Otherwise will pass the original input.

## Installation
So you read about wix angular permissions and you are psyched!!!
You want to use it but how??

This is where this guide comes handy, so let's start.

The first thing you need to do is to ask your fellow server developer to inject a new parameter into your index.vm named "authorizationInfo". You can ask him to read all about it [here](https://github.com/wix/authorization#frontend-integration).

Then all you need to do is add the following script to your index.vm and your'e all set.

So again, the steps to install and start using the permissionsManager are:

1. Make sure you have authorizationInfo object from the server ([read more here](https://github.com/wix/authorization#frontend-integration))
2. Add the following script to your vm file/s

```html
<script>
  angular.module('myApp').config(function (permissionsManagerProvider) {
    permissionsManagerProvider.setPermissions(${authorizationInfo});
  });
</script>
```
## BI Integration
wixBiLogger users:

Ready for the good news? Once you're done with the [installation](installation) part, you inadvertently did the bi integration as well. Now on each bi request sent by the wixBiLogger two extra parameters will be sent as well (ownerId and roles)

Non wixBiLogger users:

use wixBiLogger.

## API

* `permissionsManager.get(value:string):string`:
        Returns a string
* `permissionsManager.contains(value:string):boolean`:
        Returns true/false if the user has the permission or not
* `permissionsManager.getSiteToken():string`:
        Returns an JWT (json web token) of the metasite & userId (in use when opening the media manager for instance)
* `permissionsManager.loadScope(scope)`:
        Not implemented yet

## Directives

```html
<div wix-permission-if=”NAME-OF-CONSTANT or NAME-OF-PERMISSION”></div> 
For example:
 <div wix-permission-if=”myConstName”></div> //the value of the constant should equal to the permission name
 <div wix-permission-if=”nameOfPermission”></div>
<button wix-permission-disabled=”NAME-OF-CONSTANT or NAME-OF-PERMISSION” wix-permission-context="context"></button>
<div wix-permission-class="permissionUserHave" permission-values="{true: \'small\', false: \'big\'}"></div>
```

`wix-permission-if` && `wix-permission-disabled` supports negation:  
```html
<div wix-permission-if=”!NAME-OF-CONSTANT or !NAME-OF-PERMISSION”></div>
<div wix-permission-disabled=”!NAME-OF-CONSTANT or !NAME-OF-PERMISSION”></div>
```
Inner div data is shown when `NAME-OF-PERMISSION` is false or does not exists (undefined). Similar to javascript negation.
You can use as many of negation sign you'd like

## Filters

```html
<a ng-href={{url|isPermitted:'NAME-OF-CONSTANT or NAME-OF-PERMISSION':'FALLBACK TEXT': permissionContext(Optional)}}></a>
For example:
  <a ng-href={{url|isPermitted:'myConstName':''}}></a>
  <a ng-href={{url|isPermitted:'myConstName':'':metasiteId}}></a>
  <a ng-href={{url|isPermitted:'nameOfPermission':''}}></a>
``` 

`isPermitted` support negation:
```html
<a ng-href={{url|isPermitted:'!NAME-OF-CONSTANT or !NAME-OF-PERMISSION':'FALLBACK TEXT'}}></a>
```

## Usage

## Test-Kit

#### UT

For unit testing you should just use the real permissions manager.

* include wix-angular in your karma.conf.js
```js
'app/bower_components/wix-angular/dist/wix-angular.js'
```
* Load wixAngularPermissions module in your tests
```js
module('wixAngularPermissions');
```
* Set your permissions as necessary
```js
function setPermissions(permissionsDefinition: PermissionsDefinition) {
  module((permissionsManagerProvider:PermissionsManagerProvider) => {
    permissionsManagerProvider.setPermissions(permissionsDefinition);
  });
}
```

#### E2E

```haml
%button.rename-btn(wix-permission-if="rename") This button will be visible in case the user has 'rename' permission
%button.edit-btn(wix-permission-if="edit") This button will be visible in case the user has 'edit' permission
%button.copy-btn(wix-permission-disabled="copy") This button will be disabled in case the user doesn't have 'copy' permission
.permissions-classes(wix-permission-class="rename" permission-values="{true: 'rename-class', false:'some-other-class'}") rename
.permissions-classes(wix-permission-class="somePermissionUserDontHave" permission-values="{false: 'yoba'}") rename
```

```js
  var permissionsManager = require('../../../app/bower_components/wix-angular/test/lib/permissions-manager-test-kit');

  beforeEach(function () {
    permissionsManager.setAsOwner();
  });
  
  //DON'T FORGET TO ADD THIS CODE AS WELL
  afterEach(function () {
    browser.clearMockModules();
  });

  it('should hide/show elements with wix-permission-if', function () {
    permissionsManager.setPermissions(['rename'], false);
    page.navigate();

    expect(page.renameButton.isDisplayed()).toBe(true);
    expect(page.editButton.isPresent()).toBe(false);
  });
  
  it('should disable links / buttons with wix-permission-disable', function () {
    permissionsManager.setPermissions(['copy', 'rename'], false);
    page.navigate();

    expect(page.renameButton.getAttribute('disabled')).toBe(null);
    expect(page.copyButton.getAttribute('disabled')).toBe('true');
  });

  it('should add class with wix-permission-class', function () {
    permissionsManager.setPermissions(['rename'], false);
    page.navigate();

    expect(page.permissionsClasses).toHaveClass('rename-class');
    expect(page.permissionsClasses).not.toHaveClass('edit-class');
  });
```
