package com.dmh.filesearch

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout

/**
 * 所有的activity的基类
 */
abstract class BaseActivity : AppCompatActivity() {
    /**
     * 加载数据的对话框显示次数的计数
     */
    @Volatile
    private var loadDialogCount = 0
    /**
     * 加载数据显示的对话框
     */
    private val loadDialog: Dialog by lazy {
        Dialog(this, R.style.LoadDialog).apply {
            val loadDialogContentView = LayoutInflater.from(activity).inflate(R.layout.layout_load_dialog, null)
            /*val progressBar = loadDialogContentView.findViewById<View>(R.id.progress_load_dialog) as ProgressBar
            val progressColor = ContextCompat.getColor(activity, R.color.colorLoadDialogProgress)
            progressBar.indeterminateDrawable.setColorFilter(progressColor, PorterDuff.Mode.SRC_ATOP)*/
            setCanceledOnTouchOutside(false)
            setCancelable(isLoadDialogCancelable())
            setContentView(loadDialogContentView)
            loadDialogCount = 0
        }
    }

    protected lateinit var activity: AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutStatusBar()
        super.onCreate(savedInstanceState)
        activity = this
        setContentView(getLayoutResId())
        setSupportToolbar()
        start()
    }

    @LayoutRes
    protected abstract fun getLayoutResId(): Int

    protected open fun start() {}

    private fun setSupportToolbar() {
        val toolbar: View? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportToolbar(toolbar as Toolbar)
        }
    }

    protected open fun layoutStatusBar(statusBarDarkFont: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val lightFlag = if (statusBarDarkFont)
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else
                0
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or lightFlag)
            drawStatusBarColor()
        }
    }

    /**
     * 设置状态栏的颜色
     * @param c Android 6.0以上默认是白色.5.0和5.1默认自定义的颜色
     */
    protected open fun drawStatusBarColor(
        @ColorRes c: Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                android.R.color.white
            else
                R.color.colorNavigationBar
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, c)
        }
    }

    private fun setSupportToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val textToolbarTitle: TextView? = findAppBar()?.findViewById<View>(R.id.text_title_in_toolbar) as? TextView
        val title = actionBar?.title ?: ""
        textToolbarTitle?.let {
            it.text = title
        }
        actionBar?.title = ""
        findToolbar()?.setNavigationOnClickListener { onBackPressed() }
    }


    open protected fun isLoadDialogCancelable(): Boolean = true

    protected fun showLoadDialog() {
        if (this == null || isFinishing) {
            return
        }
        if (!loadDialog.isShowing) {
            loadDialog.show()
        }
        loadDialogCount++
    }

    protected fun cancelLoadDialog() {
        cancelLoadDialog(false)
    }

    private fun cancelLoadDialog(force: Boolean) {
        if (force) {
            loadDialogCount = 0
        }
        loadDialogCount--
        if (loadDialogCount <= 0) {
            loadDialogCount = 0
            if (loadDialog.isShowing) {
                loadDialog.cancel()
            }
        }
    }

    protected fun findAppBar(): AppBarLayout? = findViewById(R.id.appbar)

    protected fun findToolbar(): Toolbar? = findViewById(R.id.toolbar)

    protected fun findTitleText(): TextView? = findViewById(R.id.text_title_in_toolbar)

    protected fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        cancelLoadDialog(true)
        super.onDestroy()
    }
}
