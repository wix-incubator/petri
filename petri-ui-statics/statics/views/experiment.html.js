'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/experiment.html',
    "<div class=\"modal-content \" style=\"position: fixed\">\n" +
    "<!--<div ng-show=\"!newExpCtrl.form.isCollapsed\">-->\n" +
    "<!--<pre class=\"debug\" style=\"text-align: left\">-->\n" +
    "<!--<small>ab feture :{{newExpCtrl.newExperiment.type.value==='abTest'}}</small>-->\n" +
    "<!--<small> diable filters :{{newExpCtrl.newExperiment.openToAll.value===true}}</small>-->\n" +
    "<!--<small style=\"color: blue\">{{newExpCtrl.newExperiment|json}}</small>-->\n" +
    "<!--<small style=\"color: #008000\">{{newExpCtrl.form|json}}</small>-->\n" +
    "<!--</pre>-->\n" +
    "<!--</div>-->\n" +
    "\n" +
    "\n" +
    "<div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" ng-click=\"newExpCtrl.close(dismiss)\">×</button>\n" +
    "    <h4>{{newExpCtrl.header}} {{newExpCtrl.yael}}</h4>\n" +
    "    <span ng-class=\"{timeError:!newExpCtrl.form.invalid}\" class=\"help-inline text-error\"><small>{{newExpCtrl.form.error.text}}</small></span>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"experiment-body tab\" ng-class=\"{tall:'newExpCtrl.filters && newExpCtrl.filters.show',short : !newExpCtrl.filters || !newExpCtrl.filters.show}\"  id=\"tab0\" style=\"display: block\">\n" +
    "<div load-pane='newExpCtrl.dataLoading'></div>\n" +
    "<div>\n" +
    "    <alert ng-repeat=\"alert in newExpCtrl.alerts\" type=\"alert.type\" close=\"newExpCtrl.closeAlert($index)\">\n" +
    "        <p>{{alert.msg}}</p>\n" +
    "        <p>{{alert.msgEx}}</p>\n" +
    "    </alert>\n" +
    "</div>\n" +
    "<form  class=\"form-horizontal\" role=\"form\" name=\"myForm\" novalidate>\n" +
    "\n" +
    "<div class=\"group\" style=\"border-top-color: transparent\">\n" +
    "    <div class=\"row\">\n" +
    "        <div class=\"col-xs-2 starLabbel\">\n" +
    "            <label for=\"name\">Name</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-xs-5\">\n" +
    "            <input class=\"box\" id=\"name\" name=\"name\" type=\"text\" ng-disabled=\"newExpCtrl.newExperiment.name.disabled\" ng-model=\"newExpCtrl.newExperiment.name.value\" ng-required=\"true\" ng-minlength=\"2\"/>\n" +
    "            <ul ng-show=\"!myForm.$valid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "                <li ng-show=\"myForm.name.$error.required\">\n" +
    "                    <strong>Name</strong> is required.\n" +
    "                </li>\n" +
    "                <li ng-show=\"myForm.name.$error.minlength\">\n" +
    "                    At least 2 characters.\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "        <div class=\"col-xs-2\">\n" +
    "            <input type=\"radio\"   ng-model=\"newExpCtrl.newExperiment.type.value\"value='abTest' ng-change=\"newExpCtrl.onAbTest()\" ><label style=\"padding-left: 5px\">AB Test</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-xs-3\">\n" +
    "            <input type=\"radio\"   ng-model=\"newExpCtrl.newExperiment.type.value\" value='featureToggle' ng-change=\"newExpCtrl.onFeatureToggle()\"><label style=\"padding-left: 5px\">Feature Toggle</label>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"row\">\n" +
    "        <div class=\"col-md-2 starLabbel\">\n" +
    "            <label for=\"spec\">Product</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\">\n" +
    "            <select  class=\"form-select\" ui-select2  id=\"product\" data-placeholder=\"{{newExpCtrl.form.scopeSelectTest}}\"\n" +
    "                     ng-disabled=\"newExpCtrl.newExperiment.scope.disabled\"\n" +
    "                     ng-model=\"newExpCtrl.newExperiment.scope.value\"\n" +
    "                     ng-change=\"newExpCtrl.productChanged()\" >\n" +
    "                <option value=\"\"></option>\n" +
    "                <option ng-repeat=\"product in newExpCtrl.form.scopes\" value=\"{{product}}\">{{product}}</option>\n" +
    "            </select>\n" +
    "            <ul ng-show=\"newExpCtrl.form.invalid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "                <li ng-show=\"newExpCtrl.newExperiment.scope.invalid\">\n" +
    "                    <strong>Product</strong> is required.\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"row\">\n" +
    "\n" +
    "        <div class=\"col-md-2 starLabbel\">\n" +
    "            <label for=\"spec\">Spec</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\" ng-show=\"!newExpCtrl.newExperiment.isPublicScope\">\n" +
    "            <select  class=\"form-select\" ui-select2  id=\"spec\" data-placeholder=\"{{newExpCtrl.form.specSelectTest}}\"  ng-disabled=\"newExpCtrl.newExperiment.key.disabled\"  ng-model=\"newExpCtrl.newExperiment.key.value\"  ng-change=\"newExpCtrl.keyChanged()\">\n" +
    "                <option value=\"\"></option>\n" +
    "                <option ng-repeat=\"spec in newExpCtrl.form.allspecs\" value=\"{{spec.key}}\">{{spec.key}}</option>\n" +
    "            </select>\n" +
    "            <ul ng-show=\"newExpCtrl.form.invalid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "                <li ng-show=\"newExpCtrl.newExperiment.key.invalid\">\n" +
    "                    <strong>Spec</strong> {{newExpCtrl.newExperiment.key.error}}.\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\" ng-show=\"newExpCtrl.newExperiment.isPublicScope\">\n" +
    "            <input  class=\"box\"  name=\"userSpec\" id=\"userSpec\" placeholder = \"{{newExpCtrl.placeHolderForFreeKey}}\"  ng-disabled=\"newExpCtrl.newExperiment.key.disabled\"\n" +
    "                    ng-model=\"newExpCtrl.newExperiment.key.value2\" ng-pattern=\"newExpCtrl.urlPattern\" ng-required=\"true\">\n" +
    "            <ul ng-show=\"newExpCtrl.form.invalid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "                <li ng-show=\"myForm.userSpec.$error.required\">\n" +
    "                    <strong>Spec</strong> is required.\n" +
    "                </li>\n" +
    "                <li ng-show=\"myForm.userSpec.$error.pattern\">\n" +
    "                    <strong>{{newExpCtrl.placeHolderForFreeKey}}</strong> is required.\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row\">\n" +
    "        <div class=\"col-xs-2 starLabbel\">\n" +
    "            <label for=\"exposureId\">Exposure Id</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 starLabbel\">\n" +
    "            <label id=\"exposureId\" class=\"current-exposure\">{{newExpCtrl.exposureObj.name}}</label>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row\" ng-switch=\"newExpCtrl.exposureStatus\">\n" +
    "        <div class=\"col-xs-2 starLabbel\"></div>\n" +
    "        <div class=\"col-md-10\" ng-switch-default>\n" +
    "            <button class=\"btn-link exposure-id-link exposure-id-update-link\" id=\"updateExposureIdButton\" ng-disabled=\"newExpCtrl.newExperiment.key.value === '' && newExpCtrl.newExperiment.key.value2 === ''\" ng-click=\"newExpCtrl.prepareToUpdateExposure()\" title=\"Exposure Id can only be updated when a spec is chosen\">Update exposure Id</button>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 exposure-id-update-form\" ng-switch-when=\"update\">\n" +
    "            <div>\n" +
    "                <select class=\"form-select\" ui-select2 data-placeholder=\"Choose\" ng-model=\"newExpCtrl.exposureIdForServer\" id=\"updateExposureId\">\n" +
    "                    <option value=\"{{null}}\">None</option>\n" +
    "                    <option ng-repeat=\"exposure in newExpCtrl.allExposures\" value=\"{{exposure.id}}\">{{exposure.name}} (owner-{{exposure.owner}})</option>\n" +
    "                </select>\n" +
    "                <div class=\"flex-container\">\n" +
    "                    <div class=\"align-to-right-in-flex btn-margin-top\">\n" +
    "                        <button class=\"btn btn-primary btn-margin-bottom\" ng-disabled=\"newExpCtrl.exposureIdForServer === newExpCtrl.exposureObj.id\" ng-click=\"newExpCtrl.updateExposureInServer(newExpCtrl.newExperiment, newExpCtrl.exposureIdForServer)\">Apply</button>\n" +
    "                        <button class=\"btn btn-primary btn-margin-bottom\" ng-click=\"newExpCtrl.closeExposureIdUpdateStatusMessage()\">Cancel</button>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 flex-container\" ng-switch-when=\"error\">\n" +
    "            <ul class=\"error-list\">\n" +
    "                <li>\n" +
    "                    <strong>There was a problem updating the exposure id. Please contact us.</strong>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "            <button class=\"btn-link exposure-id-link exposure-id-error-ok\" ng-click=\"newExpCtrl.closeExposureIdUpdateStatusMessage()\">ok</button>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row\">\n" +
    "        <div class=\"col-md-2 starLabbel\">\n" +
    "            <label for=\"linkedToEdit\">Link To</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\">\n" +
    "            <select  class=\"form-select\" ui-select2  id=\"linkedToEdit\" data-placeholder=\"Choose\"  ng-model=\"newExpCtrl.newExperiment.linkId\"  ng-show=\"newExpCtrl.applyButtonText === 'Create Experiment'\" ng-change=\"newExpCtrl.onChangeLinkId()\" ng-disabled=\"true\">\n" +
    "                <option value=0>None</option>\n" +
    "                <option ng-repeat=\"experiment in newExpCtrl.allexperiments | filter: {state: '!ended'}\" value=\"{{experiment.originalId}}\">{{experiment.name}} (id-{{experiment.id}}) (originalId-{{experiment.originalId}})</option>\n" +
    "            </select>\n" +
    "            <input  class=\"box\" id=\"linkedTo\" ng-disabled=\"true\"  ng-model=\"newExpCtrl.newExperiment.linkId\" ng-show=\"newExpCtrl.applyButtonText !== 'Create Experiment'\">\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row\">\n" +
    "        <div class=\"col-md-2 starLabbel\">\n" +
    "            <label for=\"description\">Description</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\">\n" +
    "            <textarea id=\"description\" placeholder=\"description...\" rows=\"3\"  style=\"resize: none; \"  ng-model=\"newExpCtrl.newExperiment.description.value\" required></textarea>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"group\" >\n" +
    "    <div ng-show=\"newExpCtrl.newExperiment.type.value==='abTest'\">\n" +
    "        <label class=\"section\">Ab Testing Feature</label>\n" +
    "        <div class=\"row\">\n" +
    "            <div  class =\"col-md-2 starLabbel \" ng-show=\"newExpCtrl.newExperiment.groups.value.length\">\n" +
    "                <label>{{newExpCtrl.newExperiment.groups.length }} Test Groups</label>\n" +
    "            </div>\n" +
    "            <div  class=\"col-md-8 \" >\n" +
    "\n" +
    "                <table class=\"dynTestGroup\">\n" +
    "                    <tr ng-repeat=\"tgroup in newExpCtrl.newExperiment.groups.value\" style=\";margin: 0px;direction: ltr;\">\n" +
    "                        <td>\n" +
    "                            <input  type=\"number\" ng-model=\"tgroup.chunk\" class=\"box\" style=\"width: 43px\" ng-change=\"newExpCtrl.onAbChanged()\">\n" +
    "\n" +
    "                        </td>\n" +
    "                        <td style=\"width: 150px\">\n" +
    "                            <span  ng-click=\"newExpCtrl.editTestGroup(tgroup,'#testGroup'+tgroup.id)\" ng-show=\"tgroup.value && !tgroup.edit\">{{tgroup.value}}</span>\n" +
    "                            <form ng-submit=\"newExpCtrl.editTestGroupDone(tgroup,'testGroup'+tgroup.id)\">\n" +
    "                                <input id=\"{{'testGroup'+tgroup.id}}\" class=\"box\" placeholder=\"value...\" ng-show=\"!tgroup.value || tgroup.edit\"\n" +
    "                                       input-escape=\"newExpCtrl.editTestGroupRevert(tgroup,'testGroup'+tgroup.id)\"\n" +
    "                                       ng-blur=\"newExpCtrl.editTestGroupDone(tgroup,'testGroup'+tgroup.id)\"\n" +
    "                                       input-focus=\"tgroup == newExpCtrl.editedTgroup\">\n" +
    "\n" +
    "                            </form>\n" +
    "\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            <button class=\"btn btn-default fa  fa-times fa-fw pull-right\" ng-click=\"newExpCtrl.removeTestGroup(tgroup)\" ng-show=\"tgroup.userTestGroup\"></button>\n" +
    "                            <button class=\"btn btn-default fa  fa-edit fa-fw pull-right\" ng-click=\"newExpCtrl.editTestGroup(tgroup,'testGroup'+tgroup.id)\" ng-show=\"tgroup.userTestGroup\"></button>\n" +
    "                        </td>\n" +
    "                    </tr>\n" +
    "                </table>\n" +
    "                <ul ng-show=\"newExpCtrl.form.invalid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "                    <li ng-show=\"newExpCtrl.newExperiment.groups.invalid\">\n" +
    "                        <strong>Chunk </strong> should sum to <strong>100%</strong>\n" +
    "                    </li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div ng-show=\"newExpCtrl.newExperiment.type.value==='featureToggle'\">\n" +
    "        <label class=\"section\">Feature Toggle </label>\n" +
    "        <div class=\"row\">\n" +
    "            <div  class =\"col-md-2 starLabbel\" ng-show=\"newExpCtrl.newExperiment.groups.value.length\">\n" +
    "                <label>{{newExpCtrl.newExperiment.groups.length }} Test Groups</label>\n" +
    "            </div>\n" +
    "            <div  class=\"col-md-8\" >\n" +
    "                <table class=\"dynTestGroup\">\n" +
    "                    <tr ng-repeat=\"tgroup in newExpCtrl.newExperiment.groups.value\" style=\";margin: 0px;direction: ltr;\">\n" +
    "                        <td>\n" +
    "                            <input type=\"radio\" ng-model=\"newExpCtrl.newExperiment.featureValue.value\" value=\"{{tgroup.value}}\"  ng-change=\"newExpCtrl.onFeatureToggleChange()\">\n" +
    "                        </td>\n" +
    "                        <td style=\"width: 150px\">\n" +
    "                            <span  ng-click=\"newExpCtrl.editTestGroup(tgroup,'#testGroupFT'+tgroup.id)\" ng-show=\"tgroup.value && !tgroup.edit\">{{tgroup.value}}</span>\n" +
    "                            <form ng-submit=\"newExpCtrl.editTestGroupDone(tgroup,'testGroupFT'+tgroup.id)\">\n" +
    "                                <input id=\"{{'testGroupFT'+tgroup.id}}\" class=\"box\" placeholder=\"value...\" ng-show=\"!tgroup.value || tgroup.edit\"\n" +
    "                                       input-escape=\"newExpCtrl.editTestGroupRevert(tgroup,'testGroupFT'+tgroup.id)\"\n" +
    "                                       ng-blur=\"newExpCtrl.editTestGroupDone(tgroup,'testGroupFT'+tgroup.id)\"\n" +
    "                                       input-focus=\"tgroup == newExpCtrl.editedTgroup\">\n" +
    "                            </form>\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            <button class=\"btn btn-default fa  fa-times fa-fw pull-right\" ng-click=\"newExpCtrl.removeTestGroup(tgroup)\" ng-show=\"tgroup.userTestGroup\"></button>\n" +
    "                            <button class=\"btn btn-default fa  fa-edit fa-fw pull-right\" ng-click=\"newExpCtrl.editTestGroup(tgroup,'testGroupFT'+tgroup.id)\" ng-show=\"tgroup.userTestGroup\"></button>\n" +
    "                        </td>\n" +
    "                    </tr>\n" +
    "                </table>\n" +
    "                <ul ng-show=\"newExpCtrl.form.invalid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "                    <li ng-show=\"newExpCtrl.newExperiment.groups.invalid\">\n" +
    "                        <strong>1 Test group </strong> should be selected</strong>\n" +
    "                    </li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div >\n" +
    "        <button class=\"btn-link addTestGroup\" ng-click=\"newExpCtrl.addNewTestGroup()\" ng-show=\"newExpCtrl.enableAddTestGroup\">Add New Test Group</button>\n" +
    "    </div>\n" +
    "</div>\n" +
    "<div class=\"group\">\n" +
    "    <label class=\"section\">Time Frame Configuration</label>\n" +
    "    <div class=\"row startRow\" >\n" +
    "        <div class=\"col-md-2 starLabbel\" >\n" +
    "            <label for=\"start\">From</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-6 \" >\n" +
    "            <input id=\"start\" class=\"input-small datePicker  box\" type=\"text\" datepicker-popup=\"dd / MM / yyyy\" close-on-date-selection=\"false\"\n" +
    "                   ng-model=\"newExpCtrl.newExperiment.startDate.value\" ng-change=\"newExpCtrl.onStartChanged()\"\n" +
    "                   is-open=\"newExpCtrl.form.openedfrom\" min=\"newExpCtrl.form.minDateTime\"  disabled=\"disabled\" datepicker-options=\"newExpCtrl.dateOptions\"\n" +
    "                   show-weeks=\"false\"  date-disabled=\"disabled(date, mode)\" required/>\n" +
    "            <button  class=\"box\" ng-click=\"newExpCtrl.openfrom()\" ng-disabled=\"newExpCtrl.newExperiment.startDate.disabled\"><i class=\"fa fa-calendar fa-align-left\" ></i></button>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-4 \" style=\"max-height: 50px\" >\n" +
    "            <div id=\"timeFrom\" class=\"time123\" ng-model=\"newExpCtrl.newExperiment.startDate.value\"  ng-change=\"newExpCtrl.onStartChanged()\" style=\"display:inline-block;\">\n" +
    "                <timepicker hour-step=\"newExpCtrl.form.timePickerOptions.hstep\" minute-step=\"newExpCtrl.form.timePickerOptions.mstep\" show-meridian=\"false\" mousewheel=false ></timepicker>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <ul id=\"datetimeFromError\" ng-show=\"newExpCtrl.form.invalid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "        <li ng-show=\"newExpCtrl.newExperiment.startDate.invalid\">\n" +
    "            <span>{{newExpCtrl.newExperiment.startDate.error}}</span>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "\n" +
    "    <div class=\"row endRow\" >\n" +
    "        <div class=\"col-md-2 starLabbel \" >\n" +
    "            <label  for=\"end\" >To</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-6 \" >\n" +
    "            <input id=\"end\" class=\"input-small datePicker box\" type=\"text\" datepicker-popup=\"dd / MM / yyyy\" close-on-date-selection=\"false\"\n" +
    "                   ng-model=\"newExpCtrl.newExperiment.endDate.value\" ng-change=\"newExpCtrl.onEndChanged()\"\n" +
    "                   name=\"endDate\"  is-open=\"newExpCtrl.form.openedto\" min=\"newExpCtrl.form.minDateTimeTo\" disabled=\"disabled\" datepicker-options=\"newExpCtrl.dateOptions\"  show-weeks=\"false\"  date-disabled=\"disabled(date, mode)\" required/>\n" +
    "            <button ng-click=\"newExpCtrl.opento()\"><i class=\"fa fa-calendar fa-align-left\"></i></button>\n" +
    "\n" +
    "        </div>\n" +
    "        <div class=\"col-md-4 \" style=\"max-height: 50px\" >\n" +
    "\n" +
    "            <div id=\"timeTo\"  class=\"time123\" ng-model=\"newExpCtrl.newExperiment.endDate.value\"    ng-change=\"newExpCtrl.onEndChanged()\" style=\"display:inline-block;\">\n" +
    "                <timepicker hour-step=\"newExpCtrl.form.timePickerOptions.hstep\" minute-step=\"newExpCtrl.form.timePickerOptions.mstep\" show-meridian=\"false\" mousewheel=false></timepicker>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <ul id=\"datetimeToError\" ng-show=\"newExpCtrl.form.invalid && newExpCtrl.showErrors\" class=\"error-list\">\n" +
    "\n" +
    "        <li ng-show=\"newExpCtrl.newExperiment.endDate.invalid\">\n" +
    "            <span>{{newExpCtrl.newExperiment.endDate.error}}</span>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>\n" +
    "<div  class=\"group\" style=\"border-bottom: none\" ng-show=\"newExpCtrl.filters && newExpCtrl.filters.show\" id=\"filters\" >\n" +
    "    <!--<div class=\"row \">-->\n" +
    "    <!--<div class=\"col-md-6 \" >-->\n" +
    "    <!--<label class=\"section\">Filters</label>-->\n" +
    "    <!--</div>-->\n" +
    "    <!--</div>-->\n" +
    "    <div class=\"row \">\n" +
    "        <div class=\"col-md-2 \" >\n" +
    "            <label class=\"section\">Filters</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 \" >\n" +
    "            <div class=\"bs-glyphicons\">\n" +
    "                <ul class=\"bs-glyphicons-list\">\n" +
    "                    <li ng-modal=\"buttons\" ng-repeat=\"button in newExpCtrl.filters.buttons()\" ng-show=\"button.show\" ng-class=\"{active:newExpCtrl.filters.btn.id==button.id}\" ng-click=\"newExpCtrl.onClick(button.id)\" title=\"{{button.title}}\">\n" +
    "                        <a class=\"glyphicon-class\"  type=\"radio\"  ng-disabled=\"button.disabled\"> {{button.text}} </a>\n" +
    "                    </li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"filters\" ng-show=\"newExpCtrl.filters.text !=='Open To All'\" >\n" +
    "        <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.conductLimit.show\">\n" +
    "            <div class=\"col-md-2 \" >\n" +
    "                <label>Conduct Limit</label>\n" +
    "                <div class=\"fa fa-question-circle petri-question-circle\"  title=\"{{newExpCtrl.filters.controllers.conductLimit.title}}\"></div>\n" +
    "            </div>\n" +
    "            <div class=\"col-md-5 petri-top-small\">\n" +
    "              <span class=\"input-symbol-tilde\">\n" +
    "                <input\n" +
    "                type=\"number\" id=\"conductLimit\" min=\"0\" max=\"1000000000\" class=\"box petri-align-text-right petri-width-110\" ng-model=\"newExpCtrl.newExperiment.conductLimit.value\">\n" +
    "\n" +
    "                </input>\n" +
    "              </span>\n" +
    "              <td><span style=\"font-size:11px;position: relative;top:5px\">(Zero means unlimited)</span></td>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.excludeGuids.show\">\n" +
    "            <div class=\"col-md-2 \" >\n" +
    "                <label>Ex. Guids</label>\n" +
    "                <div class=\"fa fa-question-circle petri-question-circle\"  title=\"{{newExpCtrl.filters.controllers.excludeGuids.title}}\"></div>\n" +
    "            </div>\n" +
    "            <div class=\"col-md-10\">\n" +
    "                <input\n" +
    "                    type=\"text\"\n" +
    "                    id=\"excludeGuid\" ui-select2=\"newExpCtrl.guiIdOptions\" class=\"form-select\" ng-model=\"newExpCtrl.newExperiment.excludeGuids.value\">\n" +
    "                </input>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.includeGuids.show\">\n" +
    "            <div class=\"col-md-2 \" >\n" +
    "                <label>In. Guids</label>\n" +
    "                <div class=\"fa fa-question-circle petri-question-circle\"  title=\"{{newExpCtrl.filters.controllers.includeGuids.title}}\"></div>\n" +
    "            </div>\n" +
    "            <div class=\"col-md-10\">\n" +
    "                <input\n" +
    "                    type=\"text\"\n" +
    "                    id=\"includeGuid\" ui-select2=\"newExpCtrl.guiIdOptions\" class=\"form-select\" ng-model=\"newExpCtrl.newExperiment.includeGuids.value\">\n" +
    "                </input>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"group\">\n" +
    "    <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.geo.show.label\">\n" +
    "        <div class=\"col-md-2 \" >\n" +
    "            <label></label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\" >\n" +
    "            <label class=\"bold-text\">{{newExpCtrl.filters.controllers.geo.show.label}}</label>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row\" ng-show=\"newExpCtrl.filters.controllers.lang.show\">\n" +
    "\n" +
    "        <div class=\"col-md-2 \" >\n" +
    "            <label for=\"lang\">Languages</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\" >\n" +
    "            <input\n" +
    "                class=\"form-select\" id=\"lang\"  value=\"newExpCtrl.newExperiment.languages.value\" data-placeholder=\"choose languages...\" >\n" +
    "            </input>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.geo.show\">\n" +
    "        <div class=\"col-md-2 \">\n" +
    "            <label for=\"geo\">Geo</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 \" >\n" +
    "            <input\n" +
    "                class=\"form-select\" id=\"geo\"  value=\"newExpCtrl.newExperiment.geo.value\" data-placeholder=\"choose countries...\" >\n" +
    "            </input>\n" +
    "            <td><input style=\"height: 10px;position: relative;top:-3px\" type=\"checkbox\" ng-model=\"newExpCtrl.newExperiment.excludeGeo.value\">  <span style=\"font-size:11px;position: relative;top:-5px\">(exclude)</span></td>\n" +
    "\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.excludeUserGroups.show\">\n" +
    "        <div class=\"col-md-2 \">\n" +
    "            <label for=\"exclude-user-groups\">Ex. User Groups</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 \" >\n" +
    "            <input\n" +
    "                class=\"form-select\" id=\"exclude-user-groups\"  value=\"newExpCtrl.newExperiment.excludeUserGroups.value\" data-placeholder=\"choose user groups to exclude...\" >\n" +
    "            </input>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.includeUserAgentRegexes.show\">\n" +
    "        <div class=\"col-md-2 \">\n" +
    "            <label for=\"include-user-agents\">In. User Agents</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 \" >\n" +
    "            <input\n" +
    "                class=\"form-select\" id=\"include-user-agents\"  value=\"newExpCtrl.newExperiment.includeUserAgentRegexes.value\" data-placeholder=\"choose user agents to include...\" >\n" +
    "            </input>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.excludeUserAgentRegexes.show\">\n" +
    "        <div class=\"col-md-2 \">\n" +
    "            <label for=\"exclude-user-agents\">Ex. User Agents</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 \" >\n" +
    "            <input\n" +
    "                class=\"form-select\" id=\"exclude-user-agents\"  value=\"newExpCtrl.newExperiment.excludeUserAgentRegexes.value\" data-placeholder=\"choose user agents to exclude...\" >\n" +
    "            </input>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row\" ng-show=\"newExpCtrl.filters.controllers.hosts.show\">\n" +
    "\n" +
    "        <div class=\"col-md-2 \" >\n" +
    "            <label for=\"hosts\">Hosts</label>\n" +
    "            <div class=\"fa fa-question-circle petri-question-circle\"  title=\"{{newExpCtrl.filters.controllers.hosts.title}}\"></div>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\" >\n" +
    "            <textarea\n" +
    "                class=\"box\" rows=\"2\" id=\"hosts\"  ng-model=\"newExpCtrl.newExperiment.hosts.value\" data-placeholder=\"choose hosts...\" title=\"list of hosts seperated with ','\" >\n" +
    "            </textarea>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.metaSiteIds.show\">\n" +
    "        <div class=\"col-md-2 \" >\n" +
    "            <label>In. Meta Sites</label>\n" +
    "            <div class=\"fa fa-question-circle petri-question-circle\"  title=\"{{newExpCtrl.filters.controllers.metaSiteIds.title}}\"></div>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\">\n" +
    "            <input\n" +
    "                type=\"text\"\n" +
    "                id=\"metaSiteIds\" ui-select2=\"newExpCtrl.selectMetaSiteOptions\" class=\"form-select\" ng-model=\"newExpCtrl.newExperiment.metaSiteIds.value\">\n" +
    "            </input>\n" +
    "            <td><input style=\"height: 10px;position: relative;top:-3px\" type=\"checkbox\" ng-model=\"newExpCtrl.newExperiment.excludeMetaSiteIds.value\">  <span style=\"font-size:11px;position: relative;top:-5px\">(exclude)</span></td>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row \" ng-show=\"newExpCtrl.filters.controllers.metaSiteIds.show\">\n" +
    "        <div class=\"col-md-2 \" >\n" +
    "            <label></label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\" >\n" +
    "            <label>PLEASE NOTE - if you need MetaSiteId filter talk to your developer for adding the param</label>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"row\" ng-show=\"newExpCtrl.filters.invalid\">\n" +
    "        <div class=\"col-md-2 \" >\n" +
    "            <label></label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10 \" >\n" +
    "            <ul  class=\"error-list\">\n" +
    "                <li >\n" +
    "                    <span>{{newExpCtrl.filters.btn.error}}</span>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div id=\"bottom\"></div>\n" +
    "</form>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"experiment-body tab\" ng-class=\"{tall:'newExpCtrl.filters && newExpCtrl.filters.show',short : !newExpCtrl.filters || !newExpCtrl.filters.show}\" id=\"tab1\" style=\"display: none\">\n" +
    "    <div class=\"row\">\n" +
    "        <div class=\"col-md-2 starLabbel\">\n" +
    "            <label for=\"comment\">Comment</label>\n" +
    "        </div>\n" +
    "        <div class=\"col-md-10\">\n" +
    "            <textarea id=\"comment\" placeholder=\"comment...\" rows=\"3\"  style=\"resize: none; \"   ng-model=\"newExpCtrl.comment.value\" required></textarea>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <alert ng-repeat=\"alert in newExpCtrl.alerts\" type=\"alert.type\" close=\"newExpCtrl.closeAlert($index)\">\n" +
    "        <p>{{alert.msg}}</p>\n" +
    "        <p>{{alert.msgEx}}</p>\n" +
    "    </alert>\n" +
    "\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\"  >\n" +
    "    <button class=\"btn btn-cancel\" ng-click=\"newExpCtrl.cancel(dismiss)\">{{newExpCtrl.cancelButtonText}}</button>\n" +
    "    <button ng-disabled=\"!newExpCtrl.newExperiment.editable || newExpCtrl.applyDisabled\" class=\"btn btn-primary\" style=\"margin-left: 55px\" ng-click=\"newExpCtrl.apply(dismiss)\">{{newExpCtrl.applyButtonText}}</button>\n" +
    "</div>\n" +
    "\n" +
    "</div>\n" +
    "\n" +
    "\n"
  );
}]);