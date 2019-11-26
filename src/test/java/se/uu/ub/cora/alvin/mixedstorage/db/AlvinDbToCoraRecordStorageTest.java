/*
 * Copyright 2018, 2019 Uppsala University Library
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.alvin.mixedstorage.user.DataReaderSpy;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

public class AlvinDbToCoraRecordStorageTest {
	private AlvinDbToCoraRecordStorage alvinToCoraRecordStorage;
	private AlvinDbToCoraConverterFactorySpy converterFactory;
	private DataReaderSpy dataReader;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new AlvinDbToCoraConverterFactorySpy();
		dataReader = new DataReaderSpy();
		alvinToCoraRecordStorage = AlvinDbToCoraRecordStorage
				.usingDataReaderAndConverterFactory(dataReader, converterFactory);
	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(alvinToCoraRecordStorage);
	}

	@Test
	public void alvinToCoraRecordStorageImplementsRecordStorage() throws Exception {
		assertTrue(alvinToCoraRecordStorage instanceof RecordStorage);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "read is not implemented for type: null")
	public void readThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.read(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "read is not implemented for type: country")
	public void testReadCountryThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.read("country", "someId");
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "User not found: notAnInt")
	public void testReadUserCallsDataReaderWithStringIdThrowsException() throws Exception {
		alvinToCoraRecordStorage.read("user", "notAnInt");
	}

	@Test
	public void testReadUserCallsDataReader() throws Exception {
		alvinToCoraRecordStorage.read("user", "53");
		assertTrue(dataReader.readOneRowWasCalled);
		assertEquals(dataReader.sqlSentToReader, "select * from alvin_seam_user where id = ?");
		List<Object> valuesSentToReader = dataReader.valuesSentToReader;
		assertEquals(valuesSentToReader.get(0), 53);
		assertEquals(valuesSentToReader.size(), 1);
	}

	@Test
	public void testReadUserUsesConverter() throws Exception {
		DataGroup user = alvinToCoraRecordStorage.read("user", "53");
		assertTrue(converterFactory.factorWasCalled);
		assertEquals(converterFactory.factoredTypes.get(0), "user");
		AlvinDbToCoraConverterSpy converter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(converter.convertedDbDataGroup, user);
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "User not found: 60000")
	public void testReadUserWhenUserNotFound() throws Exception {
		alvinToCoraRecordStorage.read("user", "60000");
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "create is not implemented")
	public void createThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.create(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "deleteByTypeAndId is not implemented")
	public void deleteByTypeAndIdThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.deleteByTypeAndId(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "linksExistForRecord is not implemented")
	public void linksExistForRecordThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.linksExistForRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "update is not implemented")
	public void updateThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.update(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: null")
	public void readListThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.readList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: country")
	public void testReadCountryListFThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.readList("country", new DataGroupSpy("filter"));
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented for type someType")
	public void readAbstractListThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("someType", null);
	}

	@Test
	public void testReadUserAbstractListCallsDataReader() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("user", new DataGroupSpy("filter"));
		assertEquals(dataReader.sqlSentToReader, "select * from alvin_seam_user");
	}

	@Test
	public void testReadUserAbstractListCallsDataReaderWithEmptyValues() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("user", new DataGroupSpy("filter"));
		assertTrue(dataReader.valuesSentToReader.isEmpty());
	}

	@Test
	public void testReadUserAbstractListConverterIsFactored() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("user", new DataGroupSpy("filter"));
		AlvinDbToCoraConverter alvinDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(alvinDbToCoraConverter);
	}

	@Test
	public void testReadUserAbstractListConverterIsCalledWithDataFromDbStorage() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("user", new DataGroupSpy("filter"));
		AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertNotNull(alvinDbToCoraConverter.mapToConvert);
		assertEquals(dataReader.listOfRows.get(0), alvinDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadUserAbstractListConverteredIsAddedToList() throws Exception {
		List<DataGroup> listOfDataGroups = alvinToCoraRecordStorage.readAbstractList("user",
				new DataGroupSpy("filter")).listOfDataGroups;
		AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(dataReader.listOfRows.size(), 2);
		assertEquals(dataReader.listOfRows.get(0), alvinDbToCoraConverter.mapToConvert);
		AlvinDbToCoraConverterSpy alvinDbToCoraConverter2 = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(1);
		assertEquals(dataReader.listOfRows.get(1), alvinDbToCoraConverter2.mapToConvert);
		assertEquals(listOfDataGroups.get(0), alvinDbToCoraConverter.convertedDbDataGroup);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readLinkList is not implemented")
	public void readLinkListThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.readLinkList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "generateLinkCollectionPointingToRecord is not implemented")
	public void generateLinkCollectionPointingToRecordThrowsNotImplementedException()
			throws Exception {
		alvinToCoraRecordStorage.generateLinkCollectionPointingToRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordsExistForRecordType is not implemented")
	public void recordsExistForRecordTypeThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.recordsExistForRecordType(null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented for notUser")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		alvinToCoraRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("notUser", null);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForUser() {
		boolean userExists = alvinToCoraRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("user", "26");
		assertTrue(dataReader.readOneRowWasCalled);
		assertEquals(dataReader.sqlSentToReader, "select * from alvin_seam_user where id = ?");
		assertTrue(userExists);
	}

	@Test
	public void recordDoesNotExistWhenIdNotAnInt() {
		boolean userExists = alvinToCoraRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("user", "notAnInt");
		assertFalse(userExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForUserWhenUserDoesNotExist() {
		boolean userExists = alvinToCoraRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("user", "60000");
		assertTrue(dataReader.readOneRowWasCalled);
		assertEquals(dataReader.sqlSentToReader, "select * from alvin_seam_user where id = ?");
		assertFalse(userExists);
	}

}
