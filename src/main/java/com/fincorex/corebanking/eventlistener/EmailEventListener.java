package com.fincorex.corebanking.eventlistener;

import com.fincorex.corebanking.constants.ApiConstants;
import com.fincorex.corebanking.events.EmailEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Component
public class EmailEventListener {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEmail(EmailEvent emailEvent) throws MessagingException {
        String subject = "";
        String text = "";

        if(emailEvent.getEventType().equals(ApiConstants.OPEN_ACCOUNT_EMAIL_EVENT)){
            subject = """
                    Welcome to FinCoreX — Your account is now active (ID: %s)""".
                    formatted(emailEvent.getAccountID());
            text = """
                <!DOCTYPE html>
                <html><body style="font-family: Arial, sans-serif; color:#222;">
                <p>Hi <strong>%s</strong>,</p>
                <p>Welcome to <strong>FinCoreX</strong>! Your account has been successfully opened.</p>
                <table style="border-collapse: collapse; margin-top: 8px;">
                  <tr><td style="padding:4px 8px;"><strong>Account ID:</strong></td><td style="padding:4px 8px;">%s</td></tr>
                  <tr><td style="padding:4px 8px;"><strong>Opening Date:</strong></td><td style="padding:4px 8px;">%s</td></tr>
                </table>
                <p>You can now begin transactions and track balances securely.</p>
                <p style="margin-top:16px;">If you did not request this, please contact support immediately.</p>
                <p>Regards,<br/>FinCoreX Support</p>
                </body></html>
                """.formatted(emailEvent.getCustomerName(), emailEvent.getAccountID(), emailEvent.getDate().toString());
        } else if (emailEvent.getEventType().equals(ApiConstants.TRANSACTION_EMAIL_EVENT)) {
            char txnType = emailEvent.getTransactionAmount().compareTo(BigDecimal.ZERO) < 0 ? 'D' : 'C';
            subject = String.format("%s %s — Account %s",
                    emailEvent.getTransactionAmount(), (txnType == 'C') ? "credited" : "debited", emailEvent.getAccountID());
            text = """
                <!DOCTYPE html>
                <html><body style="font-family: Arial, sans-serif; color:#222;">
                <p>Hi <strong>%s</strong>,</p>
                <p>A transaction has been posted on your account.</p>
                <table style="border-collapse: collapse; margin-top: 8px;">
                  <tr><td style="padding:4px 8px;"><strong>Account ID:</strong></td><td style="padding:4px 8px;">%s</td></tr>
                  <tr><td style="padding:4px 8px;"><strong>Date:</strong></td><td style="padding:4px 8px;">%s</td></tr>
                  <tr><td style="padding:4px 8px;"><strong>Type:</strong></td><td style="padding:4px 8px;">%s</td></tr>
                  <tr><td style="padding:4px 8px;"><strong>Amount:</strong></td><td style="padding:4px 8px;">%s</td></tr>
                  <tr><td style="padding:4px 8px;"><strong>Available Balance:</strong></td><td style="padding:4px 8px;">%s</td></tr>
                </table>
                <p style="margin-top:16px;">If you do not recognize this activity, please contact support immediately.</p>
                <p>Regards,<br/>FinCoreX Alerts</p>
                </body></html>
                """.formatted(emailEvent.getCustomerName(), emailEvent.getAccountID(), emailEvent.getDate().toString(),
                    txnType, emailEvent.getTransactionAmount(), emailEvent.getAvailableBalance());

        }

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(senderEmail);
        helper.setTo(emailEvent.getCustomerEmail());
        helper.setSubject(subject);
        helper.setText(text, true);

        javaMailSender.send(message);

    }


}
