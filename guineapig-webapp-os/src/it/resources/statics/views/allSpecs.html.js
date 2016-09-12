'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/allSpecs.html',
    "\n" +
    "<div class=\"container ng-scope\"  ng-if=\"show\" class=\"show\">\n" +
    "    <div class=\"row form-group\">\n" +
    "              <div class=\"topButtonPannel\">\n" +
    "                  <a id=\"home\"  ui-sref=\"login.h({state: 'active'})\"><small><strong>Home</strong></small></a>\n" +
    "                  <a id=\"specs\" style=\"color: orange\" ui-sref=\"login.specs\"><small><strong >View Specs</strong></small></a>\n" +
    "                  <a id=\"faq\" href=\"http://kb.wixpress.com/display/hoopoe/FAQ\"><small><strong>Petri - FAQ</strong></small></a>\n" +
    "\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <!--<div class=\"col-xs-12 fullView\" style=\"padding-top: 54px;background: #D6D4D8;border-top-right-radius: 6px;border-top-left-radius: 6px\">-->\n" +
    "        <div class=\"leftSide\" id=\"sidebar\" role=\"navigation\" >\n" +
    "\n" +
    "\n" +
    "            <div class=\"list-group sidePanel\">\n" +
    "\n" +
    "            </div>\n" +
    "\n" +
    "        </div><!--/span-->\n" +
    "        <div class=\"rightSide \">\n" +
    "            <div load-pane='dataLoading'></div>\n" +
    "            <form  role=\"form\">\n" +
    "\n" +
    "                <div class=\"form-group\">\n" +
    "                    <div class=\"input-group\">\n" +
    "                        <input class=\"form-control petri-filter\" id=\"filter\" name=\"filterInput\" debounce=\"300\" type=\"text\" ng-change=\"onSelectedChange()\" ng-model=\"selected.text\" placeholder=\"filter...\"\n" +
    "                               title=\"search pattern : [<columnName :> text]; [<columnName :> text]...\">\n" +
    "                        <div class=\"input-group-addon fa  fa-lg fa-search\"></div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "\n" +
    "            </form>\n" +
    "            <div class=\"topPanel\"><span>{{class}} Specs ({{allSpecs.length}})</span></div>\n" +
    "            <div class=\"gridStyle\"  ng-grid=\"gridOptions\">\n" +
    "\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    <!--</div>-->\n" +
    "\n" +
    "</div>\n"
  );
}]);