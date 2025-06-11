package ch.rodano.core.helpers.builder;

import java.time.ZonedDateTime;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.Scope;

public class ScopeBuilder {
	private final DatabaseActionContext context;
	private final ZonedDateTime date;
	private Scope parent;
	private Scope scope;

	public ScopeBuilder(final DatabaseActionContext context) {
		this(context, ZonedDateTime.now());
	}

	public ScopeBuilder(final DatabaseActionContext context, final ZonedDateTime date) {
		this.context = context;
		this.date = date;
	}

	public ScopeBuilder createScope(final ScopeModel model, final Scope parent, final String code) {
		setUpScope(model);

		scope.setCode(code);
		scope.setShortname(code);
		scope.setLongname(code);
		this.parent = parent;
		return this;
	}

	public ScopeBuilder createScope(final ScopeModel model, final Scope parent, final String code, final String name) {
		setUpScope(model);

		scope.setCode(code);
		scope.setShortname(name);
		scope.setLongname(name);
		this.parent = parent;
		return this;
	}

	private void setUpScope(final ScopeModel model) {
		scope = new Scope();
		scope.setScopeModel(model);
		scope.setVirtual(model.isVirtual());
		scope.setStartDate(date);
	}

	public Scope get() {
		return scope;
	}

	public DatabaseActionContext getContext() {
		return context;
	}

	public Scope getParent() {
		return parent;
	}
}
