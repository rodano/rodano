package ch.rodano.core.services.bll.database;

import java.util.List;

public interface DatabaseDenormalizationConsistencyService {

	List<DenormalizationInconsistencyGroup> fixInconsistencies(boolean dryRun);

}
