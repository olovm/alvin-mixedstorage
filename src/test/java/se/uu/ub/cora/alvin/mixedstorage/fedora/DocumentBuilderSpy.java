package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentBuilderSpy extends DocumentBuilder {

	public boolean newDocumentWasCreated;
	public List<DocumentSpy> documents = new ArrayList<>();

	@Override
	public DOMImplementation getDOMImplementation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNamespaceAware() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Document newDocument() {
		newDocumentWasCreated = true;
		DocumentSpy documentSpy = new DocumentSpy();
		documents.add(documentSpy);
		return documentSpy;
	}

	@Override
	public Document parse(InputSource arg0) throws SAXException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEntityResolver(EntityResolver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setErrorHandler(ErrorHandler arg0) {
		// TODO Auto-generated method stub

	}

}
