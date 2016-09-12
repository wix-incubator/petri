/* global setTimeout */
'use strict';

angular.module('uiPetriMocks', ['ngMockE2E', 'ng', 'uiPetriObjectMocks'])
  .service('mockServer', ["objectMocker", function (objectMocker) {
    function MockServer() {
      var self = this;

      this.resetServer = function () {

        this.objectMocker = objectMocker;

        this.editStatus = function (toReturn) {
          return {
            errorCode: 0,
            errorDescription: 'OK',
            success: true,
            payload: toReturn
          };
        };
        this.deleteSpecs = function () {
          return self.success();
        };
        this.terminateSpecReply = function (specCanBeDeleted) {
          return {
            errorCode: 0,
            errorDescription: 'OK',
            success: true,
            payload: {
              success: true,
              specCanBeDeleted: specCanBeDeleted,
              specKey: 'specs.PublicCacheControl'
            }
          };
        };
        this.exposures = function () {
          return {
            errorCode: 0,
            errorDescription: 'OK',
            success: true,
            payload: [{
              id: 'exp1',
              owner: 'whiz1',
              name: 'whiz kid1',
              description: 'This is my exposure1'
            }, {
              id: 'exp2',
              owner: 'whiz2',
              name: 'whiz kid2',
              description: 'This is my exposure2'
            }, {
              id: 'exp3',
              owner: 'whiz3',
              name: 'whiz kid3',
              description: 'This is my exposure3'
            }]
          };
        };
        this.success = function (data) {
          return {
            errorCode: 200,
            errorDescription: 'OK',
            success: true,
            payload: data
          };
        };
        this.findExperiment = function (id) {

          for (var i = 0; i < this.experiments.length; i++) {
            if (this.experiments[i].id === id) {
              return this.experiments[i];
            }
          }
        };
        this.findExperimentIndex = function (experiment) {
          for (var i = 0; i < this.experiments.length; i++) {
            if (this.experiments[i].id === experiment.id) {
              return i;
            }
          }
        };

        this.findExperimentHistory = function (id) {
          var experiment = this.findExperiment(id);
          return [experiment, experiment, experiment, experiment, experiment, experiment, experiment];
        };

        this.createNewId = function () {
          return (this.experiments.length + 1);
        };
        this.pause = function (id) {
          var experiment = this.findExperiment(id);
          experiment.paused = true;
        };

        this.resume = function (id) {
          var experiment = this.findExperiment(id);
          experiment.paused = false;
        };

        this.stop = function (id) {
          var experiment = this.findExperiment(id);
          experiment.endDate = new Date();
        };
        this.edit = function (experiment) {
          var editableIndex = this.findExperimentIndex(experiment);
          angular.copy(experiment, this.experiments[editableIndex]);
          var state = objectMocker.state(experiment.startDate, experiment.endDate, false);
          experiment.state = state;
        };
        this.save = function (newExperiment) {
          newExperiment.id = this.createNewId();
          var state = objectMocker.state(newExperiment.startDate, newExperiment.endDate, false);
          newExperiment.state = state;
          this.experiments.push(newExperiment);
        };
        var activeExperiment = objectMocker.newExperiment('html-editor', objectMocker.specs['html-editor'][0], objectMocker.dtStart, objectMocker.dtEnd, objectMocker.experimentType(true));
        var activeExperiment1 = objectMocker.newExperiment('html-editor', objectMocker.specs['html-viewer'][0], objectMocker.dtStart, objectMocker.dtEnd, objectMocker.experimentType(true));
        var pausedExperiment = objectMocker.newExperiment('html-editor', objectMocker.specs['html-editor'][0], objectMocker.dtStart, objectMocker.dtEnd, objectMocker.experimentType(true));
        var futureExperiment = objectMocker.newExperiment('html-viewer', objectMocker.specs['html-viewer'][0], objectMocker.dtEnd, objectMocker.dtWithin2Years, objectMocker.experimentType(false));
        var nonClassifiedExperiment = objectMocker.newExperiment('html-viewer', objectMocker.specs['html-viewer'][0], objectMocker.dtEnd, objectMocker.dtWithin2Years, objectMocker.experimentType(false), 'deprecated');

        this.skelaton = objectMocker.newSkelaton();
        this.geo = [{
          id: 'IL',
          text: 'Israel'
        }, {
          id: 'AT',
          text: 'Austria'
        }, {
          id: 'CA',
          text: 'Canada'
        }, {
          id: 'BR',
          text: 'Brazil'
        }];
        this.languages = [{
          id: 'en',
          text: 'English'
        }, {
          id: 'fr',
          text: 'Franche'
        }, {
          id: 'ar',
          text: 'Arabic'
        }];
        this.userAgentRegexes = [
          {
            id: '(.*)Android(.*)',
            text: 'Android Devices All Versions'
          },
          {
            id: '(.*)Chrome(.*)',
            text: 'Chrome Browser All Versions'
          },
          {
            id: '(?i).*(ipad.*fbsn).*',
            text: 'iPad on Facebook WebView in app Browser'
          },
          {
            id: '(?i).*(MSIE\\ \\d+\\.\\d+).*|.*(Trident.*rv[ :]*11\\.).*',
            text: 'Internet Explorer All Versions'
          },
          {
            id: '(?i).*((android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk|mobile|hp-tablet).*',
            text: 'Mobile and Tablet'
          },
          {
            id: null,
            text: null
          },
          {
            id: '(?i)^(1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-).*',
            text: 'Mobile Detect By Model'
          },
          {
            id: '(?i).*(iPad.*Version\\/\\d\\.\\d.*Safari).*',
            text: 'Safari on iPad'
          },
          {
            id: '(?i).*((android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk|mobile|hp-tablet).*',
            text: 'Mobile Detect By Device'
          },
          {
            id: '(?i).*(Linux.*AppleWebKit.*Version\\/\\d+\\.\\d+.*Mobile).*',
            text: 'Android Native Browser'
          },
          {
            id: '(?i).*(MSIE\\ [0-9]\\.\\d+).*',
            text: 'IE9 and older'
          },
          {
            id: '(?i).*((android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows ce|xda|xiino).*',
            text: 'Mobile NO Tablet'
          },
          {
            id: '(?i).*(MSIE\\ [0-8]\\.\\d+).*',
            text: 'IE8 and older'
          }
        ];
        this.defaultUsersFilter = ['AnonymousOnly', 'RegisteredUsersOnly', 'NewUsersOnly'];
        this.excludeUserGroups = ['group1', 'group2'];
        this.experiments = [activeExperiment, activeExperiment1, pausedExperiment, futureExperiment, nonClassifiedExperiment];
        this.specs = objectMocker.specs;
        this.specsList = objectMocker.specsList;
        this.reports = {};

        var reportMetric = {
          experimentId: 5021,
          fiveMinuteCount: 14,
          totalCount: 36,
          lastUpdated: '2015-01-11T15:20:31.140Z',
          reportsPerValue: [{
            experimentValue: 'true',
            fiveMinuteCount: 2,
            totalCount: 6,
            lastUpdated: '2015-01-11T15:20:31.140Z',
            reportsPerServer: [
              {
                serverName: 'app12.aus.wixpress.com',
                fiveMinuteCount: 1,
                totalCount: 3,
                lastUpdated: '2015-01-11T15:20:31.140Z'
              },
              {
                serverName: 'app12.aus.wixpress.com',
                fiveMinuteCount: 1,
                totalCount: 3,
                lastUpdated: '2015-01-11T15:20:31.140Z'
              }
            ]
          },
            {
              experimentValue: 'true',
              fiveMinuteCount: 2,
              totalCount: 6,
              lastUpdated: '2015-01-11T15:20:31.140Z',
              reportsPerServer: [
                {
                  serverName: 'app12.aus.wixpress.com',
                  fiveMinuteCount: 1,
                  totalCount: 3,
                  lastUpdated: '2015-01-11T15:20:31.140Z'
                },
                {
                  serverName: 'app12.aus.wixpress.com',
                  fiveMinuteCount: 1,
                  totalCount: 3,
                  lastUpdated: '2015-01-11T15:20:31.140Z'
                }
              ]
            },
            {
              experimentValue: 'val',
              fiveMinuteCount: 10,
              totalCount: 24,
              lastUpdated: '2015-01-11T15:20:31.140Z',
              reportsPerServer: [
                {
                  serverName: 'app12.aus.wixpress.com',
                  fiveMinuteCount: 1,
                  totalCount: 3,
                  lastUpdated: '2015-01-11T15:20:31.140Z'
                },
                {
                  serverName: 'app13.aus.wixpress.com',
                  fiveMinuteCount: 3,
                  totalCount: 2,
                  lastUpdated: '2015-01-11T15:20:31.140Z'
                },
                {
                  serverName: 'app14.aus.wixpress.com',
                  fiveMinuteCount: 3,
                  totalCount: 12,
                  lastUpdated: '2015-01-11T15:20:31.140Z'
                },
                {
                  serverName: 'app78.aus.wixpress.com',
                  fiveMinuteCount: 3,
                  totalCount: 7,
                  lastUpdated: '2015-01-11T15:20:31.140Z'
                }
              ]
            }]
        };

        this.getReportMatric = function () {
          return {
            errorCode: 0,
            errorDescription: 'OK',
            success: true,
            payload: reportMetric
          };
        };

        this.findSpec = function (key) {
          for (var i = 0; i < objectMocker.specs.length; i++) {
            if (objectMocker.specs[i].key === key) {
              return objectMocker.specs[i];
            }
          }
        };

        this.productmap = objectMocker.specs;
        //        {
        //          htmleditor:[{'key':'spec1','groups':[objectMocker.createTestGroup(0,'red',0),objectMocker.createTestGroup(0,'blue',0)]}],
        //          viewer:[{'key':'spec2','groups':[objectMocker.createTestGroup(0,'new',0),objectMocker.createTestGroup(0,'old',0)]}]
        //        };

      };
      this.resetServer();
    }
    return new MockServer(1000);
  }])
  .config(["$provide", function ($provide) {
    //simulate network delay
    $provide.decorator('$httpBackend', ['$delegate', function ($delegate) {
      var proxy = function (method, url, data, callback, headers) {
        var interceptor = function () {
          var _this = this,
            _arguments = arguments;
          setTimeout(function () {
            callback.apply(_this, _arguments);
          }, 10);
        };
        return $delegate.call(this, method, url, data, interceptor, headers);
      };
      for (var key in $delegate) {
        proxy[key] = $delegate[key];
      }
      return proxy;
    }]);
  }])
  .run(["$httpBackend", "mockServer", "$log", "_", function ($httpBackend, mockServer, $log, _) {

    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/experiments\/editStatus/).respond(function () {
      return [200, mockServer.editStatus(true)];
    });

    $httpBackend.whenGET(/.*\/wix-petri-webapp\/auth\/isAuthenticated/).respond(200, mockServer.success({
      login: true,
      user: 'avgarm@wix.com'
    }));

    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/ExperimentSkeleton/).respond(200, mockServer.success(mockServer.skelaton));
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/Experiments\/report/).respond(function () {
      return [200, mockServer.getReportMatric()];
    });

    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/Experiments/).respond(function () {
      var all = mockServer.experiments;
      _.each(all, function (e) {
        e.state = mockServer.objectMocker.state(e.startDate, e.endDate, e.state, e.paused);
        e.usersFilterOptions = mockServer.objectMocker.usersFilterOptions(e.key);
      });
      return [200, mockServer.success(mockServer.experiments)];
    });

    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/geo/).respond(200, mockServer.success(mockServer.geo));
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/userGroups/).respond(200, mockServer.success(mockServer.excludeUserGroups));
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/languages/).respond(200, mockServer.success(mockServer.languages));
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/productmap/).respond(200, mockServer.success(mockServer.productmap));
    //200,mockServer.success(mockServer.experiments));
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/Experiment\/\d+\s*$/).respond(function (method, url) {
      var arr = url.match(/\d/g);
      var id = parseInt(arr[arr.length - 1], 10);
      var experiment = mockServer.findExperiment(id);
      return [200, mockServer.success(experiment)];
    });
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/Experiment\/History\/\d+\s*$/).respond(function (method, url) {
      var arr = url.match(/\d/g);
      var id = parseInt(arr[arr.length - 1], 10);
      var experiments = mockServer.findExperimentHistory(id);
      return [200, mockServer.success(experiments)];
    });
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/Spec\/.+\s*$/).respond(function (method, url) {
      var key = url.substring(url.lastIndexOf('/') + 1);
      var spec = mockServer.findSpec(key);
      return [200, mockServer.success(spec)];
    });

    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/Specs/).respond(200, mockServer.success(mockServer.specsList));
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/exposures/).respond(function () {
      return [200, mockServer.exposures()];
    });
    $httpBackend.whenPOST(/.*\/wix-petri-webapp\/v1\/specExposure/).respond(function (method, url, data) {
      return [200, mockServer.success(angular.fromJson(data).exposureId)];
    });
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/reports\/\d+$/).respond(200, mockServer.success(mockServer.reports));
    $httpBackend.whenGET(/.*\/wix-petri-webapp\/v1\/userAgentRegexes/).respond(200, mockServer.success(mockServer.userAgentRegexes));
    $httpBackend.whenPOST(/.*\/wix-petri-webapp\/v1\/Experiments/).respond(function (method, url, data) {
      $log.log('new experiment :', method, url, data);
      mockServer.save(angular.fromJson(data));
      return [200, mockServer.success(undefined)];
    });

    $httpBackend.whenPUT(/.*\/wix-petri-webapp\/v1\/Experiment\/\d+\s*$/).respond(function (method, url, data) {
      $log.log('update experiment :', method, url, data);
      mockServer.edit(angular.fromJson(data));
      return [200, mockServer.success(undefined)];
    });

    $httpBackend.whenPOST(/.*\/wix-petri-webapp\/v1\/Experiment\/\d+\/terminate\s*$/).respond(function (method, url, data) {
      $log.log('terminate experiment :', method, url, data);
      var arr = url.split('/');
      mockServer.stop(parseInt(arr[arr.length - 2], 10));
      return [200, mockServer.terminateSpecReply(true)];
    });

    $httpBackend.whenPOST(/.*\/wix-petri-webapp\/v1\/Experiment\/\d+\/pause\s*$/).respond(function (method, url, data) {
      $log.log('pause experiment :', method, url, data);
      var arr = url.split('/');
      mockServer.pause(parseInt(arr[arr.length - 2], 10));
      return [200, mockServer.success(undefined)];
    });
    $httpBackend.whenPOST(/.*\/wix-petri-webapp\/v1\/Experiment\/\d+\/resume\s*$/).respond(function (method, url, data) {
      $log.log('resume experiment :', method, url, data);
      var arr = url.split('/');
      mockServer.resume(parseInt(arr[arr.length - 2], 10));
      return [200, mockServer.success(undefined)];
    });
    $httpBackend.whenGET(/.*\.html$/).passThrough();
    //    var test = $httpBackend.whenGET(/.*/);
    //    if (angular.isfunction (test.passThrough)) {
    //      test.passThrough();
    //    }
    $httpBackend.whenPOST(/.*\/wix-petri-webapp\/v1\/deleteSpecs/).respond(function () {
      return [200, mockServer.deleteSpecs()];
    });

  }]);

