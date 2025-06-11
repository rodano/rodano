package ch.rodano.core.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {
	@Test
	@DisplayName("User's equals() method works")
	public void testUserEquality() {
		//users with no email are different if there are not the same reference
		final var user1 = new User();
		assertEquals(user1, user1);
		final var user2 = new User();
		assertNotEquals(user1, user2);

		//pk is not used to determine equality
		user1.setPk(1L);
		user2.setPk(1L);
		assertNotEquals(user1, user2);

		//same emails means same user
		user1.setEmail("user1@rodano.ch");
		user2.setEmail("user1@rodano.ch");
		assertEquals(user1, user2);

		//different emails means different users
		user1.setEmail("user1@rodano.ch");
		user2.setEmail("user2@rodano.ch");
		assertNotEquals(user1, user2);
	}

	@Test
	@DisplayName("User's compareTo() method is based on user's e-mail")
	public void testUserCompareTo() {
		final var user1 = new User();
		final var user2 = new User();
		//users with no email nor pks cannot be compared
		assertThrows(NullPointerException.class, () -> user1.compareTo(user2));

		user1.setPk(1L);
		user2.setPk(2L);
		//users with no email nor pks cannot be compared
		assertThrows(NullPointerException.class, () -> user1.compareTo(user2));

		//users with no email nor pks cannot be compared
		user1.setEmail("user1@rodano.ch");
		assertThrows(NullPointerException.class, () -> user1.compareTo(user2));

		// when both users have an e-mail, users are sorted on e-mail
		user2.setEmail("user2@rodano.ch");
		assertEquals(-1, user1.compareTo(user2));
	}
}
