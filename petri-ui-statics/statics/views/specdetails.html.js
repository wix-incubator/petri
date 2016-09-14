'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/specdetails.html',
    "<div class=\"container bs-callout\" ng-show=\"0<selectedSpecTemplate.testGroups.length\" >\n" +
    "        <h4 style=\"width: 400px\">Spec <span style=\"color: #474747;font-style: italic\"  tooltip=\"{{selectedSpecTemplate.key}}\"> {{selectedSpecTemplate.key|trunc:true:20:'...'}}</span> Details</h4>\n" +
    "\n" +
    "                <table class=\"table table-striped table-bordered\" style=\"width: 100%\" >\n" +
    "\n" +
    "                    <tbody >\n" +
    "                    <tr  >\n" +
    "                        <th style=\"width:200px\"title=\"Test groups\">Test Group</th>\n" +
    "                        <th  style=\"width:30px\" title=\"%\">%</th>\n" +
    "                        <th style=\"width:40px\"  title=\"default\">default</th>\n" +
    "                        <th style=\"width:80px\"   title=\"update\">update</th>\n" +
    "                    </tr>\n" +
    "                    <tr ng-repeat=\"group in selectedSpecTemplate.testGroups\" class=\"active\">\n" +
    "                        <td ><span tooltip-placement=\"bottom\" tooltip=\"{{tgroup.value}}\">{{group.value |trunc:true:40:'...'}}</span></td>\n" +
    "                        <td >{{group.chunk}}</td>\n" +
    "                        <td >{{group.default}}</td>\n" +
    "                        <td >{{group.lastUpdate}}</td>\n" +
    "                    </tr>\n" +
    "                    </tbody>\n" +
    "                </table>\n" +
    "</div>\n" +
    "\n" +
    "\n"
  );
}]);