package com.side.project.customrecyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.side.project.customrecyclerview.customView.XRecyclerView
import com.side.project.customrecyclerview.data.Image
import com.side.project.customrecyclerview.databinding.ActivityMainBinding
import com.side.project.customrecyclerview.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), XRecyclerView.OnItemClickListener, XRecyclerView.XRecyclerViewListener, XRecyclerView.XRecyclerViewScrollChange {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mainAdapter: MainAdapter

    private val _observeData = MutableSharedFlow<Pair<Boolean, Image>>()
    private val observeData = _observeData.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        initRv()
        doObserve()

        /* 初始化 */
        callData(true)
    }

    /**
     * 註：不須再定義 LayoutManager
     */
    private fun initRv() {
        binding.recycleView.apply {
            // 設定可往上刷新：預設 True
            setLoadMore(true)
            // 設定可往下加載：預設 True
            setRefresh(true)
            // 到底後，可自動加載：預設 False
            setAutoLoadMore(false)
            // Item 點擊事件處理
            setOnItemClickListener(this@MainActivity)
            // 設定 Refresh 和 LoadMore 事件
            setXRecyclerViewListener(this@MainActivity)
            // 設定滾動事件
            setScrollChangeListener(this@MainActivity)

            mainAdapter = MainAdapter()
            adapter = mainAdapter
        }
    }

    private fun doObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    // first: IsRefresh
                    // second: Image Data
                    observeData.collect { pair ->
                        if (!::mainAdapter.isInitialized) return@collect
                        val list = mainAdapter.currentList.toMutableList()
                        if (pair.first)
                            list.clear()
                        list.addAll(pair.second.items)
                        mainAdapter.submitList(list)
                    }
                }
            }
        }
    }

    private fun callData(isRefresh: Boolean) {
        ApiClient.getAPI.getImage().enqueue(object : Callback<Image> {
            override fun onResponse(call: Call<Image>, response: Response<Image>) {
                // 停止刷新
                binding.recycleView.stopRefresh(true)
                binding.recycleView.stopLoadMore()
                response.body()?.let {
                    CoroutineScope(Dispatchers.IO).launch { _observeData.emit(Pair(isRefresh, it)) }
                }
            }

            override fun onFailure(call: Call<Image>, t: Throwable) {
                // 停止刷新 or 加載
                binding.recycleView.stopRefresh(false)
                binding.recycleView.stopLoadMore()
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(position: Int) {
        Toast.makeText(this, "Short:$position", Toast.LENGTH_SHORT).show()
    }

    override fun onLongClick(position: Int) {
        Toast.makeText(this, "Long:$position", Toast.LENGTH_SHORT).show()
    }

    override fun onRefresh() {
        callData(true)
    }

    override fun onLoadMore() {
        callData(false)
    }

    override fun onRecyclerViewScrollChange(view: View?, i: Int, i1: Int) {
        /* 未使用 */
    }
}