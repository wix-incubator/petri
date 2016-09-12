'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/history.html',
    "<style type=\"text/css\">\n" +
    "    .modal .modal-dialog { width: 753px; }\n" +
    "</style>\n" +
    "<div class=\"modal-content \">\n" +
    "\n" +
    "    <div class=\"modal-header\" style=\"\">\n" +
    "        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" ng-click=\"cancel(dismiss)\">×</button>\n" +
    "            <h4>{{experimentName}} - History <span class=\"pull-right\">Test id : <span style=\"font-weight: bold;padding-right: 55px;\" >{{id}}</span></span></h4>\n" +
    "    </div>\n" +
    "    <div class=\"modal-body history\" >\n" +
    "        <div load-pane='dataLoading'></div>\n" +
    "        <span style=\"font-weight: bold\">Creator : {{creator}}</span>\n" +
    "        <div class=\"HistoryTable\" >\n" +
    "            <table>\n" +
    "                    <tr ng-repeat=\"experiment in history\">\n" +
    "                        <td >\n" +
    "                            <table>\n" +
    "                                <tr>\n" +
    "                                    <td style=\"width:30px \">\n" +
    "                                        <div class=\"fa fa-fw\" ng-class=\"{'fa-pause':experiment.state=='paused','fa-stop':experiment.state=='ended','fa-play':experiment.state=='active','fa-forward':experiment.state=='future'}\">{{}}\n" +
    "                                        </div>\n" +
    "                                    </td >\n" +
    "                                    <td style=\"width: 160px\">\n" +
    "                                        <div>\n" +
    "                                            <table class=\"infoTable\" style=\"max-width: 160px;\">\n" +
    "                                                <tr ng-repeat=\"line in experiment.timeInfo\" >\n" +
    "                                                    <td style=\"overflow-y: hidden; color:#767575\">\n" +
    "                                                        {{line}}\n" +
    "                                                    </td>\n" +
    "                                                </tr>\n" +
    "                                            </table>\n" +
    "                                        </div>\n" +
    "                                    </td>\n" +
    "                                    <td>\n" +
    "                                        <div >\n" +
    "                                            <table class=\"infoTable\" >\n" +
    "                                                <tr ng-repeat=\"line in experiment.info.lines\">\n" +
    "                                                    <td style=\"max-width: 55px;\" title=\"{{line.value}}\">\n" +
    "                                                        {{line.value}}\n" +
    "                                                    </td>\n" +
    "                                                    <td style=\"\">\n" +
    "                                                        :{{ experiment.info.id}}\n" +
    "                                                    </td>\n" +
    "                                                    <td style=\"\">\n" +
    "                                                        #{{line.idValue}}\n" +
    "                                                    </td>\n" +
    "                                                    <td style=\"\">\n" +
    "                                                        ({{line.chunk}}%)\n" +
    "                                                    </td>\n" +
    "                                                </tr>\n" +
    "                                            </table>\n" +
    "                                        </div>\n" +
    "                                    </td>\n" +
    "                                    <td >\n" +
    "                                        <div >\n" +
    "                                            <table  class=\"infoTable\">\n" +
    "                                                <tr ng-repeat=\"line in experiment.filterInfo\">\n" +
    "                                                    <td style=\"width: 70px;color:#767575;font-size: x-small\">\n" +
    "                                                        {{line.k}}:\n" +
    "                                                    </td>\n" +
    "                                                    <td  title=\"{{line.v}}\" style=\"max-width:100px;color:#767575;\" class=\"ngCellText\">\n" +
    "                                                        {{line.v}}\n" +
    "                                                    </td>\n" +
    "                                                </tr>\n" +
    "                                            </table>\n" +
    "                                        </div>\n" +
    "                                    </td>\n" +
    "                                </tr>\n" +
    "                                <tr ng-show=\"0 < experiment.comment.length || 0 < experiment.updater.length\">\n" +
    "                                    <td colspan=\"4\" style=\"background-color:rgba(9, 140, 163, 0.2);padding-left: 5px\">{{experiment.updater}} : {{experiment.comment}}</td>\n" +
    "                                </tr>\n" +
    "\n" +
    "                                </tr>\n" +
    "                            </table>\n" +
    "                        </td>\n" +
    "\n" +
    "\n" +
    "            </table>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div class=\"modal-footer\" style=\"background-color:#f5f5f5;max-height:0px;min-height:0;border-top: none\"  >\n" +
    "\n" +
    "    </div>\n" +
    "    <!--<div>-->\n" +
    "        <!--<button class=\"btn btn-link\" ng-click=\"form.isCollapsed = !form.isCollapsed\">debug data</button>-->\n" +
    "    <!--</div>-->\n" +
    "</div>\n" +
    "\n" +
    "\n"
  );
}]);