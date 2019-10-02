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
package se.uu.ub.cora.alvin.mixedstorage;

import se.uu.ub.cora.json.builder.org.OrgJsonObjectBuilderAdapter;

public class IndexMessageCreator {

	private String id;

	private IndexMessageCreator(String id) {
		this.id = id;
	}

	public static IndexMessageCreator usingId(String id) {
		return new IndexMessageCreator(id);
	}

	public String createMessage(String routingKey, String action) {
		OrgJsonObjectBuilderAdapter builderAdapter = createJsonObjectBuilderWithValues(routingKey,
				action);
		return builderAdapter.toJsonFormattedString();
	}

	private OrgJsonObjectBuilderAdapter createJsonObjectBuilderWithValues(String routingKey,
			String action) {
		OrgJsonObjectBuilderAdapter builderAdapter = new OrgJsonObjectBuilderAdapter();
		builderAdapter.addKeyString("pid", id);
		builderAdapter.addKeyString("routingKey", routingKey);
		builderAdapter.addKeyString("action", action);

		addHeaders(action, builderAdapter);
		return builderAdapter;
	}

	private void addHeaders(String action, OrgJsonObjectBuilderAdapter builderAdapter) {
		OrgJsonObjectBuilderAdapter builderAdapterHeaders = createJsonBuilderAdapterForHeaders(
				action);
		builderAdapter.addKeyJsonObjectBuilder("headers", builderAdapterHeaders);
	}

	private OrgJsonObjectBuilderAdapter createJsonBuilderAdapterForHeaders(String action) {
		OrgJsonObjectBuilderAdapter builderAdapterHeaders = new OrgJsonObjectBuilderAdapter();
		builderAdapterHeaders.addKeyString("ACTION", action);
		builderAdapterHeaders.addKeyString("PID", id);
		return builderAdapterHeaders;
	}

}
