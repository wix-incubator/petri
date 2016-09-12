'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/experimentView.html',
    "\n" +
    "<div class=\"container ng-scope\">\n" +
    "\n" +
    "\n" +
    "    <!--<div class=\"col-xs-12 fullView\" style=\"padding-top: 54px;background: #D6D4D8;border-top-right-radius: 6px;border-top-left-radius: 6px\">-->\n" +
    "    <div class=\"leftSide\" id=\"sidebar\" role=\"navigation\" >\n" +
    "        <div class=\"list-group sidePanel\" style=\"margin-top: 50px;\">\n" +
    "            <a ui-sref=\"view.experiment({state:'general',experimentId:{{experimentId}}})\" class=\"list-group-item\" ng-class=\"{active:class=='general'}\">General</a>\n" +
    "            <a ui-sref=\"view.experiment({state:'testGroups',experimentId:{{experimentId}}})\" class=\"list-group-item\" ng-class=\"{active:class=='testGroups'}\">Test Groups</a>\n" +
    "            <a ui-sref=\"view.experiment({state:'filters',experimentId:{{experimentId}}})\" class=\"list-group-item\" ng-class=\"{active:class=='filters'}\">Filters</a>\n" +
    "            <!--a ui-sref=\"h({state: 'unknown'})\" class=\"list-group-item\">All Experiments</a-->\n" +
    "        </div>\n" +
    "    </div><!--/span-->\n" +
    "    <div class=\"rightSide\">\n" +
    "        <div load-pane=dataLoading></div>\n" +
    "        <div class=\"topPanel\"><span>Experiment {{experiment.name}}</span></div>\n" +
    "        <div style=\"overflow-y: auto\">\n" +
    "            <div class=\"GenericTable\" ng-show=\"class==='general'\"  >\n" +
    "                <table >\n" +
    "                    <tr>\n" +
    "                        <td>\n" +
    "                            Property\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            Value\n" +
    "                        </td>\n" +
    "                    </tr>\n" +
    "                    <tr ng-repeat=\"property in experimentProperties\">\n" +
    "                        <td>\n" +
    "                            {{property}}\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            {{experiment[property]}}\n" +
    "                        </td>\n" +
    "                    </tr>\n" +
    "                </table>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"GenericTable\" ng-show=\"class==='testGroups'\"  >\n" +
    "\n" +
    "                <table >\n" +
    "                    <tr>\n" +
    "                        <td>\n" +
    "                            #id\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            Value\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            Chunk\n" +
    "                        </td>\n" +
    "                    </tr>\n" +
    "                    <tr ng-repeat=\"line in info.lines\">\n" +
    "                        <td>\n" +
    "                            {{line.id}}\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            {{line.value}}\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            {{line.chunk}} %\n" +
    "                        </td>\n" +
    "\n" +
    "                    </tr>\n" +
    "                </table>\n" +
    "        </div>\n" +
    "            <div class=\"GenericTable\" ng-show=\"class==='filters'\"  >\n" +
    "\n" +
    "                <table >\n" +
    "                    <tr>\n" +
    "                        <td>\n" +
    "                            Name\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            Value\n" +
    "                        </td>\n" +
    "\n" +
    "                    </tr>\n" +
    "                    <tr ng-repeat=\"filter in filters\">\n" +
    "                        <td>\n" +
    "                            {{filter}}\n" +
    "                        </td>\n" +
    "                        <td>\n" +
    "                            {{experiment[filter]}}\n" +
    "                        </td>\n" +
    "\n" +
    "                    </tr>\n" +
    "                </table>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <!--</div>-->\n" +
    "\n" +
    "</div>\n"
  );
}]);