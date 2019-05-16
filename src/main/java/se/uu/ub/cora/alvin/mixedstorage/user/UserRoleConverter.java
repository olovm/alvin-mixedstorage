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

public class UserRoleConverter {

	private static final String USER_ROLE = "userRole";

	private UserRoleConverter() {
		throw new UnsupportedOperationException();
	}

	public static List<DataGroup> convert(List<Map<String, Object>> rowsFromDb) {
		List<DataGroup> roles = new ArrayList<>();
		possiblyConvertAndAddRoles(rowsFromDb, roles);
		return roles;
	}

	private static void possiblyConvertAndAddRoles(List<Map<String, Object>> rowsFromDb,
			List<DataGroup> roles) {
		if (userHasAdminGroupRight(rowsFromDb)) {
			addUserRoles(roles);
		}
	}

	private static void addUserRoles(List<DataGroup> roles) {
		createAndAddRoleWithRoleId(roles, "metadataAdmin");
		createAndAddRoleWithRoleId(roles, "systemConfigurator");
		createAndAddRoleWithRoleId(roles, "binaryUserRole");
		createAndAddRoleWithRoleId(roles, "userAdminRole");

		createAndAddRoleWithExtraRulePart(roles);
		addRepeatIdToAllUserRoles(roles);
	}

	private static boolean userHasAdminGroupRight(List<Map<String, Object>> dbResult) {
		for (Map<String, Object> dbRow : dbResult) {
			if (userHasAdminGroup(dbRow)) {
				return true;
			}
		}
		return false;
	}

	private static boolean userHasAdminGroup(Map<String, Object> dbRow) {
		return dbRow.get("group_id").equals(54);
	}

	private static void createAndAddRoleWithExtraRulePart(List<DataGroup> roles) {
		DataGroup systemOneSystemUserRole = createUserRoleWithRoleId("systemOneSystemUserRole");
		roles.add(systemOneSystemUserRole);

		createAndAddExtraRulePart(systemOneSystemUserRole);
	}

	private static void createAndAddExtraRulePart(DataGroup systemOneSystemUserRole) {
		DataGroup permissionTermRulePart = DataGroup.withNameInData("permissionTermRulePart");
		permissionTermRulePart.setRepeatId("1");
		createAndAddRuleLinkUsingParentGroupAndRuleId(permissionTermRulePart,
				"permissionUnitPermissionTerm");
		permissionTermRulePart.addChild(DataAtomic.withNameInDataAndValueAndRepeatId("value",
				"system.permissionUnit_uu.ub", "0"));
		systemOneSystemUserRole.addChild(permissionTermRulePart);
	}

	private static void createAndAddRoleWithRoleId(List<DataGroup> roles, String roleId) {
		DataGroup metadataAdmin = createUserRoleWithRoleId(roleId);
		roles.add(metadataAdmin);
	}

	private static DataGroup createUserRoleWithRoleId(String roleId) {
		DataGroup userRole = DataGroup.withNameInData(USER_ROLE);
		createAndAddLinkedUserRoleUsingParentGroupAndRoleId(userRole, roleId);
		addSystemPermissionTermAccessToAllSystems(userRole);
		return userRole;
	}

	private static void createAndAddLinkedUserRoleUsingParentGroupAndRoleId(DataGroup userRole,
			String roleId) {
		DataGroup linkedUserRole = DataGroup.asLinkWithNameInDataAndTypeAndId(USER_ROLE,
				"permissionRole", roleId);
		userRole.addChild(linkedUserRole);
	}

	private static void addSystemPermissionTermAccessToAllSystems(DataGroup userRole) {
		createAndAddRulePartUsingParentGroupRuleIdAndRulePartValue(userRole, "systemPermissionTerm",
				"system.*");
	}

	private static void createAndAddRulePartUsingParentGroupRuleIdAndRulePartValue(
			DataGroup userRole, String ruleLinkRecordId, String rulePartValue) {
		DataGroup permissionTermRulePart = DataGroup.withNameInData("permissionTermRulePart");
		userRole.addChild(permissionTermRulePart);
		permissionTermRulePart.setRepeatId("0");

		createAndAddRuleLinkUsingParentGroupAndRuleId(permissionTermRulePart, ruleLinkRecordId);

		permissionTermRulePart.addChild(
				DataAtomic.withNameInDataAndValueAndRepeatId("value", rulePartValue, "0"));
	}

	private static void createAndAddRuleLinkUsingParentGroupAndRuleId(
			DataGroup permissionTermRulePart, String ruleLinkRecordId) {
		DataGroup ruleLink = DataGroup.asLinkWithNameInDataAndTypeAndId("rule",
				"collectPermissionTerm", ruleLinkRecordId);
		permissionTermRulePart.addChild(ruleLink);
	}

	private static void addRepeatIdToAllUserRoles(List<DataGroup> roles) {
		int repeatId = 0;
		for (DataGroup role : roles) {
			role.setRepeatId(String.valueOf(repeatId));
			repeatId++;
		}
	}

}
