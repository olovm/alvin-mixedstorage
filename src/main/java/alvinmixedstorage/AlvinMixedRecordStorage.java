/*
 * Copyright 2018 Uppsala University Library
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
package alvinmixedstorage;

import java.util.Collection;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public final class AlvinMixedRecordStorage implements RecordStorage {

	private static final String PLACE = "place";
	private RecordStorage basicStorage;
	private RecordStorage alvinToCoraStorage;

	public static RecordStorage usingBasicAndAlvinToCoraStorage(RecordStorage basicStorage,
			RecordStorage alvinToCoraStorage) {
		return new AlvinMixedRecordStorage(basicStorage, alvinToCoraStorage);
	}

	private AlvinMixedRecordStorage(RecordStorage basicStorage, RecordStorage alvinToCoraStorage) {
		this.basicStorage = basicStorage;
		this.alvinToCoraStorage = alvinToCoraStorage;
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PLACE.equals(type)) {
			return alvinToCoraStorage.read(type, id);
		}
		return basicStorage.read(type, id);
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		basicStorage.create(type, id, record, collectedTerms, linkList, dataDivider);
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
		basicStorage.update(type, id, record, collectedTerms, linkList, dataDivider);
	}

	@Override
	public Collection<DataGroup> readList(String type, DataGroup filter) {
		if (PLACE.equals(type)) {
			return alvinToCoraStorage.readList(type, filter);
		}
		return basicStorage.readList(type, filter);
	}

	@Override
	public Collection<DataGroup> readAbstractList(String type, DataGroup filter) {
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
		return basicStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
	}

}
