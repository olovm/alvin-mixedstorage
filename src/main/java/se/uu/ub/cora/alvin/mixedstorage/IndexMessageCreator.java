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
