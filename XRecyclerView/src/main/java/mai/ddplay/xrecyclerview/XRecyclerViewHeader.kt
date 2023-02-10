package mai.ddplay.xrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import mai.ddplay.xrecyclerview.databinding.XRecyclerViewHeaderBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create by 光廷 on 2023/02/07
 * 功能：XRecyclerView 頂部，一般用於顯示刷新狀態。
 * 來源：https://github.com/limxing/LFRecyclerView-Android
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = KEY_DATASTORE)

class XRecyclerViewHeader(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    private val binding = XRecyclerViewHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    // 刷新狀態
    private var mState: XRecyclerViewState = XRecyclerViewState.STATE_NORMAL

    // 箭頭動畫
    private var mRotateUpAnim: Animation = RotateAnimation(0.0f, -180.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    private var mRotateDownAnim: Animation = RotateAnimation(-180.0f, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

    // 初始化
    init {
        mRotateUpAnim.duration = ROTATE_ANIM_DURATION.toLong()
        mRotateUpAnim.fillAfter = true

        mRotateDownAnim.duration = ROTATE_ANIM_DURATION.toLong()
        mRotateDownAnim.fillAfter = true
    }

    fun getContentHeight(): Int = binding.content.height

    fun setState(state: XRecyclerViewState) {
        if (state == mState) return

        if (state == XRecyclerViewState.STATE_REFRESHING) {
            // 顯示Loading
            binding.apply {
                imgArrow.clearAnimation()
                imgArrow.invisible()
                pb.visible()
            }
        } else {
            // 顯示箭頭
            binding.apply {
                imgArrow.visible()
                pb.invisible()
            }
        }

        when (state) {
            XRecyclerViewState.STATE_NORMAL -> binding.apply {
                imgArrow.setImageResource(R.drawable.ic_arrow_bottom)

                if (mState == XRecyclerViewState.STATE_READY)
                    imgArrow.animation = mRotateDownAnim
                if (mState == XRecyclerViewState.STATE_REFRESHING)
                    imgArrow.clearAnimation()

                tvState.text = context.getString(R.string.header_hint_normal)
            }

            XRecyclerViewState.STATE_READY -> binding.apply {
                if (mState != XRecyclerViewState.STATE_READY) {
                    imgArrow.clearAnimation()
                    imgArrow.startAnimation(mRotateUpAnim)

                    tvState.text = context.getString(R.string.header_hint_ready)
                }
            }

            XRecyclerViewState.STATE_REFRESHING -> binding.apply {
                tvState.text = context.getText(R.string.header_hint_loading)
            }

            XRecyclerViewState.STATE_SUCCESS -> binding.apply {
                tvState.text = context.getText(R.string.header_hint_success)
                putUpdateTime(System.currentTimeMillis())
            }

            XRecyclerViewState.STATE_FAILED -> binding.apply {
                tvState.text = context.getText(R.string.header_hint_failed)
                putUpdateTime(System.currentTimeMillis())
            }
        }

        mState = state
    }

    fun setVisibleHeight(h: Int) {
        val lp = binding.content.layoutParams as LayoutParams
        lp.height = if (h < 0) 0 else h
        binding.content.layoutParams = lp
    }

    fun getVisibleHeight(): Int = binding.content.layoutParams.height

    // 顯示上次更新的文字描述
    fun refreshUpdatedAtValue() {
        // 上次更新時間
        val lastUpdateTime = getUpdateTime()
        // 當前時間
        val currentTime = System.currentTimeMillis()
        // 比較時間
        val timePassed = currentTime - lastUpdateTime

        val updateAtValue: String = if (timePassed < 0)
            context.getString(R.string.hint_time_error)
        else if (timePassed < ONE_DAY) {
            val simpleDate = SimpleDateFormat("hh:mm", Locale.getDefault())
            val date = "${context.getString(R.string.today)} ${simpleDate.format(Date(lastUpdateTime))}"
            String.format(context.getString(R.string.header_last_time), date)
        } else if (timePassed < ONE_YEAR) {
            val simpleDate = SimpleDateFormat("MM/dd hh:mm", Locale.getDefault())
            val date = simpleDate.format(Date(lastUpdateTime))
            String.format(context.getString(R.string.header_last_time), date)
        } else {
            val simpleDate = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault())
            val date = simpleDate.format(Date(lastUpdateTime))
            String.format(context.getString(R.string.header_last_time), date)
        }

        binding.tvStateTime.text = updateAtValue
    }

    // 上傳更新時間
    private fun putUpdateTime(value: Long) = runBlocking {
        context.dataStore.edit {
            it[longPreferencesKey(UPDATE_TIME)] = value
        }
    }

    // 取得更新時間
    private fun getUpdateTime(): Long = runBlocking {
        return@runBlocking context.dataStore.data.map {
            it[longPreferencesKey(UPDATE_TIME)] ?: System.currentTimeMillis()
        }.first()
    }
}