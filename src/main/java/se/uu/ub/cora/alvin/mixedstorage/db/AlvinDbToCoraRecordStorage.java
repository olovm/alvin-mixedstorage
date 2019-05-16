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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.data.SpiderReadResult;
import se.uu.ub.cora.spider.record.storage.RecordStorage;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public final class AlvinDbToCoraRecordStorage implements RecordStorage {

	private static final String COUNTRY = "country";
	private AlvinDbToCoraConverterFactory converterFactory;
	private RecordReaderFactory recordReaderFactory;

	private AlvinDbToCoraRecordStorage(RecordReaderFactory recordReaderFactory,
			AlvinDbToCoraConverterFactory converterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.converterFactory = converterFactory;
	}

	public static AlvinDbToCoraRecordStorage usingRecordReaderFactoryAndConverterFactory(
			RecordReaderFactory recordReaderFactory,
			AlvinDbToCoraConverterFactory converterFactory) {
		return new AlvinDbToCoraRecordStorage(recordReaderFactory, converterFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		if (COUNTRY.equals(type)) {
			return readAndConvertCountryFromDb(type, id);
		}
		throw NotImplementedException.withMessage("read is not implemented for type: " + type);
	}

	private DataGroup readAndConvertCountryFromDb(String type, String id) {
		Map<String, String> readRow = readOneRowFromDbUsingTypeAndId(type, id);
		return convertOneMapFromDbToDataGroup(type, readRow);
	}

	private Map<String, String> readOneRowFromDbUsingTypeAndId(String type, String id) {
		RecordReader recordReader = recordReaderFactory.factor();
		Map<String, String> conditions = new HashMap<>();
		conditions.put("alpha2code", id);
		return recordReader.readOneRowFromDbUsingTableAndConditions(type, conditions);
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
		if (COUNTRY.equals(type)) {
			return readAllFromDbAndConvertToDataGroup(type);
		}
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	private SpiderReadResult readAllFromDbAndConvertToDataGroup(String type) {
		List<Map<String, String>> readAllFromTable = readAllFromDb(type);
		return createSpiderReadResultFromDbData(type, readAllFromTable);
	}

	private List<Map<String, String>> readAllFromDb(String type) {
		RecordReader recordReader = recordReaderFactory.factor();
		return recordReader.readAllFromTable(type);
	}

	private SpiderReadResult createSpiderReadResultFromDbData(String type,
			List<Map<String, String>> readAllFromTable) {
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = convertListOfMapsFromDbToDataGroups(type,
				readAllFromTable);
		return spiderReadResult;
	}

	private List<DataGroup> convertListOfMapsFromDbToDataGroups(String type,
			List<Map<String, String>> readAllFromTable) {
		List<DataGroup> convertedList = new ArrayList<>();
		for (Map<String, String> map : readAllFromTable) {
			DataGroup convertedGroup = convertOneMapFromDbToDataGroup(type, map);
			convertedList.add(convertedGroup);
		}
		return convertedList;
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, String> map) {
		AlvinDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromMap(map);
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
		List<Map<String, String>> readAllFromTable = readAllUsersFromDb();
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = convertDataAndAddToList(type, readAllFromTable);
		return spiderReadResult;
	}

	private List<DataGroup> convertDataAndAddToList(String type,
			List<Map<String, String>> readAllFromTable) {
		List<DataGroup> convertedList = new ArrayList<>();
		for (Map<String, String> map : readAllFromTable) {
			DataGroup convertedUser = convertOneMapFromDbToDataGroup(type, map);
			convertedList.add(convertedUser);
		}
		return convertedList;
	}

	private List<Map<String, String>> readAllUsersFromDb() {
		RecordReader recordReader = recordReaderFactory.factor();
		return recordReader.readAllFromTable("alvin_seam_user");
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
		throw NotImplementedException.withMessage(
				"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented");
	}

}
