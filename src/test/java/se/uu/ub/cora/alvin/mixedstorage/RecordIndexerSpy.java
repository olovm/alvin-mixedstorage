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
