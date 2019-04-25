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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.gatekeeper.user.UserStorage;

public class AlvinMixedUserStorageTest {
	private DataReaderSpy dataReaderForUsers;
	private UserStorageSpy userStorageForGuest;
	private UserStorage alvinMixedUserStorage;
	private String userId = "someId@ab.sdl.tld";
	private String sqlToGetUserAndRoles = "select alvinuser.*, role.group_id from alvin_seam_user alvinuser"
			+ " left join alvin_role role on alvinuser.id = role.user_id where  alvinuser.userid = ?"
			+ " and alvinuser.domain=?;";

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
		DataGroup user = alvinMixedUserStorage.getUserById("someId");
		assertEquals(userStorageForGuest.idSentToGetUserById, "someId");
		assertEquals(user, userStorageForGuest.userGroupById);
	}

	@Test
	public void testGetUserByIdFromLoginUsesDataReader() {
		alvinMixedUserStorage.getUserByIdFromLogin(userId);

		assertTrue(dataReaderForUsers.executePreparedStatementWasCalled);
		assertEquals(dataReaderForUsers.sqlSentToReader, sqlToGetUserAndRoles);
		assertEquals(dataReaderForUsers.valuesSentToReader.size(), 2);
		assertEquals(dataReaderForUsers.valuesSentToReader.get(0), "someId");
		assertEquals(dataReaderForUsers.valuesSentToReader.get(1), "sdl");
	}

	@Test
	public void testGetUserByIdFromLoginUsesDataReaderOtherIdFromLogin() {
		alvinMixedUserStorage.getUserByIdFromLogin("otherId@user.uu.se");

		assertTrue(dataReaderForUsers.executePreparedStatementWasCalled);
		assertEquals(dataReaderForUsers.sqlSentToReader, sqlToGetUserAndRoles);
		assertEquals(dataReaderForUsers.valuesSentToReader.size(), 2);
		assertEquals(dataReaderForUsers.valuesSentToReader.get(0), "otherId");
		assertEquals(dataReaderForUsers.valuesSentToReader.get(1), "uu");
	}

	@Test
	public void testGetUserByIdFromLoginReturnsDataGroupWithUserInf() {
		DataGroup userDataGroup = alvinMixedUserStorage.getUserByIdFromLogin(userId);
		assertNotNull(userDataGroup);
	}

}