'use strict';
angular.module('uiPetriObjectMocks', ['ng', 'uiPetriServices'])
  .factory('objectMocker', ['Experiment', 'ExperimentDataType', function (Experiment, ExperimentDataType) {
    var mocker = {};

    mocker.experimentType = ExperimentDataType.experimentType;
    mocker.experimentState = ExperimentDataType.experimentState;
    mocker.experimentActions = ExperimentDataType.actions;

    mocker.createTestGroup = function (id, value, chunk) {
      return {
        id: id,
        chunk: chunk,
        value: value,
        isDefault: true,
        lastUpdate: new Date(0)
      };
    };
    mocker.dtStart = new Date().getTime() - 100;
    mocker.dtEnd = new Date(new Date().getTime() + 365 * 24 * 60 * 60 * 1000).getTime();
    mocker.dtWithin2Years = new Date(new Date().getTime() + 365 * 24 * 60 * 60 * 2000).getTime();

    var hourFromNow = new Date((new Date()).getTime() + 1000 * 60 * 60).getTime();
    mocker.specsList = [
      {key: 'specs.PublicCacheControl', testGroups: ['0', '3600'], canBeDeleted: true, owner: '', creationDate: 1390469370298, lastUpdateDate: 1390469370298, scopes: ['public']},
      {key: 'specs.PublicExperimentProcessorEnabler', testGroups: ['on', 'off'], canBeDeleted: true, owner: '', creationDate: 1393344635053, lastUpdateDate: 1393344635053, scopes : ['public']}];
    mocker.specs = {
      'html-editor': [{
        forRegisteredUsers: true,
        scopes: ['html-editor'],
        key: 'spec1',
        exposureId: 'exp1',
        groups: [mocker.createTestGroup(0, 'red', 0), mocker.createTestGroup(0, 'blue', 0)],
        startDate: hourFromNow,
        filterTemplate: [{
          filterName: 'users',
          enabled: true,
          mandatoryValue: [],
          optionalValue: ['New Registered Users', 'All Registered Users', 'Specific Users Only']
        }, {
          filterName: 'guids',
          enabled: true,
          mandatoryValue: [],
          optionalValue: ['exclude guids']
        }, {
          filterName: 'mainFilter',
          enabled: true,
          mandatoryValue: [],
          optionalValue: ['Wix Users Only', 'Filters', 'Open To All']
        }]
      }],
      'html-viewer': [{
        forRegisteredUsers: false,
        scopes: ['html-viewer'],
        key: 'spec2',
        exposureId: 'exp2',
        groups: [mocker.createTestGroup(0, 'old', 0), mocker.createTestGroup(0, 'new', 0)],
        startDate: -1,
        filterTemplate: [{
          filterName: 'guids',
          enabled: false,
          mandatoryValue: [],
          optionalValue: []
        }, {
          filterName: 'users',
          enabled: true,
          mandatoryValue: [],
          optionalValue: ['None', 'First Time Anonymous Visitors']
        }, {
          filterName: 'wixUsers',
          enabled: false,
          mandatoryValue: ['false'],
          optionalValue: []
        }, {
          filterName: 'mainFilter',
          enabled: true,
          mandatoryValue: [],
          optionalValue: ['Filters', 'Open To All']
        }]
      }],
      publicUrl: [{
        key: 'N/A',
        exposureId: 'exp3',
        groups: [],
        startDate: -1,
        filterTemplate: [{
          filterName: 'guids',
          enabled: false,
          mandatoryValue: [],
          optionalValue: []
        }, {
          filterName: 'users',
          enabled: true,
          mandatoryValue: [],
          optionalValue: ['None', 'First Time Anonymous Visitors']
        }, {
          filterName: 'wixUsers',
          enabled: false,
          mandatoryValue: ['false'],
          optionalValue: []
        }, {
          filterName: 'mainFilter',
          enabled: true,
          mandatoryValue: [],
          optionalValue: ['Filters', 'Open To All']
        }]
      }]
    };

    mocker.now = new Date().getTime();

    mocker.newSkelaton = function () {
      return {'name': '',
        'type': 'featureToggle',
        'creator': '',
        'scope': '',
        'state': '',
        'id': 0,
        'lastUpdated': 0,
        'key': '',
        'specKey': true,
        'creationDate': 0,
        'description': '',
        'updater': '',
        'comment': '',
        'startDate': 0,
        'endDate': 0,
        'paused': false,
        'groups': [],
        'editable': true,
        'wixUsers': false,
        'allRegistered': false,
        'newRegistered': false,
        'nonRegistered': false,
        'anonymous': false,
        'excludeGeo': false,
        'geo': [],
        'languages': [],
        'hosts': [],
        'includeGuids': [],
        'excludeGuids': [],
        'parentStartTime': -1,
        'excludeUserGroups': [],
        'includeUserAgentRegexes': [],
        'excludeUserAgentRegexes': [],
        'originalId': 0,
        'linkId': 0,
        'metaSiteIds': []
      };
    };
    mocker.usersFilterOptions = function (specKey) {
      var usersFilter;
      _.each(mocker.specs, function (value) {

        for (var i = 0; i < value.length; i++) {
          if (value[i].key === specKey) {
            usersFilter = value[i].usersFilter;
          }
        }
      });
      return usersFilter;
    };

    mocker.state = function (dtStart, dtEnd, type, isPaused) {

      var now = new Date();
      now = now.getTime();
      if (dtEnd <= now) {
        return mocker.experimentState.StateEnded;
      }
      if (isPaused) {
        return mocker.experimentState.StatePaused;
      }
      if (now <= dtStart) {
        return mocker.experimentState.StateFuture;
      }
      if (dtStart <= now && now <= dtEnd) {
        return mocker.experimentState.StateActive;
      }
      return mocker.experimentState.StateUnknown;
    };

    //['AnonymousOnly','RegisteredUsersOnly','NewUsersOnly'];
    mocker.id = 1;
    mocker.newExperiment = function (scope, spec, dtStart, dtEnd, type, specKey) {

      var state1 = mocker.state(dtStart, dtEnd, false);

      var groups1 = (function (type) {
        var groups = [];
        var i = 0;
        if (type === ExperimentDataType.experimentType(true)) {
          for (i = 0; i < spec.groups.length; i++) {
            groups.push(mocker.createTestGroup(i, spec.groups[i].value, 0));
          }
          groups[0].chunk = 100;
        } else {
          for (i = 0; i < spec.groups.length; i++) {
            groups.push(mocker.createTestGroup(i, spec.groups[i].value, 100 / spec.groups.length));
          }
        }
        return groups;
      })(type);

      var id = mocker.id++;
      var experiment = mocker.newSkelaton();
      experiment.id = id;
      experiment.linkId = 0;
      experiment.originalId = id;
      experiment.name = 'long name for experiment is experiment' + id;
      experiment.type = type;
      experiment.info = Experiment.info(groups1);
      experiment.state = state1;
      experiment.description = 'created with state :' + state1 + ' (tail text to make it long long, very long, dummy text to make it even longer, to span on 3 lines)';
      experiment.key = specKey === undefined ? spec.key : specKey; //to simulate deprocated spec
      experiment.startDate = dtStart;
      experiment.endDate = dtEnd;
      experiment.groups = groups1;
      experiment.openToAll = false;
      experiment.wixUsers = false;
      experiment.scope = scope;
      experiment.parentStartTime = -1;
      experiment.usersFilterOptions = spec.usersFilter;
      experiment.creator = 'talyag@wix.com';
      experiment.updater = 'dalias@wix.com';
      return experiment;
    };
    return mocker;

  }]);
