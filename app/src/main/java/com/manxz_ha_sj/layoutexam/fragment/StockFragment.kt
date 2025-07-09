// 패키지 경로를 파일 위치에 맞게 수정
package com.manxz_ha_sj.layoutexam.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.manxz_ha_sj.layoutexam.StockManager
import com.manxz_ha_sj.layoutexam.databinding.FragmentStockBinding
import com.manxz_ha_sj.layoutexam.ui.search.HeldStockListAdapter
import com.manxz_ha_sj.layoutexam.ui.search.SearchAllStockListAdapter
import com.manxz_ha_sj.layoutexam.service.SearchManager
import com.manxz_ha_sj.layoutexam.service.StockItem
import kotlinx.coroutines.launch

class StockFragment : Fragment() {
    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    //1. 종목명 검색
    /** @brief 종목명 검색 결과를 표시할 RecyclerView 어댑터 */
    private lateinit var searchAllStockListAdapter: SearchAllStockListAdapter
    /** @brief 종목명과 시장구분 정보를 저장할 리스트 */
    private var searchAllStockList = mutableListOf<StockItem>()


    //2. 보유 종목 관리
    /** @brief 선택한 종목 표시할 RecyclerView 어댑터 */
    private lateinit var heldStockListAdapter: HeldStockListAdapter
    // 선택된 종목명을 저장할 리스트 (Pair<String, String>으로 변경)
    private val heldStocks  = mutableSetOf<StockItem>()


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


/////////////////////////종목 검색/////////////////////////
        // 처음에는 RecyclerView를 숨김
        showSearchStockNames(false)
        // 1. 종목명 검색
        searchAllStockListAdapter = SearchAllStockListAdapter { selectedItem ->
            // 선택한 종목명을 리스트에 추가 (시장구분 정보도 함께 추가)
            heldStocks.add(selectedItem)
            heldStockListAdapter.submitList(heldStocks.toList())
            showSearchStockNames(false)
        }
        // 2. RecyclerView에 어댑터와 레이아웃매니저 연결
        binding.rvSearchAllStockList.adapter = searchAllStockListAdapter
        binding.rvSearchAllStockList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // viewLifecycleOwner의 생명주기 범위에서 코루틴 실행 (Fragment가 살아있는 동안만 실행)
        viewLifecycleOwner.lifecycleScope.launch {
            // suspend 함수 호출: 주식 이름과 시장 정보를 가져옴 (백그라운드에서 실행됨)
            val nameMarketPairs = searchManager.getStockNames()

            // Fragment가 아직 UI에 붙어있는 상태일 때만 결과를 반영
            if (isAdded) {
                // 가져온 결과를 Fragment의 변수에 저장
                this@StockFragment.searchAllStockList = nameMarketPairs.toMutableList()
                // 어댑터에 Pair<String, String>으로 변환하여 전달
                searchAllStockListAdapter.submitList(nameMarketPairs.toList())
            }
        }

        binding.etStockName.setOnClickListener() {
            binding.etStockName.setText("")
        }
        // EditText에 TextWatcher를 추가하여 텍스트 변경 시 검색 결과 업데이
        binding.etStockName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                if (keyword.isNotEmpty()) {
                    val keywordTrimmed = keyword.trim()
                    val filtered = searchAllStockList.filter {
                        searchManager.isKoreanPrefixMatch(it.itmsNm.trim(), keywordTrimmed)
                    }
                    searchAllStockListAdapter.submitList(filtered.toList())
                    showSearchStockNames(true)
                } else {
                    searchAllStockListAdapter.submitList(emptyList())
                    showSearchStockNames(false)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        ////////////////////////종목 검색/////////////////////////



        /////////////////////////보유 종목 관리/////////////////////////
        // 2. 보유 종목 관리
        heldStockListAdapter = HeldStockListAdapter()
        binding.rvResult.adapter = heldStockListAdapter
        binding.rvResult.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())


        // 리스트 아이템 클릭 시 해당 위치로 이동
        heldStockListAdapter.setItemClickListener(object : HeldStockListAdapter.OnItemClickListener {
            override fun onClickListItem(v: View, position: Int) {
                val context = requireContext()
                val intent = Intent(context, StockManager::class.java)
                // 필요하다면 선택한 종목 정보도 putExtra로 전달 가능
                intent.putExtra("stockItem", heldStocks.toList()[position])
                startActivity(intent)
            }
            override fun onClickDeleteCheckBox(v: View, position: Int, isChecked: Boolean) {}
        })



        /////////////////////////보유 종목 관리/////////////////////////
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
            binding.rvSearchAllStockList.visibility = View.VISIBLE
        } else {
            binding.rvSearchAllStockList.visibility = View.GONE
        }
    }
}