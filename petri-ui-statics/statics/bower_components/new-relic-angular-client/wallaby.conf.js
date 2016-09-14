module.exports = function (wallaby) {
	return {
		files: [
			'bower_components/angular/angular.js',
			'bower_components/angular-mocks/angular-mocks.js',
			'src/**/*.ts',
			'test/mock/*.js'
		],

		tests: [
			'test/**/*.spec.js'
		],
		loose: true,
		testFramework: 'jasmine'
	};
};