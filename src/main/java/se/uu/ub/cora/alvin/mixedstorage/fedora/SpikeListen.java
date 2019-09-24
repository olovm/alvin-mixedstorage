package se.uu.ub.cora.alvin.mixedstorage.fedora;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class SpikeListen {
	public static void main(String[] args) {
		// to access messaging.alvin-portal.org from development..access.
		// docker network connect --alias messaging.alvin-portal.org eclipseForCoraNet
		// dev-alvin-qpid
		String EXCHANGE_NAME = "index";
		try {
			com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
			factory.setHost("messaging.alvin-portal.org");
			factory.setPort(5672);
			factory.setVirtualHost("alvin");
			com.rabbitmq.client.Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();

			// channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			// channel.exchangeDeclare(EXCHANGE_NAME, "alvin.updates.place");
			// channel.exchangeDeclare(EXCHANGE_NAME, "alvin.search.query.PlaceAQLQuery");
			String queueName = channel.queueDeclare().getQueue();
			// channel.queueBind(queueName, EXCHANGE_NAME, "");
			// channel.queueBind(queueName, EXCHANGE_NAME, "alvin.search.query.PlaceAQLQuery");
			channel.queueBind(queueName, EXCHANGE_NAME, "#");

			System.out.println(" [*] Waiting for messages. To exit press CTRL+C 2");

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), "UTF-8");
				System.out.println(" [x] Received '" + message + "'");
			};
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
			});
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getCause());
		}
	}

	// public static void main(String[] args) {
	// // docker network connect --alias dev-diva-archive eclipseForCoraNet dev-diva-archive
	// // docker network connect --alias dev-diva-drafts eclipseForCoraNet dev-diva-drafts
	// try {
	// ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
	//
	// connectionFactory.setBrokerURL("tcp://messaging.alvin-portal.org:5672/alvin");
	// // connectionFactory.
	// // connectionFactory.setUserName("admin");
	// // connectionFactory.setPassword("admin");
	// Connection connection = connectionFactory.createConnection();
	// Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	// Destination destination = session.createQueue("*");
	// // Destination destination = session.createTopic("*");
	// MessageConsumer consumer = session.createConsumer(destination);
	// connection.start();
	// while (true) {
	// Message message = consumer.receive();
	//
	// if (message instanceof TextMessage) {
	// TextMessage text = (TextMessage) message;
	// System.out.println("Message is : " + text.getText());
	// }
	// }
	// // connection.close();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// System.out.println(e.getCause());
	// System.out.print(e);
	// }
	//
	// }
}
