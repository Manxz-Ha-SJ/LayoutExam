// 패키지 경로를 파일 위치에 맞게 수정
package com.manxz_ha_sj.layoutexam.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearSnapHelper
import com.manxz_ha_sj.layoutexam.databinding.FragmentStockBinding
import com.manxz_ha_sj.layoutexam.ui.search.ResultStockListAdapter
import com.manxz_ha_sj.layoutexam.ui.search.StockNameSearchListAdapter
import com.manxz_ha_sj.layoutexam.service.SearchManager

class StockFragment : Fragment() {
    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!
    /** @brief 종목명 검색 결과를 표시할 RecyclerView 어댑터 */
    private lateinit var stockNameSearchListAdapter: StockNameSearchListAdapter
    /** @brief 선택한 종목 표시할 RecyclerView 어댑터 */
    private lateinit var resultStockListAdapter: ResultStockListAdapter
    /**
     * @brief 선택된 종목명을 저장할 리스트
     * @details 종목명과 시장구분 정보를 Pair로 저장합니다.
     * Pair<String, String> 형태로 변경하여 종목명과 시장구분을 함께 저장합니다.
     */
    // 선택된 종목명을 저장할 리스트 (Pair<String, String>으로 변경)
    private val selectedStockNames = mutableListOf<Pair<String, String>>()

    // nameMarketPairs 멤버 변수 추가
    private var nameMarketPairs: List<Pair<String, String>> = emptyList()
    /** @brief 검색을 위한 SearchManager 인스턴스 */
    private val searchManager = SearchManager.getInstance()

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
    ): View {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Fragment의 뷰가 생성된 후 호출되는 메소드
     * 여기서 UI 요소에 대한 초기화 및 이벤트 리스너 설정을 수행합니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultStockListAdapter = ResultStockListAdapter()
        binding.rvResult.adapter = resultStockListAdapter
        binding.rvResult.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // 1. 어댑터 먼저 초기화
        stockNameSearchListAdapter = StockNameSearchListAdapter { selectedName ->
            // 선택한 종목명을 리스트에 추가 (시장구분 정보도 함께 추가)
            val selectedPair = nameMarketPairs.find { it.first == selectedName } ?: (selectedName to "")
            selectedStockNames.add(selectedPair)
            resultStockListAdapter.submitList(selectedStockNames.toList())
            binding.etStockName.setText("")
            showSearchStockNames(false)
        }
        // 2. RecyclerView에 어댑터와 레이아웃매니저 연결
        binding.rvSearchStockNames.adapter = stockNameSearchListAdapter
        binding.rvSearchStockNames.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // 처음에는 RecyclerView를 숨김
        showSearchStockNames(false)

        searchManager.getStockNames { nameMarketPairs ->
            activity?.runOnUiThread {
                if (isAdded) {
                    this.nameMarketPairs = nameMarketPairs
                }
            } }

        binding.etStockName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                if (keyword.isNotEmpty()) {
                    val filtered = nameMarketPairs.filter { searchManager.isKoreanPrefixMatch(it.first, keyword) }
                    stockNameSearchListAdapter.submitList(filtered)
                    showSearchStockNames(true)
                } else {
                    stockNameSearchListAdapter.submitList(emptyList())
                    showSearchStockNames(false)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Fragment의 뷰가 파괴될 때 호출되는 메소드
     * ViewBinding 객체를 null로 설정하여 메모리 누수 방지
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
     * 검색 종목명을 표시할 RecyclerView의 가시성을 설정하는 메소드
     * @param show true면 RecyclerView를 보이게, false면 숨김
     */

    private fun showSearchStockNames(show: Boolean) {
        if (show) {
            binding.rvSearchStockNames.visibility = View.VISIBLE
        } else {
            binding.rvSearchStockNames.visibility = View.GONE
        }
    }
}