package com.manxz_ha_sj.layoutexam.service

import com.manxz_ha_sj.layoutexam.BuildConfig

class SearchManager {
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
}