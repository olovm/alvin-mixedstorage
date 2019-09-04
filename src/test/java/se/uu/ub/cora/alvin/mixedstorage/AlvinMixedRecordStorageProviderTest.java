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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.naming.InitialContext;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.db.AlvinDbToCoraConverterFactoryImp;
import se.uu.ub.cora.alvin.mixedstorage.db.AlvinDbToCoraRecordStorage;
import se.uu.ub.cora.alvin.mixedstorage.fedora.AlvinFedoraConverterFactory;
import se.uu.ub.cora.alvin.mixedstorage.fedora.AlvinFedoraToCoraConverterFactoryImp;
import se.uu.ub.cora.alvin.mixedstorage.fedora.FedoraRecordStorage;
import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReaderImp;
import se.uu.ub.cora.storage.MetadataStorage;
import se.uu.ub.cora.storage.MetadataStorageProvider;
import se.uu.ub.cora.storage.RecordStorage;

public class AlvinMixedRecordStorageProviderTest {
	private Map<String, String> initInfo = new HashMap<>();
	private String basePath = "/tmp/recordStorageOnDiskTempBasicStorageProvider/";
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "AlvinMixedRecordStorageProvider";
	private AlvinMixedRecordStorageProvider recordStorageOnDiskProvider;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		initInfo = new HashMap<>();
		initInfo.put("storageType", "memory");
		initInfo.put("storageOnDiskBasePath", basePath);
		initInfo.put("fedoraURL", "http://alvin-cora-fedora:8088/fedora/");
		initInfo.put("fedoraUsername", "fedoraUser");
		initInfo.put("fedoraPassword", "fedoraPass");
		initInfo.put("databaseLookupName", "java:/comp/env/jdbc/postgres");

