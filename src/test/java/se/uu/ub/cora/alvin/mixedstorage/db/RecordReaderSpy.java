package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.sqldatabase.RecordReader;

public class RecordReaderSpy implements RecordReader {

	public String usedTableName = "";
	public List<Map<String, Object>> returnedList;
	public int noOfRecordsToReturn = 1;
	public Map<String, Object> usedConditions = new HashMap<>();
	public Map<String, Object> returnedOneRow = new HashMap<>();
	public boolean readAllWasCalled = false;
	public boolean readOneRowWasCalled = false;

	@Override
	public List<Map<String, Object>> readAllFromTable(String tableName) {
		readAllWasCalled = true;
		usedTableName = tableName;
		returnedList = new ArrayList<>();
		for (int i = 0; i < noOfRecordsToReturn; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("someKey" + i, "someValue" + i);
			returnedList.add(map);
		}
		return returnedList;
	}

	@Override
	public Map<String, Object> readOneRowFromDbUsingTableAndConditions(String tableName,
			Map<String, Object> conditions) {
		readOneRowWasCalled = true;
		usedTableName = tableName;
		usedConditions = conditions;
		returnedOneRow = new HashMap<>();
		returnedOneRow.put("someKey", "someValue");
		returnedList = new ArrayList<>();
		returnedList.add(returnedOneRow);
		return returnedOneRow;

	}

	@Override
	public List<Map<String, Object>> readFromTableUsingConditions(String arg0,
			Map<String, Object> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> readNextValueFromSequence(String sequenceName) {
		// TODO Auto-generated method stub
		return null;
	}

}
