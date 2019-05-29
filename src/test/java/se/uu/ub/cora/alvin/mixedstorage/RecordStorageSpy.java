package se.uu.ub.cora.alvin.mixedstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.data.SpiderReadResult;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public class RecordStorageSpy implements RecordStorage {
	public RecordStorageSpyData data = new RecordStorageSpyData();

	@Override
	public DataGroup read(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "read";

		DataGroup dummyDataGroup = DataGroup.withNameInData("DummyGroupFromRecordStorageSpy");
		data.answer = dummyDataGroup;
		return dummyDataGroup;
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		data.type = type;
		data.id = id;
		data.record = record;
		data.collectedTerms = collectedTerms;
		data.linkList = linkList;
		data.dataDivider = dataDivider;
		data.calledMethod = "create";

	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "deleteByTypeAndId";
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "linksExistForRecord";
		data.answer = false;
		return false;
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		data.type = type;
		data.id = id;
		data.record = record;
		data.collectedTerms = collectedTerms;
		data.linkList = linkList;
		data.dataDivider = dataDivider;
		data.calledMethod = "update";
	}

	@Override
	public SpiderReadResult readList(String type, DataGroup filter) {
		data.type = type;
		data.filter = filter;
		data.calledMethod = "readList";
		Collection<DataGroup> readList = new ArrayList<>();
		DataGroup dummyDataGroup = DataGroup.withNameInData("DummyGroupFromRecordStorageSpy");
		readList.add(dummyDataGroup);
		data.answer = readList;
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = (List<DataGroup>) readList;
		return spiderReadResult;
	}

	@Override
	public SpiderReadResult readAbstractList(String type, DataGroup filter) {
		data.type = type;
		data.filter = filter;
		data.calledMethod = "readAbstractList";
		Collection<DataGroup> readList = new ArrayList<>();
		DataGroup dummyDataGroup = DataGroup.withNameInData("DummyGroupFromRecordStorageSpy");
		readList.add(dummyDataGroup);
		data.answer = readList;
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = (List<DataGroup>) readList;
		return spiderReadResult;
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "readLinkList";

		DataGroup dummyDataGroup = DataGroup.withNameInData("DummyGroupFromRecordStorageSpy");
		data.answer = dummyDataGroup;
		return dummyDataGroup;
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "generateLinkCollectionPointingToRecord";
		Collection<DataGroup> generatedList = new ArrayList<>();
		DataGroup dummyDataGroup = DataGroup.withNameInData("DummyGroupFromRecordStorageSpy");
		generatedList.add(dummyDataGroup);
		data.answer = generatedList;
		return generatedList;
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		data.type = type;
		data.calledMethod = "recordsExistForRecordType";
		data.answer = false;
		return false;
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		data.answer = false;
		if ("coraUser:5368244264733286".equals(id)) {
			data.answer = true;
		}
		return (boolean) data.answer;
	}

}
