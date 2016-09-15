# Change Log
All notable changes to this project will be documented in this file.

## [v1.1.12][v1.1.12] (2016-03-26)
### Added
- Typescript definitions file - `/dist/types.d.ts`.
- Report ui-router state change with addPageAction to newrelic Insights with "duration" column in seconds.

### Bug fixes
- Don't override exceptionHandle when newrelic not installed.
- Always report error object to noticeError.
- Check $state service instead of existence on 'ui-router' module.

## [v1.1.0][v1.1.0] (2015-12-05)
### Added
- Override $exceptionHandler with report error.
- Report finished when no outstanding requests.
- Add API for custom page actions.

## [v1.0.11][v1.0.11] (2015-06-23)
### Added
- Tag a session with attributes (setCustomAttribute).

### Bug fixes
- Support applications without ui.router module.

## v1.0.0
### Added
- Report errors manually.
- Report finished event manually.
- Report ui.router state change complete.
- Report state change complete manually.

[v1.0.11]: https://github.com/wix/new-relic-angular-client/compare/v1.0.0...v1.0.11
[v1.1.0]: https://github.com/wix/new-relic-angular-client/compare/v1.0.11...v1.1.0-beta.0
[v1.1.12]: https://github.com/wix/new-relic-angular-client/compare/v1.1.0...v1.1.12
