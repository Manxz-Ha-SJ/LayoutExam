// 패키지 경로를 파일 위치에 맞게 수정
package com.manxz_ha_sj.layoutexam.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import com.manxz_ha_sj.layoutexam.BuildConfig
import com.manxz_ha_sj.layoutexam.service.StockPriceResponse
import com.manxz_ha_sj.layoutexam.databinding.FragmentStockBinding
import com.manxz_ha_sj.layoutexam.ui.search.ResultStockListAdapter
import com.manxz_ha_sj.layoutexam.ui.search.StockNameSearchListAdapter
import com.manxz_ha_sj.layoutexam.service.SearchManager

class StockFragment : Fragment() {
    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    private lateinit var stockNameSearchListAdapter: StockNameSearchListAdapter

    // 선택된 종목명을 저장할 리스트 (Pair<String, String>으로 변경)
    private val selectedStockNames = mutableListOf<Pair<String, String>>()
    private lateinit var resultStockListAdapter: ResultStockListAdapter

    // nameMarketPairs 멤버 변수 추가
    private var nameMarketPairs: List<Pair<String, String>> = emptyList()

    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    private val client = OkHttpClient()
    private val totalPages = 4  // 1000개씩 4페이지 = 4000개 (필요시 더 늘릴 수 있음)
    private val numOfRows = 1000

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

        getStockNames { nameMarketPairs ->
            // stockNameSearchListAdapter에서 사용할 수 있도록 nameMarketPairs를 저장
            this.nameMarketPairs = nameMarketPairs
        }

        binding.etStockName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                if (keyword.isNotEmpty()) {
                    val filtered = nameMarketPairs.filter { isKoreanPrefixMatch(it.first, keyword) }
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

        // 기본 날짜 = 어제
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

        // 버튼 클릭 시 데이터 요청
        binding.btnFetch.setOnClickListener {
            val keyword = binding.etStockName.text.toString().trim()
            // 종목명, 시장구분 정보가 있는 nameMarketPairs에서 해당 종목을 찾아 추가
            val selectedPair = nameMarketPairs.find { it.first == keyword } ?: (keyword to "")
            selectedStockNames.add(selectedPair)
            resultStockListAdapter.submitList(selectedStockNames.toList())
        }
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
     * 종목명 리스트를 API에서 받아오는 함수
     * @param onResult (종목명, 시장구분) 쌍의 리스트를 반환
     */
    private fun getStockNames(onResult: (List<Pair<String, String>>) -> Unit) {
        val serviceKey = BuildConfig.KRX_API_KEY
        val calendar = Calendar.getInstance().apply {
            // 최근 수요일 찾기
            while (get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                add(Calendar.DATE, -1)
            }
        }
        val basDt = simpleDateFormat.format(calendar.time)
        val nameMarketPairs = mutableSetOf<Pair<String, String>>()

        Thread {
            try {
                for (page in 1..totalPages) {
                    val url = searchManager.buildUrl(basDt, page)
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    if (body != null) {
                        val result = Gson().fromJson(body, StockPriceResponse::class.java)
                        result.response.body.items.item.forEach {
                            nameMarketPairs.add(it.itmsNm to it.mrktCtg)
                        }
                    }
                }
            } catch (_: Exception) { }
            if (isAdded) {
                requireActivity().runOnUiThread { onResult(nameMarketPairs.sortedBy { it.first }) }
            }
        }.start()
    }

    // 전체 문자열에서 초성 추출
    private fun getInitialSound(str: String): String {
        val initialConsonants = listOf(
            'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
        )
        val result = StringBuilder()
        for (ch in str) {
            if (ch in '\uAC00'..'\uD7A3') {
                val uniVal = ch - '\uAC00'
                val cho = uniVal / (21 * 28)
                result.append(initialConsonants[cho])
            } else {
                result.append(ch) // 한글이 아니면 그대로 추가
            }
        }
        return result.toString()
    }

    // 종목명에서 한글이 시작되는 위치 반환
    private fun getFirstHangulIndex(str: String): Int {
        for (i in str.indices) {
            if (str[i] in '\uAC00'..'\uD7A3') return i
        }
        return -1
    }

    // 한글 초성(자음)인지 판별하는 함수
    private fun isKoreanInitial(ch: Char): Boolean {
        return ch in 'ㄱ'..'ㅎ'
    }

    // 혼합/초성/완성형/조합형 모두 지원, 맨 앞에서부터만 일치
    private fun isKoreanPrefixMatch(target: String, keyword: String): Boolean {
        if (keyword.isEmpty() || target.length < keyword.length) return false
        for (i in keyword.indices) {
            val k = keyword[i]
            val t = target[i]
            if (isKoreanInitial(k)) {
                if (getInitialSound(t.toString())[0] != k) return false
            } else {
                if (t != k) return false
            }
        }
        return true
    }

    fun updateSearchListHeight(itemCount: Int, itemHeightDp: Int = 54, maxHeightDp: Int = 270) {
        val context = binding.rvSearchStockNames.context
        val density = context.resources.displayMetrics.density
        val desiredHeight = (itemCount * itemHeightDp * density).toInt()
        val maxHeightPx = (maxHeightDp * density).toInt()
        val finalHeight = desiredHeight.coerceAtMost(maxHeightPx)

        binding.rvSearchStockNames.layoutParams = binding.rvSearchStockNames.layoutParams.apply {
            height = finalHeight
        }
        binding.rvSearchStockNames.requestLayout()
    }

    private fun showSearchStockNames(show: Boolean) {
        if (show) {
            binding.rvSearchStockNames.visibility = View.VISIBLE
        } else {
            binding.rvSearchStockNames.visibility = View.GONE
        }
    }
}