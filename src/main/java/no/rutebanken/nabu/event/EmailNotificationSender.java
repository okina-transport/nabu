package no.rutebanken.nabu.event;

import no.rutebanken.nabu.domain.event.Event;
import no.rutebanken.nabu.domain.event.Notification;
import no.rutebanken.nabu.organisation.model.user.NotificationType;
import no.rutebanken.nabu.organisation.model.user.User;
import no.rutebanken.nabu.repository.NotificationRepository;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Log notifications to user (for test purposes).
 */
@Service
public class EmailNotificationSender implements NotificationProcessor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private NotificationRepository notificationRepository;

    private static final String EMAIL_SUBJECT = "Entur notifications";

    //    @Value("${email.account.username}")
//    private String emailAccountUsername;
//
//    @Value("${email.account.password}")
//    private String emailAccountPassword;
//
//    @Value("${email.host.name}")
//    private String emailHostName;
//
//    @Value("${email.smtp.port:465}")
//    private int emailSmtpPort;
//
//
    @Value("${email.max.length:8000}")
    private int emailMaxLength;

    @Override
    public void processNotificationsForUser(User user, Set<Notification> notifications) {

        if (user.getContactDetails() == null || user.getContactDetails().getEmail() == null) {
            logger.warn("Unable to notify user without registered email address: " + user.getUsername() + ". Discarding notifications: " + notifications);
            notificationRepository.delete(notifications);
            return;
        }

        logger.info("Sending email to user: " + user.getUsername() + " for notifications: " + notifications);

        String msg = formatMessage(notifications);

        boolean sent = sendEmail(user.getContactDetails().getEmail(), msg);

        if (sent) {
            notificationRepository.delete(notifications);
        }
    }

    protected boolean sendEmail(String to, String msg) {
//        try {
//            Email email = new SimpleEmail();
//            email.setHostName(emailHostName);
//            email.setSmtpPort(emailSmtpPort);
//            email.setAuthenticator(new DefaultAuthenticator(emailAccountUsername, emailAccountPassword));
//            email.setSSLOnConnect(true);
//            email.setFrom(emailAccountUsername);
//            email.setSubject(EMAIL_SUBJECT);
//            email.setMsg(msg);
//            email.addTo(to);
//
//            email.send();
//        } catch (EmailException emailException) {
//            logger.warn("Could not send notification email to: " + to + " because of : " + emailException.getMessage(), emailException);
//            return false;
//        }
        logger.info("Missing email setup. Msg not sent: " + msg);
        return true;
    }


    private String formatMessage(Set<Notification> notifications) {
        SortedSet<Event> sortedEvents = notifications.stream().map(n -> n.getEvent()).collect(Collectors.toCollection(() -> new TreeSet<>()));


// TODO Truncate

        StringBuilder contentBuilder = new StringBuilder();

        // TODO formatting
        sortedEvents.forEach(e -> contentBuilder.append(e.toString()).append("\n"));

        if (contentBuilder.length() > emailMaxLength) {
            return contentBuilder.substring(0, emailMaxLength) + "\n... (Content has been truncated)";
        }

        return contentBuilder.toString();
    }

    @Override
    public NotificationType getSupportedNotificationType() {
        return NotificationType.EMAIL;
    }
}
