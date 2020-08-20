package com.company;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main implements MessageListener {
    public static Connection connection;
    public static ConnectionFactory connectionFactory;
    private static Session session;
    private static Topic topic;
    private static String name;

    public static void main(String[] args) {
        name = args[0];
        try {
            connectionFactory = new ActiveMQConnectionFactory(
                    "tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic("customerTopic");
            connection.start();
            createConsumer();
            sendMessage("-join chat-");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msgToSend = reader.readLine();
                if (msgToSend.equalsIgnoreCase("exit")) {
                    sendMessage("leave chat");
                    Thread.sleep(300);
                    connection.close();
                    System.exit(0);
                }
                sendMessage(msgToSend);
            }
        } catch (JMSException | IOException | InterruptedException e) {
            System.out.println("Error was occurred");
            e.printStackTrace();
        }
    }

    private static void sendMessage(String msgToSend) throws JMSException {
        Message msg = null;
        if (msgToSend.equals("-join chat-")) {
            msg = session.createTextMessage(" " + name + ": " + msgToSend);
        } else {
            msg = session.createTextMessage(name + ": " + msgToSend + "  |end of message|");
        }
        MessageProducer producer = session.createProducer(topic);
        producer.send(msg);
    }

    public static void createConsumer() throws JMSException {
        MessageConsumer consumer1 = session.createConsumer(topic);
        consumer1.setMessageListener(new Main());
    }

    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            if (!text.startsWith(name + ": "))
                System.out.println(textMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}



