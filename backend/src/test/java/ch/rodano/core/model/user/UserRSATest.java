package ch.rodano.core.model.user;

import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class UserRSATest extends DatabaseTest {
	private static final String PASSWORD = "Password1!";

	@Autowired
	private RoleDAOService roleDAOService;

	@Autowired
	private UserDAOService userDAOService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserSecurityService userSecurityService;

	@Autowired
	private ScopeService scopeService;

	// TODO 2FA
	/*
	private User createUser() throws WeakPasswordException, AlreadyUsedPassword {
		final var user = User.getInstance();
	
		//main information
		user.setName("John Doe");
	
		//other information
		user.setEmail("john.doe@rodano.ch");
		userSecurityService.setValidatedPassword(user, PASSWORD);
		user.setUserData(new UserData());
	
		return user;
	}
	
	private void authenticate(final User user, final String password) throws WrongPasswordException, FeatureException, PasswordMustBeChangedException, NoNodeException, MustUseTokenException, WrongTwoStepTokenException, TooManyAttemptsException {
		userSecurityService.authenticate(user, password, null, null, null, "", context);
	}
	
	@Test
	@DisplayName("Test RSA")
	public void testRSA() throws WeakPasswordException, AlreadyUsedPassword, WrongPasswordException {
		final var user = createUser();
	
		//generate RSA keys
		try {
			userSecurityService.generateRSAKeys(user, "wrong-password");
			fail("Wrong password must not be used to generate RSA keys");
		}
		catch(final WrongPasswordException ignored) {
			//ok
		}
	
		final var pair = userSecurityService.generateRSAKeys(user, PASSWORD);
		assert pair != null;
		assertEquals(pair.getPublic(), user.getUserData().getPublicKey());
		assertEquals(pair.getPrivate(), userSecurityService.unlockPrivateKey(user, PASSWORD));
	
		//test encryption
		//encrypt and decrypt plain text
		final var message = "secret message";
	
		final var encryptedMessage = userSecurityService.encrypt(user, message.getBytes(StandardCharsets.UTF_8));
		final var decryptedMessage = userSecurityService.decrypt(user, encryptedMessage, PASSWORD);
	
		assertEquals(message, new String(decryptedMessage, StandardCharsets.UTF_8));
	
		//encrypt and decrypt random byte sequence
		final var randomData = new byte[10];
		new Random().nextBytes(randomData);
	
		assertAll("Encrypt and decrypt random byte sequence",
			() -> assertArrayEquals(userSecurityService.decrypt(user, userSecurityService.encrypt(user, randomData), PASSWORD), randomData),
			() -> assertArrayEquals(userSecurityService.decrypt(userSecurityService.encrypt(user, randomData), pair.getPrivate()), randomData));
	}
	
	@Disabled
	@Test
	@DisplayName("Test two step")
	public void testTwoStep() throws PasswordMustBeChangedException, WrongPasswordException, FeatureException, WrongDataConditionException, WrongTwoStepTokenException, MustUseTokenException, WeakPasswordException,
		AlreadyUsedPassword, NoNodeException, TooManyAttemptsException {
		final var user = createUser();
		userSecurityService.generateRSAKeys(user, PASSWORD);
		//save user and role because the must be in database for the rest of the test
		userDAOService.saveUser(user, null, TEST_RATIONALE);
	
		//add role to user to test authentication
		final var role = new Role();
		role.setProfileId("ADMIN");
		role.setUserFk(user.getPk());
		role.setScopeFk(scopeService.getRootScope().getPk());
		role.enable();
		roleDAOService.saveRole(role, null, TEST_RATIONALE);
	
		try {
			userService.enableTwoStep(user);
			fail("Two step should not be enabled when requirements are not met");
		}
		catch(final Exception ignored) {
			//ok
		}
	
		try {
			userService.disableTwoStep(user);
			fail("Two step should not be disabled when it is not enabled");
		}
		catch(final Exception ignored) {
			//ok
		}
	
		//generate two step material
		final var tsKey = userSecurityService.generateTSMaterials(user);
	
		assertAll("Generate two step material",
			() -> assertNotNull(user.getUserData().getEncryptedTwoStepKey()),
			() -> assertEquals(user.getUserData().getOneUseTwoStepCodes().size(), 5),
			() -> assertArrayEquals(tsKey, userSecurityService.decrypt(user, user.getUserData().getEncryptedTwoStepKeyAsBytes(), PASSWORD)));
	
		//two step is not enabled yet
		assertFalse(user.getUserData().isTwoStep());
		authenticate(user, PASSWORD);
	
		//enable two step
		userService.enableTwoStep(user);
		assertTrue(user.getUserData().isTwoStep());
	
		try {
			userSecurityService.authenticate(user, PASSWORD, null, null, null, "", context);
			fail("A token must be provided for authentication");
		}
		catch(final MustUseTokenException ignored) {
			//ok
		}
	
		//authenticate with one use code
		final var base32 = new Base32();
		//random code
		final var random = new byte[10];
		new Random().nextBytes(random);
		try {
			userSecurityService.authenticate(user, PASSWORD, null, base32.encodeToString(random), null, "", context);
			fail("A wrong one use code should not allow authentication");
		}
		catch(final WrongTwoStepTokenException ignored) {
			//ok
		}
	
		//real good one use code
		userSecurityService.authenticate(user, PASSWORD, null, base32.encodeToString(user.getUserData().getOneUseTwoStepCodes().get(0)), null, "", context);
	}
	 */
}
