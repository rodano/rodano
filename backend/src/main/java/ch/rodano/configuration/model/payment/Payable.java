package ch.rodano.configuration.model.payment;

import ch.rodano.configuration.model.common.Displayable;

public interface Payable extends Displayable {
	String getPayableModelId();

	PayableModel getPayableModel();
}
