package com.side.project.customrecyclerview.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.side.project.customrecyclerview.R
import com.side.project.customrecyclerview.databinding.XRecyclerViewFooterBinding

/**
 * Create by 光廷 on 2023/02/07
 * 功能：XRecyclerView 底部，一般用於載入更多資料。
 * 來源：https://github.com/limxing/LFRecyclerView-Android
 */
class XRecyclerViewFooter(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val binding = XRecyclerViewFooterBinding.inflate(LayoutInflater.from(context), this, true)

    // 加載狀態
    private var mState: XRecyclerViewState = XRecyclerViewState.STATE_NORMAL

    // 箭頭動畫
    private var mRotateUpAnim: Animation = RotateAnimation(
        0.0f, -180.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f
    )

    private var mRotateDownAnim: Animation = RotateAnimation(
        -180.0f, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f
    )

    constructor(context: Context) : this(context, null)

    init {
        mRotateUpAnim.duration = ROTATE_ANIM_DURATION.toLong()
        mRotateUpAnim.fillAfter = true

        mRotateDownAnim.duration = ROTATE_ANIM_DURATION.toLong()
        mRotateDownAnim.fillAfter = true
    }

    fun getTvState(): TextView = binding.tvState

    fun setState(state: XRecyclerViewState) {
        if (state == mState) return

        if (state == XRecyclerViewState.STATE_REFRESHING) {
            // 顯示HUD
            binding.apply {
                imgArrow.clearAnimation()
                imgArrow.invisible()
                customLoading.visible()
            }
        } else {
            // 顯示箭頭
            binding.apply {
                imgArrow.visible()
                customLoading.invisible()
            }
        }

        when (state) {
            XRecyclerViewState.STATE_NORMAL -> binding.apply {
                imgArrow.setImageResource(R.drawable.ic_arrow_top)

                if (mState == XRecyclerViewState.STATE_READY)
                    imgArrow.animation = mRotateUpAnim
                if (mState == XRecyclerViewState.STATE_REFRESHING)
                    imgArrow.clearAnimation()

                tvState.text = context.getString(R.string.footer_hint_normal)
            }

            XRecyclerViewState.STATE_READY -> binding.apply {
                if (mState != XRecyclerViewState.STATE_READY) {
                    imgArrow.clearAnimation()
                    imgArrow.startAnimation(mRotateDownAnim)

                    tvState.text = context.getString(R.string.footer_hint_ready)
                }
            }

            XRecyclerViewState.STATE_REFRESHING -> binding.apply {
                tvState.text = context.getText(R.string.footer_hint_loading)
            }

            XRecyclerViewState.STATE_SUCCESS -> binding.apply {
                tvState.text = context.getText(R.string.footer_hint_success)
            }

            XRecyclerViewState.STATE_FAILED -> binding.apply {
                tvState.text = context.getText(R.string.footer_hint_failed)
            }
        }

        mState = state
    }

    fun setBottomMargin(h: Int) {
        val lp = binding.root.layoutParams as LayoutParams
        lp.height = if (h < 0) 0 else h
        binding.root.layoutParams = lp
    }

    fun getBottomMargin(): Int {
        val lp = binding.root.layoutParams as LayoutParams
        return lp.bottomMargin
    }

    fun setNoneDataState(isNone: Boolean) {
        binding.apply {
            if (isNone) {
                imgArrow.visible()
                customLoading.visible()
                tvState.visible()
            } else {
                imgArrow.gone()
                customLoading.gone()
                tvState.gone()
            }
        }
    }
}