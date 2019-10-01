package se.uu.ub.cora.alvin.mixedstorage.fedora;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class IndexMessageInfoTest {

	@Test
	public void testIndexMessage() {
		String messageServerHostname = "someServerHostname";
		String messageServerPort = "someServerPort";
		IndexMessageInfo messageInfo = new IndexMessageInfo(messageServerHostname,
				messageServerPort);
		assertEquals(messageInfo.messageServerHostname, messageServerHostname);
		assertEquals(messageInfo.messageServerPort, messageServerPort);
	}

}
