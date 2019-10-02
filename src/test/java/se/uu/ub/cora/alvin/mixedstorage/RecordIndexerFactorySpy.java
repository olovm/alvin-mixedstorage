package se.uu.ub.cora.alvin.mixedstorage;

import se.uu.ub.cora.messaging.MessageRoutingInfo;

public class RecordIndexerFactorySpy implements RecordIndexerFactory {

	public RecordIndexerSpy factoredRecordIndexer;
	public MessageRoutingInfo messageRoutingInfo;
	public boolean throwMessageInitializationErrorOnIndex = false;
	public boolean throwRuntimeErrorOnIndex = false;

	@Override
	public RecordIndexer factor(MessageRoutingInfo messageRoutingInfo) {
		this.messageRoutingInfo = messageRoutingInfo;
		factoredRecordIndexer = new RecordIndexerSpy(messageRoutingInfo);
		factoredRecordIndexer.throwMessageInitializationErrorOnIndex = throwMessageInitializationErrorOnIndex;
		factoredRecordIndexer.throwRuntimeErrorOnIndex = throwRuntimeErrorOnIndex;
		return factoredRecordIndexer;
	}

}
