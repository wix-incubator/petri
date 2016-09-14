'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/reportMetric.html',
    "\n" +
    "<div class=\"modal-header\">\n" +
    "    <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" ng-click=\"cancel(dismiss)\">×</button>\n" +
    "    <h4 id=\"report-metric-modal-title\">{{content.header}} (Id {{reportMetric.experimentId}})</h4>\n" +
    "</div>\n" +
    "<div class=\"reports-body\" id=\"reports-metric-modal\" style=\"height: 400px\">\n" +
    "    <div style=\"height: 200px\" ng-init=\"getReportMetricData()\">\n" +
    "        <div style=\"overflow-y: hidden; color:#767575; font-weight: bold\">Total: {{reportMetric.totalCount}}</div>\n" +
    "        <div style=\"overflow-y: hidden; color:#767575; font-weight: bold\">Total in last five minutes: {{reportMetric.fiveMinuteCount}}</div>\n" +
    "        <div style=\"overflow-y: hidden; color:#767575; font-weight: bold\">Last time conducted: {{reportMetric.lastUpdated | date:'yyyy-MM-dd HH:mm:ss'}}</div>\n" +
    "        <div class=\"HistoryTable\" style=\"text-align: center\">\n" +
    "            <table>\n" +
    "                <tr ng-repeat=\"reportPerVal in reportMetric.reportsPerValue\">\n" +
    "                    <td>\n" +
    "                        <div>\n" +
    "                            <table class=\"infoTableTitle\">\n" +
    "                                <div class=\"report-metric-margin-top-bottom\">\n" +
    "                                    <td colspan=\"4\" class=\"report-metric-val-title\">Value: {{reportPerVal.experimentValue}}</td>\n" +
    "                                    <td colspan=\"4\" class=\"report-metric-val-title\">Total: {{reportPerVal.totalCount}}</td>\n" +
    "                                    <td colspan=\"4\" class=\"report-metric-val-title\">Total in last five minutes: {{reportPerVal.fiveMinuteCount}}</td>\n" +
    "                                    <td colspan=\"4\" class=\"report-metric-val-title\">Last time conducted: {{reportPerVal.lastUpdated | date:'MM-dd HH:mm:ss'}}</td>\n" +
    "                                </div>\n" +
    "                                <table class=\"infoTable\" style=\"margin-top: 20px\">\n" +
    "                                    <td style=\"overflow-y: hidden; color:#767575; font-weight: bold\">\n" +
    "                                        Server\n" +
    "                                    </td>\n" +
    "                                    <td style=\"overflow-y: hidden; color:#767575; font-weight: bold\">\n" +
    "                                        Total\n" +
    "                                    </td>\n" +
    "                                    <td style=\"overflow-y: hidden; color:#767575; font-weight: bold\">\n" +
    "                                        Total in last five minutes\n" +
    "                                    </td>\n" +
    "                                    <td style=\"overflow-y: hidden; color:#767575; font-weight: bold\">\n" +
    "                                        Last time conducted\n" +
    "                                    </td>\n" +
    "                                    <tr ng-repeat=\"serverData in reportPerVal.reportsPerServer\" >\n" +
    "                                        <td style=\"overflow-y: hidden; color:#767575\">\n" +
    "                                            {{serverData.serverName}}\n" +
    "                                        </td>\n" +
    "                                        <td style=\"overflow-y: hidden; color:#767575\">\n" +
    "                                            {{serverData.totalCount}}\n" +
    "                                        </td>\n" +
    "                                        <td style=\"overflow-y: hidden; color:#767575\">\n" +
    "                                            {{serverData.fiveMinuteCount}}\n" +
    "                                        </td>\n" +
    "                                        <td style=\"overflow-y: hidden; color:#767575\">\n" +
    "                                            {{serverData.lastUpdated | date:'MM-dd HH:mm:ss'}}\n" +
    "                                        </td>\n" +
    "                                    </tr>\n" +
    "                                </table>\n" +
    "                        </div>\n" +
    "                    </td>\n" +
    "            </table>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\"  >\n" +
    "    <button  class=\"btn btn-primary\"  ng-class=\"\" style=\"margin-left: 55px\" ng-click=\"apply(dismiss)\">{{content.okText.text}}</button>\n" +
    "</div>\n" +
    "\n"
  );
}]);