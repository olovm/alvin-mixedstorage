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
package se.uu.ub.cora.alvin.mixedstorage.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class UserConverter {

	private static final String USER_ROLE = "userRole";

	public static DataGroup convertFromRow(Map<String, Object> rowFromDb) {
		DataGroup userGroup = DataGroup.withNameInData("user");
		userGroup.addAttributeByIdWithValue("type", "coraUser");
		String userId = createUserIdFromDbInfo(rowFromDb);
		userGroup.addChild(DataAtomic.withNameInDataAndValue("userId", userId));
		userGroup.addChild(DataAtomic.withNameInDataAndValue("activeStatus", "active"));
		createAndAddRecordInfo(userGroup, rowFromDb);
		possiblyAddFirstName(userGroup, rowFromDb);
		possiblyAddLastName(userGroup, rowFromDb);
		//
		// addUserRoles(userGroup);
		return userGroup;
	}

	private static String createUserIdFromDbInfo(Map<String, Object> rowFromDb) {
		String userId = String.valueOf(rowFromDb.get("userid"));
		return userId + "@user." + rowFromDb.get("domain") + ".se";
	}

	private static void createAndAddRecordInfo(DataGroup userGroup,
			Map<String, Object> firstRowFromDb) {
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		userGroup.addChild(recordInfo);
		String idFromDb = String.valueOf(firstRowFromDb.get("id"));
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", idFromDb));
		createAndAddType(recordInfo);
		createAndAddDataDivider(recordInfo);
		addCreatedInfoToRecordInfo(recordInfo);
		addUpdatedInfoToRecordInfo(recordInfo);
	}

	private static void createAndAddType(DataGroup recordInfo) {
		DataGroup type = DataGroup.asLinkWithNameInDataAndTypeAndId("type", "recordType",
				"coraUser");
		recordInfo.addChild(type);
	}

	private static void createAndAddDataDivider(DataGroup recordInfo) {
		DataGroup dataDivider = DataGroup.asLinkWithNameInDataAndTypeAndId("dataDivider", "system",
				"alvin");
		recordInfo.addChild(dataDivider);
	}

	private static void addCreatedInfoToRecordInfo(DataGroup recordInfo) {
		DataGroup createdByGroup = createLinkToUserUsingUserIdAndNameInData("createdBy");
		recordInfo.addChild(createdByGroup);
		String predefinedTimestamp = getPredefinedTimestampAsString();
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("tsCreated", predefinedTimestamp));
	}

	private static DataGroup createLinkToUserUsingUserIdAndNameInData(String nameInData) {
		return DataGroup.asLinkWithNameInDataAndTypeAndId(nameInData, "coraUser",
				"coraUser:4412566252284358");
	}

	private static String getPredefinedTimestampAsString() {
		LocalDateTime localDateTime = LocalDateTime.of(2017, 10, 01, 00, 00, 00, 0);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		return localDateTime.format(formatter);
	}

	private static void addUpdatedInfoToRecordInfo(DataGroup recordInfo) {
		DataGroup updated = DataGroup.withNameInData("updated");
		updated.setRepeatId("0");
		DataGroup updatedBy = DataGroup.asLinkWithNameInDataAndTypeAndId("updatedBy", "coraUser",
				"coraUser:4412566252284358");
		updated.addChild(updatedBy);
		updated.addChild(
				DataAtomic.withNameInDataAndValue("tsUpdated", getPredefinedTimestampAsString()));
		recordInfo.addChild(updated);
	}

	private static void possiblyAddFirstName(DataGroup userGroup,
			Map<String, Object> firstRowFromDb) {
		if (firstRowFromDb.containsKey("firstname")) {
			userGroup.addChild(DataAtomic.withNameInDataAndValue("userFirstName",
					(String) firstRowFromDb.get("firstname")));
		}
	}

	private static void possiblyAddLastName(DataGroup userGroup,
			Map<String, Object> firstRowFromDb) {
		if (firstRowFromDb.containsKey("lastname")) {
			userGroup.addChild(DataAtomic.withNameInDataAndValue("userLastName",
					(String) firstRowFromDb.get("lastname")));
		}
	}

}
