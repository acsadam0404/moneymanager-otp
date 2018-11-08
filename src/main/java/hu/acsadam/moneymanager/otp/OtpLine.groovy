package hu.acsadam.moneymanager.otp

import java.time.LocalDate

class OtpLine {
    String type
    LocalDate date
    BigDecimal amount
    String oppositeName
    String note
    String transactionNumber

    OtpLine(String type, LocalDate date, BigDecimal amount, String oppositeName, String note, String transactionNumber) {
        this.type = type
        this.date = date
        this.amount = amount
        this.oppositeName = oppositeName
        this.note = note
        this.transactionNumber = transactionNumber
    }
}
