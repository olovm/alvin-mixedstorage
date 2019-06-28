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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.sqldatabase.DataReader;

public class AlvinMixedUserStorage implements UserStorage {

	private static final String USER_ROLE = "userRole";
	private UserStorage userStorageForGuest;
	private DataReader dataReaderForUsers;
	private Logger log = LoggerProvider.getLoggerForClass(AlvinMixedUserStorage.class);

	public static AlvinMixedUserStorage usingUserStorageForGuestAndDataReaderForUsers(
			UserStorage userStorageForGuest, DataReader dataReaderForUsers) {
		return new AlvinMixedUserStorage(userStorageForGuest, dataReaderForUsers);
	}

	private AlvinMixedUserStorage(UserStorage userStorageForGuest, DataReader dataReaderForUsers) {
		this.userStorageForGuest = userStorageForGuest;
		this.dataReaderForUsers = dataReaderForUsers;
	}

	@Override
	public DataGroup getUserById(String id) {
		return userStorageForGuest.getUserById(id);
	}

	@Override
	public DataGroup getUserByIdFromLogin(String idFromLogin) {
		logAndThrowExceptionIfUnexpectedFormatOf(idFromLogin);

		List<Map<String, Object>> dbResult = queryDbForUserUsingIdFromLogin(idFromLogin);
		throwExceptionIfUserInfoNotFoundInDb(idFromLogin, dbResult);

		return createDataGroupWithUserInfo(idFromLogin, dbResult);
	}

	private void logAndThrowExceptionIfUnexpectedFormatOf(String idFromLogin) {
		if (!idFromLogin.matches("^\\w+@(\\w+\\.){1,}\\w+$")) {
			String errorMessage = "Unrecognized format of userIdFromLogin: " + idFromLogin;
			log.logErrorUsingMessage(errorMessage);
			throw UserException.withMessage(errorMessage);
		}
	}

	private List<Map<String, Object>> queryDbForUserUsingIdFromLogin(String idFromLogin) {
		String sql = "select alvinuser.*, role.group_id from alvin_seam_user alvinuser "
				+ "left join alvin_role role on alvinuser.id = role.user_id where  alvinuser.userid = ?"
				+ " and alvinuser.domain=?;";

		List<Object> values = createValueListFromLogin(idFromLogin);
		return dataReaderForUsers.executePreparedStatementQueryUsingSqlAndValues(sql, values);
	}

	private List<Object> createValueListFromLogin(String idFromLogin) {
		List<Object> values = new ArrayList<>();
		String userId = getUserIdFromIdFromLogin(idFromLogin);
		values.add(userId);
		String domain = getDomainFromLogin(idFromLogin);
		values.add(domain);
		return values;
	}

	private String getUserIdFromIdFromLogin(String idFromLogin) {
		int indexOfAt = idFromLogin.indexOf('@');
		return idFromLogin.substring(0, indexOfAt);
	}

	private String getDomainFromLogin(String idFromLogin) {
		String[] splitAtAt = idFromLogin.split("@");
		String domainPart = splitAtAt[1];

		String[] loginDomainNameParts = domainPart.split("\\.");
		int secondLevelDomainPosition = loginDomainNameParts.length - 2;
		return loginDomainNameParts[secondLevelDomainPosition];
	}

	private void throwExceptionIfUserInfoNotFoundInDb(String idFromLogin,
			List<Map<String, Object>> dbResult) {
		if (dbResult.isEmpty()) {
			throw new RecordNotFoundException("No user found for login: " + idFromLogin);
		}
	}

	private DataGroup createDataGroupWithUserInfo(String idFromLogin,
			List<Map<String, Object>> dbResult) {
		DataGroup userGroup = UserConverterHelper.createBasicActiveUser();
		userGroup.addChild(DataAtomic.withNameInDataAndValue("userId", idFromLogin));

		Map<String, Object> firstRowFromDb = dbResult.get(0);
		createAndAddRecordInfo(userGroup, firstRowFromDb);
		possiblyAddFirstName(userGroup, firstRowFromDb);
		possiblyAddLastName(userGroup, firstRowFromDb);

		addUserRoles(userGroup, dbResult);
		return userGroup;
	}

	private void createAndAddRecordInfo(DataGroup userGroup, Map<String, Object> firstRowFromDb) {
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		userGroup.addChild(recordInfo);
		String idFromDb = String.valueOf(firstRowFromDb.get("id"));
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", idFromDb));
	}

	private void possiblyAddFirstName(DataGroup userGroup, Map<String, Object> firstRowFromDb) {
		if (firstRowFromDb.containsKey("firstname")) {
			userGroup.addChild(DataAtomic.withNameInDataAndValue("userFirstName",
					(String) firstRowFromDb.get("firstname")));
		}
	}

	private void possiblyAddLastName(DataGroup userGroup, Map<String, Object> firstRowFromDb) {
		if (firstRowFromDb.containsKey("lastname")) {
			userGroup.addChild(DataAtomic.withNameInDataAndValue("userLastName",
					(String) firstRowFromDb.get("lastname")));
		}
	}

	private void addUserRoles(DataGroup userGroup, List<Map<String, Object>> dbResult) {
		boolean userHasAdminGroupRight = UserRoleConverterHelper.userHasAdminGroupRight(dbResult);
		if (userHasAdminGroupRight) {
			addRolesForAdminUser(userGroup);
		} else {
			possiblyAddMatchingRolesForNonAdminUser(userGroup, dbResult);
		}
		addRepeatIdToAllUserRoles(userGroup);
	}

	private void addRolesForAdminUser(DataGroup userGroup) {
		createUserRoleWithRoleIdAndAddToUserGroup("metadataAdmin", userGroup);
		createUserRoleWithRoleIdAndAddToUserGroup("systemConfigurator", userGroup);
		createUserRoleWithRoleIdAndAddToUserGroup("binaryUserRole", userGroup);
		createUserRoleWithRoleIdAndAddToUserGroup("userAdminRole", userGroup);
		createUserRoleWithRoleIdAndAddToUserGroup("systemOneSystemUserRole", userGroup);
	}

	private DataGroup createUserRoleWithRoleIdAndAddToUserGroup(String roleId,
			DataGroup userGroup) {
		DataGroup userRole = UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId(roleId);
		userGroup.addChild(userRole);

		return userRole;
	}

	private void possiblyAddMatchingRolesForNonAdminUser(DataGroup userGroup,
			List<Map<String, Object>> dbResult) {
		for (Map<String, Object> role : dbResult) {
			Object id = role.get("group_id");
			possiblyAddMatchingRoleForNonAdminUser(userGroup, id);
		}
	}

	private void possiblyAddMatchingRoleForNonAdminUser(DataGroup userGroup, Object id) {
		String matchingCoraRole = UserRoleConverterHelper.getMatchingCoraRole((int) id);
		if (!matchingCoraRole.isBlank()) {
			userGroup.addChild(UserRoleConverterHelper
					.createUserRoleWithAllSystemsPermissionUsingRoleId(matchingCoraRole));
		}
	}

	private void addRepeatIdToAllUserRoles(DataGroup userGroup) {
		int repeatId = 0;
		for (DataGroup role : userGroup.getAllGroupsWithNameInData(USER_ROLE)) {
			role.setRepeatId(String.valueOf(repeatId));
			repeatId++;
		}
	}

	public UserStorage getUserStorageForGuest() {
		// needed for test
		return userStorageForGuest;
	}

	public DataReader getDataReaderForUsers() {
		// needed for test
		return dataReaderForUsers;
	}
}
