package vn.edu.ptit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.service.PayOsPaymentService;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/api/payments/payos")
@RequiredArgsConstructor
public class PayOsWebhookController {

    private final PayOS payOS;
    private final PayOsPaymentService payOsPaymentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Webhook webhook) {
        try {
            WebhookData data = payOS.webhooks().verify(webhook);
            payOsPaymentService.markPaidFromWebhook(data);
            return ResponseEntity.ok("OK");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Invalid webhook");
        }
    }
}
