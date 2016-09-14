# Angular Components Test Kit

## Talk
Can be downloaded via [Slideshare](https://u5283.ct.sendgrid.net/wf/click?upn=uO6HrIECmgsMyMFzOPMkJYGYLADpdUHdfI6J1dVtgwnTWzZ1-2B1a5bySNoLYlu9QIldML-2FPycLoyx-2FRfHUroVVRKQI0G-2BB7wD2GmagU-2F-2BaFO1GcQKZqTrLfcvI0-2Feq88U4MDDbWxKTMQq15nEkNaTrkhZIEN-2BIjBAqqg35v-2BUjy2Twh3o7-2F-2BMoBQuxY83o7cP_-2F0-2FOxt-2FTDNjPdgoVegGf3gWbc1x6lNE40K0OYIqiiEvA6QswJ-2BwAuEpEMZUWwF9pCCtXpZHa-2Blt19xDW8YLhwNH0buvisU141e9DwO8i-2FtHG4W1yIqIwmvtfz6eqxMt-2FKGTTQY4eR-2F43Sq5XMM2Dcv8czg-2FptYVNvd7fSa-2Fk1ulilJE5DRaYf8AWqy7gUQ2GQKrYehzRQiMwRXFmhxUPi9p5elNaeRifJJbzOrwyHJCJE85erCOU9BC6cqRFonXG-2FgLI0fQq9IJA61i4Z8AJDcRFZrLP3ZQqfF2Xvf-2FiSZ1jKPBpFVsd9RO-2FsqBDo6BeXrm5zSCKTDfxsL0cHIUqkcH4jesDUnfJR67EoqU8SyJo0NnGZlbOM04OaAwKTZDw)
**Note** - please download in order to be able to utilize the animations included in the deck.

## Overview
Component tests allows testing the UI (provided that it is build as components/directives of course) in order to get instant feedback and minimize the use of protractor.

The test kit provides a base driver utility class - **TurnerComponentDriver**, which allows writing tests using typescript in order to test components in a more readable, maintainable and quicker manner,
It provides basic methods to allow rendering a template, accessing underlying elements and ability to maintain drivers' hierarchy, in order to be able to reuse drivers in parent components.

The best practice implemented by TurnerJS in regards to elements' selectors is by using a 'data-hook' attribute in order to mark elements which are accessed by your code.
It implements the approach demonstrates in [this](http://html5doctor.com/html5-custom-data-attributes/) article, but it is not mandated by the framework, one can use any other data selectors approach.

## Installation - Bower
1. install using bower  
`bower install --save turnerjs`
2. Include the following reference in your Karma configuration file  
`'<path to your app>/bower_components/turnerjs/dist/test/lib/turnerjs-driver.js'`
3. *Optional* - if you are using TypeScript (recommended) add reference to the d.ts file in your tsconfig file:  
`"bower_components/turnerjs/dist/test/lib/turnerjs-driver.d.ts"`

## Installation - NPM
1. install using node package manager  
`npm install -S turnerjs`
2. Include the following reference in your Karma configuration file  
`'node_modules/turnerjs/module/generated/turnerjs-driver.js'` 
   or   
`require(turnerjs)` (when window is supported)
3. *Optional* - if you are using TypeScript (recommended) add reference to the d.ts file in your tsconfig file:  
`"../node_modules/turnerjs/module/generated/turnerjs-driver.d.ts"`

## Usage
* Create a driver that extends the base driver and implement the methods that are required for your tests, for example (in the spec file or elsewhere):

```javascript
   class ExampleComponentDriver extends TurnerComponentDriver {
           
        constructor() {
          super();
        }
      
        render(param1, param2) {
          //renders the template and adds the input parameters as scope params (second argument to renderFromTemplate method)
          //passing a selector is needed when the element containing the directive is not the first element in the template or when there is more than one driver
          this.renderFromTemplate('<example-component another-attribute="param2"><div data-hook="inner-element">{{param1}}</div></example-component>', {param1, param2}, 'example-component');
        }
      
        isInnerElementValid(): boolean {
           return this.findByDataHook('inner-element').text() === 'valid';
        }
      }
```

* Write tests that utilizes the driver

```javascript
    let driver: ExampleComponentDriver;
    beforeEach(() => {
       driver = new ExampleComponentDriver();
       driver.render('valid', 'yada yada');
       driver.connectToBody();
    });
   
    afterEach(() => {
        driver.disconnectFromBody();
    });
    
    it('should contain a valid element value', () => {
        expect(driver.isInnerElementValid()).toBeTruthy();
    });
```

## Global Methods added by the test kit
|Param|Type|Arguments|Details|
|---|---|---|---|
|byDataHook|global method|dataHook: string|Create a data-hook selector it is useful for methods that uses selectors such as ***TurnerComponentDriver.defineChild***|

## Base Driver Methods/ Members
|Param|Type|Arguments|Details|
|---|---|---|---|
|constructor|Constructor|N/A|Creates the driver|
|renderFromTemplate|protected method|**template**: string, **args**?: Object, **selector**?: string|Allows rendering the component/directive, the args is a key value pairs object that will be added to the scope of the element, initializes the root of the driver according to the selector |
|findByDataHook|protected method|**dataHook**: string|a utility method that should be used by drivers that inherits from the base driver in order to select an element (first if there are several) by **data-hook** attribute. It will throws an error if called before ***renderFromTemplate*** was called|
|findAllByDataHook|protected method|**dataHook**: string|similar to ***findByDataHook*** but allows selecting several elements with the same **data-hook**|
|defineChild|protected method|**childDriver**: Instance of T such that T extends **TurnerComponentDriver**, **selector**: string representing a CSS selector (preferably called with ***byDataHook(dataHook)***)|Declare a child driver of the current driver, allows components hierarchy, which is also returned by the method. This method should be called before ***renderFromTemplate*** was called|
|defineChildren|protected method|**factory**: function that returns an instance of T such that T extends **TurnerComponentDriver**, **selector**: string representing a CSS selector (which is expected to return more than one result)|returns an array of child drivers (instances of T), it is useful when there is more than one child driver for parent driver (e.g. ng-repeat), the returned array will change when there is a change in the number of elements in the dom. This method should be called before ***renderFromTemplate*** was called|
|applyChanges|public method|N/A|invokes $rootScope.$digest(), mainly aimed to 'hide' *AngularJS* related implementation|
|connectToBody|public method|N/A|Connects the template to the karma's browser body - allows height/width related tests. ***disconnectFromBody*** has to be called at the end of the test. It will thorw an error if called before ***renderFromTemplate*** was called|
|disconnectFromBody|public method|N/A|Clears the the body of the karma's browser, used in order to reset the browser to the original state prior to starting the next test|
|afterRender|protected method|N/A|You can override this method if you need extra setup after render|
|element|ng.IAugmentedJQuery|N/A|Reference to the element that represents the root of the driver (by selector if provided or template root)|
|scope|ng.IScope|N/A|Reference to the scope of ***element*** member|
|isRendered|boolean|N/A|defines whether the driver is rendered - whether its template was rendered and its scope is valid (defined and part of the dom)|
|appendedToBody|boolean|N/A|defines whether the driver's element is appended to body (e.g. a driver for bootstrap tooltip)|
|$rootScope|ng.IRootScopeService|N/A|Reference to the **$rootScope** service (removes the need to inject it in tests)|

## Nested drivers
In order to allow reuse of drivers, the base driver supports initializing any child element (member) that extends **TurnerComponentDriver**
For example, assuming 3 components are defined:
```javascript
   angular.module('myModule', []);
   class ItemComponentCtrl {
     public item: {value: number};

     isValid():  boolean {
       return this.item.value > 1;
     }
   }
   
   angular
     .module('myModule')
     .component('itemComponent', {
       template: `<div data-hook="item-element" ng-class="{'valid': $ctrl.isValid()}"/>`,
       controller: ItemComponentCtrl,
       bindings: {
         item: '='
       }
     });
   
   class IndividualComponentCtrl {
     public item: {value: number};

     getText():  string {
       return this.item.value > 1 ? 'valid' : 'invalid';
     }
   }
   
   angular
     .module('myModule')
     .component('individualComponent', {
       template: `<div data-hook="inner-element">{{$ctrl.getText()}}</div>`,
       controller: IndividualComponentCtrl,
       bindings: {
         item: '='
       }
     });
   
   class ParentComponentCtrl {
     public items: {value: number}[];
   
     constructor() {
       this.items = [];
       for (let i = 0; i < 5; i++) {
         //push either 1 or 2
         this.items.push({
           value: Math.floor((Math.random() * 2) + 1)
         });
       }
     }
   }
   
   angular
     .module('myModule')
     .component('parentComponent', {
       template: `<div>
                    <individual-component item="$ctrl.items[0]"/>
                    <item-component ng-repeat="item in $ctrl.items" item="item"/>
                  </div>`,
       controller: ParentComponentCtrl
     });

```

3 Drivers that corresponds to each are defined:  
(When there is a list of child drivers - e.g. when using ng-repeat, **defineChildren** method should be used in order to declare an array of child drivers)
```javascript
  
class IndividualComponentDriver extends TurnerComponentDriver {

  constructor() {
    super();
  }

  render(item) {
    this.renderFromTemplate('<individual-component item="item"/>', {item});
  }

  isValid(): boolean {
    return this.findByDataHook('inner-element').text() === 'valid';
  }
}

class ItemComponentDriver extends TurnerComponentDriver {

  constructor() {
    super();
  }

  render(item) {
    this.renderFromTemplate('<item-component item="item"/>', {item}, 'item-component');
  }

  isValid(): boolean {
    return this.findByDataHook('item-element').hasClass('valid');
  }
}

class ParentComponentDriver extends TurnerComponentDriver {
  public innerComponent: IndividualComponentDriver;
  public itemComponents: ItemComponentDriver[];

  constructor() {
    super();
    this.innerComponent = this.defineChild(new IndividualComponentDriver(), 'individual-component');
    this.itemComponents = this.defineChildren(() => new ItemComponentDriver(), 'item-component');
  }

  render() {
    this.renderFromTemplate(`<parent-component>`);
  }

  isIndividualValid(): boolean {
    return this.innerComponent.isValid();
  }

  isItemsValid(): boolean {
    let result = true;
    this.itemComponents.forEach(itemDriver => {
      result = result && itemDriver.isValid();
    });
    return result;
  }
}
```
**TurnerComponentDriver** will initialize the member's scope & element automatically as soon as the renderFromTemplate method is invoked.
The above drivers will allow testing each component separately and also testing the parent component that wraps the two:
```javascript
describe('Usage Examples when there are repeatable drivers', () => {
    let parentComponentDriver: ParentComponentDriver;
    beforeEach(() => {
      module('myModule');
      parentComponentDriver = new ParentComponentDriver();
    });

    it('should be valid when the random values are above 1', () => {
      spyOn(Math, 'random').and.returnValue(0.9);
      parentComponentDriver.render();
      expect(parentComponentDriver.isIndividualValid()).toBe(true);
      expect(parentComponentDriver.isItemsValid()).toBe(true);
    });
  });
```
## Non TypeScript usage
Though the recommendation is to use TypeScript with turnerjs, if you are not using it, you can create the driver using prototypical inheritance.
There are various ways to implement it, but the test kits includes a test spec for ES5 usage (see ***app/test/spec/components/es5-name-formatter.js***)
The below provides the basic implementation of such driver:
```javascript
/* globals TurnerComponentDriver */ //for jshint
function ES5Driver() {
  TurnerComponentDriver.call(arguments);
}

ES5Driver.prototype = new TurnerComponentDriver();
ES5Driver.constructor = ES5Driver;

ES5Driver.prototype.render = function () {
  this.renderFromTemplate('<es5-component-template></es5-component-template>');  
};
//other driver methods

describe('your tests are here', function () {
    var es5driver;
    
    beforeEach(function () {
        angular.mock.module('turnerjsAppInternal');
        driver = new ES5Driver();
    });
    
    it('...', function () {
        driver.render();
        //test e
    });
});
```
#### Contribution
Via pull requests,  
    
After cloning the repository please run `npm install` and `bower install` in order to fetch dependencies   
  
Running/Building the project is done by using ***grunt***:  
`grunt serve:clean` - will start the server, there is no real UI for it, but it will run the unit tests on each save
`grunt build` - build the project to make sure that changes are valid and meeting all code style definitions

#### Credits
Alon Yehezkel  
Shahar Talmi  
Boris Litvinski  
Amit Shvil  

## License

The MIT License.

See [LICENSE](LICENSE.md)
