/*
 * Copyright 2015 Uppsala University Library
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

package se.uu.ub.cora.storage.testdata;

import se.uu.ub.cora.alvin.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.basicstorage.RecordStorageInMemory;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.data.DataGroup;

public class TestDataAppTokenStorage {
	private static DataGroup emptyCollectedData = DataCreator.createEmptyCollectedData();

	public static RecordStorageInMemory createRecordStorageInMemoryWithTestData(String basePath) {
		RecordStorageOnDisk recordsOnDisk = RecordStorageOnDisk
				.createRecordStorageOnDiskWithBasePath(basePath);
		addRecordType(recordsOnDisk);
		addRecordTypeUser(recordsOnDisk);
		addRecordTypeRecordType(recordsOnDisk);
		addRecordTypeSystemOneUser(recordsOnDisk);
		addRecordTypeSystemTwoUser(recordsOnDisk);
		addRecordTypeAppToken(recordsOnDisk);

		addRecordTypeImage(recordsOnDisk);

		return recordsOnDisk;
	}

	public static void createUserOnDisk(RecordStorageOnDisk recordsOnDisk) {
		String noAppTokenUserJson = "{\"name\":\"user\",\"children\":[{\"name\":\"recordInfo\",\"children\":[{\"name\":\"id\",\"value\":\"createdLater\"},{\"name\":\"type\",\"value\":\"systemTwoUser\"},{\"name\":\"createdBy\",\"value\":\"131313\"},{\"name\":\"dataDivider\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"system\"},{\"name\":\"linkedRecordId\",\"value\":\"systemOne\"}]}]},{\"name\":\"userId\",\"value\":\"noAppTokenUser@ub.uu.se\"},{\"name\":\"userFirstname\",\"value\":\"Dummy\"},{\"name\":\"userLastname\",\"value\":\"Dumsson\"},{\"name\":\"userRole\",\"children\":[{\"name\":\"userRole\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"permissionRole\"},{\"name\":\"linkedRecordId\",\"value\":\"nothing\"}]},{\"name\":\"userRoleRulePart\",\"children\":[{\"name\":\"permissionRulePart\",\"children\":[{\"name\":\"permissionRulePartValue\",\"value\":\"system.\",\"repeatId\":\"0\"}],\"attributes\":{\"type\":\"organisation\"}}]}],\"repeatId\":\"0\"},{\"name\":\"activeStatus\",\"value\":\"inactive\"}]}";
		DataGroup noAppTokenUser = convertJsonStringToDataGroup(noAppTokenUserJson);
		DataGroup collectedData = createCollectedDataForUserIdWithValue("createdLater@ub.uu.se");
		recordsOnDisk.create("systemTwoUser", "createdLater", noAppTokenUser, collectedData,
				new DataGroupSpy("collectedLinksList"), "systemTwo");
	}

	private static DataGroup createCollectedDataForUserIdWithValue(String termValue) {
		// collectedData
		DataGroup collectedData = DataCreator.createCollectedDataWithTypeAndId("place",
				"place:0001");
		DataGroup collectStorageTerm = new DataGroupSpy("storage");
		collectedData.addChild(collectStorageTerm);

		DataGroup collectedDataTerm = DataCreator
				.createStorageTermWithRepeatIdAndTermIdAndTermValueAndStorageKey("1",
						"userIdStorageTerm", termValue, "userId");
		collectStorageTerm.addChild(collectedDataTerm);
		return collectedData;
	}

	private static DataGroup convertJsonStringToDataGroup(String jsonRecord) {
		DataGroupSpy dataGroupSpy = new DataGroupSpy("someNameInData");
		return dataGroupSpy;
	}

	private static void addRecordType(RecordStorageInMemory recordsInMemory) {
		String recordType = "recordType";
		DataGroup dataGroup = new DataGroupSpy(recordType);

		DataGroup recordInfo = DataCreator.createRecordInfoWithRecordTypeAndRecordId(recordType,
				"metadata");
		dataGroup.addChild(recordInfo);

		dataGroup.addChild(new DataAtomicSpy("abstract", "false"));
		recordsInMemory.create(recordType, "metadata", dataGroup, emptyCollectedData,
				new DataGroupSpy("collectedLinksList"), "cora");
	}

	private static void addRecordTypeRecordType(RecordStorageOnDisk recordsOnDisk) {
		String recordType = "recordType";
		DataGroup dataGroup = DataCreator
				.createRecordTypeWithIdAndUserSuppliedIdAndAbstract("recordType", "true", "false");
		recordsOnDisk.create(recordType, "recordType", dataGroup, emptyCollectedData,
				new DataGroupSpy("collectedLinksList"), "cora");
	}

	private static void addRecordTypeImage(RecordStorageInMemory recordsInMemory) {
		String recordType = "recordType";
		DataGroup dataGroup = DataCreator
				.createRecordTypeWithIdAndUserSuppliedIdAndParentId("image", "true", "binary");
		recordsInMemory.create(recordType, "image", dataGroup, emptyCollectedData,
				new DataGroupSpy("collectedLinksList"), "cora");
	}

	private static void addRecordTypeUser(RecordStorageInMemory recordsInMemory) {
		String recordType = "recordType";
		DataGroup dataGroup = DataCreator.createRecordTypeWithIdAndUserSuppliedIdAndAbstract("user",
				"true", "true");
		recordsInMemory.create(recordType, "user", dataGroup, emptyCollectedData,
				new DataGroupSpy("collectedLinksList"), "cora");
	}

	private static void addRecordTypeSystemOneUser(RecordStorageInMemory recordsInMemory) {
		String recordType = "recordType";
		DataGroup dataGroup = DataCreator.createRecordTypeWithIdAndUserSuppliedIdAndParentId(
				"systemOneUser", "true", "user");
		recordsInMemory.create(recordType, "systemOneUser", dataGroup, emptyCollectedData,
				new DataGroupSpy("collectedLinksList"), "cora");
	}

	private static void addRecordTypeSystemTwoUser(RecordStorageInMemory recordsInMemory) {
		String recordType = "recordType";
		DataGroup dataGroup = DataCreator.createRecordTypeWithIdAndUserSuppliedIdAndParentId(
				"systemTwoUser", "true", "user");
		recordsInMemory.create(recordType, "systemTwoUser", dataGroup, emptyCollectedData,
				new DataGroupSpy("collectedLinksList"), "cora");
	}

	private static void addRecordTypeAppToken(RecordStorageInMemory recordsInMemory) {
		String recordType = "recordType";
		DataGroup dataGroup = DataCreator
				.createRecordTypeWithIdAndUserSuppliedIdAndAbstract("appToken", "false", "false");
		recordsInMemory.create(recordType, "appToken", dataGroup, emptyCollectedData,
				new DataGroupSpy("collectedLinksList"), "cora");

	}
}
