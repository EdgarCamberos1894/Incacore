package com.Incamar.IncaCore.events;

/** Immutable snapshot: no JPA entities are accessed after the transaction commits. */
public record LowStockAlertEvent(
        String recipientEmail,
        String recipientName,
        String productName,
        Long currentStock,
        String warehouseName,
        Long minimumStock
) {
}
