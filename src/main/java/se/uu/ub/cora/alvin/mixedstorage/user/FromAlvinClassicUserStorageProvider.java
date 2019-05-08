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

import java.util.Map;

import javax.naming.InitialContext;

import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.gatekeeper.user.UserStorageProvider;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReaderImp;
import se.uu.ub.cora.storage.UserStorageImp;

public class FromAlvinClassicUserStorageProvider implements UserStorageProvider {

	private AlvinMixedUserStorage userStorage;
	private Map<String, String> initInfo;
	private Logger log = LoggerProvider
			.getLoggerForClass(FromAlvinClassicUserStorageProvider.class);

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 10;
	}

	@Override
	public UserStorage getUserStorage() {
		return userStorage;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		this.initInfo = initInfo;
		UserStorage userStorageForGuest = new UserStorageImp(initInfo);
		ContextConnectionProviderImp sqlConnectionProvider = createConnectionProvider();

		DataReaderImp dataReader = DataReaderImp.usingSqlConnectionProvider(sqlConnectionProvider);
		userStorage = AlvinMixedUserStorage
				.usingUserStorageForGuestAndDataReaderForUsers(userStorageForGuest, dataReader);
	}

	private ContextConnectionProviderImp createConnectionProvider() {
		try {
			InitialContext context = new InitialContext();
			String name = tryToGetInitParameter("databaseLookupName");
			return ContextConnectionProviderImp.usingInitialContextAndName(context, name);
		} catch (Exception e) {
			throw new RuntimeException("Error starting ContextConnectionProviderImp", e);
		}
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		String parameter = initInfo.get(parameterName);
		log.logInfoUsingMessage("Found " + parameter + " as " + parameterName);
		return parameter;
	}

	private void throwErrorIfKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			String errorMessage = "InitInfo must contain " + key;
			log.logFatalUsingMessage(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}

}
