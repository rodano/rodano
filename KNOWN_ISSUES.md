# Known Issues & Future Work

This document lists known bugs and features that were not completed within the scope of this thesis. They are documented here to provide a clear starting point for future development.

---

## Known Bugs

### Value Formula
The value formula editor is implemented in the configurator but does not correctly match the behavior of the old configurator. The formula logic needs to be reviewed and aligned with the original implementation.

### Event Action Rule Linking
Event action rules are correctly imported into the database via the batch import, but they are not linked correctly in the triggers view of the new configurator after a project is imported. The association between event actions and their triggers needs to be resolved during or after the batch import.

### Rule Action Conditions Display
For projects imported via the batch import, the conditions attached to rule actions are not displayed correctly in the configurator. This affects the readability and editability of imported rule configurations.

### Audit Trail Buttons Visible After Reload
Audit trail buttons are not visible on initial page load and only appear after a manual page refresh. The root cause is unknown.

### Project Metadata Not Included in Snapshots
Project metadata is potentially not saved as part of the snapshot. As a result, rolling back to a previous snapshot does not restore the original metadata values.

### Project Clone — Languages Not Copied Correctly
When cloning a project configuration, the languages associated with the original project are not set correctly on the cloned project.

### UUID / String Mismatches on the Frontend
There may be remaining frontend issues caused by UUID vs. string identifier mismatches in certain parts of the configurator. These are likely edge cases not covered by the fixes already applied.


### Multi-Tenant Concurrent Access
The application supports multiple projects, but there was no time to implement and test concurrent access by multiple users working on the same or a different project simultaneously. Concurrency handling, conflict resolution, and session isolation for the configurator have not been addressed.

### User Role Assignment Workflow
When a new user is created via the user management view, they should appear in the main application with no roles assigned, where an administrator can then assign profiles to them per project. This workflow is partially implemented but not complete. The connection between user creation and role assignment in the main application needs to be finalized.

### Incomplete Configurator Coverage
Not all entity types and features available in the old configurator have been implemented in the new one. Known gaps include:

- Configurator reports / Dahsboards
- Search functionality
- Any other sections present in the old configurator that are not yet visible in the new one