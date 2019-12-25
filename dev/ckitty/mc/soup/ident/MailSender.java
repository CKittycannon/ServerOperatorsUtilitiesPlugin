package dev.ckitty.mc.soup.ident;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MailSender {
    
    private String target, source, password, host, title, conts;
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public void setUser(String source, String password) {
        this.source = source;
        this.password = password;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public void setMessage(String title, String conts) {
        this.title = title;
        this.conts = conts;
    }
    
    public boolean sendMail(String pword) {
        // target email
        String to = target;
    
        // authentification & email host
        String username = this.source;
        String password = this.password;
        String host = this.host;
    
        Properties props = new Properties(); // idk if this should be changed
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "25");
    
        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    
        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);
        
            // Set From: header field of the header.
            //message.setFrom(new InternetAddress(from));
        
            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
        
            // THE MESSAGE
            message.setSubject(title);
            message.setContent(conts.replace("{password-holder-block}", pword), "text/html");
        
            // Send message
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
