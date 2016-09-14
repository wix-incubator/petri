'use strict';

try {
  angular.module('uiPetri');
} catch (e) {
  angular.module('uiPetri', []);
}

angular.module('uiPetri').run(['$templateCache', function ($templateCache) {
  'use strict';

  $templateCache.put('views/optional.html',
    "<a ng-href=\"#/home\">Home</a>\n" +
    "<H1 style=\"z-index: 1;position: absolute;left:10px;top: 265px\" >Under Construction</H1>\n" +
    "<img relative-src=\"images/petri.png\" alt=\"\" style=\"position:absolute;left:100px;top: 100px\" >\n" +
    "<pre>\n" +
    "        $state = {{$state.current.name}}\n" +
    "        $stateParams = {{$stateParams}}\n" +
    "        $state full url = {{ $state.$current.url.source }}\n" +
    "        access = {{$state.current.access}}\n" +
    "</pre>\n"
  );
}]);