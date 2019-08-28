/*
 * Copyright 2018, 2019 Uppsala University Library
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
package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.sqldatabase.DataReader;
import se.uu.ub.cora.sqldatabase.SqlStorageException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.SpiderReadResult;

public final class AlvinDbToCoraRecordStorage implements RecordStorage {

	private AlvinDbToCoraConverterFactory converterFactory;
	private DataReader dataReader;

	private AlvinDbToCoraRecordStorage(DataReader dataReader,
			AlvinDbToCoraConverterFactory converterFactory) {
		this.converterFactory = converterFactory;
		this.dataReader = dataReader;
	}

	public static AlvinDbToCoraRecordStorage usingDataReaderAndConverterFactory(
			DataReader dataReader, AlvinDbToCoraConverterFactory converterFactory) {
		return new AlvinDbToCoraRecordStorage(dataReader, converterFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		if ("user".equals(type)) {
			return readAndConvertUser(type, id);
		}
		throw NotImplementedException.withMessage("read is not implemented for type: " + type);
	}

	private DataGroup readAndConvertUser(String type, String id) {
		throwErrorIfIdNotAnIntegerValue(id);
		Map<String, Object> map = tryToReadUserFromDb(id);
		return convertOneMapFromDbToDataGroup(type, map);
	}

	private Map<String, Object> tryToReadUserFromDb(String id) {
		try {
			List<Object> values = createListOfValuesWithId(id);
			return dataReader.readOneRowOrFailUsingSqlAndValues(
					"select * from alvin_seam_user where id = ?", values);
		} catch (SqlStorageException e) {
			throw new RecordNotFoundException("User not found: " + id);
		}
	}

	private List<Object> createListOfValuesWithId(String id) {
		Integer idAsInteger = Integer.valueOf(id);
		List<Object> values = new ArrayList<>();
		values.add(idAsInteger);
		return values;
	}

	private void throwErrorIfIdNotAnIntegerValue(String id) {
		try {
			Integer.valueOf(id);
		} catch (NumberFormatException ne) {
			throw new RecordNotFoundException("User not found: " + id);
		}
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		throw NotImplementedException.withMessage("create is not implemented");
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		throw NotImplementedException.withMessage("deleteByTypeAndId is not implemented");
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		throw NotImplementedException.withMessage("linksExistForRecord is not implemented");
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		throw NotImplementedException.withMessage("update is not implemented");
	}

	@Override
	public SpiderReadResult readList(String type, DataGroup filter) {
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	@Override
	public SpiderReadResult readAbstractList(String type, DataGroup filter) {
		if ("user".contentEquals(type)) {
			return readAllUsersFromDbAndConvertToDataGroup(type);
		}
		throw NotImplementedException
				.withMessage("readAbstractList is not implemented for type " + type);
	}

	private SpiderReadResult readAllUsersFromDbAndConvertToDataGroup(String type) {
		List<Map<String, Object>> readAllFromTable = readAllUsersFromDb();
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = convertDataAndAddToList(type, readAllFromTable);
		return spiderReadResult;
	}

	private List<DataGroup> convertDataAndAddToList(String type,
			List<Map<String, Object>> readAllFromTable) {
		List<DataGroup> convertedList = new ArrayList<>();
		for (Map<String, Object> map : readAllFromTable) {
			DataGroup convertedUser = convertOneMapFromDbToDataGroup(type, map);
			convertedList.add(convertedUser);
		}
		return convertedList;
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, Object> map) {
		AlvinDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromMap(map);
	}

	private List<Map<String, Object>> readAllUsersFromDb() {
		return dataReader.executePreparedStatementQueryUsingSqlAndValues(
				"select * from alvin_seam_user", Collections.emptyList());
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		throw NotImplementedException.withMessage("readLinkList is not implemented");
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		throw NotImplementedException
				.withMessage("generateLinkCollectionPointingToRecord is not implemented");
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		throw NotImplementedException.withMessage("recordsExistForRecordType is not implemented");
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		if ("user".equals(type)) {
			return userExistsInDb(id);
		}
		throw NotImplementedException.withMessage(
				"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented for "
						+ type);
	}

	private boolean userExistsInDb(String id) {
		try {
			throwErrorIfIdNotAnIntegerValue(id);
			tryToReadUserFromDb(id);
			return true;
		} catch (RecordNotFoundException e) {
			return false;
		}
	}

	public DataReader getDataReader() {
		// needed for test
		return dataReader;
	}

	public AlvinDbToCoraConverterFactory getConverterFactory() {
		// needed for test
		return converterFactory;
	}

}
