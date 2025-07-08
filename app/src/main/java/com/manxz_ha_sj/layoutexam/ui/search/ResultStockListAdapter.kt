package com.manxz_ha_sj.layoutexam.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.manxz_ha_sj.layoutexam.databinding.ItemStockInfoBinding

class ResultStockListAdapter : ListAdapter<Pair<String, String>, ResultStockListAdapter.ViewHolder>(DIFF) {
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Pair<String, String>>() {
            override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>) = oldItem == newItem
        }
    }

    inner class ViewHolder(val binding: ItemStockInfoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, type) = getItem(position)
        holder.binding.tvStockName.text = name
        holder.binding.tvStockType.text = type
    }

    // submitList를 호출할 때 List<Pair<String, String>> 타입으로 전달해야 함
    // 예시: resultStockListAdapter.submitList(listOf(Pair("삼성전자", "코스피"), Pair("카카오", "코스닥")))
    // 만약 기존에 String 리스트를 사용했다면, Pair로 변환해서 전달해야 함
}