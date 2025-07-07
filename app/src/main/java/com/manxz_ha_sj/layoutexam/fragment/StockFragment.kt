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

class StockFragment : Fragment() {
    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    // 전체 종목명 리스트 (API로 받아올 예정)
    private var allStockNames: List<String> = emptyList()
    private lateinit var stockNameSearchListAdapter: StockNameSearchListAdapter

    // 선택된 종목명을 저장할 리스트
    private val selectedStockNames = mutableListOf<String>()
    private lateinit var resultStockListAdapter: ResultStockListAdapter

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
            // 선택한 종목명을 리스트에 추가
            selectedStockNames.add(selectedName)
            resultStockListAdapter.submitList(selectedStockNames.toList())
            binding.etStockName.setText("")
            showSearchStockNames(false)
            // binding.etStockName.setText("") // 입력창 초기화(선택사항)
        }
        // 2. RecyclerView에 어댑터와 레이아웃매니저 연결
        binding.rvSearchStockNames.adapter = stockNameSearchListAdapter
        binding.rvSearchStockNames.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // 처음에는 RecyclerView를 숨김
        showSearchStockNames(false)

        getStockNames { names ->
            allStockNames = names
            // adapter.submitList(allStockNames) // 초기에는 리스트를 보여주지 않음
        }

        binding.etStockName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                if (keyword.isNotEmpty()) {
                    val filtered = allStockNames.filter {
                        it.contains(keyword) || getInitialSound(it).contains(getInitialSound(keyword))
                    }
                    stockNameSearchListAdapter.submitList(filtered)
                    showSearchStockNames(true)
                    updateSearchListHeight(filtered.size)
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
            fetchLatestStockPrice(keyword)
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
     * 주식 가격 정보를 가져오는 메소드
     * @param keyword 검색할 주식 종목명
     */
    private fun fetchLatestStockPrice(keyword: String) {
        val serviceKey = BuildConfig.KRX_API_KEY
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val calendar = Calendar.getInstance()
        val client = OkHttpClient()

        // 최근 7일 이내의 데이터를 가져오기 위해 스레드 사용
        Thread {
            var resultText = "데이터가 없습니다."
            for (i in 0..10) { // 최근 7일 이내에서 검색
                val basDt = sdf.format(calendar.time)
                val url = "https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo" +
                        "?serviceKey=$serviceKey&numOfRows=1000&pageNo=1&resultType=json&basDt=$basDt"
                try {
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    if (body != null) {
                        val result = Gson().fromJson(body, StockPriceResponse::class.java)
                        val filtered = result.response.body.items.item
                            .filter { it.itmsNm.contains(keyword) }
                        val lastItem = filtered.lastOrNull()
                        if (lastItem != null) {
                            resultText = "[${lastItem.basDt}] ${lastItem.itmsNm} (${lastItem.mrktCtg})\n" +
                                    "종가: ${lastItem.clpr}원, 전일대비: ${lastItem.vs}원"
                            break
                        }
                    }
                } catch (_: Exception) { }
                calendar.add(Calendar.DATE, -1) // 하루 전으로 이동
            }
            requireActivity().runOnUiThread {
                // binding.tvResult.text = resultText // 기존 코드 주석 처리
                // 결과를 rvResult에 추가 (선택된 종목명 리스트에 추가)
                selectedStockNames.add(resultText)
                resultStockListAdapter.submitList(selectedStockNames.toList())
            }
        }.start()
    }

    /**
     * 종목명 리스트를 API에서 받아오는 함수
     */
    private fun getStockNames(onResult: (List<String>) -> Unit) {
        val serviceKey = BuildConfig.KRX_API_KEY
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val calendar = Calendar.getInstance()
        // 최근 수요일로 이동
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
            calendar.add(Calendar.DATE, -1)
        }
        val basDt = sdf.format(calendar.time)
        val client = OkHttpClient()
        val names = mutableSetOf<String>()
        val totalPages = 4 // 1000개씩 4페이지 = 4000개 (필요시 더 늘릴 수 있음)
        Thread {
            try {
                for (page in 1..totalPages) {
                    val url = "https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo" +
                            "?serviceKey=$serviceKey&numOfRows=1000&pageNo=$page&resultType=json&basDt=$basDt"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    if (body != null) {
                        val result = Gson().fromJson(body, StockPriceResponse::class.java)
                        result.response.body.items.item.forEach { names.add(it.itmsNm) }
                    }
                }
            } catch (_: Exception) { }
            if (isAdded) { // 프래그먼트가 아직 붙어있을 때만 실행
            requireActivity().runOnUiThread { onResult(names.sorted()) }
                }
        }.start()
    }

    // 한글 초성 추출 함수
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
                result.append(ch)
            }
        }
        return result.toString()
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