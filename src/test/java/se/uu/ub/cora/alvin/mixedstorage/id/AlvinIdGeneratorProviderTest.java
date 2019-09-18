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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.fedora.FedoraException;
import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordIdGenerator;

public class AlvinIdGeneratorProviderTest {
	private Map<String, String> initInfo = new HashMap<>();
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "AlvinIdGeneratorProvider";
	private AlvinIdGeneratorProvider alvinIdGeneratorProvider;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		initInfo = new HashMap<>();
		initInfo.put("fedoraURL", "someFedoraURL");
		initInfo.put("fedoraUsername", "someFedoraUsername");
		initInfo.put("fedoraPassword", "someFedoraPassword");
		alvinIdGeneratorProvider = new AlvinIdGeneratorProvider();
	}

	@Test
	public void testGetOrderToSelectImplementationsByIsOne() {
		assertEquals(alvinIdGeneratorProvider.getOrderToSelectImplementionsBy(), 1);
	}

	@Test
	public void testNormalStartupReturnsAlvinIdGenerator() {
		alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
		RecordIdGenerator recordIdGenerator = alvinIdGeneratorProvider.getRecordIdGenerator();
		assertTrue(recordIdGenerator instanceof AlvinIdGenerator);
	}

	@Test
	public void testNormalStartupReturnsTheSameIdGeneratorForMultipleCalls() {
		alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
		RecordIdGenerator recordIdGenerator = alvinIdGeneratorProvider.getRecordIdGenerator();
		RecordIdGenerator recordIdGenerator2 = alvinIdGeneratorProvider.getRecordIdGenerator();
		assertSame(recordIdGenerator, recordIdGenerator2);
	}

	@Test
	public void testInitInfoParametersAreUsedInGenerator() throws Exception {
		alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
		AlvinIdGenerator recordIdGenerator = (AlvinIdGenerator) alvinIdGeneratorProvider
				.getRecordIdGenerator();
		IdGeneratorConnectionInfo connectInfo = recordIdGenerator.getConnectInfo();
		assertEquals(initInfo.get("fedoraURL"), connectInfo.fedoraURL);
		assertEquals(initInfo.get("fedoraUsername"), connectInfo.fedoraUsername);
		assertEquals(initInfo.get("fedoraPassword"), connectInfo.fedoraPassword);
	}

	@Test
	public void testLoggingNormalStartup() {
		alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinIdGeneratorProvider starting AlvinIdGenerator...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found someFedoraURL as fedoraURL");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"AlvinIdGeneratorProvider started AlvinIdGenerator");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 3);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 0);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "InitInfo must contain fedoraURL")
	public void testErrorIfMissingStartParameterFedoraURL() {
		initInfo.remove("fedoraURL");
		alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "InitInfo must contain fedoraUsername")
	public void testErrorIfMissingStartParameterFedoraUsername() {
		initInfo.remove("fedoraUsername");
		alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "InitInfo must contain fedoraPassword")
	public void testErrorIfMissingStartParameterFedoraPassword() {
		initInfo.remove("fedoraPassword");
		alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
	}

	@Test
	public void testLoggingAndErrorIfMissingParameterFedoraURL() {
		assertFatalLogMessageForMissingParameter("fedoraURL");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 1);
	}

	private void assertFatalLogMessageForMissingParameter(String parameterName) {
		initInfo.remove(parameterName);
		try {
			alvinIdGeneratorProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinIdGeneratorProvider starting AlvinIdGenerator...");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain " + parameterName);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test
	public void testLoggingAndErrorIfMissingParameterFedoraUsername() {
		assertFatalLogMessageForMissingParameter("fedoraUsername");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 2);
	}

	@Test
	public void testLoggingAndErrorIfMissingParameterFedoraPassword() {
		assertFatalLogMessageForMissingParameter("fedoraPassword");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 2);
	}
}
