package se.uu.ub.cora.alvin.mixedstorage;

import se.uu.ub.cora.messaging.MessageRoutingInfo;

public class RecordIndexerSpy implements RecordIndexer {

	public String type;
	public String pid;
	public MessageRoutingInfo messageRoutingInfo;

	public RecordIndexerSpy(MessageRoutingInfo messageRoutingInfo) {
		this.messageRoutingInfo = messageRoutingInfo;
	}

	@Override
	public void index(String type, String pid) {
		this.type = type;
		this.pid = pid;
	}

}
