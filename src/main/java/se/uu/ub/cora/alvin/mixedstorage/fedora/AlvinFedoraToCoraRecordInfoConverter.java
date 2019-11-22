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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.uu.ub.cora.alvin.mixedstorage.parse.XMLXPathParser;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class AlvinFedoraToCoraRecordInfoConverter {
	private XMLXPathParser parser;
	private DataGroup recordInfo;

	public AlvinFedoraToCoraRecordInfoConverter(XMLXPathParser parser) {
		this.parser = parser;
	}

	public static DataGroup createRecordInfo(XMLXPathParser parser) {
		AlvinFedoraToCoraRecordInfoConverter alvinToCoraRecordInfoConverter = new AlvinFedoraToCoraRecordInfoConverter(
				parser);
		return alvinToCoraRecordInfoConverter.createRecordInfoAsDataGroup();
	}

	private DataGroup createRecordInfoAsDataGroup() {
		recordInfo = DataGroupProvider.getDataGroupUsingNameInData("recordInfo");
		addType();
		parseAndAddId();
		addDataDivider();
		addCreatedBy();
		parseAndAddTsCreated();

		parseAndAddUpdateInfo();
		return recordInfo;
	}

	private void addType() {
		DataGroup type = createLinkWithNameInDataAndTypeAndId("type", "recordType", "place");
		recordInfo.addChild(type);
	}

	private static DataGroup createLinkWithNameInDataAndTypeAndId(String nameInData,
			String linkedRecordType, String linkedRecordId) {
		DataGroup type = DataGroupProvider.getDataGroupUsingNameInData(nameInData);
		type.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("linkedRecordType",
				linkedRecordType));
		type.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("linkedRecordId",
				linkedRecordId));
		return type;
	}

	private void parseAndAddId() {
		String pid = parser.getStringFromDocumentUsingXPath("/place/pid/text()");
		recordInfo.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("id", pid));
	}

	private void addDataDivider() {
		DataGroup dataDivider = createLinkWithNameInDataAndTypeAndId("dataDivider", "system",
				"alvin");
		recordInfo.addChild(dataDivider);
	}

	private void addCreatedBy() {
		DataGroup createdBy = createLinkWithNameInDataAndTypeAndId("createdBy", "user", "12345");
		recordInfo.addChild(createdBy);
	}

	private void parseAndAddTsCreated() {
		String tsCreatedWithUTC = parser
				.getStringFromDocumentUsingXPath("/place/recordInfo/created/date");
		String tsCreated = removeUTCFromTimestamp(tsCreatedWithUTC);
		recordInfo.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("tsCreated", tsCreated));
	}

	private String removeUTCFromTimestamp(String tsCreatedWithUTC) {
		return tsCreatedWithUTC.substring(0, tsCreatedWithUTC.indexOf("UTC") - 1);
	}

	private void parseAndAddUpdateInfo() {
		NodeList updates = extractAllUpdates();
		for (int i = 0; i < updates.getLength(); i++) {
			Node updated = updates.item(i);
			createAndAddUpdateUsingNodeAndRepeatId(updated, i);
		}
	}

	private NodeList extractAllUpdates() {
		return parser.getNodeListFromDocumentUsingXPath("/place/recordInfo/updated/userAction");
	}

	private void createAndAddUpdateUsingNodeAndRepeatId(Node userUpdated, int repeatId) {
		DataGroup updated = DataGroupProvider.getDataGroupUsingNameInData("updated");
		updated.setRepeatId(String.valueOf(repeatId));
		addUpdatedBy(updated);
		parseAndAddTsUpdated(updated, userUpdated);
		recordInfo.addChild(updated);
	}

	private void addUpdatedBy(DataGroup updated) {
		DataGroup updatedBy = createLinkWithNameInDataAndTypeAndId("updatedBy", "user", "12345");
		updated.addChild(updatedBy);
	}

	private void parseAndAddTsUpdated(DataGroup updated, Node node) {
		String tsUpdatedWithUTC = parser.getStringFromNodeUsingXPath(node, "date/text()");
		String tsUpdated = removeUTCFromTimestampOrUseTsCreated(tsUpdatedWithUTC);
		updated.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("tsUpdated", tsUpdated));
	}

	private String removeUTCFromTimestampOrUseTsCreated(String tsUpdatedWithUTC) {
		if (isNotEmpty(tsUpdatedWithUTC)) {
			return removeUTCFromTimestamp(tsUpdatedWithUTC);
		}
		return tsCreatedToUseAsTsUpdated();
	}

	private boolean isNotEmpty(String tsUpdatedWithUTC) {
		return !"".equals(tsUpdatedWithUTC);
	}

	private String tsCreatedToUseAsTsUpdated() {
		return recordInfo.getFirstAtomicValueWithNameInData("tsCreated");
	}
}
