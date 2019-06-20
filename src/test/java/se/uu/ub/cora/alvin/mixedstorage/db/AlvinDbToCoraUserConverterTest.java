package se.uu.ub.cora.alvin.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.alvin.mixedstorage.user.DataReaderSpy;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.sqldatabase.DataReader;

public class AlvinDbToCoraUserConverterTest {

	private AlvinDbToCoraUserConverter converter;
	private Map<String, Object> rowFromDb;
	private DataReaderSpy dataReader;

	@BeforeMethod
	public void beforeMethod() {
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

		assertCorrectRecordInfoWithId(user, 52);
		assertEquals(user.getFirstAtomicValueWithNameInData("activeStatus"), "active");

		assertFalse(user.containsChildWithNameInData("userFirstname"));
		assertFalse(user.containsChildWithNameInData("userLastname"));
		assertFalse(user.containsChildWithNameInData("email"));
	}

	@Test
	public void testCompleteUser() {
		rowFromDb.put("firstname", "johan");
		rowFromDb.put("lastname", "andersson");
		rowFromDb.put("email", "johan.andersson@ub.uu.se");
		DataGroup user = converter.fromMap(rowFromDb);
		assertEquals(user.getFirstAtomicValueWithNameInData("userFirstname"), "johan");
		assertEquals(user.getFirstAtomicValueWithNameInData("userLastname"), "andersson");
		// assertEquals(user.getFirstAtomicValueWithNameInData("email"),
		// "johan.andersson@ub.uu.se");
	}

	@Test
	public void testUser() {
		rowFromDb.put("id", 54);
		DataGroup user = converter.fromMap(rowFromDb);
		assertTrue(dataReader.executePreparedStatementWasCalled);
		assertTrue(dataReader.valuesSentToReader.contains(54));
	}

	@Test
	public void testUserRoles() {
		DataReaderRolesSpy dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		DataGroup user = converter.fromMap(rowFromDb);

		assertEquals(dataReaderRoles.sqlSentToReader,
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?");
		assertTrue(dataReaderRoles.valuesSentToReader.contains(52));

		List<DataGroup> userRoles = user.getAllGroupsWithNameInData("userRole");
		assertEquals(userRoles.size(), 4);

		assertCorrectRole(userRoles, 0, "metadataAdmin");
		assertCorrectRole(userRoles, 1, "binaryUserRole");
		assertCorrectRole(userRoles, 2, "systemConfigurator");
		assertCorrectRole(userRoles, 3, "systemOneSystemUserRole");
	}

	@Test
	public void testNoUserRoles() {
		rowFromDb.put("id", 54);
		DataReader dataReaderRoles = new DataReaderRolesSpy();
		converter = AlvinDbToCoraUserConverter.usingDataReader(dataReaderRoles);
		DataGroup user = converter.fromMap(rowFromDb);
		assertFalse(user.containsChildWithNameInData("userRole"));
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

	private void assertCorrectRecordInfoWithId(DataGroup dataGroup, int id) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), String.valueOf(id));

		DataGroup type = recordInfo.getFirstGroupWithNameInData("type");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordId"), "coraUser");

		DataGroup dataDivider = recordInfo.getFirstGroupWithNameInData("dataDivider");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordType"), "system");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordId"), "alvin");

		assertCorrectCreatedInfo(recordInfo);
		assertCorrectUpdatedInfo(recordInfo);
	}

	private void assertCorrectCreatedInfo(DataGroup recordInfo) {
		String tsCreated = recordInfo.getFirstAtomicValueWithNameInData("tsCreated");
		assertEquals(tsCreated, "2017-10-01 00:00:00.000");
		DataGroup createdBy = recordInfo.getFirstGroupWithNameInData("createdBy");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412566252284358");
	}

	private void assertCorrectUpdatedInfo(DataGroup recordInfo) {
		DataGroup updated = recordInfo.getFirstGroupWithNameInData("updated");
		DataGroup updatedBy = updated.getFirstGroupWithNameInData("updatedBy");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412566252284358");
		assertEquals(updated.getRepeatId(), "0");

		assertEquals(updated.getFirstAtomicValueWithNameInData("tsUpdated"),
				"2017-10-01 00:00:00.000");
	}

	// @Test
	// public void testMapContainsValueReturnsDataGroupWithCorrectChildren() {
	// rowFromDb.put("defaultname", "Sverige");
	// rowFromDb.put("alpha3code", "SWE");
	// rowFromDb.put("numericalcode", "752");
	// rowFromDb.put("marccode", "sw");
	// DataGroup country = converter.fromMap(rowFromDb);
	//
	// assertEquals(country.getFirstAtomicValueWithNameInData("iso31661Alpha2"), "someAlpha2Code");
	// DataGroup text = country.getFirstGroupWithNameInData("textId");
	// assertEquals(text.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraText");
	// assertEquals(text.getFirstAtomicValueWithNameInData("linkedRecordId"),
	// "countrysomeAlpha2CodeText");
	// assertEquals(country.getFirstAtomicValueWithNameInData("iso31661Alpha3"), "SWE");
	// assertEquals(country.getFirstAtomicValueWithNameInData("iso31661Numeric"), "752");
	// assertEquals(country.getFirstAtomicValueWithNameInData("marcCountryCode"), "sw");
	//
	// }

}