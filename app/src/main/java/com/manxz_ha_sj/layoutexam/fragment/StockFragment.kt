package com.manxz_ha_sj.layoutexam

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.manxz_ha_sj.layoutexam.R
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import com.manxz_ha_sj.layoutexam.service.StockPriceResponse
import com.manxz_ha_sj.layoutexam.BuildConfig // BuildConfig에서 ServiceKey를 가져옵니다.
import com.manxz_ha_sj.layoutexam.databinding.FragmentStockBinding
import com.manxz_ha_sj.layoutexam.ui.search.StockNameListAdapter

class StockFragment : Fragment() {
    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    // 전체 종목명 리스트 (예시, 실제로는 API나 로컬 파일에서 불러와야 함)
    private val allStockNames = listOf("하이트진로", "하림지주", "한국사료", "삼성전자", "LG화학", "현대차")

    private lateinit var adapter: StockNameListAdapter

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
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Fragment의 뷰가 생성된 후 호출되는 메소드
     * 여기서 UI 요소에 대한 초기화 및 이벤트 리스너 설정을 수행합니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 및 Adapter 설정
        adapter = StockNameListAdapter()
        binding.rvStockNames.adapter = adapter


        // EditText 입력 시 실시간 필터링
        binding.etStockName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                val filtered = allStockNames.filter { it.contains(keyword) }
                adapter.submitList(filtered)
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
                binding.tvResult.text = resultText
            }
        }.start()
    }
}