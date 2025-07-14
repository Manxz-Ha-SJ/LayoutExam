package com.manxz_ha_sj.layoutexam.ui.menu

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.manxz_ha_sj.layoutexam.fragment.HomeFragment
import com.manxz_ha_sj.layoutexam.fragment.StockFragment
import com.manxz_ha_sj.layoutexam.fragment.SavingFragment
import com.manxz_ha_sj.layoutexam.fragment.LoanFragment


class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 4 // 탭 개수
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> StockFragment()
            2 -> SavingFragment()
            3 -> LoanFragment()
            else -> HomeFragment()
        }
    }
}