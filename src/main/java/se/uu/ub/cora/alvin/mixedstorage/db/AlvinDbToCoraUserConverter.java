package se.uu.ub.cora.alvin.mixedstorage.db;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class AlvinDbToCoraUserConverter implements AlvinDbToCoraConverter {

	private Map<String, String> map;

	@Override
	public DataGroup fromMap(Map<String, String> map) {
		this.map = map;
		checkMapContainsRequiredValue("id");
		checkMapContainsRequiredValue("domain");
		checkMapContainsRequiredValue("userid");

		return createUserDataGroup(map);
	}

	private void checkMapContainsRequiredValue(String valueToGet) {
		if (map.isEmpty() || !map.containsKey(valueToGet) || "".equals(map.get(valueToGet))) {
			throw ConversionException.withMessageAndException(
					"Error converting user to Cora user: Map does not contain value for "
							+ valueToGet,
					null);
		}
	}

	private DataGroup createUserDataGroup(Map<String, String> map) {
		DataGroup user = DataGroup.withNameInData("user");
		createAndAddRecordInfo(user);
		return user;
	}

	private void createAndAddRecordInfo(DataGroup user) {
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", map.get("id")));
		createAndAddType(recordInfo);
		createAndAddDataDivider(recordInfo);
		addCreatedInfoToRecordInfo(recordInfo);
		addUpdatedInfoToRecordInfo(recordInfo);
		user.addChild(recordInfo);
	}

	private void addUpdatedInfoToRecordInfo(DataGroup recordInfo) {
		DataGroup updated = DataGroup.withNameInData("updated");
		updated.setRepeatId("0");
		DataGroup updatedBy = DataGroup.asLinkWithNameInDataAndTypeAndId("updatedBy", "coraUser",
				"coraUser:4412566252284358");
		updated.addChild(updatedBy);
		updated.addChild(
				DataAtomic.withNameInDataAndValue("tsUpdated", getPredefinedTimestampAsString()));
		recordInfo.addChild(updated);
	}

	private void createAndAddDataDivider(DataGroup recordInfo) {
		DataGroup dataDivider = DataGroup.asLinkWithNameInDataAndTypeAndId("dataDivider", "system",
				"alvin");
		recordInfo.addChild(dataDivider);
	}

	private void createAndAddType(DataGroup recordInfo) {
		DataGroup type = DataGroup.asLinkWithNameInDataAndTypeAndId("type", "recordType",
				"coraUser");
		recordInfo.addChild(type);
	}

	private void addCreatedInfoToRecordInfo(DataGroup recordInfo) {
		DataGroup createdByGroup = createLinkToUserUsingUserIdAndNameInData("createdBy");
		recordInfo.addChild(createdByGroup);
		String predefinedTimestamp = getPredefinedTimestampAsString();
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("tsCreated", predefinedTimestamp));
	}

	private DataGroup createLinkToUserUsingUserIdAndNameInData(String nameInData) {
		return DataGroup.asLinkWithNameInDataAndTypeAndId(nameInData, "coraUser",
				"coraUser:4412566252284358");
	}

	private String getPredefinedTimestampAsString() {
		LocalDateTime localDateTime = LocalDateTime.of(2017, 10, 01, 00, 00, 00, 0);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		return localDateTime.format(formatter);
	}

}
