package se.uu.ub.cora.alvin.mixedstorage;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class RecordStorageSpyData {
	public String type;
	public String id;
	public String calledMethod;
	public DataGroup filter;
	public Object answer;
	public DataGroup record;
	public DataGroup collectedTerms;
	public DataGroup linkList;
	public String dataDivider;

	public RecordStorageSpyData() {
	}
}