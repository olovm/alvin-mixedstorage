package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.alvin.mixedstorage.user.UserConverterHelper;
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
		DataGroup updated = UserConverterHelper
				.createUpdatedInfoUsingUserId("coraUser:4412566252284358");
		recordInfo.addChild(updated);
	}

	private void createAndAddDataDivider(DataGroup recordInfo) {
		DataGroup dataDivider = UserConverterHelper.createDataDivider();
		recordInfo.addChild(dataDivider);
	}

	private void createAndAddType(DataGroup recordInfo) {
		DataGroup type = UserConverterHelper.createType();
		recordInfo.addChild(type);
	}

	private void addCreatedInfoToRecordInfo(DataGroup recordInfo) {
		DataGroup createdByGroup = UserConverterHelper
				.createCreatedByUsingUserId("coraUser:4412566252284358");
		recordInfo.addChild(createdByGroup);
		DataAtomic tsCreated = UserConverterHelper.createTsCreated();
		recordInfo.addChild(tsCreated);
	}

}
