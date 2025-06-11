# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [4.2.1] 2017-12-04
### Fixed
- Fix handling of events for charts and chart requests.
- Fix search when node have a label method but don't have any label.
- Fix integration tests.

## [4.2.0] 2017-11-27
### Added
- KV-775: Add widget "Visits due".
- KV-795: Add parameters for widget "Activity log".

### Changed
- KV-777: Remove order property of CMS widget.
- KV-778: Remove order property of CMS section.
- Global redesign of layout editor.

## [4.1.0] 2017-11-08
### Added
- Add buttons to export workflow rules and workflow diagram.

#### Changed
- Improve design of timeline graph section form.

## [4.0.0] 2017-10-04
### Changed
- First version of new era.

## 2013-05-22
### Changed
- Improve test API to do integration tests like unit tests and to generate a global report.

### Added
- Add a feature to flag some rule conditions as "break" to stop rule evaluation.

## 2013-05-15
### Changed
- Add and improve tests.

## 2013-05-07
### Changed
- Improvement of the consistency check report.
- Improve performance.

## 2013-05-01
### Changed
- Improvement of layout editor.

## 2013-04-25
### Changed
- Improvement of layout editor.
- Improvement of drag&drop.

## 2013-04-17
### Changed
- Use new REST API to manage authentication.

## 2013-04-15
### Changed
- Use new REST API to retrieve and push configuration.

## 2013-04-10
### Added
- Add "Workflow" related links for "Value" in rule engine.
- Add "Value" in "Workflow" in rule engine

## 2013-04-05
### Changed
- Improvement of the binding code between model and view.

## 2013-04-02
### Changed
- Big code reorganization to make the code more modular (as with require.js).

## 2013-03-28
### Added
- Improve attribute representation (add support for DATE_SELECT and CHECKBOX_GROUP).

## 2013-03-27
### Added
- Add advanced search.

## 2013-03-19
### Added
- Drop file in entity.

### Changed
- Code reorganization to reduce global scope pollution.

## 2013-03-08
### Changed
- Improvement of binding (model/ui) module.

## 2013-03-06
### Added
- Drag&Drop to/from a file.

### Changed
- Improve drag&drop sort.

## 2013-03-01
### Added
- Localization of rule success message.

### Changed
- Code reorganization to reduce global scope pollution.

## 2013-02-25
### Fixed
- Make the tree search works.

## 2013-02-21
### Changed
- Improvement of rules.
- Code reorganization (more modules).
- Update of the "WorkflowWidget" form.

## 2013-02-12
### Added
- Update rules when configuration changes using the event bus.

## 2013-02-11
### Changed
- Improvement of the CMS Page editor.

## 2013-02-01
### Added
- Add Drag&Drop to sort layouts in a page.

## 2013-01-31
### Changed
- Better description of node relations.

## 2013-01-30
### Changed
- Migration of layouts and cells to the new format.

### Removed
- Delete visibility criteria on attribute.

## 2013-01-29
### Added
- Check consistency report before submitting the configuration to the server.

### Changed
- Improvement of the consistency report.

## 2013-01-24
### Changed
- Improvement of the consistency report.

## 2013-01-17
### Added
- New way to create node.

## 2013-01-17
### Changed
- Rewrite sort for nodes tree.
- Improve design of forms.

## 2013-01-16
### Fixed
- Fix a bug with "WorkflowWidget" form with Chrome.

### Added
- Save nodes tree state.
- Customize display of nodes.

### Changed
- Rewrite migration script so it can be used in other applications.

## 2013-01-15
### Added
- Automatic trimming of "id", "shortname", "longname" and "description" fields for "Displayable" nodes.
- Add forms for missing entities.

## 2013-01-14
### Added
- Improve "Attribute" form.
- Display if nodes are used directly in the tree.

### Changed
- Improve nodes relational model.
