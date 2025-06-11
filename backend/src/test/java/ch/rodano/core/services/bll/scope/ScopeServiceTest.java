package ch.rodano.core.services.bll.scope;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.helpers.ScopeCreatorService;
import ch.rodano.core.helpers.builder.ScopeBuilder;
import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.enrollment.EnrollmentModel;
import ch.rodano.core.model.enrollment.SubscriptionRestriction;
import ch.rodano.core.model.scope.EnrollmentType;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.exceptions.ImpossibleScopeModelPathException;
import ch.rodano.core.model.scope.exceptions.MaxDescendantScopesReachedException;
import ch.rodano.core.services.bll.study.SubstudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class ScopeServiceTest extends DatabaseTest {

	@Autowired
	private SubstudyService substudyService;

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Autowired
	private ScopeRelationService scopeRelationService;

	@Autowired
	private ScopeCreatorService scopeCreatorService;

	@Autowired
	private TestHelperService testHelperService;

	@Test
	@DisplayName("Root scope retrieval is correct")
	public void testRootScope() {
		final var scope = scopeService.getRootScope();
		assertTrue(scopeService.isRootScope(scope));
		assertEquals("TEST", scope.getCode());
	}

	@Test
	@DisplayName("Deleted scope retrieval works correctly")
	public void testDeletedScopeRetrieval() {
		// create a patient
		final var patientScopeModel = studyService.getStudy().getScopeModel("PATIENT");
		final var parentScope = scopeDAOService.getScopeByCode("FR-01");
		final var scope = testHelperService.createPatient(parentScope, context);

		// new patient should appear in the patient scope list
		final var patientScopes = scopeService.getAll(patientScopeModel);
		assertTrue(patientScopes.stream().anyMatch(s -> Objects.equals(s.getPk(), scope.getPk())));

		// delete the patient
		scopeService.delete(scope, context, TEST_RATIONALE);

		// the patient should no longer be present in the patient list
		final var patientScopesAgain = scopeService.getAll(patientScopeModel);
		assertTrue(patientScopesAgain.stream().noneMatch(s -> Objects.equals(s.getPk(), scope.getPk())));

		// but he should still be in the database
		final var allScopes = scopeService.getAllIncludingRemoved();
		assertTrue(allScopes.stream().anyMatch(s -> Objects.equals(s.getPk(), scope.getPk())));
	}

	@Test
	@DisplayName("Scope label is generated correctly")
	public void scopeLabelTest() {
		final var scope = scopeDAOService.getScopeByCode("AT");

		// if the code is null, the codeAndShortname should be "Unknown"
		scope.setCode(null);
		assertEquals("Unknown", scope.getCodeAndShortname());

		// if the shortname is the same as the scope code, they should be coalesced in the label
		scope.setCode("AT");
		scope.setShortname("AT");
		assertEquals("AT", scope.getCodeAndShortname());
		// otherwise just display both
		scope.setShortname("Austria");
		assertEquals("AT (Austria)", scope.getCodeAndShortname());
	}

	// TODO this should be redone
	@Test
	@DisplayName("Test scope relations")
	public void testLinks() {
		final var root = scopeDAOService.getScopeByCode("Test");
		final var parent = scopeDAOService.getScopeByCode("FR");
		final var child = scopeDAOService.getScopeByCode("FR-01");
		final var brother = scopeDAOService.getScopeByCode("FR-02");
		final var descendant1 = scopeDAOService.getScopeByCode("FR-01-01");
		final var descendant2 = scopeDAOService.getScopeByCode("FR-01-03");
		final var descendant3 = scopeDAOService.getScopeByCode("AT-01-01");
		final var other = scopeDAOService.getScopeByCode("AT");

		//children
		assertEquals(12, scopeRelationService.getDescendants(root).size());

		final var study = studyService.getStudy();

		assertEquals(2, scopeRelationService.getDescendants(root, study.getScopeModel("COHORT")).size());
		assertEquals(4, scopeRelationService.getDescendants(root, study.getScopeModel("CENTER")).size());
		assertEquals(4, scopeRelationService.getDescendants(root, study.getScopeModel("PATIENT")).size());

		//ancestors
		assertEquals(5, scopeRelationService.getEnabledAncestors(descendant1).size());
		assertEquals(5, scopeRelationService.getAncestors(descendant1).size());

		assertEquals(4, scopeRelationService.getEnabledAncestors(descendant2).size());
		assertEquals(5, scopeRelationService.getAncestors(descendant2).size());

		assertEquals(3, scopeRelationService.getEnabledAncestors(descendant3).size());
		assertEquals(3, scopeRelationService.getAncestors(descendant3).size());

		assertFalse(scopeRelationService.getEnabledAncestors(descendant2).stream().anyMatch(a -> a.getPk().equals(child.getPk())));
		assertTrue(scopeRelationService.getEnabledAncestors(descendant2).stream().anyMatch(a -> a.getPk().equals(brother.getPk())));
		assertFalse(scopeRelationService.getEnabledAncestors(descendant2).stream().anyMatch(s -> s.getId().equals(child.getId())));
		assertTrue(scopeRelationService.getEnabledAncestors(descendant2).stream().anyMatch(s -> s.getId().equals(brother.getId())));

		assertEquals(2, scopeRelationService.getEnabledAncestors(brother).size());

		//relation
		assertTrue(scopeRelationService.getAllSiblings(child).stream().anyMatch(s -> s.getId().equals(brother.getId())));
		assertTrue(scopeRelationService.getAncestors(root).isEmpty());
		assertTrue(scopeRelationService.getAllParents(root).isEmpty());
		assertEquals(3, scopeRelationService.getDescendants(child).size());
		assertEquals(2, scopeRelationService.getEnabledDescendants(child).size());
		assertTrue(scopeRelationService.getAllChildren(descendant1).isEmpty());

		//assertEquals(3, child.getLeafNumber());
		assertEquals(2L, scopeService.getLeafCount(child).longValue());
		//assertEquals(1, brother.getLeafNumber());

		assertEquals(2, scopeRelationService.getShortestDistanceBetween(descendant1, root));
		assertEquals(3, scopeRelationService.getShortestDistanceBetween(descendant3, root));
		assertEquals(2, scopeRelationService.getShortestDistanceBetween(parent, descendant1));
		assertThrows(
			ScopeRelationException.class,
			() -> scopeRelationService.getShortestDistanceBetween(descendant1, other),
			"We manage to find a relation between FR-01-01 and AT"
		);

		//dispatch
		final List<Scope> descendants = new ArrayList<>();
		descendants.add(scopeDAOService.getScopeByCode("FR-01-01"));
		descendants.add(scopeDAOService.getScopeByCode("FR-01-02"));
		descendants.add(scopeDAOService.getScopeByCode("AT-01-01"));
		final List<Scope> ancestors = new ArrayList<>();
		ancestors.add(scopeDAOService.getScopeByCode("FR"));
		ancestors.add(scopeDAOService.getScopeByCode("AT"));
		final var scopesDispatch = dispatchScopeByAncestors(ancestors, descendants);
		assertEquals(2, scopesDispatch.get(scopeDAOService.getScopeByCode("FR")).size());
		assertEquals(1, scopesDispatch.get(scopeDAOService.getScopeByCode("AT")).size());

		assertTrue(scopeRelationService.isChildOf(child, parent));
		assertTrue(scopeRelationService.isDescendantOf(child.getPk(), root.getPk()));

		assertFalse(scopeRelationService.getAllChildren(child).isEmpty());
		for(final var s : scopeRelationService.getAllChildren(child)) {
			assertTrue(s.getCode().startsWith("FR-01"));
		}

		assertFalse(scopeRelationService.getDescendants(child).isEmpty());
		for(final var s : scopeRelationService.getDescendants(child)) {
			assertTrue(s.getCode().startsWith("FR-01"));
		}
	}

	@Test
	@DisplayName("Test scope enrollment targets")
	public void testEnrollment() {
		final var scope = scopeDAOService.getScopeByCode("FR-01");
		final var date2010 = ZonedDateTime.of(2010, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
		scope.getData().setEnrollmentModel(new EnrollmentModel());
		scope.getData().setEnrollmentStart(date2010);
		final var date2011 = ZonedDateTime.of(2011, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
		scope.getData().setEnrollmentStop(date2011);
		scope.setExpectedNumber(120);
		scope.updateEnrollmentTarget();

		final var scopeData = scope.getData();

		assertEquals(50, scopeData.getEnrollmentTargets().get(5).getExpectedNumber().intValue());

		final var stak = scopeData.getEnrollmentTargets().get(2).getDate();
		assertEquals(3, stak.getMonth().getValue());
	}

	// TODO we should probably test the subscription restrictions, but this tests nothing
	@Test
	@Disabled
	@DisplayName("Test subscription restrictions")
	public void stepMTestSubscriptionRestrictions() {
		final var restriction = new SubscriptionRestriction();
		restriction.setModel("Country");
		restriction.getScopeIds().add(scopeDAOService.getScopeByCode("FR").getId());
	}

	@Test
	@DisplayName("Test scope model maximum number of scopes limit")
	public void scopeCreationTest() {
		// create a patient
		final var study = studyService.getStudy();
		final var parentScope = scopeDAOService.getScopeByCode("FR-01");

		// set a limit on the max number of patients
		study.getScopeModel("PATIENT").setMaxNumber(3);

		// patient should be impossible to create
		assertThrows(
			MaxDescendantScopesReachedException.class,
			() -> testHelperService.createPatient(parentScope, context),
			"We manage to create a patient despite exceeding the established maximum number of patients"
		);
	}

	@Test
	@DisplayName("Test scope model parent-child limitation")
	public void impossibleScopePathTest() {
		// try to create a center inside a center and it should fail
		final var parentScope = scopeDAOService.getScopeByCode("FR-01");
		assertThrows(
			ImpossibleScopeModelPathException.class,
			() -> testHelperService.createCenter(parentScope, context),
			"We manage to create a center inside a center"
		);
	}

	// TODO this should be in its own separate substudy test class
	@Disabled
	@Test
	@DisplayName("Test substudy")
	public void stepZTestSubstudy() {
		final var rationale = "Test substudy";
		final var study = studyService.getStudy();

		//create scope model
		final var groupModel = new ScopeModel();
		groupModel.setId("GROUP");

		final var rootScopeModelId = study.getRootScopeModel().getId();
		groupModel.setParentIds(Collections.singletonList(rootScopeModelId));
		groupModel.setDefaultParentId(rootScopeModelId);
		groupModel.setVirtual(true);
		groupModel.setScopeFormat("${parent}-${siblingsNumber:2}");

		//add scope model in study
		groupModel.setStudy(study);
		final var scopeModels = new TreeSet<>(study.getScopeModels());
		scopeModels.add(groupModel);
		study.setScopeModels(scopeModels);

		final var substudyScopeModel = study.getScopeModel("GROUP");
		final var groupCode = scopeService.getNextCode(substudyScopeModel, scopeService.getRootScope());
		final var group = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(substudyScopeModel, scopeService.getRootScope(), groupCode));

		assertTrue(group.getVirtual());
		assertTrue(scopeRelationService.isVirtual(scopeRelationService.getDefaultParentRelation(group)));

		final var employment = study.getDatasetModel("PATIENT_DOCUMENTATION").getFieldModel("EMPLOYMENT");
		final var ac = new FieldModelCriterion(employment, Operator.EQUALS, "EMPLOYED");
		final var model = new EnrollmentModel();
		model.setCriteria(Collections.singletonList(ac));
		group.getData().setEnrollmentModel(model);
		scopeService.save(group, context, rationale);

		assertThrows(
			ScopeRelationException.class,
			() -> substudyService.enrollScopesInSubstudy(group, context, rationale),
			"We manage to enroll scopes in a non automatic substudy"
		);

		model.setType(EnrollmentType.AUTOMATIC);
		scopeService.save(group, context, rationale);

		substudyService.enrollScopesInSubstudy(group, context, rationale);
		assertTrue(scopeRelationService.getDescendants(group).isEmpty());

		model.getScopesContainerIds().add(scopeDAOService.getScopeByCode("FR-01").getId());
		substudyService.enrollScopesInSubstudy(group, context, rationale);

		final var scope = scopeDAOService.getScopeByCode("FR-01-01");
		assertTrue(scopeRelationService.isDescendantOf(scope.getPk(), group.getPk()));
		assertTrue(scopeRelationService.isChildOf(scope, group));

		assertEquals(1, scopeRelationService.getDescendants(group).size());

		model.setSystem(true);
		substudyService.enrollScopesInSubstudy(group, context, rationale);

		scopeService.delete(group, context, "Test substudy");
	}

	private Map<Scope, List<Scope>> dispatchScopeByAncestors(final Collection<Scope> ancestors, final Collection<Scope> scopes) {
		final Map<Scope, List<Scope>> results = new HashMap<>();

		// get ancestors for all scopes
		final Map<Long, Set<Long>> ancestorsByScope = scopes.stream()
			.collect(
				Collectors.toMap(
					Scope::getPk,
					scope -> scopeRelationService.getEnabledAncestors(scope).stream().map(IdentifiableObject::getPk).collect(Collectors.toSet()),
					(_, b) -> b,
					HashMap::new
				)
			);

		ancestors.forEach(ancestor -> {
			final List<Scope> scopesForAncestor = scopes.stream()
				.filter(scope -> ancestorsByScope.get(scope.getPk()).contains(ancestor.getPk()))
				.collect(Collectors.toCollection(ArrayList::new));
			results.put(ancestor, scopesForAncestor);
		});

		return results;
	}
}
