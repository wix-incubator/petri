'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/activityPanel.html',
    "<div  class=\"box\" style=\"height: 40px;width: 150px;\" >\n" +
    "    <div ng-repeat=\"state in states | orderBy : 'state'\">\n" +
    "        <button  style=\"float: left; background: transparent;border: none;margin: 1px;padding-bottom: 1;padding-top: 1px\">\n" +
    "            <img height=\"35px\" width=\"35ps\"  relative-src=\"{{ state }}\"  >\n" +
    "        </button>\n" +
    "    </div>\n" +
    "</div>\n"
  );
}]);