		makeSureBasePathExistsAndIsEmpty();
		recordStorageOnDiskProvider = new AlvinMixedRecordStorageProvider();
		RecordStorageInstance.instance = null;
	}

	public void makeSureBasePathExistsAndIsEmpty() throws IOException {
		File dir = new File(basePath);
		dir.mkdir();
		deleteFiles(basePath);

	}

	private void deleteFiles(String path) throws IOException {
		Stream<Path> list;
		list = Files.list(Paths.get(path));

		list.forEach(p -> deleteFile(p));
		list.close();
	}

	private void deleteFile(Path path) {
		try {
			if (path.toFile().isDirectory()) {
				deleteFiles(path.toString());
			}
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetOrderToSelectImplementationsByIsOne() {
		assertEquals(recordStorageOnDiskProvider.getOrderToSelectImplementionsBy(), 1);
	}

	@Test
	public void testNormalStartupReturnsAlvinMixedRecordStorage() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = recordStorageOnDiskProvider.getRecordStorage();
		assertTrue(recordStorage instanceof AlvinMixedRecordStorage);
	}

	@Test
	public void testAlvinMixedRecordStorageContainsCorrectBasicStorageInMemory() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		AlvinMixedRecordStorage recordStorage = (AlvinMixedRecordStorage) recordStorageOnDiskProvider
				.getRecordStorage();
		RecordStorage basicStorage = recordStorage.getBasicStorage();
		assertTrue(basicStorage instanceof RecordStorageInMemoryReadFromDisk);
		assertEquals(((RecordStorageInMemoryReadFromDisk) basicStorage).getBasePath(),
				initInfo.get("storageOnDiskBasePath"));
	}

	@Test
	public void testAlvinMixedRecordStorageContainsCorrectBasicStorageOnDisk() {
		initInfo.put("storageType", "disk");
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		AlvinMixedRecordStorage recordStorage = (AlvinMixedRecordStorage) recordStorageOnDiskProvider
				.getRecordStorage();
		RecordStorage basicStorage = recordStorage.getBasicStorage();
		assertTrue(basicStorage instanceof RecordStorageOnDisk);
		assertFalse(basicStorage instanceof RecordStorageInMemoryReadFromDisk);
		assertEquals(((RecordStorageOnDisk) basicStorage).getBasePath(),
				initInfo.get("storageOnDiskBasePath"));
	}

	@Test
	public void testAlvinMixedRecordStorageContainsCorrectFedoraStorage() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		AlvinMixedRecordStorage recordStorage = (AlvinMixedRecordStorage) recordStorageOnDiskProvider
				.getRecordStorage();
		RecordStorage fedoraStorage = recordStorage.getFedoraStorage();
		assertTrue(fedoraStorage instanceof FedoraRecordStorage);

		FedoraRecordStorage fedoraToCoraStorage = (FedoraRecordStorage) fedoraStorage;
		assertTrue(fedoraToCoraStorage.getHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

		AlvinFedoraConverterFactory alvinFedoraConverterFactory = fedoraToCoraStorage
				.getAlvinFedoraConverterFactory();
		assertTrue(alvinFedoraConverterFactory instanceof AlvinFedoraToCoraConverterFactoryImp);
		String fedoraURLInConverter = ((AlvinFedoraToCoraConverterFactoryImp) alvinFedoraConverterFactory)
				.getFedoraURL();
		assertEquals(fedoraURLInConverter, initInfo.get("fedoraURL"));

		String baseURLInFedoraToCoraStorage = fedoraToCoraStorage.getBaseURL();
		assertEquals(baseURLInFedoraToCoraStorage, initInfo.get("fedoraURL"));

		String fedoraUsername = fedoraToCoraStorage.getFedoraUsername();
		assertEquals(fedoraUsername, initInfo.get("fedoraUsername"));

		String fedoraPassword = fedoraToCoraStorage.getFedoraPassword();
		assertEquals(fedoraPassword, initInfo.get("fedoraPassword"));
	}

	@Test
	public void testAlvinMixedRecordStorageContainsCorrectDbStorage() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		AlvinMixedRecordStorage recordStorage = (AlvinMixedRecordStorage) recordStorageOnDiskProvider
				.getRecordStorage();
		RecordStorage dbStorageInRecordStorage = recordStorage.getDbStorage();
		assertTrue(dbStorageInRecordStorage instanceof AlvinDbToCoraRecordStorage);

		AlvinDbToCoraRecordStorage dbStorage = (AlvinDbToCoraRecordStorage) dbStorageInRecordStorage;
		DataReaderImp dataReader = (DataReaderImp) dbStorage.getDataReader();

		ContextConnectionProviderImp connectionProvider = (ContextConnectionProviderImp) dataReader
				.getSqlConnectionProvider();
		assertTrue(connectionProvider instanceof ContextConnectionProviderImp);

		assertEquals(connectionProvider.getName(), initInfo.get("databaseLookupName"));
		assertTrue(connectionProvider.getContext() instanceof InitialContext);

		assertTrue(dbStorage.getConverterFactory() instanceof AlvinDbToCoraConverterFactoryImp);

		AlvinDbToCoraConverterFactoryImp dbToCoraConverter = (AlvinDbToCoraConverterFactoryImp) dbStorage
				.getConverterFactory();
		assertSame(dbToCoraConverter.getDataReader(), dataReader);

	}

	@Test
	public void testNormalStartupReturnsTheSameRecordStorageForMultipleCalls() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = recordStorageOnDiskProvider.getRecordStorage();
		RecordStorage recordStorage2 = recordStorageOnDiskProvider.getRecordStorage();
		assertSame(recordStorage, recordStorage2);
	}

	@Test
	public void testRecordStorageStartedByOtherProviderIsReturned() {
		RecordStorageSpy recordStorageSpy = new RecordStorageSpy();
		RecordStorageInstance.instance = recordStorageSpy;
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = recordStorageOnDiskProvider.getRecordStorage();
		assertSame(recordStorage, recordStorageSpy);
	}

	@Test
	public void testLoggingRecordStorageStartedByOtherProvider() {
		RecordStorageSpy recordStorageSpy = new RecordStorageSpy();
		RecordStorageInstance.instance = recordStorageSpy;
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinMixedRecordStorageProvider starting AlvinMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Using previously started RecordStorage as RecordStorage");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"AlvinMixedRecordStorageProvider started AlvinMixedRecordStorage");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 3);
	}

	@Test
	public void testRecordStorageIsAccessibleToOthers() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = recordStorageOnDiskProvider.getRecordStorage();
		assertSame(recordStorage, RecordStorageInstance.instance);
	}

	@Test
	public void testMetadataStorageIsRecordStorage() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		MetadataStorageProvider metadataStorageProvider = recordStorageOnDiskProvider;
		AlvinMixedRecordStorage recordStorage = (AlvinMixedRecordStorage) recordStorageOnDiskProvider
				.getRecordStorage();
		MetadataStorage metadataStorage = metadataStorageProvider.getMetadataStorage();
		assertSame(metadataStorage, recordStorage.getBasicStorage());
	}

	@Test
	public void testLoggingNormalStartup() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinMixedRecordStorageProvider starting AlvinMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found /tmp/recordStorageOnDiskTempBasicStorageProvider/ as storageOnDiskBasePath");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"Found memory as storageType");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 3),
				"Found http://alvin-cora-fedora:8088/fedora/ as fedoraURL");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 4),
				"Found java:/comp/env/jdbc/postgres as databaseLookupName");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 5),
				"AlvinMixedRecordStorageProvider started AlvinMixedRecordStorage");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 6);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterStorageOnDiskBasePath() {
		initInfo.remove("storageOnDiskBasePath");
		try {
			recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinMixedRecordStorageProvider starting AlvinMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 1);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain storageOnDiskBasePath");
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterFedoraURL() {
		assertCorrectErrorAndLogOnMissingParameter("fedoraURL", 3);
	}

	private void assertCorrectErrorAndLogOnMissingParameter(String parameter,
			int noOfInfoMessages) {
		initInfo.remove(parameter);
		String errorMessage = "InitInfo must contain " + parameter;
		try {
			recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {
			assertTrue(e instanceof DataStorageException);
			assertEquals(e.getMessage(), errorMessage);

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinMixedRecordStorageProvider starting AlvinMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName),
				noOfInfoMessages);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				errorMessage);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterFedoraUsername() {
		assertCorrectErrorAndLogOnMissingParameter("fedoraUsername", 4);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterFedoraPassword() {
		assertCorrectErrorAndLogOnMissingParameter("fedoraPassword", 4);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterDatabaseLookupName() {
		assertCorrectErrorAndLogOnMissingParameter("databaseLookupName", 4);
	}

}
