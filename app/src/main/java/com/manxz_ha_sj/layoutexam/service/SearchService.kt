package com.manxz_ha_sj.layoutexam.service
import java.io.Serializable

data class StockPriceResponse(
    val response: StockPriceOuter
)

data class StockPriceOuter(
    val body: StockPriceBody
)

data class StockPriceBody(
    val items: StockPriceItems,
    val numOfRows: Int,   // 한 페이지당 아이템 수
    val totalCount: Int   // 전체 아이템 수
)

data class StockPriceItems(
    val item: List<StockItem>
)


data class StockItem(
    val basDt: String,     // 기준일자
    val srtnCd: String,   // 종목코드
    val itmsNm: String,   // 종목명
    val mrktCtg: String,  // 시장구분
    val clpr: String,     // 종가
    val vs: String,         // 전일 대비
    val fltRt: String,    // 전일 대비율
    val mkp: String,      // 시가총액
    val hipr: String,   // 고가
    val lopr: String,   // 저가
    val trqu: String,   // 거래량
    val trPrc: String,   // 거래대금
    val istgStCnt: String, // 상장주식수
    //val iscd: String,     // ISIN 코드
): Serializable