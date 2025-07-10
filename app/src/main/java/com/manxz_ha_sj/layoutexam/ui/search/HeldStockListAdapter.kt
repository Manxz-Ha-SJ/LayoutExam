package com.manxz_ha_sj.layoutexam.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.manxz_ha_sj.layoutexam.databinding.ItemStocksHeldBinding
import com.manxz_ha_sj.layoutexam.service.StockItem

class HeldStockListAdapter : ListAdapter<StockItem, HeldStockListAdapter.ViewHolder>(DIFF) {
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<StockItem>() {
            override fun areItemsTheSame(oldItem: StockItem, newItem: StockItem) = oldItem.itmsNm == newItem.itmsNm && oldItem.mrktCtg == newItem.mrktCtg
            override fun areContentsTheSame(oldItem: StockItem, newItem: StockItem) = oldItem == newItem
        }
    }

    /// 아이템 클릭 리스너
    private lateinit var itemClickListener: OnItemClickListener /**< 아이템 클릭 리스너 */
    private var selectedPosition: Int = RecyclerView.NO_POSITION /**< 선택된 아이템 위치 */


    /**
     * @brief 리스트 아이템 및 각 버튼 클릭 이벤트 리스너 인터페이스
     * @details 리스트 아이템 클릭, 삭제 체크박스 클릭 이벤트를 정의합니다.
     */
    interface OnItemClickListener {
        /**
         * @brief 리스트 아이템 클릭 시 호출
         * @param v 클릭된 뷰
         * @param position 아이템 위치
         */
        fun onClickListItem(v: View, position: Int)

        /**
         * @brief 삭제 체크박스 클릭 시 호출
         * @param v 클릭된 뷰
         * @param position 아이템 위치
         * @param isChecked 체크 여부
         */
        fun onClickDeleteCheckBox(v: View, position: Int, isChecked: Boolean)
    }

    inner class ViewHolder(val binding: ItemStocksHeldBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStocksHeldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvStockName.text = item.itmsNm
        holder.binding.tvStockType.text = item.mrktCtg

        // 아이템 클릭 리스너 연결
        holder.binding.root.setOnClickListener {
            if (::itemClickListener.isInitialized) {
                itemClickListener.onClickListItem(it, holder.adapterPosition)
            }
        }
    }

    /**
     * @brief 아이템 클릭 리스너 등록
     * @param onItemClickListener 클릭 리스너
     */
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }



}