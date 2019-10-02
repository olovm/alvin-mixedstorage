package se.uu.ub.cora.alvin.mixedstorage;

import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingInitializationException;

public class RecordIndexerSpy implements RecordIndexer {

	public String type;
	public String pid;
	public MessageRoutingInfo messageRoutingInfo;
	public boolean throwMessageInitializationErrorOnIndex = false;
	public boolean throwRuntimeErrorOnIndex = false;

	public RecordIndexerSpy(MessageRoutingInfo messageRoutingInfo) {
		this.messageRoutingInfo = messageRoutingInfo;
	}

	@Override
	public void index(String type, String pid) {
		if (throwMessageInitializationErrorOnIndex) {
			throw new MessagingInitializationException(
					"MessagingInitialization error from RecordIndexerSpy");
		}
		if (throwRuntimeErrorOnIndex) {
			throw new RuntimeException("Runtime error from RecordIndexerSpy");
		}
		this.type = type;
		this.pid = pid;
	}

}
