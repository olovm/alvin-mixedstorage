package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.sqldatabase.DataReader;

public class DataReaderRolesSpy implements DataReader {

	public List<Map<String, Object>> listOfRows;
	public String sqlSentToReader;
	public List<Object> valuesSentToReader = new ArrayList<>();

	@Override
	public List<Map<String, Object>> executePreparedStatementQueryUsingSqlAndValues(String sql,
			List<Object> values) {
		// TODO Auto-generated method stub
		sqlSentToReader = sql;
		listOfRows = new ArrayList<>();
		valuesSentToReader = values;
		int id = (int) values.get(0);
		if (id == 52) {
			Map<String, Object> role = new HashMap<>();
			role.put("group_id", 54);
			listOfRows.add(role);
		}

		return listOfRows;
	}

	@Override
	public Map<String, Object> readOneRowOrFailUsingSqlAndValues(String sql, List<Object> values) {
		// TODO Auto-generated method stub
		return null;
	}

}
