package se.uu.ub.cora.alvin.mixedstorage;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class IndexMessageCreatorTest {

	private String expectedMessage = "{\"headers\":{\"ACTION\":\"UPDATE\","
			+ "\"PID\":\"someId\"},\"action\":\"UPDATE\",\"pid\":\"someId\","
			+ "\"routingKey\":\"alvin.updates.someType\"}";

	@Test
	public void testInit() {
		IndexMessageCreator messageCreator = IndexMessageCreator.usingId("someId");
		String message = messageCreator.createMessage("alvin.updates.someType", "UPDATE");
		assertEquals(expectedMessage, message);

	}

}
