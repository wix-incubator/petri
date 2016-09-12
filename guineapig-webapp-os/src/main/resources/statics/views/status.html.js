'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/status.html',
    "    <!--<div class=\"row\">-->\n" +
    "        <!--<div class=\"col-lg-12\">-->\n" +
    "            <!--<div><small>lastUpdate :</small><strong> {{lastUpdate| date:'dd-MM-yyyy HH:mm:ss Z'}}</strong> <small>  experiments </small><strong>{{count}}  </strong>.</div>-->\n" +
    "            <!--<div  ng-show=\"status.disconnect\"><small><strong>disconnect</strong></small></div>-->\n" +
    "        <!--</div>-->\n" +
    "    <!--</div>-->\n" +
    "    <div class=\"row\">\n" +
    "        <div class=\"col-lg-12\">\n" +
    "            <div id=\"msg-after-api-call\" ng-show=\"msg\" ng-class=\"msg.class\"><small><strong>{{msg.text}}</strong></small></div>\n" +
    "        </div>\n" +
    "    </div>\n"
  );
}]);