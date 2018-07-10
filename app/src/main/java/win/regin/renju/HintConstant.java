package win.regin.renju;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author :Reginer in  2018/7/9 21:39.
 * 联系方式:QQ:282921012
 * 功能描述:
 */
public class HintConstant {
    /**
     * 对局开始
     */
    public static final int GAME_START = 1;
    /**
     * 对局结束
     */
    public static final int GAME_OVER = 2;
    /**
     * 落子
     */
    public static final int GAME_MOVE = 3;

    @IntDef({GAME_START, GAME_OVER, GAME_MOVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HintType {
    }
}
