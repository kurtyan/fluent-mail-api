package com.guilhermechapiewski.fluentmail.transport;

import java.util.Calendar;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.guilhermechapiewski.fluentmail.email.Email;
import com.sun.mail.smtp.SMTPTransport;

public class PostalService {

	private static EmailTransportConfiguration emailTransportConfig = new EmailTransportConfiguration();
	private static Session session;

	public void send(Email email) throws AddressException, MessagingException {
		Message message = createMessage(email);
		send(message);
	}

	protected Session getSession() {
		if (session == null) {
			Properties properties = System.getProperties();
			properties.put("mail.smtp.host", emailTransportConfig
					.getSmtpServer());
			properties.put("mail.smtp.auth", emailTransportConfig
					.isAuthenticationRequired());

			session = Session.getInstance(properties);
		}
		
		return session;
	}

	protected Message createMessage(Email email)
			throws MessagingException {
		Message message = new MimeMessage(getSession());
		message.setFrom(new InternetAddress(email.getFromAddress()));

		for (String to : email.getToAddresses()) {
			message.setRecipients(Message.RecipientType.TO, InternetAddress
					.parse(to));
		}

		message.setSubject(email.getSubject());
		message.setText(email.getBody());
		message.setHeader("X-Mailer", "Fluent Mail API");
		message.setSentDate(Calendar.getInstance().getTime());

		return message;
	}

	protected void send(Message message) throws NoSuchProviderException,
			MessagingException {
		String protocol = "smtp";
		if (emailTransportConfig.useSecureSmtp()) {
			protocol = "smtps";
		}

		SMTPTransport smtpTransport = (SMTPTransport) getSession()
				.getTransport(protocol);
		if (emailTransportConfig.isAuthenticationRequired()) {
			smtpTransport.connect(emailTransportConfig.getSmtpServer(),
					emailTransportConfig.getUsername(), emailTransportConfig
							.getPassword());
		} else {
			smtpTransport.connect();
		}
		smtpTransport.sendMessage(message, message.getAllRecipients());
		smtpTransport.close();
	}
}
