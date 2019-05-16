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

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class UserConverterTest {
	Map<String, Object> rowFromDb = new HashMap<>();

	@BeforeMethod
	public void setUp() {
		rowFromDb.put("id", 52);
		rowFromDb.put("domain", "uu");
		rowFromDb.put("email", "");
		rowFromDb.put("firstname", "SomeFirstName");
		rowFromDb.put("lastname", "SomeLastName");
		rowFromDb.put("userid", "user52");
		rowFromDb.put("group_id", "");
	}

	@Test
	public void testConvertingRecordInfo() {
		DataGroup userDataGroup = UserConverter.convertFromRow(rowFromDb);

		assertEquals(userDataGroup.getNameInData(), "user");
		assertEquals(userDataGroup.getAttribute("type"), "coraUser");

		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("userId"),
				"user52@user.uu.se");
		DataGroup recordInfo = userDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), "52");

		DataGroup type = recordInfo.getFirstGroupWithNameInData("type");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordId"), "coraUser");

		DataGroup dataDivider = recordInfo.getFirstGroupWithNameInData("dataDivider");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordType"), "system");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordId"), "alvin");
		assertCorrectCreatedInfo(recordInfo);
		assertCorrectUpdatedInfo(recordInfo);
	}

	private void assertCorrectCreatedInfo(DataGroup recordInfo) {
		String tsCreated = recordInfo.getFirstAtomicValueWithNameInData("tsCreated");
		assertEquals(tsCreated, "2017-10-01 00:00:00.000");
		DataGroup createdBy = recordInfo.getFirstGroupWithNameInData("createdBy");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412566252284358");
	}

	private void assertCorrectUpdatedInfo(DataGroup recordInfo) {
		DataGroup updated = recordInfo.getFirstGroupWithNameInData("updated");
		DataGroup updatedBy = updated.getFirstGroupWithNameInData("updatedBy");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412566252284358");
		assertEquals(updated.getRepeatId(), "0");

		assertEquals(updated.getFirstAtomicValueWithNameInData("tsUpdated"),
				"2017-10-01 00:00:00.000");
	}

	@Test
	public void testConvertingUser() {
		DataGroup userDataGroup = UserConverter.convertFromRow(rowFromDb);
		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("activeStatus"), "active");
		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("userFirstName"),
				"SomeFirstName");
		assertEquals(userDataGroup.getFirstAtomicValueWithNameInData("userLastName"),
				"SomeLastName");
	}

	private void assertCorrectUserRoleWithSystemPermissionTerm(DataGroup userRole, String roleId,
			String repeatId, String rulePartValue) {
		assertEquals(userRole.getRepeatId(), repeatId);

		assertDataGroupContainsRoleWithId(userRole, roleId);

		DataGroup permissionTermRulePart = userRole
				.getFirstGroupWithNameInData("permissionTermRulePart");
		assertEquals(permissionTermRulePart.getRepeatId(), "0");

		assertDataGroupContainsCorrectPermissionTerm(permissionTermRulePart,
				"systemPermissionTerm");

		assertRulePartContainsCorrectValue(permissionTermRulePart, rulePartValue);
	}

	private void assertRulePartContainsCorrectValue(DataGroup permissionTermRulePart,
			String rulePartValue) {
		DataAtomic value = (DataAtomic) permissionTermRulePart.getFirstChildWithNameInData("value");
		assertEquals(value.getValue(), rulePartValue);
		assertEquals(value.getRepeatId(), "0");
	}

	private void assertDataGroupContainsRoleWithId(DataGroup userRole, String roleId) {
		DataGroup linkedUserRole = userRole.getFirstGroupWithNameInData("userRole");
		assertEquals(linkedUserRole.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"permissionRole");
		assertEquals(linkedUserRole.getFirstAtomicValueWithNameInData("linkedRecordId"), roleId);
	}

	private void assertDataGroupContainsCorrectPermissionTerm(DataGroup permissionTermRulePart,
			String linkedPermissionTerm) {
		DataGroup ruleLink = permissionTermRulePart.getFirstGroupWithNameInData("rule");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"collectPermissionTerm");
		assertEquals(ruleLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
				linkedPermissionTerm);
	}

}
