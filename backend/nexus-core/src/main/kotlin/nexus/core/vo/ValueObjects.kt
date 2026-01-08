package nexus.core.vo

import java.time.LocalDate

/**
 * NEXUS システム共通 Value Objects
 */

/** 氏名 */
data class PersonName(
    val lastName: String,
    val firstName: String,
    val lastNameKana: String,
    val firstNameKana: String
) {
    init {
        require(lastName.isNotBlank()) { "lastName must not be blank" }
        require(firstName.isNotBlank()) { "firstName must not be blank" }
    }

    val fullName: String get() = "$lastName $firstName"
    val fullNameKana: String get() = "$lastNameKana $firstNameKana"
}

/** 住所 */
data class Address(
    val postalCode: String,
    val prefecture: String,
    val city: String,
    val street: String,
    val building: String? = null
) {
    init {
        require(postalCode.matches(Regex("^\\d{3}-?\\d{4}$"))) {
            "Invalid postal code format: $postalCode"
        }
    }

    val fullAddress: String
        get() = listOfNotNull(prefecture, city, street, building)
            .joinToString("")
}

/** 電話番号 */
@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.matches(Regex("^[0-9-]+$"))) {
            "Invalid phone number format: $value"
        }
    }
}

/** メールアドレス */
@JvmInline
value class Email(val value: String) {
    init {
        require(value.contains("@")) { "Invalid email format: $value" }
    }
}

/** 金額（日本円） */
@JvmInline
value class Money(val yen: Long) {
    init {
        require(yen >= 0) { "Money must not be negative: $yen" }
    }

    operator fun plus(other: Money): Money = Money(yen + other.yen)
    operator fun minus(other: Money): Money = Money(yen - other.yen)
    operator fun times(multiplier: Int): Money = Money(yen * multiplier)

    companion object {
        val ZERO = Money(0)
    }
}

/** 期間 */
data class Period(
    val startDate: LocalDate,
    val endDate: LocalDate?
) {
    init {
        endDate?.let {
            require(!it.isBefore(startDate)) {
                "endDate must not be before startDate"
            }
        }
    }

    fun isActive(at: LocalDate = LocalDate.now()): Boolean {
        return !at.isBefore(startDate) && (endDate == null || !at.isAfter(endDate))
    }
}
