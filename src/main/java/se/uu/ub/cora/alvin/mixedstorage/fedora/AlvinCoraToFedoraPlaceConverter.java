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

package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.util.Collection;

import se.uu.ub.cora.alvin.mixedstorage.parse.XMLXPathParser;
import se.uu.ub.cora.alvin.mixedstorage.resource.ResourceReader;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class AlvinCoraToFedoraPlaceConverter implements AlvinCoraToFedoraConverter {

	private HttpHandlerFactory httpHandlerFactory;
	private String fedoraURL;
	private XMLXPathParser parser;

	public static AlvinCoraToFedoraPlaceConverter usingHttpHandlerFactoryAndFedoraUrl(
			HttpHandlerFactory httpHandlerFactory, String fedoraURL) {
		return new AlvinCoraToFedoraPlaceConverter(httpHandlerFactory, fedoraURL);
	}

	private AlvinCoraToFedoraPlaceConverter(HttpHandlerFactory httpHandlerFactory,
			String fedoraURL) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraURL = fedoraURL;
	}

	@Override
	public String toXML(DataGroup record) {
		String recordId = getIdFromRecord(record);
		String fedoraXML = getXMLForRecordFromFedora(recordId);
		parser = XMLXPathParser.forXML(fedoraXML);
		convertDefaultName(record);
		return parser.getDocumentAsString("/");
	}

	private String getXMLForRecordFromFedora(String recordId) {
		String url = fedoraURL + "objects/" + recordId + "/datastreams/METADATA/content";
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler.getResponseText();
	}

	private String getIdFromRecord(DataGroup record) {
		DataGroup recordInfo = record.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	private void convertDefaultName(DataGroup record) {
		String defaultNameFromPlaceRecord = getDefaultNameFromPlaceRecord(record);
		setStringFromDocumentUsingXPath("/place/defaultPlaceName/name", defaultNameFromPlaceRecord);
	}

	private void setStringFromDocumentUsingXPath(String xpathString, String newValue) {
		parser.setStringInDocumentUsingXPath(xpathString, newValue);
	}

	private String getDefaultNameFromPlaceRecord(DataGroup record) {
		Collection<DataGroup> nameGroups = record.getAllGroupsWithNameInDataAndAttributes("name",
				DataAttribute.withNameInDataAndValue("type", "authorized"));
		DataGroup nameGroup = nameGroups.iterator().next();
		Collection<DataGroup> defaultNames = nameGroup.getAllGroupsWithNameInDataAndAttributes(
				"namePart", DataAttribute.withNameInDataAndValue("type", "defaultName"));
		DataGroup defaultName = defaultNames.iterator().next();
		return defaultName.getFirstAtomicValueWithNameInData("value");
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		// needed for tests
		return httpHandlerFactory;
	}

	public String getFedorURL() {
		// needed for tests
		return fedoraURL;
	}

	@Override
	public String toNewXML(DataGroup record) {
		String newPlaceTemplate = ResourceReader.readResourceAsString("place/templatePlace.xml");
		parser = XMLXPathParser.forXML(newPlaceTemplate);
		setStringFromDocumentUsingXPath("/place/pid", getIdFromRecord(record));
		convertDefaultName(record);

		DataGroup recordInfo = record.getFirstGroupWithNameInData("recordInfo");
		convertCreatedBy(recordInfo);

		String tsCreated = recordInfo.getFirstAtomicValueWithNameInData("tsCreated");
		setStringFromDocumentUsingXPath("/place/recordInfo/created/date", tsCreated + " UTC");

		return parser.getDocumentAsString("/");
	}

	private void convertCreatedBy(DataGroup recordInfo) {
		DataGroup createdBy = recordInfo.getFirstGroupWithNameInData("createdBy");
		String userId = createdBy.getFirstAtomicValueWithNameInData("linkedRecordId");
		setStringFromDocumentUsingXPath("/place/recordInfo/created/user/userId", userId);
	}

}
