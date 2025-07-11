package com.manxz_ha_sj.layoutexam.service

import com.manxz_ha_sj.layoutexam.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class SearchManager {
    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    private val client = OkHttpClient()
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
     * 공공데이터포털을 통해 전체 주식 종목명과 시장 정보를 가져오는 함수
     * - 오늘 날짜부터 시작해서 최대 14일 전까지 거슬러 가며 데이터를 찾음
     * - 데이터가 있는 가장 최근 날짜의 데이터를 기준으로 목록을 가져옴
     * - 코루틴 기반으로 I/O 작업을 안전하게 처리함
     */
    suspend fun getStockNames(): List<StockItem> = withContext(Dispatchers.IO) {
        // 오늘 날짜를 기준으로 시작
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1) // 하루 전 날짜 설정

        // 결과를 저장할 Set (중복 제거용)
        val stockItems = mutableSetOf<StockItem>()

        var found = false // 데이터를 찾았는지 여부
        var tries = 0     // 시도 횟수 (최대 14번)
        var totalPages = 3  // 1000개씩 3페이지 = 3000개 (필요시 더 늘릴 수 있음)

        // 데이터가 없으면 하루씩 줄이며 최대 14일간 반복
        while (!found && tries < 14) {
            val basDt = simpleDateFormat.format(calendar.time) // 기준일자 (yyyyMMdd 형식)

            try {
                // 각 페이지에 대해 반복 요청
                for (page in 1..totalPages) {
                    val url = buildUrl(basDt, page) // URL 생성
                    val request = Request.Builder().url(url).build() // HTTP 요청 생성
                    val response = client.newCall(request).execute() // 동기 네트워크 요청 실행

                    val body = response.body?.string() // 응답 본문 문자열로 추출
                    if (!body.isNullOrEmpty()) {
                        // JSON 응답 파싱
                        val result = Gson().fromJson(body, StockPriceResponse::class.java)
                        val items = result.response.body.items.item

                        totalPages = result.response.body.totalCount / numOfRows + 1 // 전체 페이지 수 재계산

                        // 실제 종목 데이터가 존재하면
                        if (!items.isNullOrEmpty()) {
                            // StockItem 객체를 Set에 추가
                            items.forEach {
                                stockItems.add(StockItem(
                                    it.basDt ?: "",
                                    it.srtnCd ?: "",
                                    it.itmsNm ?: "",
                                    it.mrktCtg ?: "",
                                    it.clpr ?: "",
                                    it.vs ?: "",
                                    it.fltRt ?: "",
                                    it.mkp ?: "",
                                    it.hipr ?: "",
                                    it.lopr ?: "",
                                    it.trqu ?: "",
                                    it.trPrc ?: "",
//                                    it.iscd ?: "",
                                    it.istgStCnt ?: ""))
                            }
                            found = true // 데이터 찾음 → 루프 종료
                        }
                    }
                }
            } catch (_: Exception) {
                // 네트워크 오류, JSON 파싱 오류 등 무시하고 다음 날짜 시도
            }

            if (!found) {
                // 하루 전으로 이동하여 재시도
                calendar.add(Calendar.DATE, -1)
                tries++
            }
        }

        // 종목명을 기준으로 정렬 후 반환
        return@withContext stockItems.sortedBy { it.itmsNm }
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