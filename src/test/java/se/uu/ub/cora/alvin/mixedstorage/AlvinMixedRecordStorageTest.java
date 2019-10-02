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
package se.uu.ub.cora.alvin.mixedstorage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.fedora.IndexMessageInfo;
import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.storage.RecordStorage;

public class AlvinMixedRecordStorageTest {
	private RecordStorageSpy basicStorage;
	private RecordStorageSpy alvinFedoraToCoraStorage;
	private RecordStorageSpy alvinDbToCoraStorage;
	private RecordStorage alvinMixedRecordStorage;
	private IndexMessageInfo indexMessageInfo;
	private RecordIndexerFactorySpy recordIndexerFactory;
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "AlvinMixedRecordStorage";

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		basicStorage = new RecordStorageSpy();
		alvinFedoraToCoraStorage = new RecordStorageSpy();
		alvinDbToCoraStorage = new RecordStorageSpy();
		recordIndexerFactory = new RecordIndexerFactorySpy();
		indexMessageInfo = new IndexMessageInfo("someHostName", "somePort");
		alvinMixedRecordStorage = AlvinMixedRecordStorage
				.usingBasicAndFedoraAndDbStorageAndRecordIndexerFactoryAndIndexMessageInfo(
						basicStorage, alvinFedoraToCoraStorage, alvinDbToCoraStorage,
						recordIndexerFactory, indexMessageInfo);
	}

	@Test
	public void testInit() {
		assertNotNull(alvinMixedRecordStorage);
	}

	@Test
	public void alvinMixedStorageImplementsRecordStorage() {
		assertTrue(alvinMixedRecordStorage instanceof RecordStorage);
	}

	@Test
	public void readGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = alvinMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);

	}

	private void assertNoInteractionWithStorage(RecordStorageSpy recordStorageSpy) {
		assertNull(recordStorageSpy.data.type);
		assertNull(recordStorageSpy.data.id);
		assertNull(recordStorageSpy.data.calledMethod);
	}

	private void assertExpectedDataSameAsInStorageSpy(RecordStorageSpy recordStorageSpy,
			RecordStorageSpyData data) {
		RecordStorageSpyData spyData = recordStorageSpy.data;
		assertCorrectSpyData(data, spyData);
		assertEquals(spyData.filter, data.filter);
		assertEquals(spyData.record, data.record);
		assertEquals(spyData.collectedTerms, data.collectedTerms);
		assertEquals(spyData.linkList, data.linkList);
		assertEquals(spyData.dataDivider, data.dataDivider);

		assertEquals(spyData.answer, data.answer);
	}

	@Test
	public void readPlaceGoesToFedoraStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "place";
		expectedData.id = "someId";
		expectedData.answer = alvinMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(alvinFedoraToCoraStorage, expectedData);
	}

	@Test
	public void readUserNotGuestGoesToAlvinToCoraStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.id = "someUserId";
		expectedData.answer = alvinMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(alvinDbToCoraStorage, expectedData);
	}

	@Test
	public void readUserGUESTGoesToBasicStorage() throws Exception {
		AlvinDbToCoraStorageNotFoundSpy alvinDbToCoraStorageSpy = new AlvinDbToCoraStorageNotFoundSpy();
		alvinMixedRecordStorage = AlvinMixedRecordStorage
				.usingBasicAndFedoraAndDbStorageAndRecordIndexerFactoryAndIndexMessageInfo(
						basicStorage, alvinFedoraToCoraStorage, alvinDbToCoraStorageSpy, null,
						indexMessageInfo);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.id = "coraUser:5368244264733286";
		expectedData.answer = alvinMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertTrue(alvinDbToCoraStorageSpy.readWasCalled);
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
	}

	@Test
	public void readListGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.filter = DataGroup.withNameInData("filter");
		expectedData.answer = alvinMixedRecordStorage.readList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void readPlaceListGoesToAlvinToCoraStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "place";
		expectedData.filter = DataGroup.withNameInData("filter");
		expectedData.answer = alvinMixedRecordStorage.readList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readList";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(alvinFedoraToCoraStorage, expectedData);
	}

	@Test
	public void createGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.record = DataGroup.withNameInData("dummyRecord");
		expectedData.collectedTerms = DataGroup.withNameInData("collectedTerms");
		expectedData.linkList = DataGroup.withNameInData("linkList");
		expectedData.dataDivider = "someDataDivider";
		alvinMixedRecordStorage.create(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "create";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void createPlaceGoesToAlvinToCoraStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "place";
		alvinMixedRecordStorage.create(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "create";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(alvinFedoraToCoraStorage, expectedData);
	}

	@Test
	public void deleteByTypeAndIdGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		alvinMixedRecordStorage.deleteByTypeAndId(expectedData.type, expectedData.id);

		expectedData.calledMethod = "deleteByTypeAndId";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void deleteByTypeAndIdPlaceGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "place";
		expectedData.id = "somePlaceId";
		alvinMixedRecordStorage.deleteByTypeAndId(expectedData.type, expectedData.id);

		expectedData.calledMethod = "deleteByTypeAndId";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(alvinFedoraToCoraStorage, expectedData);
	}

	@Test
	public void linksExistForRecordGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = alvinMixedRecordStorage.linksExistForRecord(expectedData.type,
				expectedData.id);

		expectedData.calledMethod = "linksExistForRecord";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void updateGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.record = DataGroup.withNameInData("dummyRecord");
		expectedData.collectedTerms = DataGroup.withNameInData("collectedTerms");
		expectedData.linkList = DataGroup.withNameInData("linkList");
		expectedData.dataDivider = "someDataDivider";
		alvinMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void updatePlaceGoesToFedoraStorage() throws Exception {
		RecordStorageSpyData expectedData = setUpExpectedData();
		alvinMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(alvinFedoraToCoraStorage, expectedData);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);

	}

	@Test
	public void updatePlacesesIndexFactoryAndCallsIndexer() throws Exception {
		RecordStorageSpyData expectedData = setUpExpectedData();
		alvinMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		RecordIndexerSpy recordIndexerSpy = recordIndexerFactory.factoredRecordIndexer;
		MessageRoutingInfo messageRoutingInfo = recordIndexerFactory.messageRoutingInfo;
		assertEquals(messageRoutingInfo.hostname, indexMessageInfo.messageServerHostname);
		assertEquals(messageRoutingInfo.port, indexMessageInfo.messageServerPort);
		assertEquals(messageRoutingInfo.virtualHost, "alvin");
		assertEquals(messageRoutingInfo.exchange, "index");
		assertEquals(messageRoutingInfo.routingKey, "alvin.updates.place");

		assertEquals(recordIndexerSpy.type, expectedData.type);
		assertEquals(recordIndexerSpy.pid, expectedData.id);
		assertSame(recordIndexerSpy.messageRoutingInfo, messageRoutingInfo);
	}

	@Test
	public void testErrorWhenSendingIndexMessageToMessageServer() throws Exception {
		recordIndexerFactory.throwMessageInitializationErrorOnIndex = true;
		RecordStorageSpyData expectedData = setUpExpectedData();

		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);

		alvinMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 1);
		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error sending index message to classic for recordType:place and id:someId");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Runtime error from RecordIndexerSpy")
	public void testUnexpectedErrorWhenSendingIndexMessageToMessageServer() throws Exception {
		recordIndexerFactory.throwRuntimeErrorOnIndex = true;
		RecordStorageSpyData expectedData = setUpExpectedData();

		alvinMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);
	}

	private RecordStorageSpyData setUpExpectedData() {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "place";
		expectedData.id = "someId";
		expectedData.record = DataGroup.withNameInData("dummyRecord");
		expectedData.collectedTerms = DataGroup.withNameInData("collectedTerms");
		expectedData.linkList = DataGroup.withNameInData("linkList");
		expectedData.dataDivider = "someDataDivider";
		return expectedData;
	}

	@Test
	public void readAbstractListGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.filter = DataGroup.withNameInData("filter");
		expectedData.answer = alvinMixedRecordStorage.readAbstractList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readAbstractList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void readLinkListGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = alvinMixedRecordStorage.readLinkList(expectedData.type,
				expectedData.id);

		expectedData.calledMethod = "readLinkList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void generateLinkCollectionPointingToRecordGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = alvinMixedRecordStorage
				.generateLinkCollectionPointingToRecord(expectedData.type, expectedData.id);

		expectedData.calledMethod = "generateLinkCollectionPointingToRecord";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void recordsExistForRecordTypeGoesToBasicStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.answer = alvinMixedRecordStorage.recordsExistForRecordType(expectedData.type);

		expectedData.calledMethod = "recordsExistForRecordType";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdGoesToBasicStorage()
			throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = alvinMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
						expectedData.id);

		expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertNoInteractionWithStorage(alvinDbToCoraStorage);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForUserGoesToDbStorage()
			throws Exception {
		AlvinDbToCoraStorageSpy alvinDbToCoraStorageSpy = new AlvinDbToCoraStorageSpy();
		alvinMixedRecordStorage = AlvinMixedRecordStorage
				.usingBasicAndFedoraAndDbStorageAndRecordIndexerFactoryAndIndexMessageInfo(
						basicStorage, alvinFedoraToCoraStorage, alvinDbToCoraStorageSpy, null,
						indexMessageInfo);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.id = "someId";
		expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		boolean recordExists = alvinMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
						expectedData.id);

		assertTrue(recordExists);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);

		RecordStorageSpyData spyData = alvinDbToCoraStorageSpy.data;
		assertCorrectSpyData(expectedData, spyData);
	}

	private void assertCorrectSpyData(RecordStorageSpyData expectedData,
			RecordStorageSpyData spyData) {
		assertEquals(spyData.type, expectedData.type);
		assertEquals(spyData.id, expectedData.id);
		assertEquals(spyData.calledMethod, expectedData.calledMethod);
	}

	@Test
	public void recordExistsForAbstractOrImplementingForUserGoesToBasicStorageWhenNotFoundInDb()
			throws Exception {
		AlvinDbToCoraStorageNotFoundSpy alvinDbToCoraStorageSpy = new AlvinDbToCoraStorageNotFoundSpy();
		alvinMixedRecordStorage = AlvinMixedRecordStorage
				.usingBasicAndFedoraAndDbStorageAndRecordIndexerFactoryAndIndexMessageInfo(
						basicStorage, alvinFedoraToCoraStorage, alvinDbToCoraStorageSpy, null,
						indexMessageInfo);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.id = "coraUser:5368244264733286";
		expectedData.answer = true;
		expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";

		boolean recordExists = alvinMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
						expectedData.id);

		assertTrue(recordExists);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);

		RecordStorageSpyData spyData = alvinDbToCoraStorageSpy.data;
		assertCorrectSpyData(expectedData, spyData);

		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
	}

	@Test
	public void recordExistsForAbstractOrImplementingReturnFalseWhenNotFoundInEitherStorage()
			throws Exception {
		AlvinDbToCoraStorageNotFoundSpy alvinDbToCoraStorageSpy = new AlvinDbToCoraStorageNotFoundSpy();
		alvinMixedRecordStorage = AlvinMixedRecordStorage
				.usingBasicAndFedoraAndDbStorageAndRecordIndexerFactoryAndIndexMessageInfo(
						basicStorage, alvinFedoraToCoraStorage, alvinDbToCoraStorageSpy, null,
						indexMessageInfo);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.id = "notAUser";
		expectedData.answer = false;
		expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";

		boolean recordExists = alvinMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
						expectedData.id);

		assertFalse(recordExists);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);

		RecordStorageSpyData spyData = alvinDbToCoraStorageSpy.data;
		assertCorrectSpyData(expectedData, spyData);

		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
	}

	@Test
	public void readAbstractListForUserGoesToAlvinDBToCoraStorage() throws Exception {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.filter = DataGroup.withNameInData("filter");
		expectedData.answer = alvinMixedRecordStorage.readAbstractList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readAbstractList";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(alvinFedoraToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(alvinDbToCoraStorage, expectedData);
	}

	@Test
	public void testGetSearchTermGoesToBasicStorage() throws Exception {
		DataGroup searchTerm = ((AlvinMixedRecordStorage) alvinMixedRecordStorage)
				.getSearchTerm("someSearchTermId");
		assertEquals(basicStorage.searchTermId, "someSearchTermId");
		assertSame(searchTerm, basicStorage.returnedSearchTerm);
	}

	@Test
	public void testGetCollectIndexTermGoesToBasicStorage() throws Exception {
		DataGroup searchTerm = ((AlvinMixedRecordStorage) alvinMixedRecordStorage)
				.getCollectIndexTerm("someIndexTermId");
		assertEquals(basicStorage.indexTermId, "someIndexTermId");
		assertSame(searchTerm, basicStorage.returnedIndexTerm);
	}
}
