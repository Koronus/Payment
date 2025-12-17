package com.example.Payment.Service;

import com.example.Payment.Controller.PaymentFormController;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ReceiptEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String from;

    public ReceiptEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReceipt(String toEmail,
                            PaymentFormController.ReceiptView receipt,
                            Object operationId) throws MessagingException {

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setFrom(from);
        helper.setSubject("DemoShop — чек по оплате (ID " + operationId + ")");

        String html = """
        <div style="font-family:Arial,sans-serif;max-width:680px;margin:0 auto">
          <h2 style="margin:0 0 10px 0;color:#28a745">✅ Платёж успешно обработан</h2>
          <div style="padding:16px;border:1px solid #e7e7e7;border-radius:14px;background:#fff">
            <h3 style="margin:0 0 8px 0">Электронный чек</h3>
            <div style="color:#666;font-size:13px;margin-bottom:12px">
              В соответствии с 54-ФЗ: <a href="%s" target="_blank" rel="noopener">ссылка</a>
            </div>

            <table style="width:100%%;border-collapse:collapse">
              <tr><td style="padding:6px 0;color:#666">Продавец</td><td style="padding:6px 0;text-align:right"><b>%s</b></td></tr>
              <tr><td style="padding:6px 0;color:#666">ИНН продавца</td><td style="padding:6px 0;text-align:right"><b>%s</b></td></tr>
              <tr><td style="padding:6px 0;color:#666">Дата и время расчёта</td><td style="padding:6px 0;text-align:right"><b>%s</b></td></tr>
              
              <tr><td style="padding:6px 0;color:#666">Товар/услуга</td><td style="padding:6px 0;text-align:right"><b>%s</b></td></tr>
              <tr><td style="padding:10px 0;color:#666;font-size:16px"><b>Сумма расчёта</b></td>
                  <td style="padding:10px 0;text-align:right;font-size:16px"><b>%s ₽</b></td></tr>
            </table>

            <div style="margin-top:10px;font-size:13px;color:#444">
              Сайт ФНС: <a href="%s" target="_blank" rel="noopener">%s</a>
            </div>

            <div style="margin-top:12px;display:flex;align-items:center;gap:12px">
              <div>
                <a href="%s" target="_blank" rel="noopener">
                  <img src="cid:qrImage" alt="QR" style="width:180px;height:180px;border:1px solid #eee;border-radius:12px" />
                </a>
              </div>
              <div style="color:#666;font-size:12px">QR ведёт на сайт ФНС</div>
            </div>
          </div>
        </div>
        """.formatted(
                receipt.law54Url(),
                receipt.sellerName(),
                receipt.sellerInn(),
                receipt.datetime(),
                receipt.paymentSign(),
                receipt.itemName(),
                receipt.amount(),
                receipt.fnsUrl(),
                receipt.fnsUrl(),
                receipt.fnsUrl()
        );

            String text = """
            DemoShop — электронный чек
            
            Продавец: %s
            ИНН продавца: %s
            Дата/время: %s
            Товар/услуга: %s
            Сумма: %s ₽
            
            54-ФЗ: %s
            Сайт ФНС: %s
            """.formatted(
                receipt.sellerName(),
                receipt.sellerInn(),
                receipt.datetime(),
                receipt.itemName(),
                receipt.amount(),
                receipt.law54Url(),
                receipt.fnsUrl()
        );
        helper.setReplyTo("nikudot@yandex.ru");
        msg.addHeader("X-Mailer", "DemoShop");
        helper.setText(text, false);


        // Лучше для email использовать PNG (SVG многие клиенты не показывают).
        // Если положишь qr.png в src/main/resources/static/qr.png — будет inline-картинка.
        ClassPathResource qrPng = new ClassPathResource("static/qr.png");
        if (qrPng.exists()) {
            helper.addInline("qrImage", qrPng, "image/png");
        } else {
            // fallback: если есть только svg — отправим вложением, а inline не гарантируем
            ClassPathResource qrSvg = new ClassPathResource("static/qr.svg");
            if (qrSvg.exists()) {
                helper.addAttachment("qr.svg", qrSvg);
                // и поставим заглушку, чтобы письмо не ломалось
                // (если нет png, cid-картинку нельзя корректно показать)
            }
        }

        mailSender.send(msg);
    }
}
