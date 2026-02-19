package tn.esprit.Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import tn.esprit.utils.ResourceUtil;

import java.io.InputStream;
import java.time.Year;
import java.util.Properties;
import jakarta.mail.util.ByteArrayDataSource;

public class EmailService {

    private final Properties smtpProps = new Properties();
    private final String fromName;
    private final String fromEmail;
    private final String username;
    private final String passwordEnvVar;

    public EmailService() {
        // Load config/mail.properties from classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/mail.properties")) {
            if (is == null) {
                throw new IllegalStateException("Missing config/mail.properties in resources.");
            }
            smtpProps.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load mail.properties", e);
        }

        this.fromName = require("mail.from.name");
        this.fromEmail = require("mail.from.email");
        this.username = require("mail.username");
        this.passwordEnvVar = require("mail.password.env");
    }

    private String require(String key) {
        String v = smtpProps.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalStateException("Missing mail property: " + key);
        }
        return v.trim();
    }

    private String readPasswordFromEnv() {
        String pw = System.getenv(passwordEnvVar);
        if (pw == null || pw.isBlank()) {
            throw new IllegalStateException("Environment variable not set: " + passwordEnvVar);
        }
        return pw;
    }

    private Session createSession() {
        String pw = readPasswordFromEnv();

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, pw);
            }
        });

        session.setDebug(true); // for TLS debugging
        System.out.println("JAVA_HOME=" + System.getProperty("java.home"));
        System.out.println("trustStore=" + System.getProperty("javax.net.ssl.trustStore"));
        System.out.println("trustStoreType=" + System.getProperty("javax.net.ssl.trustStoreType"));
        return session;
    }

    /**
     * Sends a branded welcome email with inline logo (cid:startupflow_logo)
     */
    public void sendWelcomeEmail(String toEmail, String fullName, String loginUrl) {
        try {
            Session session = createSession();

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail, fromName));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject("Welcome to StartupFlow 🚀", "UTF-8");

            // 1) Load HTML template and replace placeholders
            String html = ResourceUtil.readResourceAsString("templates/email/welcome.html");
            html = html.replace("{{FULL_NAME}}", escapeHtml(fullName));
            html = html.replace("{{LOGIN_URL}}", loginUrl);
            html = html.replace("{{YEAR}}", String.valueOf(Year.now().getValue()));

            // 2) Create multipart/related (HTML + inline image)
            MimeMultipart related = new MimeMultipart("related");

            // HTML part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(html, "text/html; charset=UTF-8");
            related.addBodyPart(htmlPart);

            MimeBodyPart logoPart = new MimeBodyPart();
            logoPart.setHeader("Content-ID", "<startupflow_logo>");
            logoPart.setDisposition(MimeBodyPart.INLINE);

// IMPORTANT: path must be assets/email/logo.png (not assest)
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("assest/email/logo.png")) {
                if (is == null) {
                    throw new IllegalStateException("Missing assets/email/logo.png in resources.");
                }

                ByteArrayDataSource ds = new ByteArrayDataSource(is, "image/png");
                logoPart.setDataHandler(new jakarta.activation.DataHandler(ds));
            }

            related.addBodyPart(logoPart);

            msg.setContent(related);
            Transport.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send welcome email to " + toEmail, e);
        }
    }

    // Minimal HTML escaping for names
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}