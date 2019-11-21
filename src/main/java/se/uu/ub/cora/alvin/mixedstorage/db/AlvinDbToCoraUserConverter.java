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
package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.alvin.mixedstorage.user.UserConverterHelper;
import se.uu.ub.cora.alvin.mixedstorage.user.UserRoleConverterHelper;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReader;

public class AlvinDbToCoraUserConverter implements AlvinDbToCoraConverter {

	private Logger log = LoggerProvider.getLoggerForClass(AlvinDbToCoraUserConverter.class);

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
		return map.get(valueToGet) == null || "".equals(map.get(valueToGet));
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
		DataGroup recordInfo = DataGroupProvider.getDataGroupUsingNameInData("recordInfo");
		recordInfo.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("id",
				String.valueOf(map.get("id"))));
		createAndAddType(recordInfo);
		createAndAddDataDivider(recordInfo);
		addCreatedInfoToRecordInfo(recordInfo);
		addUpdatedInfoToRecordInfo(recordInfo);
		user.addChild(recordInfo);
	}

	private void createAndAddType(DataGroup recordInfo) {
		DataGroup type = UserConverterHelper.createType();
		recordInfo.addChild(type);
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
			user.addChild(
					DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData, firstname));
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
			addRolesForNonAdminUser(user, readUserRoles);
		}
		setRepeatIdForAllRoles(user);
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

		user.addChild(UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId("userAdminRole"));
	}

	private void addRolesForNonAdminUser(DataGroup userGroup,
			List<Map<String, Object>> readUserRoles) {
		addUserRoles(userGroup, readUserRoles);
		userGroup.addChild(UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId("metadataUserRole"));

	}

	private void addUserRoles(DataGroup user, List<Map<String, Object>> readUserRoles) {
		for (Map<String, Object> role : readUserRoles) {
			Object id = role.get("group_id");
			log.logInfoUsingMessage("Lookup for group_id " + id);
			String matchingCoraRole = UserRoleConverterHelper.getMatchingCoraRole((int) id);
			addRoleIfFoundMatchingInCora(user, matchingCoraRole);
		}
	}

	private void addRoleIfFoundMatchingInCora(DataGroup user, String matchingCoraRole) {
		if (!matchingCoraRole.isBlank()) {
			log.logInfoUsingMessage("Found matching role " + matchingCoraRole + " as coraRole");
			user.addChild(UserRoleConverterHelper
					.createUserRoleWithAllSystemsPermissionUsingRoleId(matchingCoraRole));
		}
	}

	private void setRepeatIdForAllRoles(DataGroup user) {
		List<DataGroup> userRoles = user.getAllGroupsWithNameInData("userRole");
		int repeatId = 0;
		for (DataGroup role : userRoles) {
			role.setRepeatId(String.valueOf(repeatId));
			repeatId++;
		}
	}

	public DataReader getDataReader() {
		return dataReader;
	}

}
