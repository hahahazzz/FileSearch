package com.dmh.filesearch

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by QiuGang on 2017/10/17 21:18
 * Email : 1607868475@qq.com
 */
class RecyclerOnItemTouchListener(private val recyclerView: RecyclerView,
                                  private val listener: RecyclerOnItemTouchListener.OnTouchListener) : RecyclerView.OnItemTouchListener {
    private val detector: GestureDetector

    init {
        detector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(e.x, e.y)
                if (childView != null) {
                    listener.onItemLongClick(recyclerView, recyclerView.getChildAdapterPosition(childView))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val childView = rv.findChildViewUnder(e.x, e.y)
        if (childView != null && detector.onTouchEvent(e)) {
            listener.onItemClick(childView, rv.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }

    abstract class OnTouchListener {
        open fun onItemClick(view: View, position: Int) {}

        open fun onItemLongClick(view: View, position: Int) {}
    }
}
