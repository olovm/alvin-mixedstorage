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
package se.uu.ub.cora.alvin.mixedstorage;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.spider.record.storage.RecordIdGenerator;

public class AlvinIdGeneratorTest {
	private RecordIdGenerator idGenerator;

	@BeforeMethod
	public void beforeMethod() {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactorySpy();
		String fedoraURL = "";
		String fedoraUserName = "";
		String fedoraPassword = "";
		IdGeneratorConnectionInfo idGeneratorConnectionInfo = new IdGeneratorConnectionInfo(
				fedoraURL, fedoraUserName, fedoraPassword);
		idGenerator = AlvinIdGenerator.usingHttpHandlerFactoryAndConnectionInfo(httpHandlerFactory,
				idGeneratorConnectionInfo);
	}

	@Test
	public void idStartsWithType() {
		String idForType = idGenerator.getIdForType("anyOldType");
		assertTrue(idForType.startsWith("anyOldType:"));
		assert (idForType.length() > 20);
	}

	@Test
	public void startsWithTypeThenColonEndsWithNumber() {
		String idForType = idGenerator.getIdForType("anyOtherType");
		assertTrue(idForType.matches("^anyOtherType:[0-9]+$"));
	}

	@Test
	public void towIdsNotEqual() throws Exception {
		String idForType = idGenerator.getIdForType("anyOldType");
		String idForType2 = idGenerator.getIdForType("anyOldType");
		assertNotEquals(idForType, idForType2);
	}

}
