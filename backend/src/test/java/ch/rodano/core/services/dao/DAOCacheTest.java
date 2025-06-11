package ch.rodano.core.services.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringTestConfiguration
public class DAOCacheTest {

	@Autowired
	private StudyService studyService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Test
	@DisplayName("Objets in the DAO cache are consistent in the running thread")
	public void testSameObjets() {
		//test when one object is returned with the same method (findUnique in the DAO)
		final var scope1 = scopeDAOService.getScopeByPk(1L);
		final var scope2 = scopeDAOService.getScopeByPk(1L);
		assertEquals("TEST", scope1.getCode());
		assertEquals("TEST", scope2.getCode());
		assertEquals(scope1, scope2);
		assertSame(scope1, scope2);

		//test when one object is returned with a different method (findUnique in the DAO)
		final var scope3 = scopeDAOService.getScopeByCode(scope1.getCode());
		assertEquals(scope1, scope3);
		assertSame(scope1, scope3);

		//test when several objects are returned (find in the DAO)
		final var scope4 = scopeDAOService.getScopesByScopeModelId(studyService.getStudy().getRootScopeModel().getId()).get(0);
		assertEquals(scope1, scope4);
		assertSame(scope1, scope4);

		final var scope5 = scopeDAOService.getAllScopes().stream().filter(s -> scope1.getCode().equals(s.getCode())).findAny().orElseThrow();
		assertEquals(scope1, scope5);
		assertSame(scope1, scope5);
	}
}
