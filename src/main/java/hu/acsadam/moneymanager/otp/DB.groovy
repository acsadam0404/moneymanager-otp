package hu.acsadam.moneymanager.otp

import groovy.sql.Sql

class DB {
    Sql sql

    DB() {
        sql = Sql.newInstance('jdbc:sqlite:c:/test/moneymanager/moneymanager.mmb', 'org.sqlite.JDBC')
        try {
            sql.execute("CREATE UNIQUE INDEX unique_transactionnumber ON CHECKINGACCOUNT_V1 (TRANSACTIONNUMBER);")
        } catch (e) {
            //NOP, already exists
        }
    }
}
