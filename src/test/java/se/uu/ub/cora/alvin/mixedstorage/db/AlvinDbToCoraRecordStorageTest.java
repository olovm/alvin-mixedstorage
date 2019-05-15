/*
 * Copyright 2018 Uppsala University Library
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public class AlvinDbToCoraRecordStorageTest {
	private AlvinDbToCoraRecordStorage alvinToCoraRecordStorage;
	private AlvinDbToCoraConverterFactorySpy converterFactory;
	private RecordReaderFactorySpy recordReaderFactory;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new AlvinDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		alvinToCoraRecordStorage = AlvinDbToCoraRecordStorage
				.usingRecordReaderFactoryAndConverterFactory(recordReaderFactory, converterFactory);
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

	@Test
	public void testReadCountryFactorDbReader() throws Exception {
		alvinToCoraRecordStorage.read("country", "someId");
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadCountryCountryTableRequestedFromReader() throws Exception {
		alvinToCoraRecordStorage.read("country", "someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableName, "country");
	}

	@Test
	public void testReadCountryConditionsForCountryTable() throws Exception {
		alvinToCoraRecordStorage.read("country", "someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		Map<String, String> conditions = recordReader.usedConditions;
		assertEquals(conditions.get("alpha2code"), "someId");
	}

	@Test
	public void testReadCountryConverterIsFactored() throws Exception {
		alvinToCoraRecordStorage.read("country", "someId");
		AlvinDbToCoraConverter alvinDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(alvinDbToCoraConverter);
	}

	@Test
	public void testReadCountryConverterIsCalledWithDataFromDbStorage() throws Exception {
		alvinToCoraRecordStorage.read("country", "someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertNotNull(alvinDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadCountryCallsDatabaseAndReturnsConvertedResult() throws Exception {
		DataGroup readCountry = alvinToCoraRecordStorage.read("country", "someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.size(), 1);
		assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
		assertEquals(readCountry, alvinDbToCoraConverter.convertedDbDataGroup);
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

	@Test
	public void testReadCountryListFactorDbReader() throws Exception {
		alvinToCoraRecordStorage.readList("country", DataGroup.withNameInData("filter"));
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadCountryListCountryTableRequestedFromReader() throws Exception {
		alvinToCoraRecordStorage.readList("country", DataGroup.withNameInData("filter"));
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableName, "country");
	}

	@Test
	public void testReadCountryListConverterIsFactored() throws Exception {
		alvinToCoraRecordStorage.readList("country", DataGroup.withNameInData("filter"));
		AlvinDbToCoraConverter alvinDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(alvinDbToCoraConverter);
	}

	@Test
	public void testReadCountryListConverterIsCalledWithDataFromDbStorage() throws Exception {
		alvinToCoraRecordStorage.readList("country", DataGroup.withNameInData("filter"));
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertNotNull(alvinDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadCountryListConverteredIsAddedToList() throws Exception {
		List<DataGroup> readCountryList = alvinToCoraRecordStorage.readList("country",
				DataGroup.withNameInData("filter")).listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.size(), 1);
		assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
		assertEquals(readCountryList.get(0), alvinDbToCoraConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadCountryListConverteredMoreThanOneIsAddedToList() throws Exception {
		recordReaderFactory.noOfRecordsToReturn = 3;
		List<DataGroup> readCountryList = alvinToCoraRecordStorage.readList("country",
				DataGroup.withNameInData("filter")).listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.returnedList.size(), 3);

		AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
		assertEquals(readCountryList.get(0), alvinDbToCoraConverter.convertedDbDataGroup);

		AlvinDbToCoraConverterSpy alvinDbToCoraConverter2 = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(1);
		assertEquals(recordReader.returnedList.get(1), alvinDbToCoraConverter2.mapToConvert);
		assertEquals(readCountryList.get(1), alvinDbToCoraConverter2.convertedDbDataGroup);

		AlvinDbToCoraConverterSpy alvinDbToCoraConverter3 = (AlvinDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(2);
		assertEquals(recordReader.returnedList.get(2), alvinDbToCoraConverter3.mapToConvert);
		assertEquals(readCountryList.get(2), alvinDbToCoraConverter3.convertedDbDataGroup);

	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented for type someType")
	public void readAbstractListThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("someType", null);
	}

	@Test
	public void testReadUserAbstractListFactorDbReader() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("user", DataGroup.withNameInData("filter"));
		assertTrue(converterFactory.factorWasCalled);
	}

	@Test
	public void testReadUserAbstractListCountryTableRequestedFromReader() throws Exception {
		alvinToCoraRecordStorage.readAbstractList("user", DataGroup.withNameInData("filter"));
		AlvinDbToCoraConverter alvinDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertEquals(alvinDbToCoraConverter.usedTableName, "country");
	}

	// @Test
	// public void testReadUserAbstractListConverterIsFactored() throws Exception {
	// alvinToCoraRecordStorage.readList("country", DataGroup.withNameInData("filter"));
	// AlvinDbToCoraConverter alvinDbToCoraConverter = converterFactory.factoredConverters.get(0);
	// assertNotNull(alvinDbToCoraConverter);
	// }

	// @Test
	// public void testReadUserAbstractListConverterIsCalledWithDataFromDbStorage() throws Exception
	// {
	// alvinToCoraRecordStorage.readList("country", DataGroup.withNameInData("filter"));
	// RecordReaderSpy recordReader = recordReaderFactory.factored;
	// AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy)
	// converterFactory.factoredConverters
	// .get(0);
	// assertNotNull(alvinDbToCoraConverter.mapToConvert);
	// assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
	// }
	//
	// @Test
	// public void testReadUserAbstractListConverteredIsAddedToList() throws Exception {
	// List<DataGroup> readCountryList = alvinToCoraRecordStorage.readList("country",
	// DataGroup.withNameInData("filter")).listOfDataGroups;
	// RecordReaderSpy recordReader = recordReaderFactory.factored;
	// AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy)
	// converterFactory.factoredConverters
	// .get(0);
	// assertEquals(recordReader.returnedList.size(), 1);
	// assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
	// assertEquals(readCountryList.get(0), alvinDbToCoraConverter.convertedDbDataGroup);
	// }

	// @Test
	// public void testReadUserAbstractListConverteredMoreThanOneIsAddedToList() throws Exception {
	// recordReaderFactory.noOfRecordsToReturn = 3;
	// List<DataGroup> readCountryList = alvinToCoraRecordStorage.readList("country",
	// DataGroup.withNameInData("filter")).listOfDataGroups;
	// RecordReaderSpy recordReader = recordReaderFactory.factored;
	//
	// assertEquals(recordReader.returnedList.size(), 3);
	//
	// AlvinDbToCoraConverterSpy alvinDbToCoraConverter = (AlvinDbToCoraConverterSpy)
	// converterFactory.factoredConverters
	// .get(0);
	// assertEquals(recordReader.returnedList.get(0), alvinDbToCoraConverter.mapToConvert);
	// assertEquals(readCountryList.get(0), alvinDbToCoraConverter.convertedDbDataGroup);
	//
	// AlvinDbToCoraConverterSpy alvinDbToCoraConverter2 = (AlvinDbToCoraConverterSpy)
	// converterFactory.factoredConverters
	// .get(1);
	// assertEquals(recordReader.returnedList.get(1), alvinDbToCoraConverter2.mapToConvert);
	// assertEquals(readCountryList.get(1), alvinDbToCoraConverter2.convertedDbDataGroup);
	//
	// AlvinDbToCoraConverterSpy alvinDbToCoraConverter3 = (AlvinDbToCoraConverterSpy)
	// converterFactory.factoredConverters
	// .get(2);
	// assertEquals(recordReader.returnedList.get(2), alvinDbToCoraConverter3.mapToConvert);
	// assertEquals(readCountryList.get(2), alvinDbToCoraConverter3.convertedDbDataGroup);
	//
	// }

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
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		alvinToCoraRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null,
				null);
	}
}
