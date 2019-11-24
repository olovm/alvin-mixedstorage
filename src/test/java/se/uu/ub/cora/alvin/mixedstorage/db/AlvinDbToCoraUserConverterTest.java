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
package se.uu.ub.cora.alvin.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.alvin.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.user.DataReaderSpy;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReader;

public class AlvinDbToCoraUserConverterTest {

	private AlvinDbToCoraUserConverter converter;
	private Map<String, Object> rowFromDb;
	private DataReaderSpy dataReader;
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "AlvinDbToCoraUserConverter";
	private DataGroupFactorySpy dataGroupFactory;
	private DataAtomicFactorySpy dataAtomicFactory;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", 52);
		rowFromDb.put("domain", "uu");
		rowFromDb.put("firstname", "someFirstname");
		rowFromDb.put("lastname", "someLastname");
		rowFromDb.put("userid", "user52");

		dataReader = new DataReaderSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReader);

	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new HashMap<>();
		DataGroup user = converter.fromMap(rowFromDb);
		assertNull(user);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", null);
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("domain", "uu");
		rowFromDb.put("id", null);
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void mapDoesNotContainsId() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("domain", "uu");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for domain")
	public void mapDoesNotContainDomain() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", 52);
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for userid")
	public void mapDoesNotContainUserId() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", 52);
		rowFromDb.put("domain", "uu");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectRecordInfo() {
		rowFromDb.put("firstname", "");
		rowFromDb.put("lastname", "");
		rowFromDb.put("email", "");
		DataGroup user = converter.fromMap(rowFromDb);

		assertEquals(user.getNameInData(), "user");
		assertEquals(user.getAttribute("type"), "coraUser");

		DataGroupSpy factoredRecordInfo = dataGroupFactory.factoredDataGroups.get(1);
		assertEquals(factoredRecordInfo.nameInData, "recordInfo");
		assertSame(factoredRecordInfo, user.getFirstChildWithNameInData("recordInfo"));

		DataAtomicSpy factoredDataAtomicForId = dataAtomicFactory.factoredDataAtomics.get(1);
		assertEquals(factoredDataAtomicForId.nameInData, "id");
		assertEquals(factoredDataAtomicForId.value, String.valueOf(52));

		DataGroupSpy factoredUpdated = dataGroupFactory.factoredDataGroups.get(2);
		assertEquals(factoredUpdated.nameInData, "type");

		DataGroupSpy factoredDataDivider = dataGroupFactory.factoredDataGroups.get(3);
		assertEquals(factoredDataDivider.nameInData, "dataDivider");

		DataGroupSpy factoredCreatedInfo = dataGroupFactory.factoredDataGroups.get(4);
		assertEquals(factoredCreatedInfo.nameInData, "createdBy");

		DataGroupSpy factoredUpdatedInfo = dataGroupFactory.factoredDataGroups.get(5);
		assertEquals(factoredUpdatedInfo.nameInData, "updated");

		int numOfAtomicsFactoredWhenNamesAreMissing = 5;
		assertEquals(dataAtomicFactory.factoredDataAtomics.size(),
				numOfAtomicsFactoredWhenNamesAreMissing);

	}

	@Test
	public void testCompleteUser() {
		rowFromDb.put("firstname", "johan");
		rowFromDb.put("lastname", "andersson");
		rowFromDb.put("email", "johan.andersson@ub.uu.se");

		converter.fromMap(rowFromDb);

		int numOfAtomicsFactoredWhenNamesArePresent = 7;
		assertEquals(dataAtomicFactory.factoredDataAtomics.size(),
				numOfAtomicsFactoredWhenNamesArePresent);

		DataAtomicSpy factoredDataAtomicForFirstname = dataAtomicFactory.factoredDataAtomics.get(4);
		assertEquals(factoredDataAtomicForFirstname.nameInData, "userFirstname");
		assertEquals(factoredDataAtomicForFirstname.value, "johan");

		DataAtomicSpy factoredDataAtomicForLastname = dataAtomicFactory.factoredDataAtomics.get(5);
		assertEquals(factoredDataAtomicForLastname.nameInData, "userLastname");
		assertEquals(factoredDataAtomicForLastname.value, "andersson");

	}

	@Test
	public void testUser() {
		rowFromDb.put("id", 54);
		converter.fromMap(rowFromDb);
		assertTrue(dataReader.executePreparedStatementWasCalled);
		assertTrue(dataReader.valuesSentToReader.contains(54));
	}

	@Test
	public void testUserRolesWhenAdmin() {
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(52));

		// 4 groups per userRole, 5 userRoles + main group and 6 groups for recordInfo group = 27
		int numOfGroupsExpected = 27;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(firstRoleGroup.recordId, "metadataAdmin");
		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(12);
		assertEquals(secondRoleGroup.recordId, "binaryUserRole");
		DataGroupSpy thirdRoleGroup = dataGroupFactory.factoredDataGroups.get(16);
		assertEquals(thirdRoleGroup.recordId, "systemConfigurator");
		DataGroupSpy fourthRoleGroup = dataGroupFactory.factoredDataGroups.get(20);
		assertEquals(fourthRoleGroup.recordId, "systemOneSystemUserRole");
		DataGroupSpy fifthRoleGroup = dataGroupFactory.factoredDataGroups.get(24);
		assertEquals(fifthRoleGroup.recordId, "userAdminRole");
	}

	private void assertCorrectRole(List<DataGroup> userRoles, int index, String roleId) {
		DataGroup role = userRoles.get(index);

		DataGroup permissionTermRulePart = role
				.getFirstGroupWithNameInData("permissionTermRulePart");
		assertEquals(permissionTermRulePart.getRepeatId(), "0");

		assertDataGroupContainsCorrectPermissionTerm(permissionTermRulePart,
				"systemPermissionTerm");

		assertRulePartContainsCorrectValue(permissionTermRulePart, "system.*");

		String linkedRecordIdBinaryUserRole = extractRoleIdUsingRole(role);

		assertEquals(linkedRecordIdBinaryUserRole, roleId);
		assertEquals(role.getRepeatId(), String.valueOf(index));
	}

	private void assertDataGroupContainsCorrectPermissionTerm(DataGroup permissionTermRulePart,
			String linkedPermissionTerm) {
		DataGroup ruleLink = permissionTermRulePart.getFirstGroupWithNameInData("rule");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"collectPermissionTerm");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
				linkedPermissionTerm);
	}

	private void assertRulePartContainsCorrectValue(DataGroup permissionTermRulePart,
			String rulePartValue) {
		DataAtomic value = (DataAtomic) permissionTermRulePart.getFirstChildWithNameInData("value");
		assertEquals(value.getValue(), rulePartValue);
		assertEquals(value.getRepeatId(), "0");
	}

	private String extractRoleIdUsingRole(DataGroup dataGroup) {
		DataGroup userRole = dataGroup.getFirstGroupWithNameInData("userRole");
		String linkedRecordId = userRole.getFirstAtomicValueWithNameInData("linkedRecordId");
		return linkedRecordId;
	}

	@Test
	public void testNoUserRolesInDbStillGivesMetadataUserRole() {
		rowFromDb.put("id", 150);
		DataReader dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		// 4 groups per userRole, 1 userRoles + main group and 6 groups for recordInfo group = 11
		int numOfGroupsExpected = 11;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(secondRoleGroup.recordId, "metadataUserRole");
	}

	@Test
	public void testUserRoleUserAdmin() {
		rowFromDb.put("id", 100);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(100));

		// 4 groups per userRole, 2 userRoles + main group and 6 groups for recordInfo group = 15
		int numOfGroupsExpected = 15;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(firstRoleGroup.recordId, "userAdminRole");
		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(12);
		assertEquals(secondRoleGroup.recordId, "metadataUserRole");

	}

	@Test
	public void testLogUserRolesUserAdmin() {
		rowFromDb.put("id", 100);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Lookup for group_id 50");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found matching role userAdminRole as coraRole");

	}

	@Test
	public void testUserRolePersonAdmin() {
		rowFromDb.put("id", 101);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(101));

		// 4 groups per userRole, 2 userRoles + main group and 6 groups for recordInfo group = 15
		int numOfGroupsExpected = 15;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(firstRoleGroup.recordId, "personAdminRole");
		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(12);
		assertEquals(secondRoleGroup.recordId, "metadataUserRole");
	}

	@Test
	public void testLogUserRolesPersonAdmin() {
		rowFromDb.put("id", 101);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Lookup for group_id 51");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found matching role personAdminRole as coraRole");

	}

	@Test
	public void testUserRoleOrganisationAdmin() {
		rowFromDb.put("id", 102);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(102));

		// 4 groups per userRole, 2 userRoles + main group and 6 groups for recordInfo group = 15
		int numOfGroupsExpected = 15;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(firstRoleGroup.recordId, "organisationAdminRole");
		DataGroupSpy fifthRoleGroup = dataGroupFactory.factoredDataGroups.get(12);
		assertEquals(fifthRoleGroup.recordId, "metadataUserRole");

	}

	@Test
	public void testLogUserRolesOrganisationAdmin() {
		rowFromDb.put("id", 102);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Lookup for group_id 52");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found matching role organisationAdminRole as coraRole");

	}

	@Test
	public void testUserRolePlaceAdmin() {
		rowFromDb.put("id", 103);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		DataGroup user = converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(103));

		// 4 groups per userRole, 2 userRoles + main group and 6 groups for recordInfo group = 15
		int numOfGroupsExpected = 15;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(firstRoleGroup.recordId, "placeAdminRole");
		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(12);
		assertEquals(secondRoleGroup.recordId, "metadataUserRole");
	}

	@Test
	public void testLogUserRolesPlaceAdmin() {
		rowFromDb.put("id", 103);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Lookup for group_id 53");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found matching role placeAdminRole as coraRole");

	}

	@Test
	public void testUserRoleAllAdminRoles() {
		rowFromDb.put("id", 110);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		DataGroup user = converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(110));

		// 4 groups per userRole, 5 userRoles + main group and 6 groups for recordInfo group = 27
		int numOfGroupsExpected = 27;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy firstRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(firstRoleGroup.recordId, "userAdminRole");
		DataGroupSpy secondRoleGroup = dataGroupFactory.factoredDataGroups.get(12);
		assertEquals(secondRoleGroup.recordId, "personAdminRole");
		DataGroupSpy thirdRoleGroup = dataGroupFactory.factoredDataGroups.get(16);
		assertEquals(thirdRoleGroup.recordId, "organisationAdminRole");
		DataGroupSpy fourthRoleGroup = dataGroupFactory.factoredDataGroups.get(20);
		assertEquals(fourthRoleGroup.recordId, "placeAdminRole");
		DataGroupSpy fifthRoleGroup = dataGroupFactory.factoredDataGroups.get(24);
		assertEquals(fifthRoleGroup.recordId, "metadataUserRole");

	}

	@Test
	public void testLogUserRoleAllAdminRoles() {
		rowFromDb.put("id", 110);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		converter.fromMap(rowFromDb);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Lookup for group_id 50");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found matching role userAdminRole as coraRole");

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"Lookup for group_id 51");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 3),
				"Found matching role personAdminRole as coraRole");

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 4),
				"Lookup for group_id 52");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 5),
				"Found matching role organisationAdminRole as coraRole");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 6),
				"Lookup for group_id 53");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 7),
				"Found matching role placeAdminRole as coraRole");

	}

	@Test
	public void testUserRoleNonExistingRole() {
		rowFromDb.put("id", 1000);
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		DataGroup user = converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(1000));

		// 4 groups per userRole, 1 userRoles + main group and 6 groups for recordInfo group = 11
		int numOfGroupsExpected = 11;
		assertEquals(dataGroupFactory.factoredDataGroups.size(), numOfGroupsExpected);
		DataGroupSpy firstFactoredGroup = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(firstFactoredGroup.nameInData, "user");

		DataGroupSpy fifthRoleGroup = dataGroupFactory.factoredDataGroups.get(8);
		assertEquals(fifthRoleGroup.recordId, "metadataUserRole");

	}

}
