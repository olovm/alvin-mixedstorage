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
import static org.testng.Assert.assertSame;
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

import se.uu.ub.cora.alvin.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class UserRoleConverterHelperTest {
	List<Map<String, Object>> rowsFromDb = new ArrayList<>();
	private DataGroupFactorySpy dataGroupFactory;
	private DataAtomicFactorySpy dataAtomicFactory;

	@BeforeMethod
	public void setUp() {
		rowsFromDb = new ArrayList<>();
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("id", 52);
		rowFromDb.put("name", "ADMIN");
		rowFromDb.put("group_id", 51);
		rowFromDb.put("user_id", 1);
		rowsFromDb.add(rowFromDb);

		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
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

		DataGroupSpy factoredUserRole = dataGroupFactory.factoredDataGroups.get(0);

		assertEquals(factoredUserRole.nameInData, "userRole");
		assertSame(factoredUserRole, role);

		DataGroupSpy innerUserRole = dataGroupFactory.factoredDataGroups.get(1);
		assertCorrectInnerUserRole(roleId, factoredUserRole, innerUserRole);

		DataGroupSpy permissionTermRulePart = dataGroupFactory.factoredDataGroups.get(2);
		assertCorrectPermissionRulePart(factoredUserRole, permissionTermRulePart);

	}

	private void assertCorrectInnerUserRole(String roleId, DataGroupSpy factoredUserRole,
			DataGroupSpy innerUserRole) {
		assertEquals(innerUserRole.nameInData, "userRole");
		assertEquals(innerUserRole.recordType, "permissionRole");
		assertEquals(innerUserRole.recordId, roleId);
		assertSame(innerUserRole, factoredUserRole.getFirstChildWithNameInData("userRole"));
	}

	private void assertCorrectPermissionRulePart(DataGroupSpy factoredUserRole,
			DataGroupSpy permissionTermRulePart) {
		assertEquals(permissionTermRulePart.nameInData, "permissionTermRulePart");
		assertEquals(permissionTermRulePart.getRepeatId(), "0");
		assertSame(permissionTermRulePart,
				factoredUserRole.getFirstChildWithNameInData("permissionTermRulePart"));

		assertCorrectRuleLink(permissionTermRulePart);

		assertCorrectValuePart(permissionTermRulePart);
	}

	private void assertCorrectRuleLink(DataGroupSpy permissionTermRulePart) {
		DataGroupSpy ruleLink = dataGroupFactory.factoredDataGroups.get(3);
		assertEquals(ruleLink.nameInData, "rule");
		assertEquals(ruleLink.recordType, "collectPermissionTerm");
		assertEquals(ruleLink.recordId, "systemPermissionTerm");
		assertSame(ruleLink, permissionTermRulePart.getFirstChildWithNameInData("rule"));
	}

	private void assertCorrectValuePart(DataGroupSpy permissionTermRulePart) {
		DataAtomicSpy factoredDataAtomic = dataAtomicFactory.factoredDataAtomic;
		assertEquals(factoredDataAtomic.nameInData, "value");
		assertEquals(factoredDataAtomic.value, "system.*");
		assertEquals(factoredDataAtomic.repeatId, "0");
		assertSame(factoredDataAtomic, permissionTermRulePart.getFirstChildWithNameInData("value"));
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
