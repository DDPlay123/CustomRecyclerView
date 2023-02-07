package com.side.project.customrecyclerview.customView

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class XRecyclerViewAdapter(val context: Context, val adapter: RecyclerView.Adapter<ViewHolder>) : RecyclerView.Adapter<ViewHolder>() {

    private var isLoadMore = true
    private var isRefresh = true
    private lateinit var itemListener: OnItemClickListener

    var itemHeight: Int = 0

    private lateinit var recyclerViewHeader: XRecyclerViewHeader
    private lateinit var recyclerViewFooter: XRecyclerViewFooter

    fun setOnItemClickListener(itemListener: OnItemClickListener) {
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
    }

    fun setLoadMore(loadMore: Boolean) {
        isLoadMore = loadMore
    }

    fun getHFCount() = 2 // 表Header和Footer

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ItemType.ITEM_TYPE_HEADER.ordinal -> HeaderBottomHolder(recyclerViewHeader)
            ItemType.ITEM_TYPE_CONTENT.ordinal -> adapter.onCreateViewHolder(parent, viewType)
            ItemType.ITEM_TYPE_BOTTOM.ordinal -> HeaderBottomHolder(recyclerViewFooter)
            else -> adapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemCount(): Int {
        var count = adapter.itemCount
        count += 2 // 表Header和Footer
        return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isHeaderView(position) || isBottomView(position)) return

        val po: Int = position - 1 // 扣掉Header
        adapter.onBindViewHolder(holder, po)

        if (itemHeight == 0)
            itemHeight = holder.itemView.height

        if (::itemListener.isInitialized) {
            holder.itemView.setOnClickListener { itemListener.onClick(po) }
            holder.itemView.setOnLongClickListener {
                itemListener.onLongClick(po)
                true
            }
        }
    }

    // 判斷當前item是否是HeadView
    fun isHeaderView(position: Int): Boolean = position < 1

    // 判斷當前item是否是FooterView
    fun isBottomView(position: Int): Boolean = position >= adapter.itemCount + 1

    inner class HeaderBottomHolder(itemView: View) : ViewHolder(itemView)
}