# rodano-configurator
The application to edit Rodano configurations.

## Install
Install the dependencies with `npm install`.

## Run
Start a server with `npm start`.

## Lint
Lint the code using `npm run lint`.

## Update cache worker
The cache worker contains an hard-coded list of files required by the application. These files must be cached by the browser. Run `npm run update-worker-cache` to update this list.

## Generate bundle
The release bundle of the application can be generated using `npm run bundle`.

## Test
Run all tests with `npm test`.

Available flags are:
* `--debug` to display result of tests
* `--no-headless` to display the browser window used to run the tests

For example, you can use `npm test -- --debug --no-headless`.

You can also run tests separately:
```
node tests/test --test=www/tools/partial_date.test.js
node tests/test --test=www/tools/custom_extension.test.js
node tests/test --test=www/tests/integration.test.js --website=www/index.html
node tests/test www/tests/suites/model_unit_tests.suite.json
node tests/test www/tests/suites/tools_unit_tests.suite.json
node tests/test www/tests/suites/integration_tests.suite.json
node tests/test www/tests/suites
```

## Changelog
See the [CHANGELOG](CHANGELOG.md) for a complete list of changes.
