// package se.uu.ub.cora.alvin.mixedstorage.fedora;
//
// import java.util.Properties;
//
// import javax.jms.JMSException;
// import javax.jms.TextMessage;
//
// import com.yourmediashelf.fedora.client.messaging.JMSManager;
// import com.yourmediashelf.fedora.client.messaging.MessagingClient;
// import com.yourmediashelf.fedora.client.messaging.MessagingException;
// import com.yourmediashelf.fedora.client.messaging.MessagingListener;
//
// public class SpikeDivaFedoraMessageListner implements MessagingListener {
// MessagingClient messagingClient;
//
// public void start() throws MessagingException {
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
// @Override
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