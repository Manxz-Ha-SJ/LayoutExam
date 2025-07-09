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

        val name = stockItem?.itmsNm ?: "종목명 없음"
    }
}