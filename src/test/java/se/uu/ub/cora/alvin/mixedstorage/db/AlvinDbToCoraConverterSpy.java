package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.data.DataGroup;

public class AlvinDbToCoraConverterSpy implements AlvinDbToCoraConverter {

	public String xml;
	public DataGroup convertedDataGroup;
	public Map<String, Object> mapToConvert;
	public DataGroup convertedDbDataGroup;

	@Override
	public DataGroup fromMap(Map<String, Object> map) {
		mapToConvert = map;
		convertedDbDataGroup = DataGroup.withNameInData("from Db converter");
		return convertedDbDataGroup;
	}

}
