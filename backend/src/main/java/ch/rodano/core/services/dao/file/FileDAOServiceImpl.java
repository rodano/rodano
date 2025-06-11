package ch.rodano.core.services.dao.file;

import java.util.Collection;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.file.File;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.FileRecord;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.FILE;
import static ch.rodano.core.model.jooq.Tables.USER_SESSION;

@Service
public class FileDAOServiceImpl extends AbstractDAOService<File, FileRecord> implements FileDAOService {

	public FileDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<FileRecord> getTable() {
		return Tables.FILE;
	}

	@Override
	protected Class<File> getDAOClass() {
		return File.class;
	}

	@Override
	public File getFileByPk(final Long pk) {
		final var query = create.selectFrom(FILE).where(FILE.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public File getFileByUUID(final String uuid) {
		final var query = create.selectFrom(FILE).where(FILE.UUID.eq(uuid));
		return findUnique(query);
	}

	@Override
	public void saveFile(final File file, final DatabaseActionContext context, final String rationale) {
		save(file, context, rationale);
	}

	@Override
	public void deleteFile(final File file) {
		delete(file);
	}

	@Override
	public List<File> getFileByScopePk(final Long scopePk) {
		final var query = create.selectFrom(FILE)
			.where(
				FILE.SCOPE_FK.eq(scopePk)
					.and(FILE.SUBMITTED.isTrue())
					.and(FILE.TRAIL_FK.isNull())
			);
		return find(query);
	}

	@Override
	public List<File> getFileByDatasetPk(final Long datasetPk) {
		final var query = create.selectFrom(FILE)
			.where(
				FILE.DATASET_FK.eq(datasetPk)
					.and(FILE.SUBMITTED.isTrue())
					.and(FILE.TRAIL_FK.isNull())
			);
		return find(query);
	}

	@Override
	public List<File> getFileByEventPk(final Long eventPk) {
		final var query = create.selectFrom(FILE)
			.where(
				FILE.EVENT_FK.eq(eventPk)
					.and(FILE.SUBMITTED.isTrue())
					.and(FILE.TRAIL_FK.isNull())
			);
		return find(query);
	}

	@Override
	public File getFileByFieldPk(final Long fieldPk) {
		final var query = create.selectFrom(FILE)
			.where(
				FILE.FIELD_FK.eq(fieldPk)
					.and(FILE.SUBMITTED.isTrue())
					.and(FILE.TRAIL_FK.isNull())
			);
		return findUnique(query);
	}

	@Override
	public List<File> getFileByFieldPks(final Collection<Long> fieldPks) {
		final var query = create.selectFrom(FILE)
			.where(
				FILE.FIELD_FK.in(fieldPks)
					.and(FILE.SUBMITTED.isTrue())
					.and(FILE.TRAIL_FK.isNull())
			);
		return find(query);
	}

	@Override
	public File getUnsubmittedFileByFieldPk(final Long fieldPk) {
		final var query = create.selectFrom(FILE)
			.where(
				FILE.FIELD_FK.eq(fieldPk)
					.and(FILE.SUBMITTED.isFalse())
			);
		return findUnique(query);
	}

	@Override
	public List<File> getUnsubmittedFiles() {
		final var query = create.selectFrom(FILE).where(FILE.SUBMITTED.isFalse());
		return find(query);
	}

	@Override
	public List<File> getUnsubmittedFilesByLoggedOutUsers() {
		final var query = create.selectFrom(FILE)
			.where(
				FILE.SUBMITTED.isFalse().and(FILE.USER_FK.notIn(create.select(USER_SESSION.USER_FK).from(USER_SESSION)))
			);
		return find(query);
	}
}
