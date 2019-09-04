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

import java.util.Map;

import javax.naming.InitialContext;

import se.uu.ub.cora.alvin.mixedstorage.db.AlvinDbToCoraConverterFactoryImp;
import se.uu.ub.cora.alvin.mixedstorage.db.AlvinDbToCoraRecordStorage;
import se.uu.ub.cora.alvin.mixedstorage.fedora.AlvinFedoraConverterFactory;
import se.uu.ub.cora.alvin.mixedstorage.fedora.AlvinFedoraToCoraConverterFactoryImp;
import se.uu.ub.cora.alvin.mixedstorage.fedora.FedoraRecordStorage;
import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReaderImp;
import se.uu.ub.cora.storage.MetadataStorage;
import se.uu.ub.cora.storage.MetadataStorageProvider;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.RecordStorageProvider;

public class AlvinMixedRecordStorageProvider
		implements RecordStorageProvider, MetadataStorageProvider {
	private Logger log = LoggerProvider.getLoggerForClass(AlvinMixedRecordStorageProvider.class);
	private Map<String, String> initInfo;

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 1;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		this.initInfo = initInfo;
		log.logInfoUsingMessage(
				"AlvinMixedRecordStorageProvider starting AlvinMixedRecordStorage...");
		startRecordStorage();
		log.logInfoUsingMessage("AlvinMixedRecordStorageProvider started AlvinMixedRecordStorage");
	}

	private void startRecordStorage() {
		if (noRunningRecordStorageExists()) {
			startNewRecordStorageOnDiskInstance();
		} else {
			useExistingRecordStorage();
		}
	}

	private boolean noRunningRecordStorageExists() {
		return RecordStorageInstance.instance == null;
	}

	private void startNewRecordStorageOnDiskInstance() {
		RecordStorage basicStorage = createBasicStorage();
		FedoraRecordStorage fedoraStorage = createFedoraStorage();
		AlvinDbToCoraRecordStorage dbStorage = createDbStorage();
		RecordStorage alvinMixedRecordStorage = AlvinMixedRecordStorage
				.usingBasicAndFedoraAndDbStorage(basicStorage, fedoraStorage, dbStorage);
		setStaticInstance(alvinMixedRecordStorage);
	}

	private RecordStorage createBasicStorage() {
		String basePath = tryToGetInitParameterLogIfFound("storageOnDiskBasePath");
		String type = tryToGetInitParameterLogIfFound("storageType");
		if ("memory".equals(type)) {
			return RecordStorageInMemoryReadFromDisk
					.createRecordStorageOnDiskWithBasePath(basePath);
		}
		return RecordStorageOnDisk.createRecordStorageOnDiskWithBasePath(basePath);
	}

	private String tryToGetInitParameterLogIfFound(String parameterName) {
		String basePath = tryToGetInitParameter(parameterName);
		log.logInfoUsingMessage("Found " + basePath + " as " + parameterName);
		return basePath;
	}

	private FedoraRecordStorage createFedoraStorage() {
		String fedoraURL = tryToGetInitParameterLogIfFound("fedoraURL");
		String fedoraUsername = tryToGetInitParameter("fedoraUsername");
		String fedoraPassword = tryToGetInitParameter("fedoraPassword");

		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		AlvinFedoraConverterFactory converterFactory = AlvinFedoraToCoraConverterFactoryImp
				.usingFedoraURL(fedoraURL);

		return FedoraRecordStorage
				.usingHttpHandlerFactoryAndConverterFactoryAndFedoraBaseURLAndFedoraUsernameAndFedoraPassword(
						httpHandlerFactory, converterFactory, fedoraURL, fedoraUsername,
						fedoraPassword);
	}

	private AlvinDbToCoraRecordStorage createDbStorage() {
		SqlConnectionProvider sqlConnectionProvider = tryToCreateConnectionProvider();
		DataReaderImp dataReader = DataReaderImp.usingSqlConnectionProvider(sqlConnectionProvider);
		AlvinDbToCoraConverterFactoryImp converterFactoryImp = AlvinDbToCoraConverterFactoryImp
				.usingDataReader(dataReader);
		return AlvinDbToCoraRecordStorage.usingDataReaderAndConverterFactory(dataReader,
				converterFactoryImp);
	}

	private SqlConnectionProvider tryToCreateConnectionProvider() {
		try {
			InitialContext context = new InitialContext();
			String databaseLookupName = tryToGetInitParameterLogIfFound("databaseLookupName");
			return ContextConnectionProviderImp.usingInitialContextAndName(context,
					databaseLookupName);
		} catch (Exception e) {
			throw DataStorageException.withMessage(e.getMessage());
		}
	}

	private void useExistingRecordStorage() {
		log.logInfoUsingMessage("Using previously started RecordStorage as RecordStorage");
	}

	static void setStaticInstance(RecordStorage recordStorage) {
		RecordStorageInstance.instance = recordStorage;
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		return initInfo.get(parameterName);
	}

	private void throwErrorIfKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			String errorMessage = "InitInfo must contain " + key;
			log.logFatalUsingMessage(errorMessage);
			throw DataStorageException.withMessage(errorMessage);
		}
	}

	@Override
	public RecordStorage getRecordStorage() {
		return RecordStorageInstance.instance;
	}

	@Override
	public MetadataStorage getMetadataStorage() {
		AlvinMixedRecordStorage mixedStorage = (AlvinMixedRecordStorage) RecordStorageInstance.instance;
		return (MetadataStorage) mixedStorage.getBasicStorage();
	}

}
