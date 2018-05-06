package com.owera.xaps.monitor;

import java.util.Date;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Trigger;

public class SendMail {

	private static Logger log = new Logger();

	public static void sendNotification(Trigger trigger, String msg) {
		String subject = trigger.getNotifyTypeAsStr() + " from trigger " + trigger.getName();
		String[] to = null;
		if (trigger.getToList() != null)
			to = trigger.getToList().split(",");
		boolean ok = send(subject, msg, new Date(), to);
		if (ok)
			log.info("Notification sent successfully:\n" + msg);
		else
			log.info("Notification sending failed:\n" + msg);
	}

	public static void sendFusionAlarm(String module, String status, String msg) {
		send("Fusion on  " + Properties.getFusionHostname() + " : " + module + " " + status, msg, new Date());
	}

	public static boolean send(String subject, String message, Date date) {
		return send(subject, message, date, null);
	}

	public static boolean send(String subject, String message, Date date, String[] to) {
		if (to == null || to.length == 0) {
			String toStr = Properties.get("alerts.to");
			if (toStr != null) {
				to = toStr.split(",");
			} else {
				log.error("No specified addresses for the properties \"alerts.to\", not possible to send mail");
				return false;
			}
		}
		String from = Properties.get("alerts.from");
		if (from == null) {
			log.error("No specified addresses for the properties \"alerts.from\", not possible to send mail");
			return false;
		}
		try {
			String smtp = Properties.get("alerts.smtp.host");
			if (smtp == null) {
				log.error("Could not send email alert '" + subject + "': SMTP host is not defined in propertyfile");
				return false;
			}

			String port = Properties.get("alerts.smtp.port");
			if (port == null)
				port = "25";

			try {
				Integer.parseInt(port);
			} catch (NumberFormatException ex) {
				log.error("Could not send email alert '" + subject + "': SMTP port is not an integer or is not defined in propertyfile");
				return false;
			}

			final String user = Properties.get("alerts.smtp.user");
			final String pass = Properties.get("alerts.smtp.pass");
			String useSSL = Properties.get("alerts.smtp.ssl");
			if (useSSL == null)
				useSSL = "false";
			String debug = Properties.get("alerts.smtp.debug");
			if (debug == null)
				debug = "false";

			java.util.Properties props = new java.util.Properties();
			props.put("mail.debug", debug);
			props.put("mail.smtp.host", smtp);
			props.put("mail.smtp.port", port);
			if (smtp.indexOf("gmail") > -1)
				props.put("mail.smtp.starttls.enable", "true");

			if (user != null && pass != null) {
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.user", user);
				props.put("mail.smtp.password", pass);
			}

			if (useSSL.equals("true") || useSSL.equals("yes") || useSSL.equals("on")) {
				props.put("mail.smtp.socketFactory.port", port);
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.put("mail.smtp.socketFactory.fallback", "false");
			}

			Authenticator auth = new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					String username = user;
					String password = pass;
					return new PasswordAuthentication(username, password);
				}
			};

			Session session = Session.getInstance(props, auth);

			Message msg = new MimeMessage(session);

			InternetAddress addressFrom = new InternetAddress(from);

			msg.setFrom(addressFrom);

			InternetAddress[] addressTo = new InternetAddress[to.length];

			for (int i = 0; i < to.length; i++) {
				addressTo[i] = new InternetAddress(to[i]);
			}

			msg.setRecipients(Message.RecipientType.TO, addressTo);

			msg.setSubject(subject);
			msg.setSentDate(date);
			msg.setContent(message, "text/html");

			Transport.send(msg);

			log.info("Email notification sent to " + to.length + " recipients");
			return true;
		} catch (MessagingException e) {
			String errorMessage = e.getMessage();
			if (e.getNextException() != null)
				errorMessage += " (Nested exception: " + e.getNextException().getMessage() + ")";
			log.error("Could not send email alert '" + subject + "': " + errorMessage);
			return false;
		}
	}

	public static void main(String[] args) {
		send("Test", "Test", new Date(), new String[] { "mortensimon@gmail.com" });
	}
}