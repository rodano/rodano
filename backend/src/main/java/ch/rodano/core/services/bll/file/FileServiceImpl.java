package ch.rodano.core.services.bll.file;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.file.File;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.file.FileDAOService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.FILE;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;

@Service
public class FileServiceImpl implements FileService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DSLContext create;
	private final FileDAOService fileDAOService;
	private final StudyService studyService;
	private final Configurator configurator;

	public FileServiceImpl(
		final FileDAOService fileDAOService,
		final StudyService studyService,
		final Configurator configurator,
		final DSLContext create
	) {
		this.create = create;
		this.fileDAOService = fileDAOService;
		this.studyService = studyService;
		this.configurator = configurator;
	}

	private void attacheContentToFile(final File file, final InputStream content) throws IOException {
		try {
			final var md = MessageDigest.getInstance("SHA-1");
			final var watchedContent = new DigestInputStream(content, md);

			//save file to disk and calculate checksum
			try(OutputStream os = new FileOutputStream(getStoredFile(file))) {
				watchedContent.transferTo(os);
			}
			catch(final FileNotFoundException e) {
				//we are creating the file!
				logger.error(e.getLocalizedMessage(), e);
			}

			//set checksum
			file.setChecksum(md.digest());
		}
		catch(final NoSuchAlgorithmException e) {
			//no way to come here, SHA-1 exists
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public File create(
		final User user,
		final Scope scope,
		final Optional<Event> event,
		final String name,
		final InputStream content,
		final DatabaseActionContext context,
		final String rationale
	) throws IOException {
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}
		if(event.isPresent() && event.get().getLocked()) {
			throw new LockedObjectException(event.get());
		}

		final var file = new File();
		file.setScopeFk(scope.getPk());
		event.map(Event::getPk).ifPresent(file::setEventFk);
		file.setUuid(UUID.randomUUID().toString());
		file.setUserFk(user.getPk());
		file.setName(name);

		attacheContentToFile(file, content);

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create file" : "Create file: " + rationale;
		fileDAOService.saveFile(file, context, enhancedRationale);

		return file;
	}

	@Override
	public File create(
		final User user,
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final String name,
		final InputStream content,
		final DatabaseActionContext context,
		final String rationale
	) throws IOException {
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}
		if(event.isPresent() && event.get().getLocked()) {
			throw new LockedObjectException(event.get());
		}

		final var file = new File();
		file.setScopeFk(scope.getPk());
		event.map(Event::getPk).ifPresent(file::setEventFk);
		file.setDatasetFk(dataset.getPk());
		file.setFieldFk(field.getPk());
		file.setUuid(UUID.randomUUID().toString());
		file.setUserFk(user.getPk());
		file.setName(name);

		attacheContentToFile(file, content);

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create file" : "Create file: " + rationale;
		fileDAOService.saveFile(file, context, enhancedRationale);

		return file;
	}

	private java.io.File getStoredFile(final String uuid) {
		return new java.io.File(configurator.getFileFolder(), uuid);
	}

	@Override
	public java.io.File getStoredFile(final File file) {
		return getStoredFile(file.getUuid());
	}

	@Override
	public void saveFile(final File File, final DatabaseActionContext context, final String rationale) {
		fileDAOService.saveFile(File, context, rationale);
	}

	@Override
	public void deleteFile(final File file) {
		fileDAOService.deleteFile(file);
		getStoredFile(file.getUuid()).delete();
	}

	@Override
	public File getFileByPk(final Long pk) {
		return fileDAOService.getFileByPk(pk);
	}

	@Override
	public File getFileByUUID(final String uuid) {
		return fileDAOService.getFileByUUID(uuid);
	}

	@Override
	public List<File> getFiles(final Scope scope) {
		return fileDAOService.getFileByScopePk(scope.getPk());
	}

	@Override
	public List<File> getFiles(final Event event) {
		return fileDAOService.getFileByEventPk(event.getPk());
	}

	@Override
	public List<File> getFiles(final Dataset dataset) {
		return fileDAOService.getFileByDatasetPk(dataset.getPk());
	}

	@Override
	public File getFile(final Field field) {
		return fileDAOService.getFileByFieldPk(field.getPk());
	}

	@Override
	public File getUnsubmittedFile(final Field field) {
		return fileDAOService.getUnsubmittedFileByFieldPk(field.getPk());
	}

	@Override
	public File linkFileToField(final File file, final Field field, final DatabaseActionContext context, final String rationale) {
		file.setDatasetFk(field.getDatasetFk());
		file.setFieldFk(field.getPk());
		fileDAOService.saveFile(file, context, rationale);
		return file;
	}

	@Override
	public void writeScopeFiles(final Scope scope, final Optional<ScopeModel> scopeModel, final OutputStream out) {
		final var conditions = new ArrayList<Condition>();
		conditions.add(FILE.SUBMITTED.isTrue());
		conditions.add(FILE.TRAIL_FK.isNull());
		conditions.add(SCOPE.DELETED.isFalse());
		conditions.add(SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk()));
		if(scopeModel.isPresent()) {
			conditions.add(SCOPE.SCOPE_MODEL_ID.eq(scopeModel.get().getId()));
		}
		//create role sub query
		final var query = create
			.select(
				FILE.asterisk(),
				FIELD.FIELD_MODEL_ID,
				FIELD.VALUE,
				DATASET.DATASET_MODEL_ID,
				EVENT.SCOPE_MODEL_ID,
				EVENT.EVENT_MODEL_ID,
				SCOPE.CODE
			)
			.from(FILE)
			.innerJoin(FIELD).on(FILE.FIELD_FK.eq(FIELD.PK))
			.innerJoin(DATASET).on(FILE.DATASET_FK.eq(DATASET.PK))
			.leftJoin(EVENT).on(FILE.EVENT_FK.eq(EVENT.PK))
			.innerJoin(SCOPE).on(FILE.SCOPE_FK.eq(SCOPE.PK))
			.innerJoin(SCOPE_ANCESTOR).on(FILE.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK).and(SCOPE_ANCESTOR.DEFAULT.isTrue()))
			.where(DSL.and(conditions));

		try(var zipStream = new ZipOutputStream(new BufferedOutputStream(out))) {

			query.fetchStream().forEach(r -> {
				// Get the file info from query
				final var fieldValue = r.getValue(FIELD.VALUE);
				final var fieldModelId = r.getValue(FIELD.FIELD_MODEL_ID);
				final var datasetModelId = r.getValue(DATASET.DATASET_MODEL_ID);
				final var scopeModelId = r.getValue(EVENT.SCOPE_MODEL_ID);
				final var eventModelId = r.getValue(EVENT.EVENT_MODEL_ID);
				final var scopeCode = r.getValue(SCOPE.CODE);

				// Generate the file's path name in the zip folder
				final Path path;
				if(StringUtils.isNotBlank(eventModelId)) {
					final var eventModelShortname = studyService.getStudy().getScopeModel(scopeModelId).getEventModel(eventModelId).getDefaultLocalizedShortname();
					path = Paths.get(scopeCode, eventModelShortname, datasetModelId, fieldModelId, fieldValue);
				}
				else {
					path = Paths.get(scopeCode, datasetModelId, fieldModelId, fieldValue);
				}

				final var storedFile = getStoredFile(r.getValue(FILE.UUID));
				try(var is = new FileInputStream(storedFile)) {
					// Create a zip folder entry and write the file's contents in it
					zipStream.putNextEntry(new ZipEntry(path.toString()));
					// Get the file's content binary stream
					is.transferTo(zipStream);
					// Close the zip entry and the binary stream
					zipStream.closeEntry();
				}
				catch(final IOException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			});
		}
		catch(final IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void deleteUnsubmittedFiles() {
		for(final var file : fileDAOService.getUnsubmittedFilesByLoggedOutUsers()) {
			deleteFile(file);
		}
	}
}
