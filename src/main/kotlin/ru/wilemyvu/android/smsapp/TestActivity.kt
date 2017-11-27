package ru.wilemyvu.android.smsapp

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class TestActivity : AppCompatActivity() {
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = applicationContext

        setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat_DayNight)
        setContentView(RecyclerView(ctx).also {
            it.layoutManager = LinearLayoutManager(ctx)
            it.adapter = object: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(group: ViewGroup, position: Int): RecyclerView.ViewHolder = run {
                    object: RecyclerView.ViewHolder(AppCompatTextView(ctx).also { textView ->

                    }) {
                    }.also { holder ->

                    }
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    (holder.itemView as AppCompatTextView).text = "onBindVH pos $position"
                }

                override fun getItemCount(): Int = 2
            }
        })
    }
}