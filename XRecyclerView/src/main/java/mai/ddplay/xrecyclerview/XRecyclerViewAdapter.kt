package mai.ddplay.xrecyclerview

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by 光廷 on 2023/02/07
 * 功能：XRecyclerView Adapter，主要用於調整 Header 和 Footer Item。
 * 來源：https://github.com/limxing/LFRecyclerView-Android
 */
class XRecyclerViewAdapter(private val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val ITEM_TYPE_HEADER = 1000
        const val ITEM_TYPE_BOTTOM = 1001
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ITEM_TYPE_HEADER -> HeaderBottomHolder(recyclerViewHeader)
            ITEM_TYPE_BOTTOM -> HeaderBottomHolder(recyclerViewFooter)
            else -> adapter.onCreateViewHolder(parent, viewType)
        }

    override fun getItemCount(): Int {
        var count = adapter.itemCount
        count += mHeaderCount + mBottomCount // 表Header和Footer
        return count
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isHeaderView(position) || isBottomView(position)) return

        val po: Int = position - mHeaderCount // 扣掉 Header
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

    override fun getItemViewType(position: Int): Int =
        if (isHeaderView(position) && isRefresh)
            ITEM_TYPE_HEADER
        else if (isBottomView(position))
            ITEM_TYPE_BOTTOM
        else
            adapter.getItemViewType(position)

    // 判斷當前 item 是否是 HeadView
    private fun isHeaderView(position: Int): Boolean = mHeaderCount != 0 && position < mBottomCount

    // 判斷當前 item 是否是 FooterView
    private fun isBottomView(position: Int): Boolean = mBottomCount != 0 && position >= (mHeaderCount + adapter.itemCount)

    inner class HeaderBottomHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}