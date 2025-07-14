package com.manxz_ha_sj.layoutexam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.manxz_ha_sj.layoutexam.databinding.ActivityMainBinding
import com.manxz_ha_sj.layoutexam.ui.menu.MainPagerAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 적용
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WindowInsets 설정
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 탭 제목 추가
//        val tabTitles = listOf("홈", "주식 종목 관리", "투자", "환율", "생활")
//        tabTitles.forEach {
//            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it))
//        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager.adapter = MainPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "홈"
                1 -> "종목 관리"
                2 -> "투자"
                3 -> "생활"
                else -> ""
            }
        }.attach()
    }
}
