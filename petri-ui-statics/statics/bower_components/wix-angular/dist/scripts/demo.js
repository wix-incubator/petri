"use strict";

angular.module("wixAngularDemoApp", [ "wixAngular", "ngAnimate" ]).config([ "experimentManagerProvider", function(experimentManagerProvider) {
    experimentManagerProvider.setExperiments({
        "active-experiment": "true",
        "background-color": "blue"
    });
} ]).run([ "experimentManager", function(experimentManager) {
    experimentManager.loadScope("shir");
    experimentManager.loadExperiment("sushi");
} ]);

var WixAngularDemoHeightController = function() {
    function WixAngularDemoHeightController() {
        this.showContent = false;
    }
    WixAngularDemoHeightController.prototype.reportHeightChanged = function(height) {
        this.height = height;
    };
    return WixAngularDemoHeightController;
}();

angular.module("wixAngularDemoApp").controller("wixAngularDemoHeightController", WixAngularDemoHeightController);