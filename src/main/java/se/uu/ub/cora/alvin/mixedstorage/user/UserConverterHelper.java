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

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class UserConverterHelper {

	private static final String CORA_USER = "coraUser";

	private UserConverterHelper() {
		throw new UnsupportedOperationException();
	}

	public static DataGroup createBasicActiveUser() {
		DataGroup user = DataGroupProvider.getDataGroupUsingNameInData("user");
		user.addAttributeByIdWithValue("type", CORA_USER);
		user.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("activeStatus", "active"));
		return user;
	}

	public static DataGroup createType() {
		return DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("type", "recordType",
				CORA_USER);
	}

	public static DataGroup createDataDivider() {
		return DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("dataDivider", "system",
				"alvin");
	}

	public static DataGroup createCreatedByUsingUserId(String userId) {
		return DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("createdBy", CORA_USER,
				userId);
	}

	public static DataAtomic createTsCreated() {
		return DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("tsCreated",
				getPredefinedTimestampAsString());
	}

	private static String getPredefinedTimestampAsString() {
		LocalDateTime localDateTime = LocalDateTime.of(2017, 10, 01, 00, 00, 00, 0);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		return localDateTime.format(formatter);
	}

	public static DataGroup createUpdatedInfoUsingUserId(String userId) {
		DataGroup updated = DataGroupProvider.getDataGroupUsingNameInData("updated");
		updated.setRepeatId("0");
		DataGroup updatedBy = DataGroupProvider
				.getDataGroupAsLinkUsingNameInDataTypeAndId("updatedBy", CORA_USER, userId);
		updated.addChild(updatedBy);
		updated.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("tsUpdated",
				getPredefinedTimestampAsString()));
		return updated;
	}

}
