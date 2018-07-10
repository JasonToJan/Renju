package win.regin.renju

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * @author :Reginer in  2018/7/9 20:52.
 * 联系方式:QQ:282921012
 * 功能描述:
 */
class SoundPlayUtils {
    companion object {
        private var sSoundPlayer: SoundPool? = null
        private var soundPlayUtils: SoundPlayUtils? = null

        /**
         * 初始化
         *
         * @param context context
         */
        fun init(context: Context): SoundPlayUtils {
            if (soundPlayUtils == null) {
                soundPlayUtils = SoundPlayUtils()
            }
            val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            val builder = SoundPool.Builder()
            builder.setAudioAttributes(audioAttributes).setMaxStreams(10)
            sSoundPlayer = builder.build()
            sSoundPlayer?.load(context, R.raw.game_start_cn, 1)
            sSoundPlayer?.load(context, R.raw.game_over, 1)
            sSoundPlayer?.load(context, R.raw.move, 1)
            return soundPlayUtils as SoundPlayUtils
        }

        /**
         * 播放声音
         *
         * @param soundID soundID
         */
        fun play(@HintConstant.HintType soundID: Int) {
            sSoundPlayer?.play(soundID, 1f, 1f, 0, 0, 1f)
        }
    }
}
