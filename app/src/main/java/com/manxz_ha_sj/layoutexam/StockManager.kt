package com.manxz_ha_sj.layoutexam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.manxz_ha_sj.layoutexam.databinding.StockManagerBinding
import com.manxz_ha_sj.layoutexam.service.StockItem

class StockManager : AppCompatActivity() {
    private lateinit var binding: StockManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding 적용
        binding = StockManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnBack = binding.btnBack
        btnBack.setOnClickListener {
            finish()
        }

        val stockItem = intent.getSerializableExtra("stockItem") as? StockItem

        // stockItem이 null인 경우를 대비하여 기본값 설정
        val stockName = stockItem?.itmsNm ?: "종목명 없음"
        val stockBaseDate = stockItem?.basDt ?: "기준일자 없음"

        val stockCode = stockItem?.srtnCd ?: "종목코드 없음"
        val stockMarketCategory = stockItem?.mrktCtg ?: "시장구분 없음"

        val stockMarketPrice = stockItem?.mkp ?: "시가총액 없음"
        val stockClosingPrice = stockItem?.clpr ?: "종가 없음"

        val stockChangePrice = stockItem?.vs ?: "전일 대비 없음"
        val stockChangePriceRate = stockItem?.fltRt ?: "전일 대비율 없음"

        val stockHighPrice = stockItem?.hipr ?: "고가 없음"
        val stockLowPrice = stockItem?.lopr ?: "저가 없음"

        val stockTradingQuantity = stockItem?.trqu ?: "거래량 없음"
        val stockTradingPrice = stockItem?.trPrc ?: "거래대금 없음"
//        val stockCount = stockItem?.istgStCnt ?: "상장주식수 없음"
//        val stockISINCode = stockItem?.iscd ?: "ISIN 코드 없음"


        binding.title.setText(stockName)
        binding.tvStockBaseDate.setText(stockBaseDate)

        binding.tvStockCode.setText(stockCode)
        binding.tvStockMarketCategory.setText(stockMarketCategory)

        binding.tvStockMarketPrice.setText(stockMarketPrice)
        binding.tvStockClosePrice.setText(stockClosingPrice)

        binding.tvStockChangePrice.setText(stockChangePrice)
        binding.tvStockChangePriceRate.setText(stockChangePriceRate)

        binding.tvStockHighPrice.setText(stockHighPrice)
        binding.tvStockLowPrice.setText(stockLowPrice)

        binding.tvStockTradingQuantity.setText(stockTradingQuantity)
        val formattedPrice = formatToKorCurrency(stockTradingPrice)
        binding.tvStockTradingPrice.setText(formattedPrice)



    }


    fun formatToKorCurrency(priceStr: String): String {
        return try {
            val price = priceStr.replace(",", "").toLong()

            val jo = price / 1_0000_0000_0000     // 조
            val eok = (price % 1_0000_0000_0000) / 100_000_000   // 억
            val man = (price % 100_000_000) / 10_000             // 만

            val builder = StringBuilder()
            if (jo > 0) builder.append("${jo}조 ")
            if (eok > 0) builder.append("${eok}억 ")
            if (man > 0) builder.append("${man}만")

            if (builder.isEmpty()) "0" else builder.toString().trim()
        } catch (e: Exception) {
            "금액 오류"
        }
    }
}