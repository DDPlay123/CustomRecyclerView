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
        const val ITEM_TYPE_CONTENT = 1001
        const val ITEM_TYPE_BOTTOM = 1002
        const val ITEM_TYPE_HEADER_VIEW = 1003
        const val ITEM_TYPE_FOOTER_VIEW = 1004
    }

    var mHeaderCount: Int = 1
    var mBottomCount: Int = 1

    private var isLoadMore = true
    private var isRefresh = true
    private var itemListener: XRecyclerView.OnItemClickListener? = null

    var itemHeight: Int = 0

    private lateinit var recyclerViewHeader: XRecyclerViewHeader
    private lateinit var recyclerViewFooter: XRecyclerViewFooter

    // HeadView / FooterView
    private var headerView: View? = null
    private var footerView: View? = null

    fun setHeaderView(headerView: View) {
        this.headerView = headerView
    }

    fun setFooterView(footerView: View) {
        this.footerView = footerView
    }

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
            ITEM_TYPE_CONTENT -> adapter.onCreateViewHolder(parent, viewType)
            ITEM_TYPE_BOTTOM -> HeaderBottomHolder(recyclerViewFooter)
            ITEM_TYPE_HEADER_VIEW -> headerView?.let { HeaderBottomHolder(it) } ?: adapter.onCreateViewHolder(parent, viewType)
            ITEM_TYPE_FOOTER_VIEW -> footerView?.let { HeaderBottomHolder(it) } ?: adapter.onCreateViewHolder(parent, viewType)
            else -> adapter.onCreateViewHolder(parent, viewType)
        }

    override fun getItemCount(): Int {
        var count = adapter.itemCount
        count += getHeaderViewCount() + getFooterViewCount() // 表Header和Footer
        return count
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isHeaderView(position) || isBottomView(position) || isCustomHeaderView(position) || isCustomFooterView(position)) return

        val po: Int = position - getHeaderViewCount() // 扣掉 Header
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
        else if (isCustomHeaderView(position))
            ITEM_TYPE_HEADER_VIEW
        else if (isCustomFooterView(position))
            ITEM_TYPE_FOOTER_VIEW
        else
            ITEM_TYPE_CONTENT

    // 判斷當前 item 是否是 HeadView
    private fun isHeaderView(position: Int): Boolean =
        mHeaderCount != 0 && position < mBottomCount

    // 判斷當前 item 是否是 FooterView
    private fun isBottomView(position: Int): Boolean =
        mBottomCount != 0 && position >= (getHeaderViewCount() + adapter.itemCount + getFooterViewCount() - mBottomCount)

    // 判斷當前是否是自定義的 HeaderView
    private fun isCustomHeaderView(position: Int): Boolean =
        headerView != null && position == mHeaderCount

    // 判斷當前是否是自訂義的 FooterView
    private fun isCustomFooterView(position: Int): Boolean =
        footerView != null && position == (getHeaderViewCount() + adapter.itemCount)

    fun getHeaderViewCount(): Int {
        var count = mHeaderCount
        headerView?.let { count++ }
        return count
    }

    fun getFooterViewCount(): Int {
        var count = mBottomCount
        footerView?.let { count++ }
        return count
    }

    inner class HeaderBottomHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}