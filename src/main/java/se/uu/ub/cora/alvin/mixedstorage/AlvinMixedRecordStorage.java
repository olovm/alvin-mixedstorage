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
package se.uu.ub.cora.alvin.mixedstorage;

import java.util.Collection;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.searchstorage.SearchStorage;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public final class AlvinMixedRecordStorage implements RecordStorage, SearchStorage {

	private static final String PLACE = "place";
	private RecordStorage basicStorage;
	private RecordStorage alvinFedoraToCoraStorage;

	private RecordStorage alvinDbToCoraStorage;

	public static RecordStorage usingBasicAndFedoraAndDbStorage(RecordStorage basicStorage,
			RecordStorage alvinFedoraToCoraStorage, RecordStorage alvinDbToCoraStorage) {
		return new AlvinMixedRecordStorage(basicStorage, alvinFedoraToCoraStorage,
				alvinDbToCoraStorage);
	}

	private AlvinMixedRecordStorage(RecordStorage basicStorage,
			RecordStorage alvinFedoraToCoraStorage, RecordStorage alvinDbToCoraStorage) {
		this.basicStorage = basicStorage;
		this.alvinFedoraToCoraStorage = alvinFedoraToCoraStorage;
		this.alvinDbToCoraStorage = alvinDbToCoraStorage;
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PLACE.equals(type)) {
			return alvinFedoraToCoraStorage.read(type, id);
		}
		if ("user".equals(type)) {
			return handleUser(type, id);
		}
		return basicStorage.read(type, id);
	}

	private DataGroup handleUser(String type, String id) {
		try {
			return alvinDbToCoraStorage.read(type, id);
		} catch (RecordNotFoundException e) {
			// do nothing, we keep looking in basicstorage
		}
		return basicStorage.read(type, id);
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (PLACE.equals(type)) {
			alvinFedoraToCoraStorage.create(type, id, record, collectedTerms, linkList,
					dataDivider);
		} else {
			basicStorage.create(type, id, record, collectedTerms, linkList, dataDivider);
		}
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		basicStorage.deleteByTypeAndId(type, id);
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		return basicStorage.linksExistForRecord(type, id);
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (PLACE.equals(type)) {
			alvinFedoraToCoraStorage.update(type, id, record, collectedTerms, linkList,
					dataDivider);
		} else {
			basicStorage.update(type, id, record, collectedTerms, linkList, dataDivider);
		}
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		if (PLACE.equals(type)) {
			return alvinFedoraToCoraStorage.readList(type, filter);
		}
		return basicStorage.readList(type, filter);
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		if ("user".equals(type)) {
			return alvinDbToCoraStorage.readAbstractList(type, filter);
		}
		return basicStorage.readAbstractList(type, filter);
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		return basicStorage.readLinkList(type, id);
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		return basicStorage.generateLinkCollectionPointingToRecord(type, id);
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		return basicStorage.recordsExistForRecordType(type);
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		if ("user".equals(type)) {
			return handleRecordExistsForAbstractOrImplementingForUser(type, id);
		}
		return recordExistsInBasicStorage(type, id);
	}

	private boolean handleRecordExistsForAbstractOrImplementingForUser(String type, String id) {
		boolean recordExistsInDb = recordExistsInDb(type, id);
		return recordExistsInDb ? recordExistsInDb : recordExistsInBasicStorage(type, id);
	}

	private boolean recordExistsInDb(String type, String id) {
		return alvinDbToCoraStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type,
				id);
	}

	private boolean recordExistsInBasicStorage(String type, String id) {
		return basicStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
	}

	RecordStorage getBasicStorage() {
		// needed for test
		return basicStorage;
	}

	public RecordStorage getFedoraStorage() {
		// needed for test
		return alvinFedoraToCoraStorage;
	}

	public RecordStorage getDbStorage() {
		// needed for test
		return alvinDbToCoraStorage;
	}

	@Override
	public DataGroup getSearchTerm(String searchTermId) {
		return ((SearchStorage) basicStorage).getSearchTerm(searchTermId);
	}

	@Override
	public DataGroup getCollectIndexTerm(String collectIndexTermId) {
		return ((SearchStorage) basicStorage).getCollectIndexTerm(collectIndexTermId);
	}

}
