'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/login.html',
    "<h3 class=\"logoSize stagingTxt\" ng-show=\"!$root.clientConfig.production\">Staging</h3>\n" +
    "\n" +
    "<div class=\"petriLogo\" id=\"login\" ng-show=\"$root.clientConfig.production\">\n" +
    "    <img   ng-show=\"!login.login\" class=\"logoSize loginLogoPos\"  relative-src='images/petri.png' >\n" +
    "\n" +
    "    <div >\n" +
    "        <div class=\"loginButton pull-right\" id=\"googleLogin\"  >\n" +
    "            <div class=\"btn btn-link \" ng-show=\"!login.login\" >\n" +
    "              <a ng-if='!useBoAuthenticationServer'  class=\"btn btn-link\" ng-href=\"{{action}}\">log in with google</a>\n" +
    "              <a ng-if='useBoAuthenticationServer' class=\"btn btn-link\" ng-click=\"loginClick()\">log in with google</a>\n" +
    "            </div>\n" +
    "            <span ng-show=\"login.login\">{{login.user}} |</span>\n" +
    "            <div class=\"btn btn-link\" ng-show=\"login.login\" ng-click=\"logout()\">logout</div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "</div>\n" +
    "<div ui-view></div>\n" +
    "\n" +
    "<footer class=\"statusView\">\n" +
    "    <div ui-view=\"status\"></div>\n" +
    "</footer>\n"
  );
}]);