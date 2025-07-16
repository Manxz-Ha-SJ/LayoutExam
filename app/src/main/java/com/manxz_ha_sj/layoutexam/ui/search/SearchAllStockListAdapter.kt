package com.manxz_ha_sj.layoutexam.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.manxz_ha_sj.layoutexam.databinding.ItemSearchStockBinding
import com.manxz_ha_sj.layoutexam.service.StockItem

/**
 * @class StockNameSearchListAdapter
 * @brief 주식 종목명 검색 결과를 표시하는 RecyclerView 어댑터
 *
 * @details
 * ListAdapter를 상속하여 효율적으로 데이터 변경을 처리하며,
 * 주식 종목명과 시장구분 정보를 포함한 Pair<String, String> 리스트를 RecyclerView에 바인딩한다.
 * 아이템 클릭 시 onItemClick 콜백이 호출된다.
 *
 * @param onItemClick 아이템 클릭 시 호출되는 콜백 (선택적)
 */
class SearchAllStockListAdapter(
    private val onItemClick: ((StockItem) -> Unit)? = null
) : ListAdapter<StockItem, SearchAllStockListAdapter.ViewHolder>(DIFF) {
    companion object {
        /**
         * @var DIFF
         * @brief DiffUtil: 리스트의 변경사항을 효율적으로 계산하여 RecyclerView에 반영
         */
        val DIFF = object : DiffUtil.ItemCallback<StockItem>() {
            /**
             * @brief 아이템이 같은지 비교
             * @param oldItem 이전 아이템
             * @param newItem 새로운 아이템
             * @return 동일 여부
             */
            override fun areItemsTheSame(oldItem: StockItem, newItem: StockItem) = oldItem.itmsNm == newItem.itmsNm && oldItem.mrktCtg == newItem.mrktCtg
            /**
             * @brief 아이템의 내용이 같은지 비교
             * @param oldItem 이전 아이템
             * @param newItem 새로운 아이템
             * @return 동일 여부
             */
            override fun areContentsTheSame(oldItem: StockItem, newItem: StockItem) = oldItem == newItem
        }
    }
    /**
     * @class ViewHolder
     * @brief 각 아이템 뷰와 데이터 바인딩을 담당
     * @param binding 아이템 레이아웃 바인딩 객체
     */
    inner class ViewHolder(val binding: ItemSearchStockBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * @brief 아이템 뷰 생성 (ViewHolder 생성)
     * @param parent 부모 ViewGroup
     * @param viewType 뷰 타입
     * @return ViewHolder 객체
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    /**
     * @brief 아이템 뷰에 데이터 바인딩 및 클릭 리스너 설정
     * @param holder ViewHolder
     * @param position 아이템 위치
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvStockName.text = item.itmsNm
        holder.binding.tvStockType.text = item.mrktCtg
        // 추가 클릭 시 콜백 호출
        holder.binding.ivFavorite.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }
}