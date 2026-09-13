package com.korku.game3d

/**
 * Oyuncunun kararlarının merkezi günlüğü (bkz. tasarım notu madde 21).
 *
 * Şu ana kadar `trust`, `liedAboutBasement` gibi bayraklar Mert'in kendi
 * içinde dağınık şekilde tutuluyordu. Bu sınıf onları tek bir yerde,
 * ne zaman olduklarıyla birlikte kaydeder; böylece hem "bu oyunda neler
 * yaptım" özetini kurmak hem de finalde/ileriki sahnelerde birden fazla
 * karara birden bakmak kolaylaşır.
 *
 * Bayraklar sadece bir kez kaydedilir (aynısını tekrar kaydetmek no-op'tur),
 * ama `record` her çağrıldığında oyun saatini güncel tutar — istenirse
 * "en son ne zaman oldu" bilgisi de okunabilir.
 */
enum class ChoiceFlag(val label: String) {
    TOLD_TRUTH_ABOUT_BASEMENT("Mert'e bodrumu hatırladığını söyledi"),
    LIED_ABOUT_BASEMENT("Mert'e bodrumu unuttuğunu söyledi (yalan)"),
    FORGOT_A_MEMORY("Takipçi yakaladı; bir anı bulanıklaştı"),
    TRUSTED_MERT_AT_DOOR("Ön kapıda Mert'e güveniyordu"),
    LEFT_MERT_BEHIND("Ön kapıda Mert geride kaldı"),
    SURVIVED_PAST_0333("Saat 03:33'ü aşıp hayatta kaldı")
}

class ChoiceHistory {

    private val entries: MutableMap<ChoiceFlag, Float> = mutableMapOf()

    /** [gameMinutes] o anki oyun-içi saat; sadece ilk kayıtta saklanır. */
    fun record(flag: ChoiceFlag, gameMinutes: Float) {
        entries.putIfAbsent(flag, gameMinutes)
    }

    fun has(flag: ChoiceFlag): Boolean = entries.containsKey(flag)

    fun timeOf(flag: ChoiceFlag): Float? = entries[flag]

    /** Finalde/epilogda gösterilecek kısa, zaman sıralı özet. */
    fun summaryLines(): List<String> =
        entries.entries
            .sortedBy { it.value }
            .map { (flag, minutes) -> "• ${clockLabelFor(minutes)} — ${flag.label}" }

    private fun clockLabelFor(gameMinutes: Float): String {
        val totalMinutes = gameMinutes.toInt() % (24 * 60)
        return "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)
    }

    fun reset() {
        entries.clear()
    }
}
