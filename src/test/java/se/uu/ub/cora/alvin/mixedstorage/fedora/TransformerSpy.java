package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public class TransformerSpy extends Transformer {

	public Map<String, String> outputProperties = new HashMap<>();
	public Source source;
	public Result result;

	@Override
	public void clearParameters() {
		// TODO Auto-generated method stub

	}

	@Override
	public ErrorListener getErrorListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getOutputProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOutputProperty(String arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URIResolver getURIResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setErrorListener(ErrorListener arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutputProperties(Properties arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutputProperty(String arg0, String arg1) throws IllegalArgumentException {
		outputProperties.put(arg0, arg1);
	}

	@Override
	public void setParameter(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setURIResolver(URIResolver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transform(Source source, Result result) throws TransformerException {
		this.source = source;
		this.result = result;

	}

}
