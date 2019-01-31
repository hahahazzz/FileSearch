package com.dmh.filesearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_file_item.view.*
import java.io.File

/**
 * @Author: QiuGang
 * @Date : 2019/1/31 11:41
 * @Email : 1607868475@qq.com
 * @Record:
 *
 */
class FileResultAdapter(private val fileList: ArrayList<File>,
                        private val selectIndexList: ArrayList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_file_item, parent, false)) {}
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemView = holder.itemView
        itemView.img_file_select_status.isSelected = position in selectIndexList
        itemView.text_item_name.text = fileList[position].name
    }
}