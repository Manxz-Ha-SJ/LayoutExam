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
        val stockBaseDate = stockItem?.basDt ?: "기준일자 없음"
        val stockName = stockItem?.itmsNm ?: "종목명 없음"
        val stockMarketCategory = stockItem?.mrktCtg ?: "시장구분 없음"
        val stockClosingPrice = stockItem?.clpr ?: "종가 없음"
        val stockChangePrice = stockItem?.vs ?: "전일 대비 없음"

        binding.title.setText(stockName)
        binding.tvStockBaseDate.setText(stockBaseDate)
        binding.tvStockClosePrice.setText(stockClosingPrice)
        binding.tvStockChangePrice.setText(stockChangePrice)

    }
}