package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

public class TransformerFactorySpy extends TransformerFactory {

	public List<TransformerSpy> transformers = new ArrayList<>();

	public static TransformerFactorySpy newInstance() {
		return new TransformerFactorySpy();
	}

	@Override
	public Source getAssociatedStylesheet(Source arg0, String arg1, String arg2, String arg3)
			throws TransformerConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ErrorListener getErrorListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getFeature(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URIResolver getURIResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Templates newTemplates(Source arg0) throws TransformerConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transformer newTransformer() throws TransformerConfigurationException {
		TransformerSpy transformerSpy = new TransformerSpy();
		transformers.add(transformerSpy);
		return transformerSpy;
	}

	@Override
	public Transformer newTransformer(Source arg0) throws TransformerConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setErrorListener(ErrorListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFeature(String arg0, boolean arg1) throws TransformerConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setURIResolver(URIResolver arg0) {
		// TODO Auto-generated method stub

	}

}
