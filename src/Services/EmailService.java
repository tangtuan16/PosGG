package Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailService {
    private String username;
    private String password;

    public EmailService() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("config.properties"));
            username = props.getProperty("email.username");
            password = props.getProperty("email.password");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không đọc được file config.properties");
        }
        System.out.println("Username: " + username);
        System.out.println("Password: " + (password != null ? "******" : "null"));

    }

    public void sendInvoiceWithAttachment(String toEmail, String subject, String body, String attachmentPath) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "PosGG"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(body, "text/html; charset=utf-8");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(attachmentPath));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Gửi hóa đơn thành công tới " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gửi hóa đơn thất bại.");
        }
    }
}
