package win.regin.renju

import android.app.Application

/**
 * @author :Reginer in  2018/7/9 22:02.
 * 联系方式:QQ:282921012
 * 功能描述:
 */
class AppRenju : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        SoundPlayUtils.init(this)
    }

    companion object {
        lateinit var instance: AppRenju
            private set
    }
}
