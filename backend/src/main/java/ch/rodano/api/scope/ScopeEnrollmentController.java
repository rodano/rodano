package ch.rodano.api.scope;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.study.SubstudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Scope")
@RestController
@RequestMapping("/scopes")
@Validated
@Transactional(readOnly = true)
public class ScopeEnrollmentController extends AbstractSecuredController {
	private final SubstudyService substudyService;
	private final ScopeService scopeService;
	private final ScopeRelationService scopeRelationService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;

	public ScopeEnrollmentController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final SubstudyService substudyService,
		final ScopeService scopeService,
		final ScopeRelationService scopeRelationService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.substudyService = substudyService;
		this.scopeService = scopeService;
		this.scopeRelationService = scopeRelationService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Enroll all scopes", description = "Enroll all scopes in a substudy")
	@PostMapping("{scopePk}/enrollment/enroll")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void autoEnroll(
		@PathVariable("scopePk") final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		substudyService.enrollScopesInSubstudy(scope, currentContext(), "Perform automatic enrollment");
	}

	@Operation(summary = "Unenroll all scopes", description = "Unenroll all scopes from a substudy")
	@PostMapping("{scopePk}/enrollment/unenroll")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void cleanEnroll(
		@PathVariable("scopePk") final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		for(final var relation : scopeRelationService.getChildRelations(scope)) {
			scopeRelationService.endRelation(relation, ZonedDateTime.now(), currentContext(), "Clean enrollment");
		}
	}

	@Operation(summary = "Count enroll-able scopes")
	@PostMapping("{scopePk}/enrollment/count")
	@ResponseStatus(HttpStatus.OK)
	public Integer countEnrollableScopes(
		@PathVariable("scopePk") final Long scopePk,
		@RequestBody final List<@Valid FieldModelCriterion> criteria
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//only virtual scope can have an enrollment
		if(!scope.getScopeModel().isVirtual()) {
			throw new BadArgumentException("Cannot count enrollment for non virtual scope");
		}

		for(final var criterion : criteria) {
			if(!criterion.isValid() || !criterion.hasValidValue(studyService.getStudy())) {
				throw new BadArgumentException(String.format("The following enrollment criterion is invalid: %s", criterion));
			}
		}

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		final var targetScopeModel = scope.getScopeModel().getDescendantsScopeModel().getFirst();
		return substudyService.findPotentialScopes(Collections.singleton(scopeService.getRootScope()), targetScopeModel, criteria).size();
	}
}
