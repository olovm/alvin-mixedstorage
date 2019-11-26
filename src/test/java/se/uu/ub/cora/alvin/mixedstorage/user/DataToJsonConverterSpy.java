package se.uu.ub.cora.alvin.mixedstorage.user;

import se.uu.ub.cora.data.converter.DataToJsonConverter;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;
import se.uu.ub.cora.json.builder.org.OrgJsonObjectBuilderAdapter;

public class DataToJsonConverterSpy implements DataToJsonConverter {

	@Override
	public JsonObjectBuilder toJsonObjectBuilder() {
		// TODO Auto-generated method stub
		return new OrgJsonObjectBuilderAdapter();
	}

	@Override
	public String toJsonCompactFormat() {
		// TODO Auto-generated method stub
		return "{\"name\":\"someNameInData\",\"value\":\"someValue\"}";
	}

	@Override
	public String toJson() {
		// TODO Auto-generated method stub
		return "{\"name\":\"someNameInData\",\"value\":\"someValue\"}";
	}

}
