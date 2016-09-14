
declare module angular {
    interface IComponent {
        controller?: any;
        controllerAs?: string;
        bindings?: Object;
        template?: any;
        templateUrl?: any;
        restrict?: string;
        transclude?: any;
    }
    interface IModule {
        component(name: string, options: IComponent): IModule;
    }
}

declare class WixRoles {
    OWNER: string;
    CONTRIBUTOR: string;
    LIMITED_ADMIN: string;
    WIX_STAFF: string;
    BACKOFFICE_MANAGER: string;
    BLOG_EDITOR: string;
    BLOG_WRITER: string;
    constructor();
}

declare class WixAngularDemoHeightController {
    height: number;
    showContent: boolean;
    reportHeightChanged(height: any): void;
}

interface IWixHeightChangedAttributes extends ng.IAttributes {
    wixHeightChanged: string;
}
declare class WixHeightChangedCtrl {
    constructor($scope: ng.IScope, $attrs: IWixHeightChangedAttributes, wixHeightWatcher: WixHeightWatcher);
}

interface Window {
    Wix: any;
}
declare class WixTpaHeightChangedDirectiveCtrl implements ng.IDirective {
    constructor($scope: ng.IScope, wixHeightWatcher: WixHeightWatcher, $window: ng.IWindowService);
}

interface Manager<T> {
    contains(value: string | Array<string>): boolean;
    get(value: string): string;
    loadScope(scope: string): ng.IPromise<T>;
}

interface IAuthorizationInfo {
    permissions: Array<string>;
    isOwner: boolean;
    roles: Array<string>;
    ownerId: string;
    siteToken?: string;
}
declare class PermissionsDefinition {
    $$ownerId: string;
    $$roles: Array<string>;
    permissions: Array<string>;
    isOwner: boolean;
    siteToken: string;
    constructor(json: IAuthorizationInfo);
}
declare class PermissionsDefinitionBuilder {
    ownerId: string;
    roles: Array<string>;
    permissions: Array<string>;
    isOwner: boolean;
    siteToken: string;
    withPermissions(permissions: Array<string>): PermissionsDefinitionBuilder;
    withIsOwner(isOwner: boolean): PermissionsDefinitionBuilder;
    withOwnerId(ownerId: string): PermissionsDefinitionBuilder;
    withSiteToken(token: string): PermissionsDefinitionBuilder;
    withRoles(roles: Array<string>): PermissionsDefinitionBuilder;
    build(): PermissionsDefinition;
}

declare class IsPermitted {
    private permissionsManager;
    private wixManagerEval;
    constructor(permissionsManager: any, wixManagerEval: WixManagerEval);
    filter(input: string, permissionId: string, fallback: string, context?: string): string;
}

interface Window {
    jsonpExperiemts: any;
    loadExperimentScopeSync: Function;
    setExperimentsSync: Function;
}
declare class ExperimentManager implements Manager<{
    [name: string]: string;
}> {
    private provider;
    private $http;
    petriUrlPrefix: string;
    getExperimentValue: Function;
    isExperimentEnabled: Function;
    constructor(provider: ExperimentManagerProvider, $http: ng.IHttpService);
    get(value: string): string;
    contains(value: string): boolean;
    loadScope(scope: any): ng.IPromise<{}>;
    loadExperiment(name: any, fallback: any): ng.IPromise<{}>;
    $$queryPetri(params: any): ng.IPromise<{}>;
    $$getExperimentsObj(): any;
    private getPetriUrl(params);
    private getQueryParams(params);
}
declare class ExperimentManagerProvider {
    experiments: any;
    constructor();
    clearExperiments(): void;
    isExperimentEnabled(name: any): boolean;
    setExperiments(map: any): void;
    getExperimentValue(name: any): any;
    $get($injector: ng.auto.IInjectorService): ExperimentManager;
}

