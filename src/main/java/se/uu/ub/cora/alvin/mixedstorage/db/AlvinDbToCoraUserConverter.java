package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.alvin.mixedstorage.user.UserConverterHelper;
import se.uu.ub.cora.alvin.mixedstorage.user.UserRoleConverterHelper;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.sqldatabase.DataReader;

public class AlvinDbToCoraUserConverter implements AlvinDbToCoraConverter {

	public static AlvinDbToCoraUserConverter usingDataReader(DataReader dataReader) {
		return new AlvinDbToCoraUserConverter(dataReader);
	}

	private Map<String, Object> map;
	private DataReader dataReader;

	private AlvinDbToCoraUserConverter(DataReader dataReader) {
		this.dataReader = dataReader;
	}

	@Override
	public DataGroup fromMap(Map<String, Object> map) {
		this.map = map;
		checkMapContainsRequiredValue("id");
		checkMapContainsRequiredValue("domain");
		checkMapContainsRequiredValue("userid");

		return createUserDataGroup();
	}

	private void checkMapContainsRequiredValue(String valueToGet) {
		if (map.isEmpty() || valueIsEmpty(valueToGet)) {
			throw ConversionException.withMessageAndException(
					"Error converting user to Cora user: Map does not contain value for "
							+ valueToGet,
					null);
		}
	}

	private boolean valueIsEmpty(String valueToGet) {
		return !map.containsKey(valueToGet) || "".equals(map.get(valueToGet))
				|| map.get(valueToGet) == null;
	}

	private DataGroup createUserDataGroup() {
		DataGroup user = UserConverterHelper.createBasicActiveUser();

		createAndAddRecordInfo(user);
		possiblyAddFirstname(user);
		possiblyAddLastname(user);
		List<Map<String, Object>> readUserRoles = readUserRoles();
		possiblyAddUserRoles(user, readUserRoles);
		return user;
	}

	private void createAndAddRecordInfo(DataGroup user) {
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", String.valueOf(map.get("id"))));
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

	private void possiblyAddFirstname(DataGroup user) {
		possiblyAddAtomicValueToUserUsingValueToGetAndNameInData(user, "firstname",
				"userFirstname");
	}

	private void possiblyAddAtomicValueToUserUsingValueToGetAndNameInData(DataGroup user,
			String valueToGet, String nameInData) {
		if (!valueIsEmpty(valueToGet)) {
			String firstname = (String) map.get(valueToGet);
			user.addChild(DataAtomic.withNameInDataAndValue(nameInData, firstname));
		}
	}

	private void possiblyAddLastname(DataGroup user) {
		possiblyAddAtomicValueToUserUsingValueToGetAndNameInData(user, "lastname", "userLastname");
	}

	private List<Map<String, Object>> readUserRoles() {
		List<Object> values = new ArrayList<>();
		values.add(map.get("id"));
		return dataReader.executePreparedStatementQueryUsingSqlAndValues(
				"select * from alvin_role ar left join alvin_group ag on ar.group_id = ag.id where user_id = ?",
				values);

	}

	private void possiblyAddUserRoles(DataGroup user, List<Map<String, Object>> readUserRoles) {
		if (UserRoleConverterHelper.userHasAdminGroupRight(readUserRoles)) {
			addUserRolesForAdminUser(user);
		} else {
			addUserRoles(user, readUserRoles);
		}
	}

	private void addUserRolesForAdminUser(DataGroup user) {
		user.addChild(UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId("metadataAdmin"));

		user.addChild(UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId("binaryUserRole"));

		user.addChild(UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId("systemConfigurator"));

		user.addChild(UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId("systemOneSystemUserRole"));
	}

	private void addUserRoles(DataGroup user, List<Map<String, Object>> readUserRoles) {
		for (Map<String, Object> role : readUserRoles) {
			Object id = role.get("group_id");
			String matchingCoraRole = UserRoleConverterHelper.getMatchingCoraRole((int) id);
			addRoleIfFoundMatchingInCora(user, matchingCoraRole);
		}
	}

	private void addRoleIfFoundMatchingInCora(DataGroup user, String matchingCoraRole) {
		if (!matchingCoraRole.isBlank()) {
			user.addChild(UserRoleConverterHelper
					.createUserRoleWithAllSystemsPermissionUsingRoleId(matchingCoraRole));
		}
	}

	public DataReader getDataReader() {
		return dataReader;
	}

}
