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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataGroup;

public class UserRoleConverterHelperTest {
	List<Map<String, Object>> rowsFromDb = new ArrayList<>();

	@BeforeMethod
	public void setUp() {
		rowsFromDb = new ArrayList<>();
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("id", 52);
		rowFromDb.put("name", "ADMIN");
		rowFromDb.put("group_id", 51);
		rowFromDb.put("user_id", 1);
		rowsFromDb.add(rowFromDb);
	}

	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<UserRoleConverterHelper> constructor = UserRoleConverterHelper.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<UserRoleConverterHelper> constructor = UserRoleConverterHelper.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testCreateRole() {
		String roleId = "someRoleId";

		DataGroup role = UserRoleConverterHelper
				.createUserRoleWithAllSystemsPermissionUsingRoleId(roleId);

		assertEquals(role.getNameInData(), "userRole");

		DataGroup innerUserRole = role.getFirstGroupWithNameInData("userRole");
		assertEquals(innerUserRole.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"permissionRole");
		assertEquals(innerUserRole.getFirstAtomicValueWithNameInData("linkedRecordId"), roleId);

		DataGroup permissionTermRulePart = role
				.getFirstGroupWithNameInData("permissionTermRulePart");
		assertEquals(permissionTermRulePart.getRepeatId(), "0");

		assertDataGroupContainsCorrectPermissionTerm(permissionTermRulePart,
				"systemPermissionTerm");

		assertRulePartContainsCorrectValue(permissionTermRulePart, "system.*");
	}

	private void assertDataGroupContainsCorrectPermissionTerm(DataGroup permissionTermRulePart,
			String linkedPermissionTerm) {
		DataGroup ruleLink = permissionTermRulePart.getFirstGroupWithNameInData("rule");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"collectPermissionTerm");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
				linkedPermissionTerm);
	}

	private void assertRulePartContainsCorrectValue(DataGroup permissionTermRulePart,
			String rulePartValue) {
		DataAtomic value = (DataAtomic) permissionTermRulePart.getFirstChildWithNameInData("value");
		assertEquals(value.getValue(), rulePartValue);
		assertEquals(value.getRepeatId(), "0");
	}

	@Test
	public void testRoleMapping() {
		String coraRoleId = UserRoleConverterHelper.getMatchingCoraRole(50);
		assertEquals(coraRoleId, "userAdminRole");

		String coraRoleId2 = UserRoleConverterHelper.getMatchingCoraRole(51);
		assertEquals(coraRoleId2, "personAdminRole");
		String coraRoleId3 = UserRoleConverterHelper.getMatchingCoraRole(52);
		assertEquals(coraRoleId3, "organisationAdminRole");
		String coraRoleId4 = UserRoleConverterHelper.getMatchingCoraRole(53);
		assertEquals(coraRoleId4, "placeAdminRole");

		String coraRoleId5 = UserRoleConverterHelper.getMatchingCoraRole(5300);
		assertEquals(coraRoleId5, "");
	}
}
