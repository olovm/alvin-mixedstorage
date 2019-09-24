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
}
