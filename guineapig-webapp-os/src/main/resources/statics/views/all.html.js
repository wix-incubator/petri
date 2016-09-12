'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/all.html',
    "\n" +
    "<div class=\"container ng-scope\"  ng-if=\"show\" class=\"show\">\n" +
    "    <div class=\"row form-group\">\n" +
    "              <div class=\"topButtonPannel\">\n" +
    "                  <a id=\"home\" style=\"color: orange\" ui-sref=\"login.h({state: 'active'})\"><small><strong >Home</strong></small></a>\n" +
    "                  <a id=\"specs\"  ui-sref=\"login.specs\"><small><strong>View Specs</strong></small></a>\n" +
    "                  <a id=\"faq\" href=\"http://kb.wixpress.com/display/hoopoe/FAQ\"><small><strong>Petri - FAQ</strong></small></a>\n" +
    "                  <a class=\"btn btn-newExperiment pull-right\" ng-click=\"newExperiment()\" ng-disabled=\"!editStatus\"><span style=\"font-size: large;margin-right: 5px \">+</span>Add Experiment</a>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <!--<div class=\"col-xs-12 fullView\" style=\"padding-top: 54px;background: #D6D4D8;border-top-right-radius: 6px;border-top-left-radius: 6px\">-->\n" +
    "        <div class=\"leftSide\" id=\"sidebar\" role=\"navigation\" >\n" +
    "\n" +
    "\n" +
    "            <div class=\"list-group sidePanel\">\n" +
    "                <a ui-sref=\"login.h({state: 'active'})\" class=\"list-group-item\" ng-class=\"{active:class=='active'}\">Active Experiments</a>\n" +
    "                <a ui-sref=\"login.h({state: 'paused'})\" class=\"list-group-item\" ng-class=\"{active:class=='paused'}\">Paused Experiments</a>\n" +
    "                <a ui-sref=\"login.h({state: 'future'})\" class=\"list-group-item\" ng-class=\"{active:class=='future'}\">Future Experiments</a>\n" +
    "                <a ui-sref=\"login.h({state: 'ended'})\" class=\"list-group-item\" ng-class=\"{active:class=='ended'}\">Ended Experiments</a>\n" +
    "                <a ui-sref=\"login.h({state: 'all'})\" class=\"list-group-item\" ng-class=\"{active:class=='all'}\">All Experiments</a>\n" +
    "            </div>\n" +
    "\n" +
    "        </div><!--/span-->\n" +
    "        <div class=\"rightSide\" id=\"mainView\">\n" +
    "            <div load-pane='dataLoading'></div>\n" +
    "\n" +
    "            <div class=\"topPanel\" id=\"editStatusOff\" ng-if=\"!editStatus\"><span>EXPERIMENT FREEZE!</span></div>\n" +
    "            <div class=\"topPanel\"><span>{{class}} Experiments ({{allexperiments.length}})</span></div>\n" +
    "            <div class=\"fa fa-question-circle\" id=\"search_q\" title=\"search pattern : [<columnName>: text]; [<columnName>: text]...\"></div>\n" +
    "            <form  role=\"form\">\n" +
    "                <div class=\"form-group\">\n" +
    "                    <div class=\"input-group\">\n" +
    "                        <input class=\"form-control petri-filter\" id=\"filter\" name=\"filterInput\" debounce=\"300\" type=\"text\" ng-change=\"onSelectedChange()\" ng-model=\"selected.text\" placeholder=\"filter...\"\n" +
    "                               title=\"search pattern : [<columnName>: text]; [<columnName>: text]...\">\n" +
    "                        <div class=\"input-group-addon fa  fa-lg fa-search\"></div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "\n" +
    "            </form>\n" +
    "            <div class=\"gridStyle\"  ng-grid=\"gridOptions\">\n" +
    "\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    <!--</div>-->\n" +
    "\n" +
    "</div>\n"
  );
}]);