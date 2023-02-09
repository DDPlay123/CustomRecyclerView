package com.side.project.customrecyclerview.customView

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * Create by 光廷 on 2023/02/07
 * 功能：XRecyclerView Adapter，主要用於調整 Header 和 Footer Item。
 * 來源：https://github.com/limxing/LFRecyclerView-Android
 */
class XRecyclerViewAdapter(private val adapter: RecyclerView.Adapter<ViewHolder>) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        const val ITEM_TYPE_HEADER = 0
        const val ITEM_TYPE_CONTENT = 1
        const val ITEM_TYPE_BOTTOM = 2
    }

    var mHeaderCount: Int = 1
    var mBottomCount: Int = 1

    private var isLoadMore = true
    private var isRefresh = true
    private var itemListener: XRecyclerView.OnItemClickListener? = null

    var itemHeight: Int = 0

    private lateinit var recyclerViewHeader: XRecyclerViewHeader
    private lateinit var recyclerViewFooter: XRecyclerViewFooter

    fun setOnItemClickListener(itemListener: XRecyclerView.OnItemClickListener) {
        this.itemListener = itemListener
    }

    fun setRecyclerViewHeader(recyclerViewHeader: XRecyclerViewHeader) {
        this.recyclerViewHeader = recyclerViewHeader
    }

    fun setRecyclerViewFooter(recyclerViewFooter: XRecyclerViewFooter) {
        this.recyclerViewFooter = recyclerViewFooter
    }

    fun setRefresh(refresh: Boolean) {
        isRefresh = refresh
        mHeaderCount = if (refresh) 1 else 0
    }

    fun setLoadMore(loadMore: Boolean) {
        isLoadMore = loadMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ITEM_TYPE_HEADER -> HeaderBottomHolder(recyclerViewHeader)
            ITEM_TYPE_CONTENT -> adapter.onCreateViewHolder(parent, viewType)
            ITEM_TYPE_BOTTOM -> HeaderBottomHolder(recyclerViewFooter)
            else -> adapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemCount(): Int {
        var count = adapter.itemCount
        count += mHeaderCount + mBottomCount // 表Header和Footer
        return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isHeaderView(position) || isBottomView(position)) return

        val po: Int = position - mHeaderCount // 扣掉Header
        adapter.onBindViewHolder(holder, po)

        if (itemHeight == 0)
            itemHeight = holder.itemView.height

        itemListener?.let { listener ->
            holder.itemView.setOnClickListener { listener.onClick(po) }
            holder.itemView.setOnLongClickListener {
                listener.onLongClick(po)
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isHeaderView(position) && isRefresh)
            ITEM_TYPE_HEADER
        else if (isBottomView(position))
            ITEM_TYPE_BOTTOM
        else
            ITEM_TYPE_CONTENT
    }

    // 判斷當前item是否是HeadView
    private fun isHeaderView(position: Int): Boolean = mHeaderCount != 0 && position < mBottomCount

    // 判斷當前item是否是FooterView
    private fun isBottomView(position: Int): Boolean = mBottomCount != 0 && position >= (mHeaderCount + adapter.itemCount)

    inner class HeaderBottomHolder(itemView: View) : ViewHolder(itemView)
}