'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/reports.html',
    "\n" +
    "<div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" ng-click=\"cancel(dismiss)\">×</button>\n" +
    "    <h4>{{content.header}}</h4>\n" +
    "</div>\n" +
    "<div class=\"reports-body\" id=\"reports-modal\">\n" +
    "    <div class=\"reports-title\" ng-show=\"reportsUrlExists\"> Existing Reports:\n" +
    "        <div>\n" +
    "            <a ng-init=\"getReportUrl()\" href=\"{{reportsUrl}}\" target=\"_blank\">Click Here</a>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div ng-switch=\"reportsStatus\">\n" +
    "        <div ng-switch-default>\n" +
    "            <div class=\"reports-title\">Upload New Report:\n" +
    "            </div>\n" +
    "            <div>\n" +
    "            <button class=\"btn upload-button upload-file\" type=\"file\" ng-file-select=\"onFileSelect($files)\">Choose File</button>\n" +
    "            </div>\n" +
    "            <div ng-file-drop\n" +
    "                 class=\"drop-box\"\n" +
    "                 ng-file-change=\"onFileSelect($files)\"\n" +
    "                 stopPropagation=\"true|false\">\n" +
    "                Drop File\n" +
    "                <div>Here</div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <i class=\"fa fa-spinner fa-3\" ng-switch-when=\"uploadingReport\">\n" +
    "            Uploading ...\n" +
    "        </i>\n" +
    "        <div class=\"reports-title\" ng-switch-when=\"uploadedSuccessfully\"> File Uploaded Successfully :)\n" +
    "        </div>\n" +
    "        <div class=\"reports-title\" ng-switch-when=\"problemUploadingFile\"> There was a problem uploading the file :(\n" +
    "            <div>Error: {{uploadError.errorDescription}}</div>\n" +
    "            <div>Please try again later or contact the Petri team.</div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "</div>\n" +
    "<div class=\"modal-footer\"  >\n" +
    "    <button  class=\"btn btn-primary\"  ng-class=\"\" style=\"margin-left: 55px\" ng-click=\"apply(dismiss)\">{{content.okText.text}}</button>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "\n"
  );
}]);