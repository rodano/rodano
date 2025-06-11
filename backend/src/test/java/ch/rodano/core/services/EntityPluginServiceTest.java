package ch.rodano.core.services;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.plugins.TestScopeActionEntityDefiner;
import ch.rodano.core.plugins.TestStaticEntityDefiner;
import ch.rodano.core.plugins.entity.AbstractStaticEntityDefiner;
import ch.rodano.core.plugins.rules.scope.ScopeActionEntityDefiner;
import ch.rodano.core.services.plugin.entity.EntityPluginService;
import ch.rodano.core.services.rule.RulableEntityBinderService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class EntityPluginServiceTest extends DatabaseTest {
	@Autowired
	private EntityPluginService entityPluginService;

	@Autowired
	private RulableEntityBinderService rulableEntityBinderService;

	@Autowired
	private List<AbstractStaticEntityDefiner> staticEntities;

	@Autowired
	private TestScopeActionEntityDefiner testScopeActionEntityDefiner;

	@Autowired
	private ScopeActionEntityDefiner scopeActionEntityDefiner;

	@Autowired
	private TestStaticEntityDefiner testStaticEntityDefiner;

	@Test
	@DisplayName("The plugin actions are loaded")
	public void mapLoaderTest() {
		assertTrue(entityPluginService.checkPluginActionExists(RulableEntity.SCOPE, "TOTO"));
		assertTrue(entityPluginService.checkPluginActionExists(RulableEntity.SCOPE, "TITI"));
		assertTrue(entityPluginService.checkPluginActionExists(RulableEntity.SCOPE, "CHANGE_CODE"));

		final var registeredBeans = testScopeActionEntityDefiner.getRegisteredBeans();

		final var totoPlugin = registeredBeans.stream().filter(bean -> bean.getId().equals("TOTO")).findFirst().get();
		final var titiPlugin = registeredBeans.stream().filter(bean -> bean.getId().equals("TITI")).findFirst().get();

		assertSame(entityPluginService.getPluginAction(RulableEntity.SCOPE, "TOTO").getId(), totoPlugin.getId());
		assertSame(entityPluginService.getPluginAction(RulableEntity.SCOPE, "TITI").getId(), titiPlugin.getId());
	}

	@Test
	@DisplayName("Core action override works")
	public void getActionTest() {
		//Check that the plugin action is identified correctly
		assertEquals(rulableEntityBinderService.getAction(RulableEntity.SCOPE, "TOTO"), entityPluginService.getPluginAction(RulableEntity.SCOPE, "TOTO"));

		//Check that the core action is overridden correctly by the plugin
		assertEquals(rulableEntityBinderService.getAction(RulableEntity.SCOPE, "CHANGE_CODE"), entityPluginService.getPluginAction(RulableEntity.SCOPE, "CHANGE_CODE"));

		final var registeredBeans = scopeActionEntityDefiner.getRegisteredBeans();

		final var changeStatusPlugin = registeredBeans.stream()
			.filter(bean -> bean.getId().equals("INITIALIZE_WORKFLOW"))
			.findFirst()
			.get();

		//Check that the core actions still work
		assertSame(rulableEntityBinderService.getAction(RulableEntity.SCOPE, "INITIALIZE_WORKFLOW").getId(), changeStatusPlugin.getId());
	}

	@Test
	@DisplayName("Attribute existence confirmed")
	public void attributeExistsTest() {
		//Check that the core attribute check passes
		assertTrue(rulableEntityBinderService.attributeExists(RulableEntity.SCOPE, "CODE"));

		//Check that a new plugin attribute check passes
		assertTrue(rulableEntityBinderService.attributeExists(RulableEntity.SCOPE, "TOTO"));
	}

	@Test
	@DisplayName("Plugin attribute override works")
	public void pluginAttributeOverride() {
		final var pluginString = (String) rulableEntityBinderService.getAttribute(RulableEntity.SCOPE, "CODE").getValue(null);
		assertEquals("CODE OVERRIDEN BY PLUGIN", pluginString);
	}

	@Test
	@DisplayName("All static entities are present")
	public void checkStaticEntities() {
		//Check that all the core static and specific entities are present
		staticEntities.forEach(
			entity -> entity.getRegisteredBeans().stream()
				.map(bean -> entityPluginService.checkStaticPluginExists(bean.getId()))
				.forEach(Assertions::assertTrue)
		);
	}

	@Test
	@DisplayName("Static entities override works")
	public void staticEntityOverride() {
		final var registeredBeans = testStaticEntityDefiner.getRegisteredBeans();

		final var plugin = registeredBeans.stream()
			.filter(bean -> bean.getId().equals("WRITE_TO_CONSOLE"))
			.findFirst()
			.get();

		assertSame(entityPluginService.getStaticPlugin("WRITE_TO_CONSOLE").getId(), plugin.getId());
	}
}
