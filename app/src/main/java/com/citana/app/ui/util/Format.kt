package com.citana.app.ui.util

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

private val UTC = ZoneId.of("UTC")
private val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
private val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

fun formatMoney(cents: Long, currency: String = "USD"): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.US)
    runCatching { nf.currency = Currency.getInstance(currency) }
    nf.maximumFractionDigits = 0
    return nf.format(cents / 100.0)
}

fun formatDate(iso: String): String =
    runCatching { Instant.parse(iso).atZone(UTC).format(dateFmt) }.getOrDefault(iso)

fun formatTime(iso: String): String =
    runCatching { Instant.parse(iso).atZone(UTC).format(timeFmt) }.getOrDefault(iso)

fun formatDateTime(iso: String): String = "${formatDate(iso)} · ${formatTime(iso)}"

/** "09:00" (24h) -> "9:00 AM" for slot chips. */
fun formatSlot(hhmm: String): String = runCatching {
    val (h, m) = hhmm.split(":").map { it.toInt() }
    val ampm = if (h < 12) "AM" else "PM"
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    "%d:%02d %s".format(h12, m, ampm)
}.getOrDefault(hhmm)
