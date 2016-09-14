"use strict";

angular.module("wixAngularAppMocks", [ "ngMockE2E" ]).run([ "$httpBackend", function($httpBackend) {
    var keyStorage = {};
    var siteStorage = {};
    var getKeyRegExp = /^\/_api\/wix-user-preferences-webapp\/getVolatilePrefForKey\/wixAngularStorage\/([^\/]*)$/;
    var getSiteRegExp = /^\/_api\/wix-user-preferences-webapp\/getVolatilePrefForSite\/wixAngularStorage\/([^\/]*)\/([^\/]*)$/;
    function addToStorage(key, value, siteId) {
        keyStorage[key] = value;
        if (siteId) {
            if (!siteStorage[siteId]) {
                siteStorage[siteId] = {};
            }
            siteStorage[siteId][key] = value;
        }
    }
    $httpBackend.whenPOST("/_api/wix-user-preferences-webapp/set").respond(function(method, url, data) {
        var response = {};
        var payload = JSON.parse(data);
        var key = payload.key;
        var value = payload.blob;
        var siteId = payload.siteId;
        addToStorage(key, value, siteId);
        response[key] = value;
        return [ 200, response, {} ];
    });
    $httpBackend.whenGET(getKeyRegExp).respond(function(method, url) {
        var key = url.match(getKeyRegExp)[1];
        var response = {};
        if (key in keyStorage) {
            response[key] = keyStorage[key];
            return [ 200, response, {} ];
        } else {
            return [ 404, {}, {} ];
        }
    });
    $httpBackend.whenGET(getSiteRegExp).respond(function(method, url) {
        var tokens = url.match(getSiteRegExp);
        var siteId = tokens[1];
        var key = tokens[2];
        var response = {};
        if (siteStorage[siteId] && key in siteStorage[siteId]) {
            response[key] = siteStorage[siteId][key];
            return [ 200, response, {} ];
        } else {
            return [ 404, {}, {} ];
        }
    });
    $httpBackend.whenGET(/^\/_api\/wix-laboratory-server\/laboratory\/conductExperiment\/.*/).respond("");
    $httpBackend.whenGET(/^\/_api\/wix-laboratory-server\/laboratory\/conductAllInScope\/.*/).respond({});
    $httpBackend.whenGET(/.*/).passThrough();
    $httpBackend.whenPOST(/.*/).passThrough();
    $httpBackend.whenPUT(/.*/).passThrough();
    $httpBackend.whenDELETE(/.*/).passThrough();
} ]).factory("wixCookies", function() {
    return {
        userGUID: "1234",
        userName: "loggedInUser"
    };
});