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
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class UserConverterHelperTest {
	Map<String, Object> rowFromDb = new HashMap<>();
	private DataGroupFactorySpy dataGroupFactory;
	private DataAtomicFactorySpy dataAtomicFactory;

	@BeforeMethod
	public void setUp() {
		rowFromDb.put("id", 52);
		rowFromDb.put("domain", "uu");
		rowFromDb.put("email", "");
		rowFromDb.put("firstname", "SomeFirstName");
		rowFromDb.put("lastname", "SomeLastName");
		rowFromDb.put("userid", "user52");
		rowFromDb.put("group_id", "");

		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
	}

	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<UserConverterHelper> constructor = UserConverterHelper.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<UserConverterHelper> constructor = UserConverterHelper.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testCreateBasicActiveUser() {
		DataGroup user = UserConverterHelper.createBasicActiveUser();
		DataGroupSpy factoredDataGroup = dataGroupFactory.factoredDataGroup;
		assertEquals(factoredDataGroup.nameInData, "user");
		assertEquals(factoredDataGroup.addedAttributes.get("type"), "coraUser");
		assertSame(factoredDataGroup, user);

		DataAtomicSpy factoredDataAtomic = dataAtomicFactory.factoredDataAtomic;
		assertEquals(factoredDataAtomic.getNameInData(), "activeStatus");
		assertEquals(factoredDataAtomic.getValue(), "active");
	}

	@Test
	public void testCreateType() {
		DataGroup type = UserConverterHelper.createType();
		DataGroupSpy factoredDataGroup = dataGroupFactory.factoredDataGroup;
		assertEquals(factoredDataGroup.nameInData, "type");
		assertEquals(factoredDataGroup.recordType, "recordType");
		assertEquals(factoredDataGroup.recordId, "coraUser");
		assertSame(factoredDataGroup, type);

	}

	@Test
	public void testCreateDataDivider() {
		DataGroup dataDivider = UserConverterHelper.createDataDivider();
		DataGroupSpy factoredDataGroup = dataGroupFactory.factoredDataGroup;
		assertEquals(factoredDataGroup.nameInData, "dataDivider");
		assertEquals(factoredDataGroup.recordType, "system");
		assertEquals(factoredDataGroup.recordId, "alvin");
		assertSame(factoredDataGroup, dataDivider);

	}

	@Test
	public void testCreateUpdatedInfoUsingUserId() {
		DataGroup updated = UserConverterHelper.createUpdatedInfoUsingUserId("someUserId");
		DataGroupSpy factoredDataGroupUpdated = dataGroupFactory.factoredDataGroups.get(0);
		assertEquals(factoredDataGroupUpdated.nameInData, "updated");
		assertEquals(factoredDataGroupUpdated.getRepeatId(), "0");
		assertSame(factoredDataGroupUpdated, updated);

		DataGroupSpy factoredDataGroupUpdatedBy = assertCorrectUpdatedByGroup();
		DataAtomicSpy factoredDataAtomic = assertCorrectTsUpdated();

		assertFactoredElementsAreAddedAsChildren(factoredDataGroupUpdated,
				factoredDataGroupUpdatedBy, factoredDataAtomic);

	}

	private DataAtomicSpy assertCorrectTsUpdated() {
		DataAtomicSpy factoredDataAtomic = dataAtomicFactory.factoredDataAtomic;
		assertEquals(factoredDataAtomic.getNameInData(), "tsUpdated");
		assertEquals(factoredDataAtomic.getValue(), "2017-10-01 00:00:00.000");
		return factoredDataAtomic;
	}

	private DataGroupSpy assertCorrectUpdatedByGroup() {
		DataGroupSpy factoredDataGroupUpdatedBy = dataGroupFactory.factoredDataGroups.get(1);
		assertEquals(factoredDataGroupUpdatedBy.nameInData, "updatedBy");
		assertEquals(factoredDataGroupUpdatedBy.recordType, "coraUser");
		assertEquals(factoredDataGroupUpdatedBy.recordId, "someUserId");
		return factoredDataGroupUpdatedBy;
	}

	private void assertFactoredElementsAreAddedAsChildren(DataGroupSpy factoredDataGroupUpdated,
			DataGroupSpy factoredDataGroupUpdatedBy, DataAtomicSpy factoredDataAtomic) {
		assertSame(factoredDataGroupUpdated.getFirstChildWithNameInData("updatedBy"),
				factoredDataGroupUpdatedBy);
		assertSame(factoredDataGroupUpdated.getFirstChildWithNameInData("tsUpdated"),
				factoredDataAtomic);
	}

	@Test
	public void testCreateCreatedByUsingUserId() {
		DataGroup createdBy = UserConverterHelper.createCreatedByUsingUserId("someUserId");
		DataGroupSpy factoredDataGroup = dataGroupFactory.factoredDataGroup;
		assertEquals(factoredDataGroup.nameInData, "createdBy");
		assertEquals(factoredDataGroup.recordType, "coraUser");
		assertEquals(factoredDataGroup.recordId, "someUserId");
		assertSame(factoredDataGroup, createdBy);
	}

	@Test
	public void testCreateTsCreated() {
		DataAtomic tsCreated = UserConverterHelper.createTsCreated();
		assertEquals(tsCreated.getValue(), "2017-10-01 00:00:00.000");
	}

}
