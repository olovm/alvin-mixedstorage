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
