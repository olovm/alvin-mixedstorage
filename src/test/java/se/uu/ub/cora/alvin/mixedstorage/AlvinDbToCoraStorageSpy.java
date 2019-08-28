package se.uu.ub.cora.alvin.mixedstorage;

import java.util.Collection;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.SpiderReadResult;

public class AlvinDbToCoraStorageSpy implements RecordStorage {
	public RecordStorageSpyData data = new RecordStorageSpyData();

	@Override
	public void create(String arg0, String arg1, DataGroup arg2, DataGroup arg3, DataGroup arg4,
			String arg5) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByTypeAndId(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean linksExistForRecord(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataGroup read(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderReadResult readAbstractList(String arg0, DataGroup arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataGroup readLinkList(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpiderReadResult readList(String arg0, DataGroup arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		data.answer = true;
		return true;
	}

	@Override
	public boolean recordsExistForRecordType(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(String arg0, String arg1, DataGroup arg2, DataGroup arg3, DataGroup arg4,
			String arg5) {
		// TODO Auto-generated method stub

	}

}