declare class PermissionsManager implements Manager<Array<string>> {
    private provider;
    private wixRoles;
    private permissionsContextMap;
    private contextGetter;
    private defaultContextKey;
    private defaultContextGetter;
    constructor(provider: PermissionsManagerProvider, wixRoles: WixRoles);
    contains(value: string, context?: string): boolean;
    get(value: string): string;
    loadScope(scope: string): ng.IPromise<Array<string>>;
    assignPermissionsMap(permissionsMap: {
        [context: string]: PermissionsDefinition;
    }): void;
    setContextGetter(contextGetter: Function): void;
    resetContextGetter(): void;
    $$getOwnerId(): string;
    getSiteToken(): string;
    $$getRoles(): Array<string>;
    $$isOwner(): boolean;
    $$getCurrentPermissionsMap(): {
        [context: string]: PermissionsDefinition;
    };
    private isWixStaff(roles);
    private getCurrentPermissionsDefinition();
    private hasPermission(permissionsDefinition, permission);
}
declare class PermissionsManagerProvider {
    permissionsDefinition: PermissionsDefinition;
    permissionsContextMap: any;
    constructor();
    setAuthorizationInfo(authorizationInfo: IAuthorizationInfo): void;
    setPermissions(permissionsDefinition: PermissionsDefinition): void;
    setPermissionsContextMap(permissionsContextMap: any): void;
    $get($injector: ng.auto.IInjectorService): PermissionsManager;
}

declare class WixCache {
    private $q;
    private recordUtils;
    private cleanableStorage;
    private wixAngularStorageErrors;
    private DEFAULT_AGE_IN_SEC;
    private DATA_TYPE;
    private ADHOC_TYPE;
    private REMOTE_TYPE;
    private CLEAN_EPSILON;
    namespace: string;
    constructor(provider: WixCacheProvider, $q: any, recordUtils: any, cleanableStorage: any, wixAngularStorageErrors: any, DEFAULT_AGE_IN_SEC: any, DATA_TYPE: any, ADHOC_TYPE: any, REMOTE_TYPE: any, CLEAN_EPSILON: any);
    private rejectUserNotLoggedIn();
    private rejectWithRuntimeException();
    private tryToSet(key, value);
    private withNamespace(opts);
    set(key: any, data: any, options: any): any;
    setWithGUID(data: any, opts?: any): any;
    get(key: any, opts: any): any;
    getAll(opts: any): any;
    remove(key: any, opts: any): any;
}
declare class WixCacheProvider {
    namespace: string;
    setNamespace(namespace: string): void;
    $get($injector: ng.auto.IInjectorService): WixCache;
}

declare module WixHeightWatcherModule {
    const HEIGHT_CHANGED_EVENT: string;
}
declare class WixHeightWatcher {
    private wixDebounce;
    private $document;
    private $rootScope;
    private $timeout;
    private height;
    constructor(wixDebounce: any, $document: ng.IDocumentService, $rootScope: ng.IRootScopeService, $timeout: any);
    private reportHeight(height);
    private checkHeight();
    subscribe(func: Function): Function;
}

declare class WixManagerEval {
    private wixConstValue;
    constructor(wixConstValue: any);
    eval(manager: any, expr: any, context?: string): boolean;
}

declare class WixStorage {
    private $q;
    private $http;
    private recordUtils;
    private wixCache;
    private wixAngularStorageErrors;
    private ANGULAR_STORAGE_PREFIX;
    private REMOTE_TYPE;
    private DEFAULT_AGE_IN_SEC;
    namespace: string;
    constructor(provider: WixStorageProvider, $q: any, $http: any, recordUtils: any, wixCache: any, wixAngularStorageErrors: any, ANGULAR_STORAGE_PREFIX: any, REMOTE_TYPE: any, DEFAULT_AGE_IN_SEC: any);
    private rejectUserNotLoggedIn();
    private cacheRemoteData(key, data, options);
    private getUrl(path, options, key);
    private getRemote(key, options);
    private getAllRemote(options);
    private handleNamespaceMigration(key, options);
    private tryCache(key, options);
    private tryCacheGetAll(options);
    private rejectNotFound();
    private withNamespace(opts);
    set(key: any, data: any, opts?: any): any;
    get(key: any, opts?: any): any;
    getAll(opts?: any): any;
    remove(key: any, opts?: any): any;
}
declare class WixStorageProvider {
    namespace: string;
    setNamespace(namespace: string): void;
    $get($injector: ng.auto.IInjectorService): WixStorage;
}
