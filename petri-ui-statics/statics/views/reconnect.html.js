'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/reconnect.html',
    "<div class=\"container\" style=\"top: 174px;\">\n" +
    "\n" +
    "    <div id=\"reconnect\">\n" +
    "        <button class=\"btn btn-default reload\" ui-sref=\"login.h({state: 'active'})\">Reload</button><button class=\"btn btn-default more\" ng-click=\"showMore()\">{{buttonMoreText}}</button>\n" +
    "\n" +
    "    </div>\n" +
    "    <div id=\"details\" ng-show=\"buttonMoreText==='Less'\">\n" +
    "        <h4>Error Code</h4>\n" +
    "    <span style=\"margin: 5px 10px 20px 50px;\" >\n" +
    "           {{response.data.errorCode}}\n" +
    "    </span>\n" +
    "        <h4>Status</h4>\n" +
    "    <span style=\"margin: 5px 10px 20px 50px;\" >\n" +
    "           {{response.status}}\n" +
    "    </span>\n" +
    "        <h4>Description</h4>\n" +
    "    <span style=\"margin: 5px 10px 20px 50px;\" >\n" +
    "           {{response.data.errorDescription}}\n" +
    "    </span>\n" +
    "        <h4>More details</h4>\n" +
    "    <span style=\"margin: 5px 10px 20px 50px;\" >\n" +
    "           {{response.config.method}} : {{response.config.url}}\n" +
    "    </span>\n" +
    "        <h4>message</h4>\n" +
    "    <span style=\"margin: 5px 10px 20px 50px;\" >\n" +
    "           {{response.message}}\n" +
    "    </span>\n" +
    "    </div>\n" +
    "    <img id=\"reconnectLogo\" class=\"petriLogo logoSize\" relative-src=\"images/petri.png\" >\n" +
    "</div>\n" +
    "\n"
  );
}]);