declare const enum TurnerChildDriverType {
    CHILD_REGULAR = 0,
    CHILD_ARRAY = 1,
}
interface TurnerChildDriver {
    selector?: string;
    selectorIndex?: number;
    type: TurnerChildDriverType;
    factory?: <T extends TurnerComponentDriver>(item?, index?) => T;
    drivers?: Array<TurnerComponentDriver>;
    fullDriversArr?: Array<TurnerComponentDriver>;
}
declare class TurnerComponentDriver {
    $rootScope: ng.IRootScopeService;
    $compile: ng.ICompileService;
    body: ng.IAugmentedJQuery;
    appendedToBody: boolean;
    private _element;
    private _scope;
    private parent;
    private templateRoot;
    private childDrivers;
    constructor();
    static byDataHook(dataHook: any): string;
    element: ng.IAugmentedJQuery;
    scope: ng.IScope;
    isRendered: boolean;
    connectToBody(): void;
    disconnectFromBody(): void;
    applyChanges(): void;
    protected findByDataHook(dataHook: string): ng.IAugmentedJQuery;
    protected findAllByDataHook(dataHook: string): ng.IAugmentedJQuery;
    protected renderFromTemplate(template: string, args?: Object, selector?: any): void;
    protected initChildDrivers(): void;
    protected defineChild<T extends TurnerComponentDriver>(childDriver: T, selector?: string): T;
    protected defineChildren<T extends TurnerComponentDriver>(factory: (item?, index?) => T, selector: string): Array<T>;
    private defineIndexedChild<T>(childDriver, selector?, selectorIndex?);
    private initializeDriver(containingElement, selector?, selectorIndex?);
    private initArrayChild(child);
    private initRegularChild(child);
    verifyRendered(): void;
}
declare function byDataHook(dataHook: string): string;
