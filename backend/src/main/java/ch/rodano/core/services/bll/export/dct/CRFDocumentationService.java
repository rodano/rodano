package ch.rodano.core.services.bll.export.dct;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.scope.Scope;

public interface CRFDocumentationService {

	/**
	 * Generate the CRF blank
	 *
	 * @param actor        The actor asking for the report
	 * @param scopeModel   The scope model CRF to export
	 * @param annotated    Enable or disabled annotation in the CRF
	 * @param outputStream The output stream to write the report in
	 */
	void generateCRFBlank(Actor actor, ScopeModel scopeModel, boolean annotated, OutputStream outputStream);

	/**
	 * Generate the CRF archive
	 *
	 * @param actor           The actor asking for the report
	 * @param scopeModel      The scope model the descendants scopes must match to be exported
	 * @param scopes          The root scopes for the export
	 * @param withAuditTrails Include the audit trails in the archive
	 * @return An id identifying the task
	 */
	String generateCRFArchive(Actor actor, ScopeModel scopeModel, List<Scope> scopes, boolean withAuditTrails);

	/**
	 * Generate the CRF archive
	 *
	 * @param actor           The actor asking for the report
	 * @param scope           The scope to export to the report
	 * @param withAuditTrails Include the audit trails in the archive
	 * @param outputStream    The output stream to write the report in
	 * */
	void generateCRFArchive(Actor actor, Scope scope, boolean withAuditTrails, OutputStream outputStream);

	/**
	 * Get the status of the archive generation process
	 *
	 * @return Status of the archive generation process
	 */
	CRFDocumentationGenerationStatus getCRFArchiveGenerationStatus();

	/**
	 * Stream the last generated tree into a zip
	 *
	 * @param os An output stream
	 * @throws IOException Thrown if an error occurred while creating the zip file
	 */
	void streamCRFArchive(OutputStream os) throws IOException;

	/**
	 * Get the filename of the archive
	 */
	String getCRFArchiveFilename();

	/**
	 * Generate the CRF archive filename
	 *
	 * @param study The study
	 * @param scope The scope
	 * @return The filename of the CRF archive for the given scope
	 */
	String generateCRFArchiveFilename(Study study, Scope scope);
}
