/*!
 * new-relic-angular-client
 * https://github.com/wix/new-relic-angular-client
 * Created: 2016-04-03T09:01:19.042Z
 * License: 
 */


var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
angular
    .module('nr-ng-client', [])
    .run(["nrNgClient", "nrNgUiRouter", function (nrNgClient, nrNgUiRouter) {
    //
}]);
var relic;
(function (relic) {
    var NrNgClient = (function () {
        function NrNgClient() {
            this.newrelic = window.newrelic;
        }
        NrNgClient.prototype.tag = function (name, value) {
            if (this.newrelic) {
                this.newrelic.setCustomAttribute(name, value);
            }
        };
        NrNgClient.prototype.addPageAction = function (name, values) {
            if (this.newrelic) {
                this.newrelic.addPageAction(name, values);
            }
        };
        NrNgClient.prototype.reportError = function (exception, cause) {
            if (this.newrelic) {
                try {
                    throw typeof exception === 'string' ? new Error(exception) : exception;
                }
                catch (e) {
                    this.newrelic.noticeError(e);
                    if (typeof this.newrelic.addPageAction === 'function') {
                        this.newrelic.addPageAction('error', {
                            href: window.location.href,
                            message: e.message,
                            stack: e.stack,
                            cause: cause || ''
                        });
                    }
                }
            }
        };
        NrNgClient.prototype.reportFinishLoading = function () {
            if (this.newrelic) {
                this.newrelic.finished();
            }
        };
        return NrNgClient;
    }());
    relic.NrNgClient = NrNgClient;
    var NrNgClientProvider = (function (_super) {
        __extends(NrNgClientProvider, _super);
        function NrNgClientProvider() {
            _super.apply(this, arguments);
            this.config = {
                reportError: true,
                reportFinish: true
            };
        }
        NrNgClientProvider.prototype.reportFinishedEnabled = function (value) {
            this.config.reportFinish = value;
        };
        NrNgClientProvider.prototype.reportErrorsEnabled = function (value) {
            this.config.reportError = value;
        };
        NrNgClientProvider.prototype.decorateExceptionHandler = function ($provide) {
            var _this = this;
            if (this.newrelic && this.config.reportError) {
                /* @ngInject */
                $provide.decorator('$exceptionHandler', ["$delegate", function ($delegate) {
                    return function (exception, cause) {
                        _this.reportError(exception, cause);
                        $delegate(exception, cause);
                    };
                }]);
            }
        };
        /* @ngInject */
        NrNgClientProvider.prototype.$get = function ($browser, $timeout) {
            var _this = this;
            if (this.config.reportFinish) {
                $timeout(function () {
                    $browser.notifyWhenNoOutstandingRequests(function () {
                        _this.reportFinishLoading();
                    });
                }, 0, false);
            }
            return new NrNgClient();
        };
        NrNgClientProvider.prototype.$get.$inject = ["$browser", "$timeout"];
        return NrNgClientProvider;
    }(NrNgClient));
    relic.NrNgClientProvider = NrNgClientProvider;
    angular
        .module('nr-ng-client')
        .provider('nrNgClient', NrNgClientProvider)
        .config(["$provide", "nrNgClientProvider", function ($provide, nrNgClientProvider) {
        nrNgClientProvider.decorateExceptionHandler($provide);
    }]);
})(relic || (relic = {}));
var relic;
(function (relic) {
    var STATE_CHANGE_START_EVENT_NAME = '$stateChangeStart';
    var STATE_CHANGE_END_EVENT_NAME = '$viewContentLoaded';
    var NrNgUiRouter = (function () {
        function NrNgUiRouter($state, addStateToTrace) {
            this.$state = $state;
            this.addStateToTrace = addStateToTrace;
        }
        NrNgUiRouter.prototype.reportStateDataLoaded = function () {
            if (this.$state) {
                this.addStateToTrace(this.$state.current.url);
            }
        };
        return NrNgUiRouter;
    }());
    relic.NrNgUiRouter = NrNgUiRouter;
    var NrNgUiRouterProvider = (function () {
        function NrNgUiRouterProvider() {
            this.newrelic = window.newrelic;
            this.config = {
                reportStateChange: true,
                threshold: 300
            };
            this.routsData = {
                start: 0,
                inTransition: false
            };
        }
        NrNgUiRouterProvider.prototype.stateChangedReportEnabled = function (val) {
            this.config.reportStateChange = val;
        };
        NrNgUiRouterProvider.prototype.threshold = function (value) {
            this.config.threshold = value;
        };
        NrNgUiRouterProvider.prototype.registerStateChangeEvents = function ($rootScope, $browser, $state) {
            var _this = this;
            $rootScope.$on(STATE_CHANGE_START_EVENT_NAME, function (ev, toState, toParams, fromState) {
                if (fromState && fromState.name) {
                    _this.routsData.start = Date.now();
                    _this.routsData.inTransition = true;
                }
            });
            if (this.config.reportStateChange) {
                $rootScope.$on(STATE_CHANGE_END_EVENT_NAME, function () {
                    if (_this.routsData.inTransition) {
                        $browser.notifyWhenNoOutstandingRequests(function () {
                            _this.addStateToTrace($state.current.url);
                        });
                    }
                });
            }
        };
        NrNgUiRouterProvider.prototype.addStateToTrace = function (name) {
            this.routsData.inTransition = false;
            if (this.newrelic && (Date.now() - this.routsData.start) >= this.config.threshold) {
                // report to browser
                this.newrelic.addToTrace({
                    name: 'RENDER_STATE' + name,
                    start: this.routsData.start,
                    end: Date.now()
                });
                // report to insights
                this.newrelic.addPageAction('RENDER_STATE' + name, { duration: (Date.now() - this.routsData.start) / 1000 });
            }
        };
        NrNgUiRouterProvider.prototype.getStateService = function ($injector) {
            var $state;
            var routerService = '$state';
            try {
                var hasUiRouter = $injector.has(routerService);
                if (hasUiRouter) {
                    $state = $injector.get(routerService);
                }
            }
            catch (e) { }
            return $state;
        };
        /* @ngInject */
        NrNgUiRouterProvider.prototype.$get = function ($rootScope, $browser, $injector) {
            var _this = this;
            var $state = this.getStateService($injector);
            if ($state) {
                this.registerStateChangeEvents($rootScope, $browser, $state);
            }
            return new NrNgUiRouter($state, function (url) { return _this.addStateToTrace(url); });
        };
        NrNgUiRouterProvider.prototype.$get.$inject = ["$rootScope", "$browser", "$injector"];
        return NrNgUiRouterProvider;
    }());
    relic.NrNgUiRouterProvider = NrNgUiRouterProvider;
    angular
        .module('nr-ng-client')
        .provider('nrNgUiRouter', NrNgUiRouterProvider);
})(relic || (relic = {}));
