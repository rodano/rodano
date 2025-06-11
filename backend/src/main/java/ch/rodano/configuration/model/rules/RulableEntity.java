package ch.rodano.configuration.model.rules;

public enum RulableEntity {
	//pay attention to the order of the entities in this enum
	//this order is very important when a rule constraint is evaluated
	//that's because a condition may be referenced by another one and the referenced condition must be evaluated first
	//this order is the same that the one that is displayed in the configuration to the final user
	//when the user builds a constraint, he takes care of not referencing a condition that does not appear above the current condition
	//by the way, remember that the order of the values of an enum is the order or declaration
	SCOPE,
	EVENT,
	DATASET,
	FIELD,
	FORM,
	WORKFLOW
}
