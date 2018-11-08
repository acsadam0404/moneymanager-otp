package hu.acsadam.moneymanager.otp

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Main {

    private static DB db = new DB()

    static void main(String[] args) {
        Path path = Paths.get("c:\\test\\moneymanager\\")

        Files.newDirectoryStream(path).forEach({ file ->
            if (file.fileName.toString().endsWith("xlsx")) {
                readFile(file)
            } else {
                System.err.println("Hibás fájl (nem xlsx kiterjesztés): ${file.toString()}")
            }
        })
    }

    private static void readFile(Path file) {
        List<OtpLine> otpLines = []
        String accountNumber
        new ExcelReader(file).eachLine {
            if (it.rowNum == 2) {
                accountNumber = cell(1)
            }
            if (it.rowNum >= 14) {
                def line = new OtpLine(
                        cell(1),
                        LocalDate.from(DateTimeFormatter.ofPattern("yyyy.MM.dd.").parse(cell(3).replace("\u00a0", ""))),
                        new BigDecimal(new DecimalFormat("#,#.00").parse(cell(4))),
                        cell(7),
                        cell(8),
                        cell(9))
                otpLines << line
            }
        }

        updateMoneyManager(accountNumber, otpLines)
    }

    static void updateMoneyManager(String accountNumber, List<OtpLine> list) {
        println accountNumber
        def mmAccountId = db.sql.firstRow("select ACCOUNTID from ACCOUNTLIST_V1 where ACCOUNTNUM = ${accountNumber}").ACCOUNTID
        list.each {
            String mmTransCode = it.amount < 0 ? "Withdrawal" : "Deposit"
            String payeeId = getPayeeId(it)
            try {
                db.sql.executeInsert("""insert into CHECKINGACCOUNT_V1 
                (ACCOUNTID, TRANSCODE, TRANSAMOUNT, STATUS, TRANSACTIONNUMBER, TRANSDATE, PAYEEID, NOTES) values (?, ?, ?, 'R', ?, ?, ?, ?);""",
                        mmAccountId, mmTransCode, it.amount.abs(), it.transactionNumber, it.date, payeeId, it.note)
            } catch (e) {
                if (!e.toString().contains("Abort due to constraint violation")) {
                    throw e
                }
            }
        }
        println ""
    }

    static String getPayeeId(OtpLine otpLine) {
        def payeeIdRow = db.sql.firstRow("select PAYEEID from PAYEE_V1 where PAYEENAME = ${otpLine.oppositeName}")
        if (payeeIdRow == null) {
            db.sql.executeInsert("insert into PAYEE_V1 (PAYEENAME) values (${otpLine.oppositeName})")
            return getPayeeId(otpLine)
        }
        return payeeIdRow.payeeId
    }
}
