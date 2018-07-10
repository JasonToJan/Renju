package win.regin.renju

import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.core.widget.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * @author :Reginer in  2018/7/9 21:36.
 *         联系方式:QQ:282921012
 *         功能描述:
 */
class MainActivity : AppCompatActivity(), RenjuCallback, View.OnClickListener {

    /**
     * 引擎
     */
    private lateinit var mAi: Ai
    private var mChooseChess: PopupWindow? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAi = Ai(this)
        rvRenju.setCallBack(this)
        rvRenju.viewTreeObserver.addOnGlobalLayoutListener { initPop(getWidth(), getHeight()) }
        val levelPosition = SpUtils[RenjuConstant.RENJU_LEVEL, 0] as Int
        level.text = resources.getStringArray(R.array.renju_level)[levelPosition]
        mAi.setLevel(levelPosition)

        newRenju.setOnClickListener(this)
        undo.setOnClickListener(this)
        level.setOnClickListener(this)
        suggest.setOnClickListener(this)
        mode.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (mAi.isAiThing) {
            toast(R.string.ai_thing)
            return
        }
        when (view) {
            newRenju -> {
                if (rvRenju.isHumanComputer) {
                    mChooseChess?.showAtLocation(rvRenju, Gravity.CENTER, 0, 0)
                }
                mAi.restart()
                rvRenju.start()
            }
            undo -> {
                if (rvRenju.chessCount >= 2) {
                    mAi.undo()
                    rvRenju.undo()
                }
            }
            level -> {
                var levelPosition = SpUtils[RenjuConstant.RENJU_LEVEL, 0] as Int
                levelPosition = if (levelPosition < 4) levelPosition + 1 else 0
                level.text = resources.getStringArray(R.array.renju_level)[levelPosition]
                SpUtils.put(RenjuConstant.RENJU_LEVEL,levelPosition)
                mAi.setLevel(levelPosition)
            }
            suggest -> {
                if (!rvRenju.isGameOver) {
                    rvRenju.setUserBout(false)
                    launch {
                        val suggestPoint = mAi.suggest()
                        launch(UI) {
                            rvRenju.setUserBout(true)
                            rvRenju.showSuggest(suggestPoint)
                        }
                    }
                }
            }
            mode -> {
                if (TextUtils.equals(mode.text.toString(), getString(R.string.human_human))) {
                    mode.text = getString(R.string.human_computer)
                    rvRenju.gameMode = RenjuConstant.HUMAN_COMPUTER
                    mAi.aiChess = rvRenju.userChess
                    rvRenju.userChess = mAi.getUserChess()
                    rvRenju.setUserBout(false)
                    aiThink(null)
                } else {
                    mode.text = getString(R.string.human_human)
                    rvRenju.gameMode = RenjuConstant.HUMAN_HUMAN
                }
            }
        }
    }


    /**
     * 初始化PopWindow
     *
     * @param width  宽度
     * @param height 高度
     */
    private fun initPop(width: Int, height: Int) {
        if (mChooseChess == null) {
            val view = View.inflate(this, R.layout.view_pop_choose_chess, null)
            val white = view.findViewById<ImageButton>(R.id.choose_white)
            val black = view.findViewById<ImageButton>(R.id.choose_black)
            white.setOnClickListener {
                SoundPlayUtils.play(HintConstant.GAME_START)
                rvRenju.setUserBout(false)
                rvRenju.userChess = RenjuConstant.WHITE_CHESS
                mAi.aiChess = RenjuConstant.BLACK_CHESS
                aiThink(null)
                mChooseChess?.dismiss()
            }
            black.setOnClickListener {
                SoundPlayUtils.play(HintConstant.GAME_START)
                rvRenju.setUserBout(true)
                rvRenju.userChess = RenjuConstant.BLACK_CHESS
                mAi.aiChess = RenjuConstant.WHITE_CHESS
                mChooseChess?.dismiss()
            }
            mChooseChess = PopupWindow(view, width, height)
            mChooseChess?.isOutsideTouchable = false
            mChooseChess?.showAtLocation(rvRenju, Gravity.CENTER, 0, 0)
        }
    }

    private fun aiThink(p: Point?) {
        mAi.aiBout(p)
    }

    override fun gameOver(winner: Int) {
        SoundPlayUtils.play(HintConstant.GAME_OVER)
        toast(
                when (winner) {
                    RenjuConstant.BLACK_CHESS -> R.string.black_win
                    RenjuConstant.WHITE_CHESS -> R.string.white_win
                    else -> R.string.no_win
                })
    }

    override fun atBell(p0: Point, isAi: Boolean, isBlack: Boolean) {
        imgFlag.setImageResource(if (isBlack) R.drawable.ic_white_chess
        else R.drawable.ic_black_chess)
        SoundPlayUtils.play(HintConstant.GAME_MOVE)
        if (isAi) aiAtBell(p0) else userAtBell(p0)
    }

    /**
     * ai落子
     */
    private fun aiAtBell(p: Point?) {
        rvRenju.addChess(p, mAi.aiChess)
        rvRenju.setUserBout(true)
        rvRenju.checkGameOver()
    }

    /**
     * 玩家落子
     */
    private fun userAtBell(p: Point) {
        if (rvRenju.isHumanComputer) aiThink(p) else mAi.addChess(p)
    }
}
