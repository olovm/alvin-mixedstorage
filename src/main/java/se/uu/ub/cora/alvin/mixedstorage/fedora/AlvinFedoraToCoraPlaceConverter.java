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
package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.uu.ub.cora.alvin.mixedstorage.TextUtil;
import se.uu.ub.cora.alvin.mixedstorage.parse.XMLXPathParser;
import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class AlvinFedoraToCoraPlaceConverter implements AlvinFedoraToCoraConverter {

	private XMLXPathParser parser;

	@Override
	public DataGroup fromXML(String xml) {

		String readString;
		try {
			readString = Files.readString(
					Path.of("./src/main/resources/xslt/AlvinFedoraToCoraPlace.xsl"),
					StandardCharsets.UTF_8);
			//
			// TransformerFactory factory = TransformerFactory.newInstance();
			// Source xsltSource = new StreamSource(readString);
			// Templates xslt = factory.newTemplates(xsltSource);
			//
			// Source text = new StreamSource(xml);
			// StreamResult output = new StreamResult();
			// Transformer transformer = xslt.newTransformer();
			// transformer.transform(text, output);
			//
			// Converter converter = ConverterProvider.getConverter("xml");
			// return (DataGroup) converter.convert(output.toString());

			Source xmlInput = new StreamSource(new StringReader(xml));
			Source xslInput = new StreamSource(new StringReader(readString));

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(xslInput); // this is the line that
																			// throws the exception
			// Result result = new StreamResult();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Result result = new StreamResult(baos);
			// transformer.transform(xmlInput, result);
			// return baos.toString();

			transformer.transform(xmlInput, result);

			Converter converter = ConverterProvider.getConverter("xml");
			String xmlString = baos.toString();
			return (DataGroup) converter.convert(xmlString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// @Override
	// public DataGroup fromXML(String xml) {
	// try {
	// parser = XMLXPathParser.forXML(xml);
	// return tryToCreateDataGroupFromDocument();
	// } catch (Exception e) {
	// throw ParseException.withMessageAndException(
	// "Error converting place to Cora place: " + e.getMessage(), e);
	// }
	// }

	private DataGroup tryToCreateDataGroupFromDocument() {
		DataGroup place = DataGroupProvider.getDataGroupUsingNameInData("authority");
		place.addAttributeByIdWithValue("type", "place");
		createRecordInfoAndAddToPlace(place);
		createDefaultNameAndAddToPlace(place);
		possiblyCreateCoordinatesAndAddToPlace(place);
		possiblyCreateCountryAndAddToPlace(place);
		possiblyCreateAlternativeNamesAndAddToPlace(place);
		possiblyCreateIdentifiersAndAddToPlace(place);

		return place;
	}

	private void createRecordInfoAndAddToPlace(DataGroup place) {
		DataGroup recordInfo = AlvinFedoraToCoraRecordInfoConverter.createRecordInfo(parser);
		place.addChild(recordInfo);
	}

	private void createDefaultNameAndAddToPlace(DataGroup place) {
		DataGroup defaultName = DataGroupProvider.getDataGroupUsingNameInData("name");
		place.addChild(defaultName);
		defaultName.addAttributeByIdWithValue("type", "authorized");
		createDefaultNamePartAndAddToName(defaultName);
	}

	private void createDefaultNamePartAndAddToName(DataGroup defaultName) {
		DataGroup defaultNamePart = DataGroupProvider.getDataGroupUsingNameInData("namePart");
		defaultName.addChild(defaultNamePart);
		defaultNamePart.addAttributeByIdWithValue("type", "defaultName");
		defaultNamePart.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("value",
				getStringFromDocumentUsingXPath("/place/defaultPlaceName/name/text()")));
	}

	private String getStringFromDocumentUsingXPath(String xpathString) {
		return parser.getStringFromDocumentUsingXPath(xpathString);
	}

	private void possiblyCreateCoordinatesAndAddToPlace(DataGroup place) {
		String latitude = getStringFromDocumentUsingXPath("/place/latitude/text()");
		String longitude = getStringFromDocumentUsingXPath("/place/longitude/text()");
		if (xmlContainsACompleteCoordinate(latitude, longitude)) {
			createCoordinatesAndAddToPlace(place, latitude, longitude);
		}
	}

	private boolean xmlContainsACompleteCoordinate(String latitude, String longitude) {
		return valueExists(latitude) && valueExists(longitude);
	}

	private void createCoordinatesAndAddToPlace(DataGroup place, String latitude,
			String longitude) {
		DataGroup coordinates = DataGroupProvider.getDataGroupUsingNameInData("coordinates");
		coordinates.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("latitude", latitude));
		coordinates.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("longitude", longitude));
		place.addChild(coordinates);
	}

	private void possiblyCreateCountryAndAddToPlace(DataGroup place) {
		String alpha2Code = getStringFromDocumentUsingXPath(
				"/place/country[@class='country']/alpha2Code/text()");
		if (valueExists(alpha2Code)) {
			createCountryAndAddToPlace(place, alpha2Code);
		} else {
			possiblyCreateHistoricCountryAndAddToPlace(place);
		}
	}

	private boolean valueExists(String alpha2Code) {
		return !"".equals(alpha2Code);
	}

	private void createCountryAndAddToPlace(DataGroup place, String alpha2Code) {
		DataAtomic country = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("country",
				alpha2Code);
		place.addChild(country);
	}

	private void possiblyCreateHistoricCountryAndAddToPlace(DataGroup place) {
		String historicCountryCode = getStringFromDocumentUsingXPath(
				"/place/country[@class='historicCountry']/code/text()");
		if (valueExists(historicCountryCode)) {
			createHistoricCountryAndAddToPlace(place, historicCountryCode);
		}
	}

	private void createHistoricCountryAndAddToPlace(DataGroup place, String historicCountryCode) {
		String modifiedCodeString = removeNonAlpahbeticCharacters(historicCountryCode);
		DataAtomic historicCountry = DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("historicCountry", modifiedCodeString);
		place.addChild(historicCountry);
	}

	private String removeNonAlpahbeticCharacters(String code) {
		String normalizedString = TextUtil.normalizeString(code);
		return TextUtil.turnStringIntoCamelCase(normalizedString);
	}

	private void possiblyCreateAlternativeNamesAndAddToPlace(DataGroup place) {
		NodeList placeNames = parser
				.getNodeListFromDocumentUsingXPath("/place/placeNameForms/entry");
		extractAlternativeNameFromNodesInListAndAddToPlace(place, placeNames);
	}

	private void extractAlternativeNameFromNodesInListAndAddToPlace(DataGroup place,
			NodeList placeNames) {
		for (int i = 0; i < placeNames.getLength(); i++) {
			Node placeName = placeNames.item(i);
			extractAlternativeNameFromNodeAndAddToPlace(place, placeName, i);
		}
	}

	private void extractAlternativeNameFromNodeAndAddToPlace(DataGroup place,
			Node alternativeNameNode, int repeatId) {
		DataGroup nameGroup = createDataGroupWithRepeatId(repeatId);
		convertLanguagePart(nameGroup, alternativeNameNode);
		convertNamePart(nameGroup, alternativeNameNode);
		place.addChild(nameGroup);
	}

	private DataGroup createDataGroupWithRepeatId(int repeatId) {
		DataGroup localName = DataGroupProvider.getDataGroupUsingNameInData("name");
		localName.setRepeatId(String.valueOf(repeatId));
		localName.addAttributeByIdWithValue("type", "alternative");
		return localName;
	}

	private void convertLanguagePart(DataGroup localName, Node placeName) {
		String languageValue = extractValueFromNode(placeName, "placeNameForm/language/alpha3Code");
		DataAtomic alternativeLanguage = DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("language", languageValue);
		localName.addChild(alternativeLanguage);
	}

	private String extractValueFromNode(Node placeName, String propertyXPath) {
		return parser.getStringFromNodeUsingXPath(placeName, propertyXPath);
	}

	private void convertNamePart(DataGroup localName, Node alternativeNameNode) {
		DataGroup namePart = createNamePart();
		extractNameValueAndAddToNamePart(namePart, alternativeNameNode);
		localName.addChild(namePart);
	}

	private DataGroup createNamePart() {
		DataGroup namePart = DataGroupProvider.getDataGroupUsingNameInData("namePart");
		namePart.addAttributeByIdWithValue("type", "defaultName");
		return namePart;
	}

	private void extractNameValueAndAddToNamePart(DataGroup namePart, Node alternativeNameXML) {
		String nameValue = extractValueFromNode(alternativeNameXML, "placeNameForm/name");
		DataAtomic alternativeName = DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("value", nameValue);
		namePart.addChild(alternativeName);
	}

	private void possiblyCreateIdentifiersAndAddToPlace(DataGroup place) {
		NodeList localIdentifiers = parser
				.getNodeListFromDocumentUsingXPath("/place/localIdentifiers/localIdentifier");
		extractLocalIdentifiersAndAddToPlace(place, localIdentifiers);
	}

	private void extractLocalIdentifiersAndAddToPlace(DataGroup place, NodeList localIdentifiers) {
		for (int i = 0; i < localIdentifiers.getLength(); i++) {
			Node localIdentifier = localIdentifiers.item(i);
			DataGroup identifierGroup = extractLocalIdentifier(i, localIdentifier);
			place.addChild(identifierGroup);
		}
	}

	private DataGroup extractLocalIdentifier(int repeatId, Node localIdentifier) {
		DataGroup identifierGroup = createIdentifierGroupWithRepeatId(repeatId);

		extractAndAddLocalIdentifierType(localIdentifier, identifierGroup);

		extractAndAddLocalIdentifierValue(localIdentifier, identifierGroup);
		return identifierGroup;
	}

	private DataGroup createIdentifierGroupWithRepeatId(int repeatId) {
		DataGroup identifierGroup = DataGroupProvider.getDataGroupUsingNameInData("identifier");
		identifierGroup.setRepeatId(String.valueOf(repeatId));
		return identifierGroup;
	}

	private void extractAndAddLocalIdentifierType(Node localIdentifier, DataGroup identifierGroup) {
		String code = extractValueFromNode(localIdentifier, "type/code");
		identifierGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("identifierType", code));
	}

	private void extractAndAddLocalIdentifierValue(Node localIdentifier,
			DataGroup identifierGroup) {
		String text = extractValueFromNode(localIdentifier, "text");
		identifierGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("identifierValue", text));
	}

}
