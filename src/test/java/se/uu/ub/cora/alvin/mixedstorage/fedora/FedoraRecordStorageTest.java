/*
 * Copyright 2018, 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 * Cora is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Cora is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Cora. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.alvin.mixedstorage.fedora;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public class FedoraRecordStorageTest {
	private FedoraRecordStorage alvinToCoraRecordStorage;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private AlvinFedoraToCoraConverterFactorySpy converterFactory;
	private String baseURL = "http://alvin-cora-fedora:8088/fedora/";
	private String fedoraUsername = "fedoraUser";
	private String fedoraPassword = "fedoraPassword";

	@BeforeMethod
	public void BeforeMethod() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		converterFactory = new AlvinFedoraToCoraConverterFactorySpy();
		alvinToCoraRecordStorage = FedoraRecordStorage
				.usingHttpHandlerFactoryAndConverterFactoryAndFedoraBaseURLAndFedoraUsernameAndFedoraPassword(
						httpHandlerFactory, converterFactory, baseURL, fedoraUsername,
						fedoraPassword);
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
	public void readPlaceCallsFedoraAndReturnsConvertedResult() throws Exception {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add(createXMLForPlaceList());
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseCodes.add(200);

		DataGroup readPlace = alvinToCoraRecordStorage.read("place", "alvin-place:22");
		assertEquals(httpHandlerFactory.urls.get(0),
				baseURL + "objects?pid=true&maxResults=100&resultFormat=xml&"
						+ "query=state%3DA+pid%3Dalvin-place%3A22");
		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "GET");

		assertEquals(httpHandlerFactory.urls.get(1),
				baseURL + "objects/alvin-place:22/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 2);
		HttpHandlerSpy httpHandler2 = httpHandlerFactory.factoredHttpHandlers.get(1);
		assertEquals(httpHandler2.requestMethod, "GET");

		assertEquals(converterFactory.factoredToCoraConverters.size(), 1);
		assertEquals(converterFactory.factoredToCoraTypes.get(0), "place");
		AlvinFedoraToCoraConverterSpy alvinToCoraConverter = (AlvinFedoraToCoraConverterSpy) converterFactory.factoredToCoraConverters
				.get(0);
		assertEquals(alvinToCoraConverter.xml, httpHandlerFactory.responseTexts.get(1));
		assertEquals(readPlace, alvinToCoraConverter.convertedDataGroup);
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for type: place and id: alvin-place:22")
	public void testRecordNotFoundInStorage() throws Exception {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add(createXMLForPlaceListNoRecordsFound());
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseCodes.add(404);
		alvinToCoraRecordStorage.read("place", "alvin-place:22");
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for type: place and id: alvin-place:22")
	public void testRecordMarkedAsDeletedInStorage() throws Exception {
		httpHandlerFactory.responseTexts.add(createXMLForPlaceListNoRecordsFound());
		httpHandlerFactory.responseCodes.add(200);
		alvinToCoraRecordStorage.read("place", "alvin-place:22");
	}

	private String createXMLForPlaceListNoRecordsFound() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<result xmlns=\"http://www.fedora.info/definitions/1/0/types/\" "
				+ "xmlns:types=\"http://www.fedora.info/definitions/1/0/types/\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/types/ "
				+ "http://localhost:8088/fedora/schema/findObjects.xsd\">\n" + "  <resultList>\n"
				+ "  </resultList>\n" + "</result>";
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for type: place and id: alvin-place:22")
	public void testRecordFoundWhenLookingForNonDeletedButThenDeletedBeforeWeCanReadIt()
			throws Exception {
		httpHandlerFactory.responseTexts.add(createXMLForPlaceList());
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseCodes.add(404);
		alvinToCoraRecordStorage.read("place", "alvin-place:22");
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "create is not implemented")
	public void createThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.create(null, null, null, null, null, null);
	}

	// create object:
	// http://localhost:8089/fedora/objects/new?namespace=alvin-place&logMessage=coraWritten&label=Place'

	// create datastream
	// http://localhost:8089/fedora/objects/alvin-place:1685/datastreams/METADATA?&logMessage=coraWritten&dsLabel=Place&checksumType=SHA-512&controlGroup=M

	@Test
	public void createPlaceCreatesRecordInStorages() throws Exception {
		setUpResponsesForOkCreate();
		DataGroup record = DataGroup.withNameInData("authority");

		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		DataGroup linkList = null;
		String dataDivider = null;

		alvinToCoraRecordStorage.create("place", "alvin-place:22", record, collectedTerms, linkList,
				dataDivider);

		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 3);

		assertCorrectHttpHandlerForCreatingObject();
		assertCorrectHttpHandlerForRelation();

		assertEquals(converterFactory.factoredToFedoraConverters.size(), 1);
		assertEquals(converterFactory.factoredToFedoraTypes.get(0), "place");
		AlvinCoraToFedoraConverterSpy converter = (AlvinCoraToFedoraConverterSpy) converterFactory.factoredToFedoraConverters
				.get(0);
		assertEquals(converter.record, record);

		assertCorrectHttpHandlerForCreatingDatastream(converter);

	}

	private void setUpResponsesForOkCreate() {
		httpHandlerFactory.responseCodes.add(201);
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseCodes.add(201);
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseTexts.add("Dummy response text");
	}

	private void assertCorrectHttpHandlerForCreatingObject() throws UnsupportedEncodingException {
		HttpHandlerSpy httpHandlerForObject = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandlerForObject.requestMethod, "POST");
		String encoded = getEncodedAuthorization();

		assertEquals(httpHandlerForObject.requestProperties.get("Authorization"),
				"Basic " + encoded);

		String encodedLabel = urlEncode("Some Place Collected Name åäö");
		assertEquals(httpHandlerFactory.urls.get(0),
				baseURL + "objects/alvin-place:22?namespace=alvin-place"
						+ "&logMessage=coraWritten&label=" + encodedLabel);
		assertTrue(httpHandlerForObject.responseCodeWasRequested);
	}

	private void assertCorrectHttpHandlerForRelation() {
		HttpHandlerSpy httpHandlerForRelation = httpHandlerFactory.factoredHttpHandlers.get(1);
		assertEquals(httpHandlerForRelation.requestMethod, "POST");
		String encoded = getEncodedAuthorization();

		assertEquals(httpHandlerForRelation.requestProperties.get("Authorization"),
				"Basic " + encoded);
		assertEquals(httpHandlerFactory.urls.get(1),
				baseURL + "objects/alvin-place:22/relationships/new?object="
						+ urlEncode("info:fedora/alvin-model:place") + "&predicate="
						+ urlEncode("info:fedora/fedora-system:def/model#hasModel"));
	}

	private String getEncodedAuthorization() {
		String encoded = Base64.getEncoder().encodeToString(
				(fedoraUsername + ":" + fedoraPassword).getBytes(StandardCharsets.UTF_8));
		return encoded;
	}

	private void assertCorrectHttpHandlerForCreatingDatastream(
			AlvinCoraToFedoraConverterSpy converterSpy) throws UnsupportedEncodingException {
		HttpHandlerSpy httpHandlerForDatastream = httpHandlerFactory.factoredHttpHandlers.get(2);
		assertEquals(httpHandlerForDatastream.requestMethod, "POST");
		String encoded = getEncodedAuthorization();

		assertEquals(httpHandlerForDatastream.requestProperties.get("Authorization"),
				"Basic " + encoded);

		String encodedLabel = urlEncode("Some Place Collected Name åäö");
		assertEquals(httpHandlerFactory.urls.get(2),
				baseURL + "objects/alvin-place:22/datastreams/METADATA?controlGroup=M"
						+ "&logMessage=coraWritten&dsLabel=" + encodedLabel
						+ "&checksumType=SHA-512&mimeType=text/xml");
		assertEquals(converterSpy.returnedNewXML, httpHandlerForDatastream.outputStrings.get(0));
		assertTrue(httpHandlerForDatastream.responseCodeWasRequested);
	}

	private String urlEncode(String stringToUrlEncode) {
		try {
			return URLEncoder.encode(stringToUrlEncode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			assertTrue(false);
		}
		return null;
	}

	@Test
	public void createPlaceErrorCreatingObjectDoNotCreateRelationOrDatastreamAndThrowsException()
			throws Exception {
		setUpResponsesForObjectCreationFailure();
		DataGroup record = DataGroup.withNameInData("authority");

		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		DataGroup linkList = null;
		String dataDivider = null;
		boolean exceptionWasCaught = false;
		try {
			alvinToCoraRecordStorage.create("place", "alvin-place:22", record, collectedTerms,
					linkList, dataDivider);
		} catch (FedoraException e) {
			exceptionWasCaught = true;
			assertEquals(e.getMessage(),
					"create in fedora failed with message: creating object in fedora failed, with response code: 500");

		}
		assertTrue(exceptionWasCaught);
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 1);
		assertCorrectHttpHandlerForCreatingObject();

		assertEquals(converterFactory.factoredToFedoraConverters.size(), 0);
	}

	private void setUpResponsesForObjectCreationFailure() {
		httpHandlerFactory.responseCodes.add(500);
		httpHandlerFactory.responseTexts.add("Error from creating object");
	}

	@Test
	public void createPlaceErrorCreatingRelationDoNotCreateDatastreamAndThrowsException()
			throws Exception {
		setUpResponsesForRelationCreationFailure();
		DataGroup record = DataGroup.withNameInData("authority");

		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		DataGroup linkList = null;
		String dataDivider = null;
		boolean exceptionWasCaught = false;
		try {
			alvinToCoraRecordStorage.create("place", "alvin-place:22", record, collectedTerms,
					linkList, dataDivider);
		} catch (FedoraException e) {
			exceptionWasCaught = true;
			assertEquals(e.getMessage(),
					"create in fedora failed with message: creating relation in fedora failed, with response code: 500");

		}
		assertTrue(exceptionWasCaught);
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 2);
		assertCorrectHttpHandlerForCreatingObject();
		assertCorrectHttpHandlerForRelation();

		assertEquals(converterFactory.factoredToFedoraConverters.size(), 0);
	}

	private void setUpResponsesForRelationCreationFailure() {
		httpHandlerFactory.responseCodes.add(201);
		httpHandlerFactory.responseCodes.add(500);
		httpHandlerFactory.responseTexts.add("Dummy response");
		httpHandlerFactory.responseTexts.add("Error from creating object");
	}

	@Test
	public void createPlaceErrorCreatingDatastreamThrowsException() throws Exception {
		setUpResponsesForDatastreamFailure();
		DataGroup record = DataGroup.withNameInData("authority");

		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		DataGroup linkList = null;
		String dataDivider = null;
		boolean exceptionWasCaught = false;
		try {
			alvinToCoraRecordStorage.create("place", "alvin-place:22", record, collectedTerms,
					linkList, dataDivider);
		} catch (FedoraException e) {
			exceptionWasCaught = true;
			assertEquals(e.getMessage(),
					"create in fedora failed with message: creating datastream in fedora failed, with response code: 500");
		}
		assertTrue(exceptionWasCaught);
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 3);
		assertCorrectHttpHandlerForCreatingObject();
		assertCorrectHttpHandlerForRelation();

		assertEquals(converterFactory.factoredToFedoraConverters.size(), 1);
	}

	private void setUpResponsesForDatastreamFailure() {
		httpHandlerFactory.responseCodes.add(201);
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseCodes.add(500);
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseTexts.add("Error from creating object");
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
			+ "update is not implemented for type: someNotImplementedType")
	public void updateThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.update("someNotImplementedType", null, null, null, null, null);
	}

	@Test
	public void updateUpdatesRecordInStoragesName() throws Exception {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add("Dummy response text");
		DataGroup record = DataGroup.withNameInData("authority");

		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		DataGroup linkList = null;
		String dataDivider = null;

		alvinToCoraRecordStorage.update("place", "alvin-place:22", record, collectedTerms, linkList,
				dataDivider);

		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 1);

		String encodedLabel = URLEncoder.encode("Some Place Collected Name åäö", "UTF-8");
		assertEquals(httpHandlerFactory.urls.get(0),
				baseURL + "objects/alvin-place:22/datastreams/METADATA?format=?xml&controlGroup=M"
						+ "&logMessage=coraWritten&checksumType=SHA-512&dsLabel=" + encodedLabel);

		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "PUT");
		String encoded = Base64.getEncoder().encodeToString(
				(fedoraUsername + ":" + fedoraPassword).getBytes(StandardCharsets.UTF_8));
		assertEquals(httpHandler.requestProperties.get("Authorization"), "Basic " + encoded);

		assertEquals(converterFactory.factoredToFedoraConverters.size(), 1);
		assertEquals(converterFactory.factoredToFedoraTypes.get(0), "place");
		AlvinCoraToFedoraConverterSpy converterSpy = (AlvinCoraToFedoraConverterSpy) converterFactory.factoredToFedoraConverters
				.get(0);
		assertSame(converterSpy.record, record);
		assertEquals(converterSpy.returnedXML, httpHandler.outputStrings.get(0));
	}

	private DataGroup createCollectTermsWithRecordLabel() {
		DataGroup collectedTerms = DataGroup.withNameInData("collectedData");
		collectedTerms.addChild(DataAtomic.withNameInDataAndValue("type", "place"));
		collectedTerms.addChild(DataAtomic.withNameInDataAndValue("id", "alvin-place:22"));

		DataGroup storageTerms = DataGroup.withNameInData("storage");
		collectedTerms.addChild(storageTerms);

		DataGroup collectedRecordLabel = DataGroup.withNameInData("collectedDataTerm");
		storageTerms.addChild(collectedRecordLabel);
		collectedRecordLabel.setRepeatId("someRepeatId");
		collectedRecordLabel.addChild(
				DataAtomic.withNameInDataAndValue("collectTermId", "recordLabelStorageTerm"));
		collectedRecordLabel.addChild(DataAtomic.withNameInDataAndValue("collectTermValue",
				"Some Place Collected Name åäö"));
		return collectedTerms;
	}

	@Test
	public void updateIsMissingRecordLabelInCollectedStorageTerms() throws Exception {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add("Dummy response text");
		DataGroup record = DataGroup.withNameInData("authority");

		DataGroup collectedTerms = DataGroup.withNameInData("collectedData");
		collectedTerms.addChild(DataAtomic.withNameInDataAndValue("type", "place"));
		collectedTerms.addChild(DataAtomic.withNameInDataAndValue("id", "alvin-place:22"));

		DataGroup storageTerms = DataGroup.withNameInData("storage");
		collectedTerms.addChild(storageTerms);

		DataGroup collectedRecordLabel = DataGroup.withNameInData("collectedDataTerm");
		storageTerms.addChild(collectedRecordLabel);
		collectedRecordLabel.setRepeatId("someRepeatId");
		collectedRecordLabel.addChild(
				DataAtomic.withNameInDataAndValue("collectTermId", "NOTrecordLabelStorageTerm"));
		collectedRecordLabel.addChild(
				DataAtomic.withNameInDataAndValue("collectTermValue", "SomePlaceCollectedName"));

		DataGroup linkList = null;
		String dataDivider = null;

		alvinToCoraRecordStorage.update("place", "alvin-place:22", record, collectedTerms, linkList,
				dataDivider);

		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 1);
		assertEquals(httpHandlerFactory.urls.get(0), baseURL
				+ "objects/alvin-place:22/datastreams/METADATA?format=?xml&controlGroup=M"
				+ "&logMessage=coraWritten&checksumType=SHA-512&dsLabel=LabelNotPresentInStorageTerms");
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "update to fedora failed for record: alvin-place:22")
	public void updateIfNotOkFromFedoraThrowException() throws Exception {
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseCodes.add(500);

		DataGroup record = DataGroup.withNameInData("authority");
		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		alvinToCoraRecordStorage.update("place", "alvin-place:22", record, collectedTerms, null,
				null);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "update to fedora failed for record: alvin-place:23")
	public void updateIfNotOkFromFedoraThrowExceptionOtherRecord() throws Exception {
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseCodes.add(505);

		DataGroup record = DataGroup.withNameInData("authority");
		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		alvinToCoraRecordStorage.update("place", "alvin-place:23", record, collectedTerms, null,
				null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: null")
	public void readListThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.readList(null, null);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to read list of places: Can not read xml: "
			+ "The element type \"someTag\" must be terminated by the matching end-tag \"</someTag>\".")
	public void readListThrowsParseExceptionOnBrokenXML() throws Exception {
		httpHandlerFactory.responseTexts.add("<someTag></notSameTag>");
		httpHandlerFactory.responseCodes.add(200);
		alvinToCoraRecordStorage.readList("place", DataGroup.withNameInData("filter"));
	}

	@Test
	public void readPlaceListCallsFedoraAndReturnsConvertedResult() throws Exception {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add(createXMLForPlaceList());
		addDummyResponsesForAllObjectsInList();

		Collection<DataGroup> readPlaceList = alvinToCoraRecordStorage.readList("place",
				DataGroup.withNameInData("filter")).listOfDataGroups;
		assertEquals(httpHandlerFactory.urls.get(0), baseURL
				+ "objects?pid=true&maxResults=100&resultFormat=xml&query=state%3DA+pid%7Ealvin-place%3A*");
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 7);
		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "GET");

		assertEquals(httpHandlerFactory.urls.get(1),
				baseURL + "objects/alvin-place:22/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(2),
				baseURL + "objects/alvin-place:24/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(3),
				baseURL + "objects/alvin-place:679/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(4),
				baseURL + "objects/alvin-place:692/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(5),
				baseURL + "objects/alvin-place:15/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(6),
				baseURL + "objects/alvin-place:1684/datastreams/METADATA/content");

		assertEquals(converterFactory.factoredToCoraConverters.size(), 6);
		assertEquals(converterFactory.factoredToCoraTypes.get(0), "place");
		AlvinFedoraToCoraConverterSpy alvinToCoraConverter = (AlvinFedoraToCoraConverterSpy) converterFactory.factoredToCoraConverters
				.get(0);
		assertEquals(alvinToCoraConverter.xml, httpHandlerFactory.responseTexts.get(1));
		assertEquals(readPlaceList.size(), 6);
		Iterator<DataGroup> readPlaceIterator = readPlaceList.iterator();
		assertEquals(readPlaceIterator.next(), alvinToCoraConverter.convertedDataGroup);
	}

	private void addDummyResponsesForAllObjectsInList() {
		for (int i = 0; i < 6; i++) {
			httpHandlerFactory.responseTexts.add("Dummy response text");
			httpHandlerFactory.responseCodes.add(200);
		}
	}

	private String createXMLForPlaceList() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<result xmlns=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:types=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/types/ http://localhost:8088/fedora/schema/findObjects.xsd\">\n"
				+ "  <resultList>\n" + "  <objectFields>\n" + "      <pid>alvin-place:22</pid>\n"
				+ "  </objectFields>\n" + "  <objectFields>\n" + "      <pid>alvin-place:24</pid>\n"
				+ "  </objectFields>\n" + "  <objectFields>\n"
				+ "      <pid>alvin-place:679</pid>\n" + "  </objectFields>\n"
				+ "  <objectFields>\n" + "      <pid>alvin-place:692</pid>\n"
				+ "  </objectFields>\n" + "  <objectFields>\n" + "      <pid>alvin-place:15</pid>\n"
				+ "  </objectFields>\n" + "  <objectFields>\n"
				+ "      <pid>alvin-place:1684</pid>\n" + "  </objectFields>\n"
				+ "  </resultList>\n" + "</result>";
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented")
	public void readAbstractListThrowsNotImplementedException() throws Exception {
		alvinToCoraRecordStorage.readAbstractList(null, null);
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
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		alvinToCoraRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null,
				null);
	}
}
