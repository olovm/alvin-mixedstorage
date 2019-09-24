package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class SpikeListenDiVA {
	// public static void main(String[] args) {
	// // to access messaging.alvin-portal.org from development..access.
	// // docker network connect --alias archive.diva-portal.org eclipseForCoraNet
	// // dev-archive-service
	// String EXCHANGE_NAME = "index";
	// try {
	// // com.rabbitmq.client.ConnectionFactory factory = new
	// // com.rabbitmq.client.ConnectionFactory();
	// com.rabbitmq.client.ConnectionFactory factory = new RMQObjectFactory();
	// factory.setHost("archive.diva-portal.org");
	// // factory.setHost("archive.diva-portal.org");
	// factory.setPort(61616);
	// factory.setVirtualHost("diva");
	// com.rabbitmq.client.Connection connection = factory.newConnection();
	// Channel channel = connection.createChannel();
	//
	// String queueName = channel.queueDeclare().getQueue();
	// channel.queueBind(queueName, EXCHANGE_NAME, "#");
	//
	// System.out.println(" [*] Waiting for messages from diva. To exit press CTRL+C 2");
	//
	// DeliverCallback deliverCallback = (consumerTag, delivery) -> {
	// String message = new String(delivery.getBody(), "UTF-8");
	// System.out.println(" [x] Received '" + message + "'");
	// };
	// channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
	// });
	// } catch (Exception e) {
	// // TODO: handle exception
	// System.out.println(e.getCause());
	// }
	// }

	public static void main(String[] args) {
		// to access messaging.alvin-portal.org from development..access.
		// docker network connect --alias messaging.alvin-portal.org eclipseForCoraNet
		// dev-alvin-qpid

		// docker network connect --alias archive2.diva-portal.org eclipseForCoraNet
		// dev-diva-archive
		// values should be in fedora.fcfg!!
		// <module role="fedora.server.messaging.Messaging"
		// class="fedora.server.messaging.MessagingModule">
		// <comment>Fedora's Java Messaging Service (JMS) Module</comment>
		// <param name="enabled" value="true"/>
		// <param name="java.naming.factory.initial"
		// value="org.apache.activemq.jndi.ActiveMQInitialContextFactory"/>
		// <param name="java.naming.provider.url" value="vm:(broker:(tcp://localhost:61616))"/>
		// <param name="datastore1" value="apimUpdateMessages">
		// <comment>A datastore representing a JMS Destination for APIM events which update the
		// repository</comment>
		// </param>
		// <param name="datastore2" value="apimAccessMessages">
		// <comment>A datastore representing a JMS Destination for APIM events which do not update
		// the repository</comment>
		// </param>
		// </module>
		String EXCHANGE_NAME = "index";
		try {
			com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
			// factory.setHost("archive2.diva-portal.org");
			factory.setHost("dev-diva-archive");
			factory.setPort(61616);
			// factory.setcl
			// factory.set
			factory.setShutdownTimeout(30000);
			Map<String, Object> clientProperties = new HashMap<>();
			clientProperties.put("topic.fedora", "fedora.apim.*");
			// factory.setClientProperties(clientProperties);

			// factory.setVirtualHost("broker");

			com.rabbitmq.client.Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();

			// channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			// channel.exchangeDeclare(EXCHANGE_NAME, "alvin.updates.place");
			// channel.exchangeDeclare(EXCHANGE_NAME, "alvin.search.query.PlaceAQLQuery");
			String queueName = channel.queueDeclare().getQueue();
			// channel.queueBind(queueName, EXCHANGE_NAME, "");
			// channel.queueBind(queueName, EXCHANGE_NAME, "alvin.search.query.PlaceAQLQuery");
			channel.queueBind(queueName, EXCHANGE_NAME, "#");

			System.out.println(" [*] Waiting for messages. To exit press CTRL+C 22");

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), "UTF-8");
				System.out.println(" [x] Received '" + message + "'");
			};
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
			});
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getCause());
			System.out.print(e);
		}
	}

	// public class Example implements MessagingListener {
	// MessagingClient messagingClient;
	// public void start() throws MessagingException {
	// public void spikeTestFedoraJMS() {
	// Properties properties = new Properties();
	// properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
	// "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
	// properties.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
	// properties.setProperty(JMSManager.CONNECTION_FACTORY_NAME, "ConnectionFactory");
	// properties.setProperty("topic.fedora", "fedora.apim.*");
	// messagingClient = new JmsMessagingClient("example1", this, properties, false);
	// messagingClient.start();
	// }
	//
	// public void stop() throws MessagingException {
	// messagingClient.stop(false);
	// }
	//
	// public void onMessage(String clientId, Message message) {
	// String messageText = "";
	// try {
	// messageText = ((TextMessage) message).getText();
	// } catch (JMSException e) {
	// System.err.println("Error retrieving message text " + e.getMessage());
	// }
	// System.out.println("Message received: " + messageText + " from client " + clientId);
	// }
	// }

}
