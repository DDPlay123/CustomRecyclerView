package com.side.project.customrecyclerview.customView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

/**
 * Create by 光廷 on 2023/02/07
 * 功能：XRecyclerView 本體，繼承RecyclerView。
 * 來源：https://github.com/limxing/LFRecyclerView-Android
 */
class XRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    companion object {
        const val OFFSET_RADIO = 1.8f
        const val PULL_LOAD_MORE_DELTA = 50
        const val SCROLL_DURATION = 400
        const val SCROLL_BACK_HEADER = 4
        const val SCROLL_BACK_FOOTER = 3
    }

    constructor(context: Context) : this(context, null)

    private lateinit var mLayoutManager: GridLayoutManager

    private var mScroller: Scroller
    private lateinit var mAdapter: XRecyclerViewAdapter
    private var isAutoLoadMore = false
    private var isLoadMore = true
    private var isRefresh = true
    private var mPullLoad = false

    private var itemListener: OnItemClickListener? = null
    private var mRecyclerViewListener: XRecyclerViewListener? = null
    private var scrollerListener: XRecyclerViewScrollChange? = null

    private var recyclerViewHeader: XRecyclerViewHeader
    private var recyclerViewFooter: XRecyclerViewFooter

    private var mHeaderViewHeight = 0
    private var mScrollBack = 0
    // 上一次Y值
    private var mLastY = 0f
    // 是否正在更新
    private var mPullRefreshing = false
    // 是否正在加載
    private var mPullLoading = false

    private lateinit var adapter: Adapter<*>
    private var observer: XAdapterDataObserve? = null

    init {
        mScroller = Scroller(context, DecelerateInterpolator())
        recyclerViewHeader = XRecyclerViewHeader(context)
        recyclerViewFooter = XRecyclerViewFooter(context)

        recyclerViewHeader.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mHeaderViewHeight = recyclerViewHeader.getContent().height
                viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })

        val gridLayoutManager = GridLayoutManager(context, 1)
        layoutManager = gridLayoutManager

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                onScrollChange(recyclerView, dx, dy)
            }
        })
        observer = XAdapterDataObserve()
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        mLayoutManager = layout as GridLayoutManager
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter == null) return

        this.adapter = adapter
        if (observer == null)
            observer = XAdapterDataObserve()

        adapter.registerAdapterDataObserver(observer ?: return)

        mAdapter = XRecyclerViewAdapter(adapter as Adapter<ViewHolder>)
        mAdapter.setRecyclerViewHeader(recyclerViewHeader)
        mAdapter.setRecyclerViewFooter(recyclerViewFooter)

        mAdapter.setLoadMore(isLoadMore)
        mAdapter.setRefresh(isRefresh)
        mAdapter.setOnItemClickListener(itemListener ?: return)

        super.setAdapter(mAdapter)
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLL_BACK_HEADER)
                recyclerViewHeader.setVisibleHeight(mScroller.currY)
            else
                recyclerViewFooter.setBottomMargin(mScroller.currY)

            postInvalidate()
        }
        super.computeScroll()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (mLastY == -1F || mLastY == 0F) {
            mLastY = ev.rawY
            if (!mPullRefreshing && mLayoutManager.findFirstVisibleItemPosition() <= 1)
                recyclerViewHeader.refreshUpdatedAtValue()
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> mLastY = ev.rawY
            MotionEvent.ACTION_MOVE -> {
                val moveY = ev.rawY - mLastY
                mLastY = ev.rawY
                if (isRefresh && !mPullLoad && mLayoutManager.findFirstVisibleItemPosition() <= 1 &&
                    (recyclerViewHeader.getVisibleHeight() > 0 || moveY > 0))
                    updateHeaderHeight(moveY / OFFSET_RADIO)
                else if (isLoadMore && !mPullRefreshing && !mPullLoad && mLayoutManager.findLastVisibleItemPosition() == mAdapter.itemCount - 1 && (recyclerViewFooter.getBottomMargin() > 0 || moveY < 0) && adapter.itemCount > 0)
                    updateFooterHeight(-moveY / OFFSET_RADIO)
            }
            MotionEvent.ACTION_UP -> {
                mLastY = -1F // reset
                if (!mPullRefreshing && mLayoutManager.findFirstVisibleItemPosition() == 0) {
                    // invoke refresh
                    if (isRefresh && recyclerViewHeader.getVisibleHeight() > mHeaderViewHeight) {
                        mPullRefreshing = true
                        recyclerViewHeader.setState(XRecyclerViewState.STATE_REFRESHING)
                        mRecyclerViewListener?.onRefresh()
                    }
                }
                if (isLoadMore && mPullLoading && mLayoutManager.findLastVisibleItemPosition() == mAdapter.itemCount - 1 && recyclerViewFooter.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
                    recyclerViewFooter.setState(XRecyclerViewState.STATE_REFRESHING)
                    mPullLoad = true
                    startLoadMore()
                }
                resetHeaderHeight()
                resetFooterHeight()
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun updateHeaderHeight(delta: Float) {
        recyclerViewHeader.setVisibleHeight(delta.toInt() + recyclerViewHeader.getVisibleHeight())
        if (isRefresh && !mPullRefreshing) {
            if (recyclerViewHeader.getVisibleHeight() > mHeaderViewHeight)
                recyclerViewHeader.setState(XRecyclerViewState.STATE_READY)
            else
                recyclerViewHeader.setState(XRecyclerViewState.STATE_NORMAL)
        }
    }

    private fun resetHeaderHeight() {
        val height: Int = recyclerViewHeader.getVisibleHeight()
        if (height == 0 || mPullRefreshing && height <= mHeaderViewHeight) return

        var finalHeight = 0
        if (mPullRefreshing)
            finalHeight = mHeaderViewHeight

        mScrollBack = SCROLL_BACK_HEADER
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION)
        invalidate()
    }

    fun stopRefresh(isSuccess: Boolean) {
        if (mPullRefreshing) {
            var de: Long = 1000
            if (isSuccess)
                recyclerViewHeader.setState(XRecyclerViewState.STATE_SUCCESS)
            else {
                recyclerViewHeader.setState(XRecyclerViewState.STATE_FAILED)
                de = 2000
            }

            recyclerViewHeader.postDelayed({
                mPullRefreshing = false
                resetHeaderHeight()
            }, de)
        }
    }

    private fun updateFooterHeight(delta: Float) {
        val height = recyclerViewFooter.getBottomMargin() + delta.toInt()
        if (isLoadMore) {
            if (height > PULL_LOAD_MORE_DELTA) {
                recyclerViewFooter.setState(XRecyclerViewState.STATE_READY)
                mPullLoading = true
            } else {
                recyclerViewFooter.setState(XRecyclerViewState.STATE_NORMAL)
                mPullLoading = false
                mPullLoad = false
            }
        }
        recyclerViewFooter.setBottomMargin(height)
    }

    private fun resetFooterHeight() {
        val bottomMargin = recyclerViewFooter.getBottomMargin()
        if (bottomMargin > 0) {
            mScrollBack = SCROLL_BACK_FOOTER
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION)
            invalidate()
        }
    }

    private fun startLoadMore() {
        if (mRecyclerViewListener != null) {
            recyclerViewFooter.setState(XRecyclerViewState.STATE_REFRESHING)
            mRecyclerViewListener?.onLoadMore()
        }
    }

    fun stopLoadMore() {
        if (mPullLoading) {
            mPullLoad = false
            mPullLoading = false
            recyclerViewFooter.setState(XRecyclerViewState.STATE_NORMAL)
            resetFooterHeight()
        }
    }

    fun setLoadMore(b: Boolean) {
        isLoadMore = b
    }

    fun setRefresh(b: Boolean) {
        isRefresh = b
    }

    fun setOnItemClickListener(itemListener: OnItemClickListener) {
        this.itemListener = itemListener
    }

    fun setXRecyclerViewListener(l: XRecyclerViewListener) {
        mRecyclerViewListener = l
    }

    fun setScrollChangeListener(listener: XRecyclerViewScrollChange) {
        scrollerListener = listener
    }

    fun setAutoLoadMore(autoLoadMore: Boolean) {
        isAutoLoadMore = autoLoadMore
    }

    private var currentLastNum = 0
    private var num = 0

    fun onScrollChange(view: View?, i: Int, i1: Int) {
        if (mAdapter.itemHeight > 0 && num == 0)
            num = ceil((height / mAdapter.itemHeight).toDouble()).toInt()

        if (isAutoLoadMore && (mLayoutManager.findLastVisibleItemPosition() == mAdapter.itemCount - 1) && currentLastNum != mLayoutManager.findLastVisibleItemPosition() && num > 0 && adapter.itemCount > num && !mPullLoading) {
            currentLastNum = mLayoutManager.findLastVisibleItemPosition()
            mPullLoading = true
            startLoadMore()
        }

        scrollerListener?.onRecyclerViewScrollChange(view, i, i1)
    }

    interface XRecyclerViewListener {
        fun onRefresh()
        fun onLoadMore()
    }

    interface XRecyclerViewScrollChange {
        fun onRecyclerViewScrollChange(view: View?, i: Int, i1: Int)
    }

    inner class XAdapterDataObserve : AdapterDataObserver() {
        override fun onChanged() {
            mAdapter.notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            mAdapter.notifyItemRangeChanged(positionStart + mAdapter.mHeaderCount, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            mAdapter.notifyItemRangeChanged(positionStart + mAdapter.mHeaderCount, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            mAdapter.notifyItemRangeInserted(positionStart + mAdapter.mHeaderCount, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            mAdapter.notifyItemRangeRemoved(positionStart + mAdapter.mHeaderCount, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            mAdapter.notifyItemMoved(fromPosition + mAdapter.mHeaderCount, toPosition + mAdapter.mHeaderCount)
        }
    }
}