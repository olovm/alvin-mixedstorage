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

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class UserRoleConverterTest {
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
		Constructor<UserRoleConverter> constructor = UserRoleConverter.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<UserRoleConverter> constructor = UserRoleConverter.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testNotAdmin() {
		List<DataGroup> roles = UserRoleConverter.convert(rowsFromDb);
		assertTrue(roles.isEmpty());
	}

	@Test
	public void testAdmin() {
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("id", 53);
		rowFromDb.put("name", "ADMIN");
		rowFromDb.put("group_id", 54);
		rowFromDb.put("user_id", 1);
		rowsFromDb.add(rowFromDb);

		List<DataGroup> roles = UserRoleConverter.convert(rowsFromDb);
		assertCorrectRoleInListUsingIndexNameAndRepeatId(roles, 0, "metadataAdmin", "0");
		assertCorrectRoleInListUsingIndexNameAndRepeatId(roles, 1, "systemConfigurator", "1");
		assertCorrectRoleInListUsingIndexNameAndRepeatId(roles, 2, "binaryUserRole", "2");
		assertCorrectRoleInListUsingIndexNameAndRepeatId(roles, 3, "userAdminRole", "3");
		assertCorrectRoleInListUsingIndexNameAndRepeatId(roles, 4, "systemOneSystemUserRole", "4");

		List<DataGroup> permissionTermRuleParts = roles.get(4)
				.getAllGroupsWithNameInData("permissionTermRulePart");
		DataGroup extraPermissionRulePart = permissionTermRuleParts.get(1);
		assertEquals(extraPermissionRulePart.getRepeatId(), "1");
		DataGroup ruleLink = extraPermissionRulePart.getFirstGroupWithNameInData("rule");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"collectPermissionTerm");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"permissionUnitPermissionTerm");
		assertRulePartContainsCorrectValue(extraPermissionRulePart, "system.permissionUnit_uu.ub");

		assertEquals(roles.size(), 5);
	}

	private void assertCorrectRoleInListUsingIndexNameAndRepeatId(List<DataGroup> roles, int index,
			String roleName, String repeatId) {
		DataGroup outerUserRole = roles.get(index);
		assertEquals(outerUserRole.getRepeatId(), repeatId);

		DataGroup innerUserRole = outerUserRole.getFirstGroupWithNameInData("userRole");
		assertEquals(innerUserRole.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"permissionRole");
		assertEquals(innerUserRole.getFirstAtomicValueWithNameInData("linkedRecordId"), roleName);

		DataGroup permissionTermRulePart = outerUserRole
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

}
