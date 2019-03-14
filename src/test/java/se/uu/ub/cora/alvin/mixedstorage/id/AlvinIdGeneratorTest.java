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
package se.uu.ub.cora.alvin.mixedstorage.id;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.HttpHandlerFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.HttpHandlerSpy;
import se.uu.ub.cora.alvin.mixedstorage.fedora.FedoraException;
import se.uu.ub.cora.alvin.mixedstorage.resource.ResourceReader;

public class AlvinIdGeneratorTest {
	private AlvinIdGenerator idGenerator;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private String fedoraBaseURL = "http://alvin-cora-fedora:8088/fedora/";
	private String fedoraUsername = "fedoraUser";
	private String fedoraPassword = "fedoraPassword";
	private IdGeneratorConnectionInfo idGeneratorConnectionInfo;

	@BeforeMethod
	public void beforeMethod() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		idGeneratorConnectionInfo = new IdGeneratorConnectionInfo(fedoraBaseURL, fedoraUsername,
				fedoraPassword);
		idGenerator = AlvinIdGenerator.usingHttpHandlerFactoryAndConnectionInfo(httpHandlerFactory,
				idGeneratorConnectionInfo);
		httpHandlerFactory.responseText = ResourceReader
				.readResourceAsString("place/nextPlacePid.xml");
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

	@Test
	public void testIdForPlaceFromFedora() throws Exception {
		String generatedPlaceId = idGenerator.getIdForType("place");
		assertCorrectHttpHandlerForNextPid();
		assertEquals(generatedPlaceId, "next-pid:444");
	}

	private void assertCorrectHttpHandlerForNextPid() {
		HttpHandlerSpy httpHandlerForPid = httpHandlerFactory.factoredHttpHandlers.get(0);

		assertEquals(httpHandlerFactory.urls.get(0),
				fedoraBaseURL + "objects/nextPID?namespace=alvin-place" + "&format=xml");

		assertEquals(httpHandlerForPid.requestMethod, "POST");
		assertEquals(httpHandlerForPid.username, fedoraUsername);
		assertEquals(httpHandlerForPid.password, fedoraPassword);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "getting next pid from fedora failed, with response code: 500")
	public void testErrorFetchingPlacePid() throws Exception {
		httpHandlerFactory.responseCode = 500;
		idGenerator.getIdForType("place");
	}

	@Test
	public void testMethodsNeededForTest() throws Exception {
		assertEquals(idGenerator.getFedoraURL(), fedoraBaseURL);
		assertEquals(idGenerator.getFedoraUsername(), fedoraUsername);
		assertEquals(idGenerator.getFedoraPassword(), fedoraPassword);
	}

}
