package ch.rodano.api.utils;

import java.util.Optional;

import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.file.File;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;

public class URLConsistencyUtils {

	public static void checkConsistency(final Scope scope, final Event event) {
		if(!scope.getPk().equals(event.getScopeFk())) {
			throw new BadArgumentException("Scope and event parameters are not consistent");
		}
	}

	public static void checkConsistency(final Scope scope, final Optional<Event> event) {
		event.ifPresent(e -> checkConsistency(scope, e));
	}

	public static void checkConsistency(final Scope scope, final Dataset dataset) {
		if(!scope.getPk().equals(dataset.getScopeFk())) {
			throw new BadArgumentException("Scope and dataset parameters are not consistent");
		}
	}

	public static void checkConsistency(final Scope scope, final Form form) {
		if(!scope.getPk().equals(form.getScopeFk())) {
			throw new BadArgumentException("Scope and form parameters are not consistent");
		}
	}

	public static void checkConsistency(final Event event, final Dataset dataset) {
		if(!event.getPk().equals(dataset.getEventFk())) {
			throw new BadArgumentException("Event and dataset parameters are not consistent");
		}
	}

	public static void checkConsistency(final Event event, final Form form) {
		if(!event.getPk().equals(form.getEventFk())) {
			throw new BadArgumentException("Event and form parameters are not consistent");
		}
	}

	public static void checkConsistency(final Scope scope, final Optional<Event> event, final Form form) {
		if(event.isPresent()) {
			checkConsistency(scope, event.get());
			if(!event.get().getPk().equals(form.getEventFk())) {
				throw new BadArgumentException("Event and form parameters are not consistent");
			}
		}
		else {
			checkConsistency(scope, form);
		}
	}

	public static void checkConsistency(final Scope scope, final Optional<Event> event, final Dataset dataset) {
		if(event.isPresent()) {
			checkConsistency(scope, event.get());
			if(!event.get().getPk().equals(dataset.getEventFk())) {
				throw new BadArgumentException("Event and dataset parameters are not consistent");
			}
		}
		else {
			checkConsistency(scope, dataset);
		}
	}

	public static void checkConsistency(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
		checkConsistency(scope, event, dataset);
		if(!dataset.getPk().equals(field.getDatasetFk())) {
			throw new BadArgumentException("Dataset and field parameters are not consistent");
		}
	}

	public static void checkConsistency(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field, final File file) {
		checkConsistency(scope, event, dataset, field);
		if(!field.getPk().equals(file.getFieldFk())) {
			throw new BadArgumentException("Field and file parameters are not consistent");
		}
	}

	public static void checkConsistency(final User user, final Role role) {
		if(!user.getPk().equals(role.getUserFk())) {
			throw new BadArgumentException("User and role are not consistent");
		}
	}

	public static void checkConsistency(final Robot robot, final Role role) {
		if(!robot.getPk().equals(role.getRobotFk())) {
			throw new BadArgumentException("Robot and role are not consistent");
		}
	}
}
