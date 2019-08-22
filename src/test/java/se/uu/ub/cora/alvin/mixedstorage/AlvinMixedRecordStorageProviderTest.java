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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.fedora.AlvinFedoraConverterFactory;
import se.uu.ub.cora.alvin.mixedstorage.fedora.AlvinFedoraToCoraConverterFactoryImp;
import se.uu.ub.cora.alvin.mixedstorage.fedora.FedoraRecordStorage;
import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.bookkeeper.storage.MetadataStorage;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
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
		initInfo.put("storageOnDiskBasePath", basePath);
		initInfo.put("fedoraURL", "http://alvin-cora-fedora:8088/fedora/");
		initInfo.put("fedoraUsername", "fedoraUser");
		initInfo.put("fedoraPassword", "fedoraPass");
		initInfo.put("databaseLookupName", "java:/comp/env/jdbc/postgres");

		makeSureBasePathExistsAndIsEmpty();
		recordStorageOnDiskProvider = new AlvinMixedRecordStorageProvider();
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
	public void testAlvinMixedRecordStorageContainsCorrectBasicStorage() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		AlvinMixedRecordStorage recordStorage = (AlvinMixedRecordStorage) recordStorageOnDiskProvider
				.getRecordStorage();
		RecordStorage basicStorage = recordStorage.getBasicStorage();
		assertTrue(basicStorage instanceof RecordStorageInMemoryReadFromDisk);
		assertEquals(((RecordStorageInMemoryReadFromDisk) basicStorage).getBasePath(),
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
		String fedoraURL = ((AlvinFedoraToCoraConverterFactoryImp) alvinFedoraConverterFactory)
				.getFedoraURL();
		assertEquals(fedoraURL, initInfo.get("fedoraURL"));
		// TODO: check for error and logs if not found fedora url in initinfo

		String baseURL = fedoraToCoraStorage.getBaseURL();
		assertEquals(baseURL, initInfo.get("fedoraURL"));

		// AlvinFedoraToCoraConverterFactoryImp converterFactory =
		// (AlvinFedoraToCoraConverterFactoryImp) fedoraToCoraStorage.converterFactory;
		// assertTrue(converterFactory instanceof AlvinFedoraToCoraConverterFactoryImp);
		// assertEquals(converterFactory.getFedoraURL(), initInfo.get("fedoraURL"));
		// assertEquals(fedoraToCoraStorage.baseURL, initInfo.get("fedoraURL"));
		// assertEquals(fedoraToCoraStorage.fedoraUsername, initInfo.get("fedoraUsername"));
		// assertEquals(fedoraToCoraStorage.fedoraPassword, initInfo.get("fedoraPassword"));
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
		RecordStorage recordStorage = recordStorageOnDiskProvider.getRecordStorage();
		MetadataStorage metadataStorage = metadataStorageProvider.getMetadataStorage();
		assertSame(metadataStorage, recordStorage);
	}

	@Test
	public void testLoggingNormalStartup() {
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinMixedRecordStorageProvider starting AlvinMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found /tmp/recordStorageOnDiskTempBasicStorageProvider/ as storageOnDiskBasePath");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"AlvinMixedRecordStorageProvider started AlvinMixedRecordStorage");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 3);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameters() {
		initInfo.remove("storageOnDiskBasePath");
		try {
			recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AlvinMixedRecordStorageProvider starting RecordStorageInMemoryReadFromDisk...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 1);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain storageOnDiskBasePath");
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test(expectedExceptions = DataStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "InitInfo must contain storageOnDiskBasePath")
	public void testErrorIfMissingStartParameters() {
		initInfo.remove("storageOnDiskBasePath");
		recordStorageOnDiskProvider.startUsingInitInfo(initInfo);
	}

}
