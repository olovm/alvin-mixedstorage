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

import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.fedora.FedoraException;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordIdGenerator;
import se.uu.ub.cora.storage.RecordIdGeneratorProvider;

public class AlvinIdGeneratorProvider implements RecordIdGeneratorProvider {
	private Logger log = LoggerProvider.getLoggerForClass(AlvinIdGeneratorProvider.class);
	private AlvinIdGenerator idGenerator;
	private Map<String, String> initInfo;

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 1;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		log.logInfoUsingMessage("AlvinIdGeneratorProvider starting AlvinIdGenerator...");
		createIdGeneratorUsingInitInfo(initInfo);
		log.logInfoUsingMessage("AlvinIdGeneratorProvider started AlvinIdGenerator");
	}

	private void createIdGeneratorUsingInitInfo(Map<String, String> initInfo) {
		this.initInfo = initInfo;
		IdGeneratorConnectionInfo connectionInfo = createConnectionInfoUsingInitInfo();
		idGenerator = AlvinIdGeneratorFactory.factorUsingConnectionInfo(connectionInfo);
	}

	private IdGeneratorConnectionInfo createConnectionInfoUsingInitInfo() {
		String fedoraURL = tryToGetInitParameter("fedoraURL");
		log.logInfoUsingMessage("Found " + fedoraURL + " as fedoraURL");
		String fedoraUsername = tryToGetInitParameter("fedoraUsername");
		String fedoraPassword = tryToGetInitParameter("fedoraPassword");
		return new IdGeneratorConnectionInfo(fedoraURL, fedoraUsername, fedoraPassword);
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		return initInfo.get(parameterName);
	}

	private void throwErrorIfKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			String errorMessage = "InitInfo must contain " + key;
			log.logFatalUsingMessage(errorMessage);
			throw FedoraException.withMessage(errorMessage);
		}
	}

	@Override
	public RecordIdGenerator getRecordIdGenerator() {
		return idGenerator;
	}

}
