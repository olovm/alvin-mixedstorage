package se.uu.ub.cora.alvin.mixedstorage.db;

import java.io.InputStream;

import se.uu.ub.cora.httphandler.HttpHandler;

public class HttpHandlerSpy implements HttpHandler {

	public String requestMetod;
	public String responseText;

	@Override
	public void setRequestMethod(String requestMetod) {
		this.requestMetod = requestMetod;
	}

	@Override
	public String getResponseText() {
		return responseText;
	}

	@Override
	public int getResponseCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOutput(String outputString) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRequestProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getErrorText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStreamOutput(InputStream stream) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHeaderField(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBasicAuthorization(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
