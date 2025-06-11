package ch.rodano.core.services.bll.scope;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeRelation;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class ScopeRelationServiceTest extends DatabaseTest {

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeRelationService scopeRelationService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Autowired
	private TestHelperService testHelperService;

	private Scope country;
	private Scope center1;
	private Scope center2;
	private Scope patient;

	@BeforeEach
	public void setup() {
		country = testHelperService.createCountry(context);
		center1 = testHelperService.createCenter(context);
		center2 = testHelperService.createCenter(context);
		patient = testHelperService.createPatient(center2, context);
	}

	@Test
	@DisplayName("Create a new relation")
	public void addRelation() {
		final var parentRelationsBefore = scopeRelationService.getAllParentRelations(center1);
		final var parentScopesBefore = scopeRelationService.getParents(center1);

		assertFalse(parentRelationsBefore.stream().anyMatch(rel -> rel.getParentFk().equals(country.getPk()) && rel.getScopeFk().equals(center1.getPk())));
		assertFalse(parentScopesBefore.stream().anyMatch(s -> s.getPk().equals(country.getPk())));

		scopeRelationService.createRelation(center1, country, context, "Add a new parent relation");

		final var parentRelationsAfter = scopeRelationService.getAllParentRelations(center1);
		final var parentScopesAfter = scopeRelationService.getParents(center1);

		assertTrue(parentRelationsAfter.stream().anyMatch(rel -> rel.getParentFk().equals(country.getPk()) && rel.getScopeFk().equals(center1.getPk())));
		assertTrue(parentScopesAfter.stream().anyMatch(s -> s.getPk().equals(country.getPk())));
	}

	@Test
	@DisplayName("Fail to delete a default relation")
	public void deleteDefaultRelation() {
		final var parentRelations = scopeRelationService.getAllParentRelations(center1);
		final var defaultRelation = parentRelations.stream()
			.filter(ScopeRelation::getDefault)
			.findFirst()
			.get();

		assertThrows(
			ScopeRelationException.class,
			() -> scopeRelationService.endRelation(defaultRelation, ZonedDateTime.now(), context, "Deleting default relation")
		);
	}

	@Test
	@DisplayName("Delete a relation")
	public void deleteRelation() {
		scopeRelationService.createRelation(center1, country, context, "Add a new parent relation");

		// End all the relations that are not default
		final var parentRelationsToDelete = scopeRelationService.getAllParentRelations(center1).stream()
			.filter(rel -> !rel.getDefault())
			.toList();
		final var deletedRelationPks = parentRelationsToDelete.stream()
			.map(IdentifiableObject::getPk)
			.toList();

		for(final var parentRel : parentRelationsToDelete) {
			scopeRelationService.endRelation(parentRel, ZonedDateTime.now(), context, "Delete relation");
		}

		// Check that only the single default relation remains
		assertEquals(scopeRelationService.getActiveParentRelations(center1, ZonedDateTime.now()).size(), 1);

		// Check that the end dates are set correctly
		for(final var relPk : deletedRelationPks) {
			final var relation = scopeRelationService.getRelationByPk(relPk);
			assertTrue(relation.getEndDate().isBefore(ZonedDateTime.now()));
		}
	}

	@Test
	@DisplayName("Virtual relation is hard deleted")
	public void deleteVirtualRelation() {
		// Make the parent scope virtual
		country.setVirtual(true);
		scopeService.save(country, context, "Make the scope virtual");

		final var newRelation = scopeRelationService.createRelation(center1, country, context, "Add a new relation");

		scopeRelationService.endRelation(newRelation, ZonedDateTime.now(), context, "Delete the virtual relation");

		// Check that the virtual relation is no longer present in the database
		assertNull(scopeRelationService.getRelationByPk(newRelation.getPk()));
	}

	@Test
	@DisplayName("Test the ancestor relation")
	public void ancestorRelation() {
		// Link the centre to the country and the patient to the centre
		final var countryCentreRelation = scopeRelationService.createRelation(center1, country, ZonedDateTime.now(), Optional.empty(), context, "New relation");
		@SuppressWarnings("unused")
		final var centerPatientRelation = scopeRelationService.createRelation(patient, center1, ZonedDateTime.now(), Optional.empty(), context, "New relation");

		// Check that the ancestors are set correctly
		final var ancestors = scopeRelationService.getEnabledAncestors(patient);
		assertTrue(ancestors.stream().anyMatch(s -> s.getPk().equals(country.getPk())));
		assertTrue(ancestors.stream().anyMatch(s -> s.getPk().equals(center1.getPk())));

		scopeRelationService.endRelation(countryCentreRelation, ZonedDateTime.now(), context, "Remove the relation");

		// Check that the patient and the country are no longer related
		final var newAncestors = scopeRelationService.getEnabledAncestors(patient);
		assertFalse(newAncestors.stream().anyMatch(s -> s.getPk().equals(country.getPk())));
		assertTrue(newAncestors.stream().anyMatch(s -> s.getPk().equals(center1.getPk())));
	}

	@Test
	@DisplayName("Test the descendant relation")
	public void descendantRelation() {
		// Link the centre to the country and the patient to the centre
		@SuppressWarnings("unused")
		final var countryCentreRelation = scopeRelationService.createRelation(center1, country, ZonedDateTime.now(), Optional.empty(), context, "New relation");
		final var centerPatientRelation = scopeRelationService.createRelation(patient, center1, ZonedDateTime.now(), Optional.empty(), context, "New relation");

		// Check that the descendants are set correctly
		final var descendants = scopeRelationService.getEnabledDescendants(country);
		assertTrue(descendants.stream().anyMatch(s -> s.getPk().equals(center1.getPk())));
		assertTrue(descendants.stream().anyMatch(s -> s.getPk().equals(patient.getPk())));

		scopeRelationService.endRelation(centerPatientRelation, ZonedDateTime.now(), context, "Remove the relation");

		// Check that the patient and the country are no longer related
		final var newDescendants = scopeRelationService.getEnabledDescendants(country);
		assertTrue(newDescendants.stream().anyMatch(s -> s.getPk().equals(center1.getPk())));
		assertFalse(newDescendants.stream().anyMatch(s -> s.getPk().equals(patient.getPk())));
	}

	@Test
	@DisplayName("Test the simple virtual relation propagation")
	public void virtualRelationPropagation() {
		final var defaultParent = scopeRelationService.getDefaultParent(patient);
		defaultParent.setVirtual(true);
		scopeService.save(defaultParent, context, "Make the scope virtual");

		// Check that the whole chain until root is virtual
		final var virtualAncestors = scopeDAOService.getVirtualAncestors(patient.getPk());
		final var allAncestorPks = scopeRelationService.getAncestors(patient)
			.stream()
			.map(IdentifiableObject::getPk)
			.toList();

		assertTrue(virtualAncestors.stream().allMatch(s -> allAncestorPks.contains(s.getPk())));
	}

	@Test
	@DisplayName("Test advanced virtual relation propagation")
	public void advancedVirtualRelationPropagation() {
		// Get the existing default relation
		final var originalAncestors = scopeRelationService.getAncestors(patient);
		final var originalAncestorPks = originalAncestors.stream()
			.map(IdentifiableObject::getPk)
			.toList();

		// Male the new parent scope virtual
		center1.setVirtual(true);
		scopeService.save(center1, context, "Make the scope virtual");
		// Add a new relation
		scopeRelationService.createRelation(patient, center1, ZonedDateTime.now(), Optional.empty(), context, "New relation");

		// Check that the relation that we marked as virtual is actually virtual
		final var virtualAncestorsPks = scopeDAOService.getVirtualAncestors(patient.getPk()).stream()
			.map(IdentifiableObject::getPk)
			.toList();
		assertEquals(1, virtualAncestorsPks.size());
		assertTrue(virtualAncestorsPks.stream().allMatch(s -> s.equals(center1.getPk())));

		// Check that the original ancestors are not marked as virtual
		assertTrue(
			scopeDAOService.getAncestors(patient.getPk()).stream()
				.filter(s -> !virtualAncestorsPks.contains(s.getPk()))
				.allMatch(s -> originalAncestorPks.contains(s.getPk()))
		);
	}

	@Test
	@DisplayName("Test the default relation propagation")
	public void defaultRelationPropagation() {
		// Get the root scope
		final var rootScope = scopeService.getRootScope();

		// Get the original parent
		final var originalParent = scopeRelationService.getDefaultParent(patient);
		// Get the original parent's parent
		scopeRelationService.getDefaultParent(originalParent);

		// Add a new relation parent and grandparent
		final var centerPatientRelation = scopeRelationService.createRelation(patient, center1, ZonedDateTime.now(), Optional.empty(), context, "New relation");
		final var countryCentreRelation = scopeRelationService.createRelation(center1, country, ZonedDateTime.now(), Optional.empty(), context, "New relation");

		// Make them default
		scopeRelationService.makeDefault(centerPatientRelation, context, "Make the new parent relation default");
		scopeRelationService.makeDefault(countryCentreRelation, context, "Make the new grandparent relation default");

		// Check that it's the new default relation and that the old parent is no longer default
		final var newDefaultAncestors = scopeRelationService.getDefaultAncestors(patient);
		assertTrue(newDefaultAncestors.stream().anyMatch(s -> s.getPk().equals(center1.getPk())));
		assertTrue(newDefaultAncestors.stream().anyMatch(s -> s.getPk().equals(country.getPk())));
		assertTrue(newDefaultAncestors.stream().anyMatch(s -> s.getPk().equals(rootScope.getPk())));

		assertTrue(newDefaultAncestors.stream().noneMatch(s -> s.getPk().equals(originalParent.getPk())));
	}
}
