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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class AlvinRecordIndexerTest {

	private LoggerFactorySpy loggerFactorySpy;
	private MessagingFactorySpy messagingFactory;
	private AlvinRecordIndexer indexer;
	private AmqpMessageRoutingInfo messageRoutingInfo;
	private String messageForCreateForSomeRecordType = "{\"headers\":{\"ACTION\":\"UPDATE\","
			+ "\"PID\":\"alvin-place:1\"},\"action\":\"UPDATE\",\"pid\":\"alvin-place:1\","
			+ "\"routingKey\":\"alvin.updates.someRecordType\"}";
	// private String message = "{\"pid\":\"alvin-place:1\",\"routingKey\":\"alvin.updates.place\","
	// + "\"action\":\"UPDATE\",\"dsId\":null,"
	// + "\"headers\":{\"ACTION\":\"UPDATE\",\"PID\":\"alvin-place:1\"}}";

	// headers.put("__TypeId__", "epc.messaging.amqp.EPCFedoraMessage");
	// headers.put("ACTION", "UPDATE");
	// headers.put("PID", "alvin-place:1");
	// headers.put("messageSentFrom", "Cora");

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		messagingFactory = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactory);

		messageRoutingInfo = new AmqpMessageRoutingInfo("someHostname", "somePort",
				"someVirtualHost", "index", "alvin.updates.place");
		indexer = new AlvinRecordIndexer(messageRoutingInfo);
	}

	@Test
	public void testIndex() {
		indexer.index("someRecordType", "alvin-place:1");

		assertTrue(messagingFactory.factorMessageSenderWasCalled);
		MessageSenderSpy messageSenderSpy = messagingFactory.messageSenderSpy;
		assertTrue(messageSenderSpy.sendMessageWasCalled);

		String sentMessage = messageSenderSpy.messageSentToSpy;
		assertEquals(sentMessage, messageForCreateForSomeRecordType);
		Map<String, Object> headersSentToSpy = messageSenderSpy.headersSentToSpy;
		assertEquals(headersSentToSpy.size(), 4);
		assertEquals(headersSentToSpy.get("__TypeId__"), "epc.messaging.amqp.EPCFedoraMessage");
		assertEquals(headersSentToSpy.get("ACTION"), "UPDATE");
		assertEquals(headersSentToSpy.get("PID"), "alvin-place:1");
		assertEquals(headersSentToSpy.get("messageSentFrom"), "Cora");

	}

	@Test
	public void testExtendedFunctionalityCorrectChannelInfoSentToMessageFactory() {

		indexer.index("someRecordType", "alvin-place:1");

		MessageSenderSpy messageSenderSpy = messagingFactory.messageSenderSpy;

		AmqpMessageRoutingInfo messageRoutingInfoSentToFactory = (AmqpMessageRoutingInfo) messagingFactory.messageRoutingInfo;
		assertEquals(messageRoutingInfoSentToFactory.hostname, messageRoutingInfo.hostname);
		assertEquals(messageRoutingInfoSentToFactory.port, messageRoutingInfo.port);
		assertEquals(messageRoutingInfoSentToFactory.virtualHost, messageRoutingInfo.virtualHost);
		assertEquals(messageRoutingInfoSentToFactory.exchange, messageRoutingInfo.exchange);
		assertEquals(messageRoutingInfoSentToFactory.routingKey, messageRoutingInfo.routingKey);

		String sentMessage = messageSenderSpy.messageSentToSpy;
		assertEquals(sentMessage, messageForCreateForSomeRecordType);

	}

	@Test
	public void testExtendedFunctionalityCorrectHeadersSentToMessageFactory() {
		indexer.index("someRecordType", "alvin-place:1");
		MessageSenderSpy messageSenderSpy = messagingFactory.messageSenderSpy;

		Map<String, Object> headersSentToSpy = messageSenderSpy.headersSentToSpy;
		assertEquals(headersSentToSpy.size(), 4);
		assertEquals(headersSentToSpy.get("__TypeId__"), "epc.messaging.amqp.EPCFedoraMessage");
		assertEquals(headersSentToSpy.get("ACTION"), "UPDATE");
		assertEquals(headersSentToSpy.get("PID"), "alvin-place:1");
		assertEquals(headersSentToSpy.get("messageSentFrom"), "Cora");

	}

	@Test
	public void testGetMessageRoutingInfo() {
		AmqpMessageRoutingInfo requestedMessageRoutingInfo = (AmqpMessageRoutingInfo) indexer
				.getMessageRoutingInfo();
		assertEquals(requestedMessageRoutingInfo.hostname, messageRoutingInfo.hostname);
		assertEquals(requestedMessageRoutingInfo.port, messageRoutingInfo.port);
		assertEquals(requestedMessageRoutingInfo.exchange, messageRoutingInfo.exchange);
		assertEquals(requestedMessageRoutingInfo.routingKey, messageRoutingInfo.routingKey);
	}
}
