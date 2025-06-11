package ch.rodano.core.services.dao.file;

import java.util.Collection;
import java.util.List;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.file.File;

public interface FileDAOService {

	File getFileByPk(Long pk);

	File getFileByUUID(final String uuid);

	List<File> getFileByScopePk(Long scopePk);

	List<File> getFileByDatasetPk(Long datasetPk);

	void saveFile(File file, DatabaseActionContext context, String rationale);

	void deleteFile(File file);

	List<File> getFileByEventPk(Long eventPk);

	File getFileByFieldPk(Long fieldPk);

	/**
	 * Get submitted file associated with a given collection of field pks
	 * @param fieldPks The field's pks
	 * @return         The submitted file associated with the fields
	 */
	List<File> getFileByFieldPks(final Collection<Long> fieldPks);

	File getUnsubmittedFileByFieldPk(Long fieldPk);

	Collection<File> getUnsubmittedFiles();

	Collection<File> getUnsubmittedFilesByLoggedOutUsers();

}
