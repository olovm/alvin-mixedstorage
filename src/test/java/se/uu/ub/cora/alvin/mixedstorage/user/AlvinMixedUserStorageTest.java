/*
 * Copyright 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.alvin.mixedstorage.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.gatekeeper.user.UserStorage;

public class AlvinMixedUserStorageTest {
	private DataReaderSpy dataReaderForUsers;
	private UserStorageSpy userStorageForGuest;
	private UserStorage alvinMixedUserStorage;
	private String userId = "someId";

	@BeforeMethod
	public void BeforeMethod() {
		dataReaderForUsers = new DataReaderSpy();
		userStorageForGuest = new UserStorageSpy();
		alvinMixedUserStorage = AlvinMixedUserStorage.usingUserStorageForGuestAndDataReaderForUsers(
				userStorageForGuest, dataReaderForUsers);
	}

	@Test
	public void test() {
		assertNotNull(alvinMixedUserStorage);
	}

	@Test
	public void testGetGuestUserGoesToUserStorage() {
		DataGroup userById = alvinMixedUserStorage.getUserById("someId");
		assertEquals(userStorageForGuest.idSentToGetUserById, userId);
		assertEquals(userById, userStorageForGuest.userById);
	}

	@Test
	public void testGetUserByIdFromLoginGoesUsesDataReader() {
		String sql = "select alvinuser.*, role.group_id from alvin_seam_user alvinuser"
				+ " left join alvin_role role on alvinuser.id = role.user_id where  alvinuser.userid = ?";

		DataGroup userByIdFromLogin = alvinMixedUserStorage.getUserByIdFromLogin(userId);

		assertTrue(dataReaderForUsers.executePreparedStatementWasCalled);
		assertEquals(dataReaderForUsers.sqlSentToReader, sql);
		assertEquals(dataReaderForUsers.valuesSentToReader.size(), 1);
		assertSame(dataReaderForUsers.valuesSentToReader.get(0), userId);

	}
}
