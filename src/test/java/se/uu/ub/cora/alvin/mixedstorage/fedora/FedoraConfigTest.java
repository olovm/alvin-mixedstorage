package se.uu.ub.cora.alvin.mixedstorage.fedora;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class FedoraConfigTest {

	@Test
	public void testFedoraConfig() {
		String userName = "someUserName";
		String password = "somePassword";
		String baseUrl = "someBaseUrl";
		FedoraConfig fedoraConfig = new FedoraConfig(userName, password, baseUrl);
		assertEquals(fedoraConfig.userName, userName);
		assertEquals(fedoraConfig.password, password);
		assertEquals(fedoraConfig.baseUrl, baseUrl);
	}

}
