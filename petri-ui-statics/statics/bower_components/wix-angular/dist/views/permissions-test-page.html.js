'use strict';

try {
  angular.module('angularApp');
} catch (e) {
  angular.module('angularApp', []);
}

angular.module('angularApp').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/permissions-test-page.html',
    "<button class='rename-btn' wix-permission-if='rename'>This button will be visible in case the user has 'rename' permission</button>\n" +
    "<button class='edit-btn' wix-permission-if='edit'>This button will be visible in case the user has 'edit' permission</button>\n" +
    "<button class='copy-btn' wix-permission-disabled='!copy'>This button will be disabled in case the user doesn't have 'copy' permission</button>\n" +
    "<button class='copy-btn2' wix-permission-disabled='copy'>This button will be disabled in case the user does have 'copy' permission</button>\n" +
    "<div class='permissions-classes' permission-values=\"{true: 'rename-class', false:'some-other-class'}\" wix-permission-class='rename'>rename</div>\n" +
    "<div class='permissions-classes-2' permission-values=\"{false: 'yoba'}\" wix-permission-class='somePermissionUserDontHave'>rename</div>\n" +
    "<a class='permissions-anchor' ng-href=\"{{'http://www.someurl.com' | isPermitted:'copy':''}}\">This anchor will be disabled in case the user doesn't have 'copy' permission</a>\n" +
    "<a class='permissions-anchor-2' ng-href=\"{{'http://www.someurl.com' | isPermitted:'!copy':''}}\">This anchor will be disabled in case the user does have 'copy' permission</a>\n"
  );
}]);