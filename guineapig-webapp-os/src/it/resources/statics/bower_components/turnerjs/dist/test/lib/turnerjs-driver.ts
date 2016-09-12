'use strict';

const enum TurnerChildDriverType {
  CHILD_REGULAR,
  CHILD_ARRAY
}

interface TurnerChildDriver {
  selector?: string;
  selectorIndex?: number;
  type: TurnerChildDriverType;
  factory?: <T extends TurnerComponentDriver>(item?, index?) => T;
  drivers?: Array<TurnerComponentDriver>;
  fullDriversArr?: Array<TurnerComponentDriver>;
}

class TurnerComponentDriver {

  public $rootScope: ng.IRootScopeService;
  public $compile: ng.ICompileService;
  public body: ng.IAugmentedJQuery;

  public appendedToBody: boolean;

  private _element: ng.IAugmentedJQuery;
  private _scope: ng.IScope;
  private parent: TurnerComponentDriver;
  private templateRoot: ng.IAugmentedJQuery;
  private childDrivers: Array<TurnerChildDriver> = [];

  constructor() {
    this.body = angular.element(document.body);
  }

  static byDataHook(dataHook): string {
    return `[data-hook='${dataHook}']`;
  }

  public get element() {
    this.verifyRendered();
    return this._element;
  }

  public get scope() {
    this.verifyRendered();
    return this._scope;
  }

  public get isRendered() {
    return !!this._scope;
  }

  public connectToBody() {
    this.verifyRendered();
    this.body.append(this.templateRoot);
  }

  public disconnectFromBody() {
    if (this.templateRoot) {
      this.templateRoot.remove();
    }
    if (this.appendedToBody) {
      this._element.remove();
    }
  }

  public applyChanges() {
    this.$rootScope.$digest();
  }

  protected findByDataHook(dataHook: string): ng.IAugmentedJQuery {
    return angular.element(this.element[0].querySelector(TurnerComponentDriver.byDataHook(dataHook)));
  }

  protected findAllByDataHook(dataHook: string): ng.IAugmentedJQuery {
    return angular.element(this.element[0].querySelectorAll(TurnerComponentDriver.byDataHook(dataHook)));
  }

  protected renderFromTemplate(template: string, args: Object = {}, selector?) {
    inject(($rootScope: ng.IRootScopeService, $compile: ng.ICompileService) => {
      this.$rootScope = $rootScope;
      this.$compile = $compile;
    });
    let scope = this.$rootScope.$new();
    scope = angular.extend(scope, args);

    this.templateRoot = angular.element(template);
    this.$compile(this.templateRoot)(scope);
    this.$rootScope.$digest();

    this.initializeDriver(this.templateRoot, selector);
    this.$rootScope.$watch(() => this.initChildDrivers());
  }

  protected initChildDrivers() {
    this.childDrivers.forEach(child => {
      if (child.type === TurnerChildDriverType.CHILD_REGULAR) {
        this.initRegularChild(child);
      } else if (child.type === TurnerChildDriverType.CHILD_ARRAY) {
        this.initArrayChild(child);
      }
    });
  }

  protected defineChild<T extends TurnerComponentDriver>(childDriver: T, selector?: string): T {
    return this.defineIndexedChild(childDriver, selector, 0);
  }

  protected defineChildren<T extends TurnerComponentDriver>(factory: (item?, index?) => T, selector: string): Array<T> {
    let children = [];
    this.childDrivers.push({
      type: TurnerChildDriverType.CHILD_ARRAY,
      selector,
      factory,
      drivers: children,
      fullDriversArr: []
    });
    return children;
  }

  private defineIndexedChild<T extends TurnerComponentDriver>(childDriver: T, selector?: string, selectorIndex: number = 0): T {
    this.childDrivers.push({
      selector,
      selectorIndex,
      type: TurnerChildDriverType.CHILD_REGULAR,
      drivers: [childDriver]
    });
    childDriver.parent = this;
    return childDriver;
  }

  private initializeDriver(containingElement: ng.IAugmentedJQuery, selector?: string, selectorIndex: number = 0): void {
    let searchElement = this.appendedToBody ? this.body : containingElement;
    this._element = selector ? angular.element(searchElement[0].querySelectorAll(selector)[selectorIndex]) : containingElement;
    this._scope = this._element.isolateScope() || this._element.scope();
    if (this.isRendered) {
      this.initChildDrivers();
    }
  }

  private initArrayChild(child) {
    child.drivers.splice(0, child.drivers.length);
    [].forEach.call(this._element[0].querySelectorAll(child.selector), (item, index) => {
      if (child.fullDriversArr.length <= index) {
        child.fullDriversArr.push(this.defineIndexedChild(child.factory(item, index), child.selector, index));
      }
      child.drivers.push(child.fullDriversArr[index]);
    });
  };

  private initRegularChild(child) {
    let childDriver = child.drivers[0];
    childDriver.initializeDriver(this._element, child.selector, child.selectorIndex);
    childDriver.$compile = this.$compile;
    childDriver.$rootScope = this.$rootScope;
  };

  verifyRendered() {
    if (this.parent) {
      this.parent.verifyRendered();
    } else {
      this.initChildDrivers();
    }
    if (!this.isRendered) {
      throw 'cannot interact with driver before element is rendered';
    }
  }
}

if (window) {
  window['byDataHook'] = window['byDataHook'] || TurnerComponentDriver.byDataHook;
}

declare function byDataHook(dataHook: string): string;
