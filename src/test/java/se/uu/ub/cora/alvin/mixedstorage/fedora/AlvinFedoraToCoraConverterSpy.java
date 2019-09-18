package se.uu.ub.cora.alvin.mixedstorage.fedora;

import se.uu.ub.cora.data.DataGroup;

public class AlvinFedoraToCoraConverterSpy implements AlvinFedoraToCoraConverter {

	public String xml;
	public DataGroup convertedDataGroup;

	@Override
	public DataGroup fromXML(String xml) {
		this.xml = xml;
		convertedDataGroup = DataGroup.withNameInData("Converted xml");
		return convertedDataGroup;
	}

}
