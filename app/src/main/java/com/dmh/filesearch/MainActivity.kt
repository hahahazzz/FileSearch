package com.dmh.filesearch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseActivity() {

    private val fileList: ArrayList<File> by lazy { ArrayList<File>() }

    private val fileSelectIndexList: ArrayList<Int> by lazy { ArrayList<Int>() }

    private val fileAdapter: FileResultAdapter by lazy { FileResultAdapter(fileList, fileSelectIndexList) }

    override fun getLayoutResId(): Int = R.layout.activity_main

    override fun start() {
        findViewById<ViewGroup>(android.R.id.content).getChildAt(0)?.let {
            val resources = it.context.resources
            val currentStatusBarHeight = resources.getDimensionPixelOffset(R.dimen.status_bar_height)
            if (currentStatusBarHeight > 0 && it.paddingTop == currentStatusBarHeight) {
                val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
                val realStatusBarHeight = resources.getDimensionPixelOffset(resId)
                if (realStatusBarHeight != currentStatusBarHeight) {
                    it.setPadding(it.paddingLeft, realStatusBarHeight, it.paddingRight, it.paddingBottom)
                }
            }
        }

        findToolbar()?.navigationIcon = null

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            text_permission_status.visibility = View.GONE
            showToast("权限已获取")
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        rv_file_list.layoutManager = LinearLayoutManager(this)
        rv_file_list.adapter = fileAdapter
        rv_file_list.addOnItemTouchListener(RecyclerOnItemTouchListener(rv_file_list,
                object : RecyclerOnItemTouchListener.OnTouchListener() {
                    override fun onItemClick(view: View, position: Int) {
                        if (position in fileSelectIndexList) {
                            fileSelectIndexList.remove(position)
                        } else {
                            fileSelectIndexList.add(position)
                            fileSelectIndexList.sort()
                        }
                        fileAdapter.notifyDataSetChanged()
                    }
                }))
        edit_file_extension.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    searchFile()
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    private fun searchFile() {
        val ext = edit_file_extension.text.toString()
        if (ext.isNullOrEmpty()) {
            return
        }
        showLoadDialog()
        text_current_path.visibility = View.VISIBLE
        Thread(Runnable {
            searchFile(Environment.getExternalStorageDirectory(), ext.split(",").toTypedArray())
            runOnUiThread {
                text_current_path.text = ""
                text_current_path.visibility = View.GONE
                fileAdapter.notifyDataSetChanged()
                cancelLoadDialog()
                if (fileList.isEmpty()) {
                    showToast("没有相关结果")
                }
            }
        }).start()
    }

    private fun searchFile(file: File, ext: Array<String>) {
        if (file.isDirectory) {
            file.listFiles().forEach {
                println("----> File search search dir ${file.absolutePath}")
                runOnUiThread {
                    text_current_path.text = "正在搜索:\n${file.absolutePath}"
                }
                searchFile(it, ext)
            }
        } else {
            if (file.extension in ext) {
                println("----> File search add File ${file.absolutePath}")
                fileList.add(file)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                text_permission_status.visibility = View.GONE
                showToast("权限已获取")
            } else {
                text_permission_status.visibility = View.VISIBLE
                showToast("未获取到权限")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_delete -> {
                if (fileSelectIndexList.isNotEmpty()) {
                    fileSelectIndexList.reversed().forEach {
                        val file = fileList.removeAt(it)
                        file.delete()
                        println("----> File search delete ${file.absolutePath}")
                    }
                    fileSelectIndexList.clear()
                    fileAdapter.notifyDataSetChanged()
                }
            }
            R.id.item_select_all -> {
                if (fileSelectIndexList.isNotEmpty()) {
                    fileSelectIndexList.clear()
                } else {
                    fileSelectIndexList.addAll(0..fileList.lastIndex)
                }
                fileSelectIndexList.sort()
                fileAdapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
