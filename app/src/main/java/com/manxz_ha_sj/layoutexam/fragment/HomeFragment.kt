package com.manxz_ha_sj.layoutexam.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.manxz_ha_sj.layoutexam.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    /**
     * Fragment의 뷰를 생성하는 메소드
     * ViewBinding을 사용하여 레이아웃을 inflate합니다.
     * @param inflater LayoutInflater 객체
     * @param container Fragment가 부착될 ViewGroup
     * @param savedInstanceState 이전 상태 정보
     * @return Fragment의 뷰
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
}