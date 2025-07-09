package com.manxz_ha_sj.layoutexam.service
import java.io.Serializable

data class StockPriceResponse(
    val response: StockPriceOuter
)

data class StockPriceOuter(
    val body: StockPriceBody
)

data class StockPriceBody(
    val items: StockPriceItems
)

data class StockPriceItems(
    val item: List<StockItem>
)

data class StockItem(
    val basDt: String,     // 기준일자
    val itmsNm: String,    // 종목명
    val mrktCtg: String,   // 시장구분 (KOSDAQ 등)
    val clpr: String,      // 종가
    val vs: String         // 전일 대비
): Serializable