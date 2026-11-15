package ir.sadteam.loancalc.core

enum class TransactionType(val label: String) {
    DEPOSIT("واریز"),
    WITHDRAWAL("برداشت"),
}

/** OWED_TO_ME یعنی طرفِ‌حساب به من بدهکاره (طلبِ من)؛ I_OWE یعنی من به طرفِ‌حساب بدهکارم. */
enum class DebtType(val label: String) {
    OWED_TO_ME("طلب"),
    I_OWE("بدهی"),
}
