package com.Incamar.IncaCore.events;

import com.Incamar.IncaCore.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockAlertEmailListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(LowStockAlertEvent alert) {
        try {
            emailService.sendStockAlertEmail(
                    alert.recipientEmail(),
                    alert.recipientName(),
                    alert.productName(),
                    alert.currentStock(),
                    alert.warehouseName(),
                    alert.minimumStock()
            );
        } catch (RuntimeException exception) {
            // The movement already committed: email errors must not make the API report a failed write.
            log.error("No se pudo enviar la alerta de stock después de confirmar la transacción", exception);
        }
    }
}
