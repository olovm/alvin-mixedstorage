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

import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class UserRoleConverterHelper {

	private static final int ADMIN_ROLE = 54;
	private static final int PLACE_ADMIN_ROLE = 53;
	private static final int ORGANISATION_ADMIN_ROLE = 52;
	private static final int PERSON_ADMIN_ROLE = 51;
	private static final int USER_ADMIN_ROLE = 50;
	private static final String USER_ROLE = "userRole";

	private UserRoleConverterHelper() {
		throw new UnsupportedOperationException();
	}

	public static DataGroup createUserRoleWithAllSystemsPermissionUsingRoleId(String roleId) {
		return createUserRoleWithRoleId(roleId);
	}

	public static boolean userHasAdminGroupRight(List<Map<String, Object>> dbResult) {
		for (Map<String, Object> dbRow : dbResult) {
			if (userHasAdminGroup(dbRow)) {
				return true;
			}
		}
		return false;
	}

	private static boolean userHasAdminGroup(Map<String, Object> dbRow) {
		return dbRow.get("group_id").equals(ADMIN_ROLE);
	}

	private static DataGroup createUserRoleWithRoleId(String roleId) {
		DataGroup userRole = DataGroupProvider.getDataGroupUsingNameInData(USER_ROLE);
		createAndAddLinkedUserRoleUsingParentGroupAndRoleId(userRole, roleId);
		addSystemPermissionTermAccessToAllSystems(userRole);
		return userRole;
	}

	private static void createAndAddLinkedUserRoleUsingParentGroupAndRoleId(DataGroup userRole,
			String roleId) {
		DataGroup linkedUserRole = DataGroupProvider
				.getDataGroupAsLinkUsingNameInDataTypeAndId(USER_ROLE, "permissionRole", roleId);
		userRole.addChild(linkedUserRole);
	}

	private static void addSystemPermissionTermAccessToAllSystems(DataGroup userRole) {
		createAndAddRulePartUsingParentGroupRuleIdAndRulePartValue(userRole, "systemPermissionTerm",
				"system.*");
	}

	private static void createAndAddRulePartUsingParentGroupRuleIdAndRulePartValue(
			DataGroup userRole, String ruleLinkRecordId, String rulePartValue) {
		DataGroup permissionTermRulePart = DataGroupProvider
				.getDataGroupUsingNameInData("permissionTermRulePart");
		userRole.addChild(permissionTermRulePart);
		permissionTermRulePart.setRepeatId("0");

		createAndAddRuleLinkUsingParentGroupAndRuleId(permissionTermRulePart, ruleLinkRecordId);

		DataAtomic rulePartAtomic = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("value",
				rulePartValue);
		rulePartAtomic.setRepeatId("0");
		permissionTermRulePart.addChild(rulePartAtomic);

	}

	private static void createAndAddRuleLinkUsingParentGroupAndRuleId(
			DataGroup permissionTermRulePart, String ruleLinkRecordId) {
		DataGroup ruleLink = DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("rule",
				"collectPermissionTerm", ruleLinkRecordId);
		permissionTermRulePart.addChild(ruleLink);
	}

	public static String getMatchingCoraRole(int roleId) {
		if (USER_ADMIN_ROLE == roleId) {
			return "userAdminRole";
		} else if (PERSON_ADMIN_ROLE == roleId) {
			return "personAdminRole";
		} else if (ORGANISATION_ADMIN_ROLE == roleId) {
			return "organisationAdminRole";
		} else if (PLACE_ADMIN_ROLE == roleId) {
			return "placeAdminRole";
		}
		return "";
	}

}
