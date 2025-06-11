package ch.rodano.core.services.bll.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.file.File;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;

public interface FileService {

	File create(User user, Scope scope, Optional<Event> event, String name, InputStream content, DatabaseActionContext context, String rationale) throws IOException;

	File create(User user, Scope scope, Optional<Event> event, Dataset dataset, Field field, String name, InputStream content, DatabaseActionContext context, String rationale) throws IOException;

	void saveFile(File File, DatabaseActionContext context, String rationale);

	void deleteFile(File File);

	File getFileByPk(Long pk);

	File getFileByUUID(String uuid);

	List<File> getFiles(Scope scope);

	List<File> getFiles(Event event);

	List<File> getFiles(Dataset dataset);

	File getFile(Field field);

	File getUnsubmittedFile(Field field);

	/**
	 * Write all the submitted files of the descendants of the given scope to a given stream
	 * in form of a zip file
	 * @param scope         The ancestor scope
	 * @param scopeModel    An optional scope model to filter the returned files
	 * @param out           The output stream to which the .zip file will be written to
	 */
	void writeScopeFiles(Scope scope, Optional<ScopeModel> scopeModel, OutputStream out);

	File linkFileToField(File file, Field field, DatabaseActionContext context, String rationale);

	/**
	 * Get the storage file stream for a file
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	java.io.File getStoredFile(File file) throws FileNotFoundException;

	/**
	 * Delete all the unsubmitted files that are not linked to a user currently having a session open
	 * Warning:
	 * This method deletes the files sequentially
	 * The deletion of a file consists in 2 steps: first, the deletion of the row in the database, then the deletion of the associated file from the hard drive
	 * So we have: delete file1 row, delete file1 hdd, delete file2 row, delete file2 hdd, etc
	 * If one of the SQL queries fails, the whole SQL transaction will be rolled back, but the files that have already been deleted from the hard drive will be lost
	 * This could be improved by doing this in 2 steps: first delete all the rows in the database, then delete the orphan files from the hard drive
	 * This would require to list all the files on the hard drives that no longer have a matching in the database
	 */
	void deleteUnsubmittedFiles();

}
