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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.alvin.mixedstorage.parse.XMLXPathParser;
import se.uu.ub.cora.alvin.mixedstorage.util.URLEncoder;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.spider.data.SpiderReadResult;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public final class FedoraRecordStorage implements RecordStorage {

	private static final String WITH_RESPONSE_CODE_MESSAGE_PART = ", with response code: ";
	private static final String OBJECTS_PART_OF_URL = "objects/";
	private static final String PLACE = "place";
	private HttpHandlerFactory httpHandlerFactory;
	private String baseURL;
	private AlvinFedoraConverterFactory converterFactory;
	private String fedoraUsername;
	private String fedoraPassword;

	private FedoraRecordStorage(HttpHandlerFactory httpHandlerFactory,
			AlvinFedoraConverterFactory converterFactory, String baseURL, String fedoraUsername,
			String fedoraPassword) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.converterFactory = converterFactory;
		this.baseURL = baseURL;
		this.fedoraUsername = fedoraUsername;
		this.fedoraPassword = fedoraPassword;
	}

	public static FedoraRecordStorage usingHttpHandlerFactoryAndConverterFactoryAndFedoraBaseURLAndFedoraUsernameAndFedoraPassword(
			HttpHandlerFactory httpHandlerFactory, AlvinFedoraConverterFactory converterFactory,
			String baseURL, String fedoraUsername, String fedoraPassword) {
		return new FedoraRecordStorage(httpHandlerFactory, converterFactory, baseURL,
				fedoraUsername, fedoraPassword);
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PLACE.equals(type)) {
			ensurePlaceIsNotDeleted(id);
			return readAndConvertPlaceFromFedora(id);
		}
		throw NotImplementedException.withMessage("read is not implemented for type: " + type);
	}

	private DataGroup readAndConvertPlaceFromFedora(String id) {
		HttpHandler httpHandler = createHttpHandlerForReadingPlace(id);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfRecordNotFound(id, responseCode);
		AlvinFedoraToCoraConverter toCoraConverter = converterFactory.factorToCoraConverter(PLACE);
		String responseText = httpHandler.getResponseText();
		return toCoraConverter.fromXML(responseText);
	}

	private void throwErrorIfRecordNotFound(String id, int responseCode) {
		if (404 == responseCode) {
			throw new RecordNotFoundException("Record not found for type: place and id: " + id);
		}
	}

	private void ensurePlaceIsNotDeleted(String id) {
		String query = "state=A pid=" + id;
		String placeListXML = getRecordListXMLFromFedora(query);
		NodeList list = extractNodeListWithPidsFromXML(placeListXML);
		if (0 == list.getLength()) {
			throw new RecordNotFoundException("Record not found for type: place and id: " + id);
		}
	}

	private HttpHandler createHttpHandlerForReadingPlace(String id) {
		String url = baseURL + OBJECTS_PART_OF_URL + id + "/datastreams/METADATA/content";
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (PLACE.equals(type)) {
			createPlaceInFedora(type, id, record, collectedTerms);
		} else {
			throw NotImplementedException.withMessage("create is not implemented");
		}
	}

	private void createPlaceInFedora(String type, String id, DataGroup record,
			DataGroup collectedTerms) {
		try {
			tryToConvertAndCreatePlaceInFedora(type, id, record, collectedTerms);
		} catch (Exception e) {
			throw FedoraException.withMessageAndException(
					"create in fedora failed with message: " + e.getMessage(), e);
		}
	}

	private void tryToConvertAndCreatePlaceInFedora(String type, String id, DataGroup record,
			DataGroup collectedTerms) {
		String recordLabel = getRecordLabelValueFromStorageTerms(collectedTerms);
		createObjectForPlace(id, recordLabel);
		createRelationToModelForPlace(id);
		String newXML = convertRecordToXML(type, record);
		createDatastreamForPlace(id, recordLabel, newXML);
	}

	private void createObjectForPlace(String nextPidFromFedora, String recordLabel) {
		String url = createUrlForCreatingObjectInFedora(nextPidFromFedora, recordLabel);
		HttpHandler httpHandler = createHttpHandlerForWritingUsingUrlAndRequestMethod(url, "POST");
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfUnableToCreate(responseCode, "creating object in fedora failed");
	}

	private HttpHandler createHttpHandlerForWritingUsingUrlAndRequestMethod(String url,
			String requestMethod) {
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod(requestMethod);
		setAuthorizationInHttpHandler(httpHandler);
		return httpHandler;
	}

	private String createUrlForCreatingObjectInFedora(String pid, String recordLabel) {
		String encodedDatastreamLabel = encodeLabel(recordLabel);
		return baseURL + OBJECTS_PART_OF_URL + pid + "?namespace=alvin-place"
				+ "&logMessage=coraWritten&label=" + encodedDatastreamLabel;
	}

	private String encodeLabel(String objectLabel) {
		return URLEncoder.encode(objectLabel);
	}

	private void createRelationToModelForPlace(String pid) {
		String url = createUrlForCreatingRelationInFedora(pid);
		HttpHandler httpHandler = createHttpHandlerForWritingUsingUrlAndRequestMethod(url, "POST");
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfUnableToCreateRelation(responseCode);
	}

	private void throwErrorIfUnableToCreateRelation(int responseCode) {
		if (200 != responseCode) {
			throw FedoraException.withMessage(
					"creating relation in fedora failed, with response code: " + responseCode);
		}
	}

	private String createUrlForCreatingRelationInFedora(String pid) {
		StringBuilder url = new StringBuilder(baseURL);
		url.append(OBJECTS_PART_OF_URL);
		url.append(pid);
		url.append("/relationships/new?");
		url.append("object=");
		url.append(URLEncoder.encode("info:fedora/alvin-model:place"));
		url.append("&predicate=");
		url.append(URLEncoder.encode("info:fedora/fedora-system:def/model#hasModel"));
		return url.toString();
	}

	private void setAuthorizationInHttpHandler(HttpHandler httpHandler) {
		String encoded = Base64.getEncoder().encodeToString(
				(fedoraUsername + ":" + fedoraPassword).getBytes(StandardCharsets.UTF_8));
		httpHandler.setRequestProperty("Authorization", "Basic " + encoded);
	}

	private void throwErrorIfUnableToCreate(int responseCode, String messagePart) {
		if (responseCode != 201) {
			throw FedoraException
					.withMessage(messagePart + WITH_RESPONSE_CODE_MESSAGE_PART + responseCode);
		}
	}

	private String convertRecordToXML(String type, DataGroup record) {
		AlvinCoraToFedoraConverter converter = converterFactory.factorToFedoraConverter(type);
		return converter.toNewXML(record);
	}

	private void createDatastreamForPlace(String nextPidFromFedora, String recordLabel,
			String newXML) {
		String url = createUrlForCreatingDatastreamInFedora(nextPidFromFedora, recordLabel);
		HttpHandler httpHandler = createHttpHandlerForWritingUsingUrlAndRequestMethod(url, "POST");

		httpHandler.setOutput(newXML);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfUnableToCreate(responseCode, "creating datastream in fedora failed");
	}

	private String createUrlForCreatingDatastreamInFedora(String nextPidFromFedora,
			String recordLabel) {
		String encodedDatastreamLabel = encodeLabel(recordLabel);
		return baseURL + OBJECTS_PART_OF_URL + nextPidFromFedora
				+ "/datastreams/METADATA?controlGroup=M" + "&logMessage=coraWritten&dsLabel="
				+ encodedDatastreamLabel + "&checksumType=SHA-512&mimeType=text/xml";
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		throw NotImplementedException.withMessage("deleteByTypeAndId is not implemented");
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		throw NotImplementedException.withMessage("linksExistForRecord is not implemented");
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (PLACE.equals(type)) {
			convertAndWritePlaceToFedora(type, id, record, collectedTerms);
		} else {
			throw NotImplementedException
					.withMessage("update is not implemented for type: " + type);
		}
	}

	private void convertAndWritePlaceToFedora(String type, String id, DataGroup record,
			DataGroup collectedTerms) {
		try {
			tryToConvertAndWritePlaceToFedora(type, id, record, collectedTerms);
		} catch (Exception e) {
			throw FedoraException
					.withMessageAndException("update to fedora failed for record: " + id, e);
		}
	}

	private void tryToConvertAndWritePlaceToFedora(String type, String id, DataGroup record,
			DataGroup collectedTerms) {
		String url = createUrlForWritingMetadataStreamToFedora(id, collectedTerms);
		HttpHandler httpHandler = createHttpHandlerForWritingUsingUrlAndRequestMethod(url, "PUT");

		String fedoraXML = convertRecordToFedoraXML(type, record);
		httpHandler.setOutput(fedoraXML);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfNotOkFromFedora(id, responseCode);
	}

	private void throwErrorIfNotOkFromFedora(String id, int responseCode) {
		if (200 != responseCode) {
			throw FedoraException.withMessage("update to fedora failed for record: " + id
					+ WITH_RESPONSE_CODE_MESSAGE_PART + responseCode);
		}
	}

	private String createUrlForWritingMetadataStreamToFedora(String id, DataGroup collectedTerms) {
		String datastreamLabel = getRecordLabelValueFromStorageTerms(collectedTerms);
		String encodedDatastreamLabel = encodeLabel(datastreamLabel);
		return baseURL + OBJECTS_PART_OF_URL + id
				+ "/datastreams/METADATA?format=?xml&controlGroup=M"
				+ "&logMessage=coraWritten&checksumType=SHA-512&dsLabel=" + encodedDatastreamLabel;
	}

	private String getRecordLabelValueFromStorageTerms(DataGroup collectedTerms) {
		DataGroup storageGroup = collectedTerms.getFirstGroupWithNameInData("storage");
		List<DataGroup> collectedDataTerms = storageGroup
				.getAllGroupsWithNameInData("collectedDataTerm");
		Optional<DataGroup> firstGroupWithRecordLabelStorageTerm = collectedDataTerms.stream()
				.filter(filterByCollectTermId()).findFirst();
		return getRecordLabelFromCollectedTermsOrDefaultLabel(firstGroupWithRecordLabelStorageTerm);
	}

	private String getRecordLabelFromCollectedTermsOrDefaultLabel(
			Optional<DataGroup> firstGroupWithRecordLabelStorageTerm) {
		if (firstGroupWithRecordLabelStorageTerm.isPresent()) {
			return firstGroupWithRecordLabelStorageTerm.get()
					.getFirstAtomicValueWithNameInData("collectTermValue");
		}
		return "LabelNotPresentInStorageTerms";
	}

	private Predicate<DataGroup> filterByCollectTermId() {
		return this::collectedDataTermIsRecordLabel;
	}

	private boolean collectedDataTermIsRecordLabel(DataGroup collectedDataTerm) {
		String collectTermId = collectedDataTerm.getFirstAtomicValueWithNameInData("collectTermId");
		return collectTermId.equals("recordLabelStorageTerm");
	}

	private String convertRecordToFedoraXML(String type, DataGroup record) {
		AlvinCoraToFedoraConverter converter = converterFactory.factorToFedoraConverter(type);
		return converter.toXML(record);
	}

	@Override
	public SpiderReadResult readList(String type, DataGroup filter) {
		if (PLACE.equals(type)) {
			return readAndConvertPlaceListFromFedora();
		}
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	private SpiderReadResult readAndConvertPlaceListFromFedora() {
		try {
			return tryCreateSpiderReadResultFromReadingAndConvertingPlaceListInFedora();
		} catch (Exception e) {
			throw FedoraException
					.withMessageAndException("Unable to read list of places: " + e.getMessage(), e);
		}
	}

	private SpiderReadResult tryCreateSpiderReadResultFromReadingAndConvertingPlaceListInFedora() {
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = (List<DataGroup>) tryReadAndConvertPlaceListFromFedora();
		return spiderReadResult;
	}

	private Collection<DataGroup> tryReadAndConvertPlaceListFromFedora() {
		String query = "state=A pid~alvin-place:*";
		String placeListXML = getRecordListXMLFromFedora(query);
		NodeList list = extractNodeListWithPidsFromXML(placeListXML);
		return constructCollectionOfPlacesFromFedora(list);
	}

	private String getRecordListXMLFromFedora(String query) {
		HttpHandler httpHandler = createHttpHandlerForRecordList(query);
		return httpHandler.getResponseText();
	}

	private HttpHandler createHttpHandlerForRecordList(String query) {
		String urlEncodedQuery = URLEncoder.encode(query);
		String url = baseURL + "objects?pid=true&maxResults=100&resultFormat=xml&query="
				+ urlEncodedQuery;
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private NodeList extractNodeListWithPidsFromXML(String placeListXML) {
		XMLXPathParser parser = XMLXPathParser.forXML(placeListXML);
		return parser
				.getNodeListFromDocumentUsingXPath("/result/resultList/objectFields/pid/text()");
	}

	private Collection<DataGroup> constructCollectionOfPlacesFromFedora(NodeList list) {
		Collection<DataGroup> placeList = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String pid = node.getTextContent();
			placeList.add(readAndConvertPlaceFromFedora(pid));
		}
		return placeList;
	}

	@Override
	public SpiderReadResult readAbstractList(String type, DataGroup filter) {
		throw NotImplementedException.withMessage("readAbstractList is not implemented");
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		throw NotImplementedException.withMessage("readLinkList is not implemented");
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		throw NotImplementedException
				.withMessage("generateLinkCollectionPointingToRecord is not implemented");
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		throw NotImplementedException.withMessage("recordsExistForRecordType is not implemented");
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		throw NotImplementedException.withMessage(
				"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented");
	}

}
