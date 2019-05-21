package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.alvin.mixedstorage.ConversionException;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class AlvinDbToCoraCountryConverter implements AlvinDbToCoraConverter {

	private static final String COUNTRY_STRING = "country";
	private static final String ALPHA2CODE = "alpha2code";
	private Map<String, Object> map;

	@Override
	public DataGroup fromMap(Map<String, Object> map) {
		this.map = map;
		checkMapContainsRequiredValue(ALPHA2CODE);

		DataGroup country = createCountryDataGroupWithRecordInfo();

		addChildrenFromMapToDataGroup(map, country);
		return country;
	}

	private void checkMapContainsRequiredValue(String valueToGet) {
		if (map.isEmpty() || !map.containsKey(valueToGet) || "".equals(map.get(valueToGet))) {
			throw ConversionException.withMessageAndException(
					"Error converting country to Cora country: Map does not contain value for "
							+ valueToGet,
					null);
		}
	}

	private DataGroup createCountryDataGroupWithRecordInfo() {
		DataGroup country = DataGroup.withNameInData(COUNTRY_STRING);
		// DataGroup recordInfo = createAndAddRecordInfo(map);
		// country.addChild(recordInfo);
		return country;
	}

	private DataGroup createAndAddRecordInfo(Map<String, String> map) {
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", map.get(ALPHA2CODE)));
		addType(recordInfo);
		addDataDivider(recordInfo);

		addTimestampInfo(recordInfo);
		addUserInfo(recordInfo);

		return recordInfo;
	}

	private void addType(DataGroup recordInfo) {
		DataGroup type = createLinkWithNameInDataTypeAndId("type", "recordType", COUNTRY_STRING);
		recordInfo.addChild(type);
	}

	private void addDataDivider(DataGroup recordInfo) {
		DataGroup dataDivider = createLinkWithNameInDataTypeAndId("dataDivider", "system", "alvin");
		recordInfo.addChild(dataDivider);
	}

	private void addTimestampInfo(DataGroup recordInfo) {
		possiblyAddAtomicChildWithNameInDataAndKeyToDataGroup("tsCreated", "lastupdated",
				recordInfo);
		possiblyAddAtomicChildWithNameInDataAndKeyToDataGroup("tsUpdated", "lastupdated",
				recordInfo);
	}

	private void addUserInfo(DataGroup recordInfo) {
		DataGroup createdBy = createLinkWithNameInDataTypeAndId("createdBy", "systemOneUser",
				"12345");
		recordInfo.addChild(createdBy);
		DataGroup updatedBy = createLinkWithNameInDataTypeAndId("updatedBy", "systemOneUser",
				"12345");
		recordInfo.addChild(updatedBy);
	}

	private DataGroup createLinkWithNameInDataTypeAndId(String nameInData, String linkedRecordType,
			String linkedRecordId) {
		DataGroup type = DataGroup.withNameInData(nameInData);
		type.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", linkedRecordType));
		type.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", linkedRecordId));
		return type;
	}

	private void addChildrenFromMapToDataGroup(Map<String, Object> map, DataGroup country) {
		String alpha2 = (String) map.get(ALPHA2CODE);
		possiblyAddTextLink(country, alpha2);
		addAtomicChildrenToDataGroup(country);
	}

	private void possiblyAddTextLink(DataGroup country, String alpha2) {
		if (mapContainsValueForKey("defaultname")) {
			DataGroup text = createText(alpha2);
			country.addChild(text);
		}
	}

	private boolean mapContainsValueForKey(String key) {
		return !"".equals(map.get(key)) && null != map.get(key);
	}

	private DataGroup createText(String alpha2) {
		DataGroup text = DataGroup.withNameInData("textId");
		text.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", "coraText"));
		String linkedRecordId = COUNTRY_STRING + alpha2 + "Text";
		text.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", linkedRecordId));
		return text;
	}

	private void addAtomicChildrenToDataGroup(DataGroup country) {
		DataAtomic isoAlpha2 = createDataAtomicWithNameInDataUsingKey("iso31661Alpha2", ALPHA2CODE);
		country.addChild(isoAlpha2);

		possiblyAddAtomicChildWithNameInDataAndKeyToDataGroup("iso31661Alpha3", "alpha3code",
				country);
		possiblyAddAtomicChildWithNameInDataAndKeyToDataGroup("iso31661Numeric", "numericalcode",
				country);
		possiblyAddAtomicChildWithNameInDataAndKeyToDataGroup("marcCountryCode", "marccode",
				country);
	}

	private DataAtomic createDataAtomicWithNameInDataUsingKey(String nameInData, String key) {
		String value = (String) map.get(key);
		return DataAtomic.withNameInDataAndValue(nameInData, value);
	}

	void possiblyAddAtomicChildWithNameInDataAndKeyToDataGroup(String nameInData, String key,
			DataGroup country) {
		if (mapContainsValueForKey(key)) {
			DataAtomic dataAtomic = createDataAtomicWithNameInDataUsingKey(nameInData, key);
			country.addChild(dataAtomic);
		}
	}

}
