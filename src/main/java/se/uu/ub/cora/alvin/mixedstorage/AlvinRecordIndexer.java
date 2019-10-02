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

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessageSender;
import se.uu.ub.cora.messaging.MessagingProvider;

public class AlvinRecordIndexer implements RecordIndexer {

	private MessageRoutingInfo messageRoutingInfo;
	private String type;
	private String pid;

	public AlvinRecordIndexer(MessageRoutingInfo messageRoutingInfo) {
		this.messageRoutingInfo = messageRoutingInfo;
	}

	public void index(String type, String pid) {
		this.type = type;
		this.pid = pid;
		Map<String, Object> headers = createHeaders();
		String message = createMessage();

		MessageSender messageSender = MessagingProvider.getTopicMessageSender(messageRoutingInfo);
		messageSender.sendMessage(headers, message);
	}

	private Map<String, Object> createHeaders() {
		Map<String, Object> headers = new HashMap<>();
		headers.put("__TypeId__", "epc.messaging.amqp.EPCFedoraMessage");
		headers.put("ACTION", "UPDATE");
		headers.put("PID", pid);
		headers.put("messageSentFrom", "Cora");
		return headers;
	}

	private String createMessage() {
		IndexMessageCreator indexMessageCreator = IndexMessageCreator.usingId(pid);
		return indexMessageCreator.createMessage("alvin.updates." + type, "UPDATE");
	}

	MessageRoutingInfo getMessageRoutingInfo() {
		// needed for test
		return messageRoutingInfo;
	}

}
