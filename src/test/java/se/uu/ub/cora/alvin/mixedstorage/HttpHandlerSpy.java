/*
 * Copyright 2018, 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.alvin.mixedstorage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.httphandler.HttpHandler;

public class HttpHandlerSpy implements HttpHandler {

	public String requestMethod;
	public String responseText;
	public int responseCode;
	public Map<String, String> requestProperties = new HashMap<String, String>();
	public List<String> outputStrings = new ArrayList<String>();
	public boolean responseCodeWasRequested = false;
	public String username;
	public String password;

	@Override
	public void setRequestMethod(String requestMetod) {
		this.requestMethod = requestMetod;
	}

	@Override
	public String getResponseText() {

		return responseText;
	}

	@Override
	public int getResponseCode() {
		responseCodeWasRequested = true;
		return responseCode;
	}

	@Override
	public void setOutput(String outputString) {
		outputStrings.add(outputString);
	}

	@Override
	public void setRequestProperty(String key, String value) {
		requestProperties.put(key, value);

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
	public void setBasicAuthorization(String username, String password) {
		this.username = username;
		this.password = password;
		// TODO Auto-generated method stub

	}

}
