package com.manxz_ha_sj.layoutexam.service

import com.manxz_ha_sj.layoutexam.BuildConfig
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SearchManager {
    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    private val client = OkHttpClient()
    private val totalPages = 4  // 1000개씩 4페이지 = 4000개 (필요시 더 늘릴 수 있음)
    private val numOfRows = 1000

    companion object {
        private var instance: SearchManager? = null
        /**
         * @brief SearchManager 싱글톤 인스턴스를 반환합니다.
         * @return SearchManager 인스턴스
         */
        fun getInstance(): SearchManager =
            instance ?: synchronized(this) {
                instance ?: SearchManager().also { instance = it }
            }
    }

    /**
     * KRX API URL 생성 함수
     */
    fun buildUrl(basDt: String, page: Int): String {
        val key = BuildConfig.KRX_API_KEY
        return "https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo" +
                "?serviceKey=$key&numOfRows=1000&pageNo=$page&resultType=json&basDt=$basDt"
    }

    /**
     * 종목명 리스트를 API에서 받아오는 함수
     * @param onResult (종목명, 시장구분) 쌍의 리스트를 반환
     */
    fun getStockNames(
        onResult: (List<Pair<String, String>>) -> Unit
    ) {
        val calendar = Calendar.getInstance().apply {
            while (get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                add(Calendar.DATE, -1)
            }
        }
        val basDt = simpleDateFormat.format(calendar.time)
        val nameMarketPairs = mutableSetOf<Pair<String, String>>()
        Thread {
            try {
                for (page in 1..totalPages) {
                    val url = buildUrl(basDt, page)
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
            onResult(nameMarketPairs.sortedBy { it.first })
        }.start()
    }

    // 초성 추출 함수
    fun getInitialSound(str: String): String {
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

    // 한글 초성(자음)인지 판별
    fun isKoreanInitial(ch: Char): Boolean {
        return ch in 'ㄱ'..'ㅎ'
    }

    // 혼합/초성/완성형/조합형 모두 지원, 맨 앞에서부터만 일치
    fun isKoreanPrefixMatch(target: String, keyword: String): Boolean {
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
}