package com.manxz_ha_sj.layoutexam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.manxz_ha_sj.layoutexam.databinding.ActivityMainBinding
import com.manxz_ha_sj.layoutexam.fragment.StockFragment
import com.manxz_ha_sj.layoutexam.fragment.SavingFragment
import com.manxz_ha_sj.layoutexam.fragment.HomeFragment
import com.manxz_ha_sj.layoutexam.fragment.LoanFragment

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

        // 첫 화면에 '홈' 프래그먼트 표시
        replaceFragment(HomeFragment())

        // 탭 선택 리스너
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val fragment = when (tab.position) {
                    0 -> HomeFragment()
                    1 -> StockFragment()
                    2 -> LoanFragment()
                    3 -> SavingFragment()
                    else -> HomeFragment()
                }
                replaceFragment(fragment)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
