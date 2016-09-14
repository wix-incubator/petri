'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/confirm.html',
    "<!--<style type=\"text/css\">-->\n" +
    "    <!--.modal .modal-dialog { width: 360px; }-->\n" +
    "<!--</style>-->\n" +
    "\n" +
    "<div class=\"confirm-content \" id=\"confirm-dialog\">\n" +
    "\n" +
    "    <div class=\"modal-header\">\n" +
    "        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" ng-click=\"cancel(dismiss)\">×</button>\n" +
    "        <h4>{{content.header}}</h4>\n" +
    "    </div>\n" +
    "    <div class=\"confirm-body\">\n" +
    "        <div class=\"text-error\" id=\"confirm-modal-text\">{{content.body.text}}</div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"confirm-footer\"  >\n" +
    "        <textarea ng-show=\"content.data!==null\" id=\"comment\" placeholder=\"comment...\" rows=\"3\"  style=\"resize: none; \"  ng-model=\"content.data\" required></textarea>\n" +
    "        <div class=\"flex-container\">\n" +
    "            <button class=\"btn  btn-default btn-cancel\" ng-class=\"content.cancelText.class\" ng-click=\"cancel(dismiss)\">{{content.cancelText.text}}</button>\n" +
    "            <button class=\"btn align-to-right-in-flex\"  ng-class=\"content.okText.class\" ng-click=\"apply(dismiss)\" id=\"confirm-modal-ok-button\">{{content.okText.text}}</button>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <!--<div>-->\n" +
    "    <!--<button class=\"btn btn-link\" ng-click=\"form.isCollapsed = !form.isCollapsed\">debug data</button>-->\n" +
    "    <!--</div>-->\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "\n"
  );
}]);