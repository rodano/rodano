# rodano-frontend

The frontend of Rodano.

## Requirements

Latest LTS version of Node and NPM.

## Installation

Clone the repo and execute `npm install`.

## Development server

Run `npm start` to spin up the dev server with hot reload. Navigate to `http://localhost:4200/` to see the app.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`. More info about scaffolding can be found [here](https://angular.io/guide/schematics).

## Build

Run `npm build` to build the project. Run `npm run build:prod` for a production build. The build artifacts will be stored in the `www/` directory.

## Running unit tests

Run `npm test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Generate API typescript model

The typescript API model is generated automatically using the [openapi-generator](https://openapi-generator.tech) from the OpenAPI specifications provided by the backend server. To generate the model, the backend must run on your computer on the port 8080. Then, just execute `npm run generate-model` to generate the model.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
