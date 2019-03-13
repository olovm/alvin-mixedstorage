package se.uu.ub.cora.alvin.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;

public class HttpHandlerFactorySpy implements HttpHandlerFactory {
	public List<String> urls = new ArrayList<>();
	public List<HttpHandler> factoredHttpHandlers = new ArrayList<>();
	public String responseText = "";

	@Override
	public HttpHandler factor(String url) {
		urls.add(url);
		HttpHandlerSpy httpHandlerSpy = new HttpHandlerSpy();
		factoredHttpHandlers.add(httpHandlerSpy);
		httpHandlerSpy.responseText = responseText;
		return httpHandlerSpy;
	}

	@Override
	public HttpMultiPartUploader factorHttpMultiPartUploader(String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
