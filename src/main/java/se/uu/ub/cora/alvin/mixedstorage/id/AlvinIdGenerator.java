/*
 * Copyright 2019 Uppsala University Library
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

package se.uu.ub.cora.alvin.mixedstorage.id;

import se.uu.ub.cora.alvin.mixedstorage.fedora.FedoraException;
import se.uu.ub.cora.alvin.mixedstorage.parse.XMLXPathParser;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.storage.RecordIdGenerator;

public class AlvinIdGenerator implements RecordIdGenerator {

	private static final int OK = 200;
	private static final String WITH_RESPONSE_CODE_MESSAGE_PART = ", with response code: ";
	private HttpHandlerFactory httpHandlerFactory;
	private IdGeneratorConnectionInfo connectionInfo;

	static AlvinIdGenerator usingHttpHandlerFactoryAndConnectionInfo(
			HttpHandlerFactory httpHandlerFactory,
			IdGeneratorConnectionInfo idGeneratorConnectionInfo) {
		return new AlvinIdGenerator(httpHandlerFactory, idGeneratorConnectionInfo);
	}

	private AlvinIdGenerator(HttpHandlerFactory httpHandlerFactory,
			IdGeneratorConnectionInfo idGeneratorConnectionInfo) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.connectionInfo = idGeneratorConnectionInfo;
	}

	@Override
	public String getIdForType(String type) {
		if ("place".equals(type)) {
			return getPidForPlaceFromFedora();
		}
		return type + ":" + System.nanoTime();
	}

	private String getPidForPlaceFromFedora() {
		String nextPidXML = getNextPidFromFedora();
		return parseXMLAndExtractPid(nextPidXML);
	}

	private String getNextPidFromFedora() {
		String urlForNextPid = connectionInfo.fedoraURL
				+ "objects/nextPID?namespace=alvin-place&format=xml";
		HttpHandler httpHandlerForPid = httpHandlerFactory.factor(urlForNextPid);
		httpHandlerForPid.setRequestMethod("POST");
		httpHandlerForPid.setBasicAuthorization(connectionInfo.fedoraUsername,
				connectionInfo.fedoraPassword);
		throwErrorIfPidCouldNotBeFetched(httpHandlerForPid);
		return httpHandlerForPid.getResponseText();
	}

	private void throwErrorIfPidCouldNotBeFetched(HttpHandler httpHandlerForPid) {
		if (httpHandlerForPid.getResponseCode() != OK) {
			throw FedoraException.withMessage("getting next pid from fedora failed"
					+ WITH_RESPONSE_CODE_MESSAGE_PART + httpHandlerForPid.getResponseCode());
		}
	}

	private String parseXMLAndExtractPid(String nextPidXML) {
		XMLXPathParser parser = XMLXPathParser.forXML(nextPidXML);
		return parser.getStringFromDocumentUsingXPath("/pidList/pid/text()");
	}

	HttpHandlerFactory getHttpHandlerFactory() {
		// needed for test
		return httpHandlerFactory;
	}

	IdGeneratorConnectionInfo getConnectInfo() {
		// needed for test
		return connectionInfo;
	}

	public String getFedoraURL() {
		// needed for test
		return connectionInfo.fedoraURL;
	}

	public String getFedoraUsername() {
		// needed for test
		return connectionInfo.fedoraUsername;
	}

	public String getFedoraPassword() {
		// needed for test
		return connectionInfo.fedoraPassword;
	}
}
