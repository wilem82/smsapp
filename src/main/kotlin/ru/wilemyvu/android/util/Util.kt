package ru.wilemyvu.android.util

import android.util.Log
import android.view.View
import android.widget.ListView

@Suppress("UNCHECKED_CAST")
fun <T : View> ListView.asSequence(): Sequence<T> = object : Iterator<T> {
    private var i = -1
    override fun hasNext(): Boolean = i < this@asSequence.childCount - 1
    override fun next(): T = this@asSequence.getChildAt(++i) as T
}.asSequence()


fun <T> benchmark(label: String, op: () -> T): T {
    val started = System.nanoTime()
    val result = op.invoke()
    val ended = System.nanoTime()
    Log.d("benchmark", "Benchmark, $label took ${(ended - started) / 1000000} ms")
    return result
}