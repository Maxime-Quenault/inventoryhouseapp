package com.example.inventoryhouse.ui.screen.scanner

import org.junit.Assert.assertEquals
import org.junit.Test

class ReceiptTextParserTest {
    @Test
    fun parse_extractsPackageFormatAndCount() {
        val result = ReceiptTextParser.parse(
            """
            MAGASIN TEST
            2x FARINE BLE T55 500G 2,40
            LAIT ENTIER 1L x3 4,50
            TOTAL 6,90
            """.trimIndent()
        )

        assertEquals(2, result.size)
        assertEquals(ParsedReceiptItem("Farine Ble T55", "500g", 2), result[0])
        assertEquals(ParsedReceiptItem("Lait Entier", "1l", 3), result[1])
    }

    @Test
    fun parse_mergesDuplicateProducts() {
        val result = ReceiptTextParser.parse(
            """
            PATES 500G 1,20
            PATES 500G 1,20
            """.trimIndent()
        )

        assertEquals(listOf(ParsedReceiptItem("Pates", "500g", 2)), result)
    }
}
