package com.example.inventoryhouse.ui.screen.scanner

internal data class ParsedReceiptItem(
    val name: String,
    val packageFormat: String,
    val count: Int
)

internal object ReceiptTextParser {
    private val ignoredKeywords = listOf(
        "total",
        "sous total",
        "tva",
        "carte",
        "banque",
        "cb",
        "ticket",
        "caisse",
        "merci",
        "rendu",
        "monnaie",
        "date",
        "heure",
        "numero",
        "siret",
        "article",
        "prix",
        "euro",
        "solde",
        "fidelite"
    )

    private val priceAtEndRegex = Regex("(?i)\\s*-?\\d+[,.]\\d{2}\\s*(?:eur|euro|euros|€)?\\s*$")
    private val prefixCountRegex = Regex("(?i)^\\s*(?:(\\d+)\\s?[x*]\\s+|x\\s?(\\d+)\\s+)")
    private val suffixCountRegex = Regex("(?i)(?:\\s+[x*]\\s?(\\d+)|\\s+(\\d+)\\s?x)\\s*$")
    private val packageRegex = Regex(
        "(?i)\\b\\d+(?:[,.]\\d+)?\\s?(?:kg|g|gr|gramme|grammes|l|litre|litres|cl|ml|dl|u|unite|unites|piece|pieces|pcs?)\\b"
    )
    private val leadingCodeRegex = Regex("^\\d{3,}\\s+")
    private val repeatedSpacesRegex = Regex("\\s{2,}")

    fun parse(rawText: String): List<ParsedReceiptItem> {
        val merged = linkedMapOf<String, ParsedReceiptItem>()

        rawText
            .lineSequence()
            .mapNotNull(::parseLine)
            .forEach { item ->
                val key = "${item.name.lowercase()}|${item.packageFormat.lowercase()}"
                val current = merged[key]
                merged[key] = if (current == null) {
                    item
                } else {
                    current.copy(count = current.count + item.count)
                }
            }

        return merged.values.toList()
    }

    private fun parseLine(rawLine: String): ParsedReceiptItem? {
        var line = rawLine.trim()
        if (line.length < 3 || line.none { it.isLetter() }) return null

        val searchable = line.lowercase()
        if (ignoredKeywords.any { searchable.contains(it) }) return null

        val hadTrailingPrice = priceAtEndRegex.containsMatchIn(line)
        line = line
            .replace('€', ' ')
            .replace(leadingCodeRegex, "")
            .replace(priceAtEndRegex, "")
            .normalizeSpaces()

        if (line.length < 3 || line.none { it.isLetter() }) return null

        val prefixCount = prefixCountRegex.find(line)
        var count = prefixCount?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }?.toIntOrNull()
            ?: 1
        if (prefixCount != null) {
            line = line.removeRange(prefixCount.range).normalizeSpaces()
        }

        val suffixCount = suffixCountRegex.find(line)
        val suffixValue = suffixCount?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }?.toIntOrNull()
        if (suffixValue != null) {
            count = suffixValue
            line = line.removeRange(suffixCount.range).normalizeSpaces()
        }

        val packageMatch = packageRegex.findAll(line).lastOrNull()
        val packageFormat = packageMatch?.value?.toPackageFormat() ?: "piece"
        if (packageMatch != null) {
            line = line.removeRange(packageMatch.range).normalizeSpaces()
        }

        val hasQuantityMarker = prefixCount != null || suffixValue != null
        if (!hadTrailingPrice && !hasQuantityMarker && packageMatch == null) return null

        val name = line
            .trim(' ', '-', '*', '.', ',', ':', ';')
            .normalizeSpaces()
            .toDisplayName()

        if (name.length < 2 || name.count { it.isLetter() } < 2) return null

        return ParsedReceiptItem(
            name = name,
            packageFormat = packageFormat,
            count = count.coerceAtLeast(1)
        )
    }

    private fun String.normalizeSpaces(): String = replace(repeatedSpacesRegex, " ").trim()

    private fun String.toPackageFormat(): String {
        val compact = lowercase()
            .replace(",", ".")
            .replace(" ", "")
            .replace("grammes", "g")
            .replace("gramme", "g")
            .replace("gr", "g")
            .replace("litres", "l")
            .replace("litre", "l")
            .replace("unites", "u")
            .replace("unite", "u")
            .replace("pieces", "piece")
            .replace("pcs", "piece")
            .replace("pc", "piece")

        return compact.ifBlank { "piece" }
    }

    private fun String.toDisplayName(): String {
        return lowercase()
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            }
    }
}
