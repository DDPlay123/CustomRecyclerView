package com.side.project.customrecyclerview.customView

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.side.project.customrecyclerview.databinding.XRecyclerViewLoadingBinding

/**
 * Create by 光廷 on 2023/02/07
 * 功能：XRecyclerView Loading 小元件
 */
class XRecyclerViewLoading(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val binding = XRecyclerViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : this(context, null)

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 不可見就停止動畫
        (binding.imgLoading.background as AnimationDrawable).stop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 可見就開始動畫
        (binding.imgLoading.background as AnimationDrawable).start()
    }
}