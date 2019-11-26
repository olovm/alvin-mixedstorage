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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordNotFoundException;

public class AlvinMixedUserStorageTest {
	private DataReaderSpy dataReaderForUsers;
	private UserStorageSpy userStorageForGuest;
	private AlvinMixedUserStorage alvinMixedUserStorage;
	private String userId = "someId@ab.sld.tld";
	private String sqlToGetUserAndRoles = "select alvinuser.*, role.group_id from alvin_seam_user alvinuser"
			+ " left join alvin_role role on alvinuser.id = role.user_id where  alvinuser.userid = ?"
			+ " and alvinuser.domain=?;";
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "AlvinMixedUserStorage";
	private DataGroupFactorySpy dataGroupFactory;
	private DataAtomicFactorySpy dataAtomicFactory;

	@BeforeMethod
	public void BeforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
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
	public void testGetUserStorageForGuest() {
		assertSame(alvinMixedUserStorage.getUserStorageForGuest(), userStorageForGuest);
	}

	@Test
	public void testGetDataReaderForUsers() {
		assertSame(alvinMixedUserStorage.getDataReaderForUsers(), dataReaderForUsers);
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
		assertEquals(dataReaderForUsers.valuesSentToReader.get(1), "sld");
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
	public void testGetUserByIdFromLoginUsesDataReaderOnlySecondLevelDomainIdFromLogin() {
		alvinMixedUserStorage.getUserByIdFromLogin("otherId@uu.se");

		assertTrue(dataReaderForUsers.executePreparedStatementWasCalled);
		assertEquals(dataReaderForUsers.sqlSentToReader, sqlToGetUserAndRoles);
		assertEquals(dataReaderForUsers.valuesSentToReader.size(), 2);
		assertEquals(dataReaderForUsers.valuesSentToReader.get(0), "otherId");
		assertEquals(dataReaderForUsers.valuesSentToReader.get(1), "uu");
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "No user found for login: userIdNotFound@some.domain.org")
	public void testUserNotFound() {
		alvinMixedUserStorage.getUserByIdFromLogin("userIdNotFound@some.domain.org");
	}

	@Test(expectedExceptions = UserException.class, expectedExceptionsMessageRegExp = ""
			+ "Unrecognized format of userIdFromLogin: userId@somedomainorg")
	public void testUnexpectedFormatOfUserIdFromLogin() {
		alvinMixedUserStorage.getUserByIdFromLogin("userId@somedomainorg");
	}

	@Test
	public void testUnexpectedFormatOfUserIdFromLoginIsLogged() {
		try {
			alvinMixedUserStorage.getUserByIdFromLogin("userId@somedomainorg");
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unrecognized format of userIdFromLogin: userId@somedomainorg");
	}

	@Test
	public void testGetUserByIdFromLoginReturnsDataGroupWithUserInfo() {
		DataGroup userDataGroup = alvinMixedUserStorage.getUserByIdFromLogin(userId);
		assertNotNull(userDataGroup);
		assertEquals(userDataGroup.getNameInData(), "user");
		assertEquals(userDataGroup.getAttribute("type"), "coraUser");

		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("userId"), userId);
		DataGroup recordInfo = userDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), "52");

		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("userFirstName"),
				"SomeFirstName");
		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("userLastName"),
				"SomeLastName");
		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("activeStatus"), "active");
	}

	@Test
	public void testGetUserByIdFromLoginReturnsDataGroupWithoutNameIfMissing() {
		DataGroup userDataGroup = alvinMixedUserStorage
				.getUserByIdFromLogin("userHasNoName@some.domain.org");

		assertFalse(userDataGroup.containsChildWithNameInData("userFirstName"));
		assertFalse(userDataGroup.containsChildWithNameInData("userLastName"));
	}

	@Test
	public void testGetUserByIdFromLoginReturnsDataGroupWithRoleInfoForAdminUser() {
		alvinMixedUserStorage.getUserByIdFromLogin(userId);

		// 4 groups per userRole, 5 userRoles + main group and recordInfo group = 22
		int numOfGroupsExpected = 22;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(3);
		assertEquals(firstRoleGroup.recordId, "metadataAdmin");
		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(7);
		assertEquals(secondRoleGroup.recordId, "systemConfigurator");
		DataGroupSpy thirdRoleGroup = dataGroupFactory.factoredDataGroups.get(11);
		assertEquals(thirdRoleGroup.recordId, "binaryUserRole");
		DataGroupSpy fourthRoleGroup = dataGroupFactory.factoredDataGroups.get(15);
		assertEquals(fourthRoleGroup.recordId, "userAdminRole");
		DataGroupSpy fifthRoleGroup = dataGroupFactory.factoredDataGroups.get(19);
		assertEquals(fifthRoleGroup.recordId, "systemOneSystemUserRole");

	}

	@Test
	public void testGetUserByIdFromLoginReturnsDataGroupWithRoleInfoForNOTAdminUser() {
		alvinMixedUserStorage.getUserByIdFromLogin("userIdNotAdmin@ab.sld.tld");

		// 4 groups per userRole, 3 userRoles + main group and recordInfo group = 14
		int numOfGroupsExpected = 14;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(3);
		assertEquals(firstRoleGroup.recordId, "personAdminRole");
		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(7);
		assertEquals(secondRoleGroup.recordId, "organisationAdminRole");
		DataGroupSpy thirdRoleGroup = dataGroupFactory.factoredDataGroups.get(11);
		assertEquals(thirdRoleGroup.recordId, "metadataUserRole");
	}

	@Test
	public void testGetUserByIdFromLoginReturnsDataGroupWithRoleInfoForNOTAdminUserNoMatchingRoles() {
		alvinMixedUserStorage.getUserByIdFromLogin("userIdNotAdminNoRole@ab.sld.tld");

		// 4 groups per userRole, 1 userRoles + main group and recordInfo group = 6
		int numOfGroupsExpected = 6;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy thirdRoleGroup = dataGroupFactory.factoredDataGroups.get(3);
		assertEquals(thirdRoleGroup.recordId, "metadataUserRole");

	}

}
