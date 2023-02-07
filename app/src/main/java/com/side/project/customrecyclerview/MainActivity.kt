package com.side.project.customrecyclerview

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import com.side.project.customrecyclerview.customView.OnItemClickListener
import com.side.project.customrecyclerview.customView.XRecyclerView
import com.side.project.customrecyclerview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnItemClickListener, XRecyclerView.XRecyclerViewListener, XRecyclerView.XRecyclerViewScrollChange {
    private lateinit var binding: ActivityMainBinding

    private var list: ArrayList<String> = ArrayList()
    private lateinit var mainAdapter: MainAdapter

    private var b = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        list = java.util.ArrayList()
        for (i in 0..9) {
            list.add("leefeng.me$i")
        }

        binding.apply {
            recycleview.setLoadMore(true)
            recycleview.setRefresh(true)
            recycleview.setAutoLoadMore(true)
            recycleview.setOnItemClickListener(this@MainActivity)
            recycleview.setXRecyclerViewListener(this@MainActivity)
            recycleview.setScrollChangeListener(this@MainActivity)
            recycleview.itemAnimator = DefaultItemAnimator()
            mainAdapter = MainAdapter(list)
            recycleview.adapter = mainAdapter
        }
    }

    override fun onClick(position: Int) {
        Toast.makeText(this, "" + position, Toast.LENGTH_SHORT).show()
    }

    override fun onLongClick(position: Int) {
        Toast.makeText(this, "Long:$position", Toast.LENGTH_SHORT).show()
    }

    override fun onRefresh() {
        Handler().postDelayed({
            b = !b
            list.add(0, "leefeng.me" + "==onRefresh")
            binding.recycleview.stopRefresh(b)
            mainAdapter.notifyItemInserted(0)
            mainAdapter.notifyItemRangeChanged(0, list.size)
        }, 2000)
    }

    override fun onLoadMore() {
        Handler().postDelayed({
            binding.recycleview.stopLoadMore()
            list.add(list.size, "leefeng.me" + "==onLoadMore")
            mainAdapter.notifyItemRangeInserted(list.size - 1, 1)
        }, 2000)
    }

    override fun onRecyclerViewScrollChange(view: View?, i: Int, i1: Int) {

    }
}