package se.uu.ub.cora.alvin.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class AlvinDbToCoraUserConverterTest {

	private AlvinDbToCoraUserConverter converter;
	private Map<String, String> rowFromDb;

	// TODO: id är integer????

	@BeforeMethod
	public void beforeMethod() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", "52");
		rowFromDb.put("domain", "uu");
		rowFromDb.put("firstname", "someFirstname");
		rowFromDb.put("lastname", "someLastname");
		rowFromDb.put("userid", "user52");
		converter = new AlvinDbToCoraUserConverter();
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
		rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		Map<String, String> rowFromDb = new HashMap<>();
		rowFromDb.put("domain", "uu");
		rowFromDb.put("id", "");
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
		rowFromDb.put("id", "52");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for userid")
	public void mapDoesNotContainUserId() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", "52");
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

		assertCorrectRecordInfoWithId(user, "52");
		// assertEquals(user.getFirstAtomicValueWithNameInData("iso31661Alpha2"), "someAlpha2Code");
		// assertEquals(user.getChildren().size(), 2);
	}

	// @Test
	// public void testMinimalNullValuesReturnsDataGroupWithCorrectRecordInfo() {
	// rowFromDb.put("defaultname", null);
	// rowFromDb.put("alpha3code", null);
	// rowFromDb.put("numericalcode", null);
	// DataGroup country = converter.fromMap(rowFromDb);
	// assertEquals(country.getNameInData(), "country");
	//
	// assertCorrectRecordInfoWithId(country, "someAlpha2Code");
	// assertEquals(country.getFirstAtomicValueWithNameInData("iso31661Alpha2"), "someAlpha2Code");
	// assertEquals(country.getChildren().size(), 2);
	// }
	//
	private void assertCorrectRecordInfoWithId(DataGroup country, String id) {
		DataGroup recordInfo = country.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), id);

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
