'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/overide.html',
    "\n" +
    "<div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" ng-click=\"cancel(dismiss)\">×</button>\n" +
    "    <h4>{{content.header}}</h4>\n" +
    "</div>\n" +
    "<div class=\"overide-body\">\n" +
    "    <div style=\"padding-left:5px\">\n" +
    "        <table>\n" +
    "            <tr ng-repeat=\"line in content.body.lines\">\n" +
    "                <td>\n" +
    "                    {{'petri_ovr='+content.body.key+':'+ line.value}}\n" +
    "                </td>\n" +
    "            </tr>\n" +
    "        </table>\n" +
    "    </div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\"  >\n" +
    "    <button class=\"btn  btn-default btn-cancel\" ng-class=\"content.cancelText.class\" ng-click=\"cancel(dismiss)\">{{content.cancelText.text}}</button>\n" +
    "    <button  class=\"btn btn-primary\"  ng-class=\"\" style=\"margin-left: 55px\" ng-click=\"apply(dismiss)\">{{content.okText.text}}</button>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "\n"
  );
}]);