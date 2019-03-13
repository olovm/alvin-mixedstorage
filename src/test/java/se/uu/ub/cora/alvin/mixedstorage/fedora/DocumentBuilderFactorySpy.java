package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentBuilderFactorySpy extends DocumentBuilderFactory {

	public boolean newDocumentBuilderWasCalled = false;
	public List<DocumentBuilderSpy> factoredDocumentBuilders = new ArrayList<>();

	public static DocumentBuilderFactorySpy newInstance() {
		return new DocumentBuilderFactorySpy();
	}

	@Override
	public Object getAttribute(String arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getFeature(String arg0) throws ParserConfigurationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		newDocumentBuilderWasCalled = true;
		DocumentBuilderSpy documentBuilderSpy = new DocumentBuilderSpy();
		factoredDocumentBuilders.add(documentBuilderSpy);
		return documentBuilderSpy;
	}

	@Override
	public void setAttribute(String arg0, Object arg1) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFeature(String arg0, boolean arg1) throws ParserConfigurationException {
		// TODO Auto-generated method stub

	}

}
