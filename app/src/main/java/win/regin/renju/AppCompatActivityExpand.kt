package win.regin.renju

import android.support.v7.app.AppCompatActivity


/**
 * @author :Reginer in  2018/7/9 21:31.
 *         联系方式:QQ:282921012
 *         功能描述:AppCompatActivity扩展
 */
fun AppCompatActivity.getWidth(): Int {
    val dm = resources.displayMetrics
    return dm.widthPixels
}

fun AppCompatActivity.getHeight(): Int {
    val dm = resources.displayMetrics
    return dm.heightPixels
}

