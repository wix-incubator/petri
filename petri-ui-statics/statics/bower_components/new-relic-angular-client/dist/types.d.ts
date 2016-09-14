declare module relic {
    class NrNgClient {
        protected newrelic: any;
        tag(name: any, value: any): void;
        addPageAction(name: any, values: any): void;
        reportError(exception: any, cause: any): void;
        reportFinishLoading(): void;
    }
    class NrNgClientProvider extends NrNgClient implements ng.IServiceProvider {
        private config;
        reportFinishedEnabled(value: any): void;
        reportErrorsEnabled(value: any): void;
        decorateExceptionHandler($provide: any): void;
        $get($browser: any, $timeout: any): NrNgClient;
    }
}
declare module relic {
    class NrNgUiRouter {
        private $state;
        private addStateToTrace;
        constructor($state: any, addStateToTrace: (a: string) => void);
        reportStateDataLoaded(): void;
    }
    class NrNgUiRouterProvider implements ng.IServiceProvider {
        private newrelic;
        private config;
        private routsData;
        stateChangedReportEnabled(val: any): void;
        threshold(value: any): void;
        private registerStateChangeEvents($rootScope, $browser, $state);
        private addStateToTrace(name);
        private getStateService($injector);
        $get($rootScope: any, $browser: any, $injector: any): NrNgUiRouter;
    }
}
