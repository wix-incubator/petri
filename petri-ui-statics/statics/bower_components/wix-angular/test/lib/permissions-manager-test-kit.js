'use strict';
if (typeof browser !== 'undefined') {
  afterEach(function () {
    browser.removeMockModule('permissionsManagersMock');
  });
}

module.exports = {
  setPermissions: function (permissions, isOwner) {
    browser.addMockModule('permissionsManagersSetPermissionsMock', function (permissions, isOwner) {
      try { angular.module('permissionsManagersSetPermissionsMock', []);} catch (e) {}
      angular.module('permissionsManagersSetPermissionsMock').config(function (permissionsManagerProvider) {
        var permissionsParsed = JSON.parse(permissions);
        console.log('setting permissions to: ', permissionsParsed);
        permissionsManagerProvider.setPermissions(new PermissionsDefinitionBuilder().withPermissions(permissionsParsed).withIsOwner(isOwner).build());
      });
    }, JSON.stringify(permissions), isOwner);
  },
  setAsOwner: function () {
    browser.addMockModule('permissionsManagersSetAsOwnerMock', function () {
      try { angular.module('permissionsManagersSetAsOwnerMock', []);} catch (e) {}
      angular.module('permissionsManagersSetAsOwnerMock').config(function (permissionsManagerProvider) {
        console.log('setAsOwner');
        permissionsManagerProvider.setPermissions(new PermissionsDefinitionBuilder().withPermissions([]).withIsOwner(true).build());
      });
    });
  },
  setContext: function (context) {
    browser.addMockModule('permissionsManagersSetContextMock', function (context) {
      try { angular.module('permissionsManagersSetContextMock', []);} catch (e) {}
      angular.module('permissionsManagersSetContextMock').run(function (permissionsManager) {
        console.log('setting context to: ', context);
        permissionsManager.setContextGetter(function () {
          return context;
        });
      });
    }, context);
  },
  addPermissions: function (context, permissions) {
    browser.addMockModule('permissionsManagersAddPermissionsMock', function (context, permissions) {
      try { angular.module('permissionsManagersAddPermissionsMock', []);} catch (e) {}
      angular.module('permissionsManagersAddPermissionsMock').run(function (permissionsManager) {
        var permissionsArray = JSON.parse(permissions);
        var permissionsMap = {};
        permissionsMap[context] = new PermissionsDefinitionBuilder().withPermissions(permissionsArray).build();
        console.log('setting permissions context map to: ', permissionsMap);
        permissionsManager.assignPermissionsMap(permissionsMap);
      });
    }, context, JSON.stringify(permissions));
  }
};
