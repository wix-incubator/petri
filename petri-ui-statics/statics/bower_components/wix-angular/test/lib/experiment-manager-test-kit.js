'use strict';

if (typeof browser !== 'undefined') {
  afterEach(function () {
    browser.removeMockModule('experimentManagerMock');
  });
}

module.exports = {
  setExperiments: function (experiments) {
    browser.addMockModule('experimentManagerMock', function (experiments) {
      angular.module('experimentManagerMock').config(function (experimentManagerProvider) {
        experimentManagerProvider.setExperiments(JSON.parse(experiments));
      });
    }, JSON.stringify(experiments));
  },
  setScopeExperiments: function (scope, experiments) {
    browser.addMockModule('experimentManagerMock', function (scope, experiments) {
      angular.module('experimentManagerMock').run(function (experimentManager) {
        experimentManager.setScopeExperiments(scope, JSON.parse(experiments));
      });
    }, scope, JSON.stringify(experiments));
  },
  clearExperiments: function () {
    browser.addMockModule('experimentManagerMock', function () {
      angular.module('experimentManagerMock').config(function (experimentManagerProvider) {
        experimentManagerProvider.clearExperiments();
      });
    });
  }
};
