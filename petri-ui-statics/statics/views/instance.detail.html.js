'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/instance.detail.html',
    "<div   ng-show=\"experiment.id\" class=\"row well inset\">\n" +
    "    <!--ng-show=\"gridOptions.selectedItems.length\"-->\n" +
    "\n" +
    "    <h4 class=\"truncate\" style=\"clear:left;\">Experiment {{experiment.id}}</h4>\n" +
    "    <label class=\"slabel truncate\"  >Spec. key </label> {{experiment.key}}</p>\n" +
    "    <div style=\"height: 60px;overflow-y: auto;\">\n" +
    "\n" +
    "        <label class=\"slabel truncate\"  >Description </label>\n" +
    "        <p> {{experiment.description}}</p>\n" +
    "\n" +
    "    </div>\n" +
    "\n" +
    "    <table class=\"table  table-condensed table-bordered\" style=\"width: 100%\" >\n" +
    "\n" +
    "        <tbody >\n" +
    "        <tr  >\n" +
    "            <th scope=\"col\" title=\"Test groups\">Test Group</th>\n" +
    "            <th scope=\"col\"   title=\"%\">%</th>\n" +
    "        </tr>\n" +
    "        <tr ng-repeat=\"group in experiment.groups\" class=\"active\">\n" +
    "            <td  >{{group.value}}</td>\n" +
    "            <td >{{group.chunk}}</td>\n" +
    "        </tr>\n" +
    "        </tbody>\n" +
    "    </table>\n" +
    "</div>\n" +
    "\n" +
    "\n"
  );
}]);