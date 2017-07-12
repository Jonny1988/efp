package var.mom.jms.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class FileServer{
	private static final String DESTINATION = "var.mom.jms.file.requestqueue";

	private String fileRoot;
	private Session session;
	private MessageConsumer consumer;

	public FileServer(String fileRoot) throws NamingException, JMSException {
		this.fileRoot = fileRoot;
		Context ctx = new InitialContext();
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		Destination requestQueue = (Destination) ctx.lookup(DESTINATION);
		Connection connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(requestQueue);
		connection.start();
		System.out.println("FileServer gestartet ...");
	}

	public void process() throws JMSException {
		while (true) {
			TextMessage request = (TextMessage) consumer.receive(1000);
			if (request != null) {
				String filename = "";
				try {
					filename = (new File(fileRoot)).getCanonicalPath() + File.separator + request.getText();
				} catch (IOException e) {
					System.err.println("Problem mit Wurzelverzeichnis: " + fileRoot);
				}
				System.out.println("Angefordert: " + filename);
				Destination replyQueue = request.getJMSReplyTo();
				MessageProducer producer = session.createProducer(replyQueue);
				BytesMessage reply = session.createBytesMessage();
				try {
					InputStream in = new FileInputStream(filename);
					int c;
					while ((c = in.read()) != -1) {
						reply.writeByte((byte) c);
					}
					in.close();
					reply.setBooleanProperty("status", true);
					System.out.println("Datei lesbar");
				} catch (IOException e) {
					System.out.println("Datei nicht lesbar.");
					reply.setBooleanProperty("status", false);
				} finally {
					producer.send(reply);
					producer.close();
					System.out.println("BytesMessage gesendet");
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new FileServer(args[0]).process();
	}
}