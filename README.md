测试棋力在相同配置下，初级水平基本相当于五子棋大师的特级大师水平。
一楼上图：
![主页面](https://img-blog.csdn.net/20180710103709367?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NDEzMjQ5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

五子棋的引擎和自定义控件我封装在lib中，可以通过
`implementation 'win.regin:renju:1.0.0'`来引用，而ndk的调用类Ai是用kotlin编写的，上传的时候传不上去，做为一个jar包引用了。

五子棋自定义控件代码，关键地方有注释:

```
package win.regin.renju;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * @author :Reginer in  2018/7/9 20:00.
 * 联系方式:QQ:282921012
 * 功能描述:
 */

public class RenjuView extends View {
    /**
     * 棋盘面板宽度
     */
    private int mPanelWidth;
    /**
     * 棋盘每格的行高
     */
    private float mLineHeight;
    /**
     * 棋盘尺寸
     */
    private static final int MAX_LINE = 15;
    /**
     * 棋子占行高比例
     */
    private float ratioPieceOfLineHeight = 0.9f;
    private float startPos;
    private float endPos;

    /**
     * 白棋数组
     */
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    /**
     * 黑棋数组
     */
    private ArrayList<Point> mBlackArray = new ArrayList<>();
    /**
     * 棋盘数组
     */
    private ArrayList<Point> chessArray = new ArrayList<>();
    private int[][] mBoard = new int[MAX_LINE][MAX_LINE];

    /**
     * 胜利玩家
     */
    private int mWinner;
    /**
     * 连成五个的棋子
     */
    private ArrayList<Point> fiveArray = new ArrayList<>();
    /**
     * 游戏是否结束
     */
    private boolean mIsGameOver = false;


    private int gameMode = RenjuConstant.HUMAN_COMPUTER;

    /**
     * 玩家以及AI得分纪录
     */
    private int userScore, aiScore;
    /**
     * 玩家执子
     */
    private int userChess;
    /**
     * 当前回合是否轮到玩家
     */
    private boolean isUserBout;
    /**
     * 显示棋子编号
     */
    private boolean isDrawChessNum = true;

    private RenjuCallback callBack;

    private Paint mPaint = new Paint();
    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;

    private Point suggestPoint;

    public RenjuView(Context context) {
        this(context, null);
    }

    public RenjuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenjuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint.setColor(Color.BLACK);
        //抗锯齿
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(24);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.ic_white_chess);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.ic_black_chess);
        userScore = 0;
        aiScore = 0;
        start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int measureSize = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            measureSize = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            measureSize = widthSize;
        }
        setMeasuredDimension(measureSize, measureSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mPanelWidth = w;
        float outLine = 10.0f;
        mLineHeight = (mPanelWidth * 1.0f - 2.0f * outLine) / MAX_LINE;
        startPos = outLine;
        endPos = w - outLine;
        int pieceSize = (int) (mLineHeight * ratioPieceOfLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceSize, pieceSize, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceSize, pieceSize, false);
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    public int getChessCount() {
        return chessArray.size();
    }

    public void removeLastChess() {
        Point p = chessArray.get(chessArray.size() - 1);
        chessArray.remove(chessArray.size() - 1);
        if (mBoard[p.y][p.x] == RenjuConstant.BLACK_CHESS) {
            mBlackArray.remove(mBlackArray.size() - 1);
        } else if (mBoard[p.y][p.x] == RenjuConstant.WHITE_CHESS) {
            mWhiteArray.remove(mWhiteArray.size() - 1);
        }
        mBoard[p.y][p.x] = RenjuConstant.N0_CHESS;
    }

    public void undo() {
        final int canUndoSize = 2;
        if (chessArray.size() >= canUndoSize) {
            mIsGameOver = false;
            suggestPoint = null;
            removeLastChess();
            removeLastChess();
        }
        postInvalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsGameOver || !isUserBout) {
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            Point p = getValidPoint(x, y);
            if (mWhiteArray.contains(p) || mBlackArray.contains(p)) {
                return false;
            }
            suggestPoint = null;
            addChess(p, userChess);
            setUserBout(!isHumanComputer());
            if (!isHumanComputer()) {
                userChess = (userChess == RenjuConstant.WHITE_CHESS) ? RenjuConstant.BLACK_CHESS : RenjuConstant.WHITE_CHESS;
            }
            checkGameOver();
            invalidate();

            if (!mIsGameOver) {
                callBack.atBell(p, false, isUserBlack());
            }
            return true;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawPiece(canvas);
    }

    public void addChess(Point point, int chessType) {
        mBoard[point.y][point.x] = chessType;
        chessArray.add(point);
        if (chessType == RenjuConstant.BLACK_CHESS) {
            mBlackArray.add(point);
        } else if (chessType == RenjuConstant.WHITE_CHESS) {
            mWhiteArray.add(point);
        }
        invalidate();
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void checkGameOver() {
        boolean blackWin = checkFiveInLine(mBlackArray);
        boolean whiteWin = !blackWin && checkFiveInLine(mWhiteArray);
        if (whiteWin || blackWin) {
            mIsGameOver = true;
            mWinner = whiteWin ? RenjuConstant.WHITE_CHESS : RenjuConstant.BLACK_CHESS;
            if (mWinner == userChess) {
                userScore++;
            } else {
                aiScore++;
            }
            callBack.gameOver(mWinner);
            invalidate();
        } else if (isFull()) {
            mIsGameOver = true;
            mWinner = RenjuConstant.N0_CHESS;
            callBack.gameOver(mWinner);
            invalidate();
        }
    }

    public boolean checkFiveInLine(List<Point> points) {
        List<Point> dirArray = new ArrayList<>();
        dirArray.add(new Point(1, 0));
        dirArray.add(new Point(0, 1));
        dirArray.add(new Point(1, 1));
        dirArray.add(new Point(1, -1));
        for (Point p : points) {
            for (Point dir : dirArray) {
                if (checkFiveOneLine(dir, p.x, p.y, points)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查棋子在某个方向是否已经连五
     *
     * @param dir    dir
     * @param x      x
     * @param y      y
     * @param points points
     * @return 胜利
     */
    public boolean checkFiveOneLine(Point dir, int x, int y, List<Point> points) {
        int count = 1;
        fiveArray.clear();
        fiveArray.add(new Point(x, y));
        final int successSize = 5;
        for (int i = 1; i < successSize; i++) {
            Point p = new Point(x + dir.x * i, y + dir.y * i);
            if (points.contains(p)) {
                fiveArray.add(p);
                count++;
            } else {
                break;
            }
        }
        for (int i = 1; i < successSize; i++) {
            Point p = new Point(x - dir.x * i, y - dir.y * i);
            if (points.contains(p)) {
                fiveArray.add(p);
                count++;
            } else {
                break;
            }
        }
        return count >= 5;
    }


    private void drawPiece(Canvas canvas) {
        mPaint.setStrokeWidth(2.0f);
        mPaint.setTextSize(24);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        for (int i = 0; i < mWhiteArray.size(); i++) {
            Point whitePoint = mWhiteArray.get(i);
            float left = startPos + (whitePoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight;
            float top = startPos + (whitePoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight;
            canvas.drawBitmap(mWhitePiece, left, top, null);
            if (isDrawChessNum) {
                mPaint.setColor(Color.BLACK);
                float textTop = startPos + whitePoint.y * mLineHeight;
                float textBottom = textTop + mLineHeight;
                float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
                float centerX = startPos + (whitePoint.x + 0.5f) * mLineHeight;
                canvas.drawText(String.format("%S", 2 + i * 2), centerX, baseline, mPaint);
            }
        }
        for (int i = 0; i < mBlackArray.size(); i++) {
            Point blackPoint = mBlackArray.get(i);
            float left = startPos + (blackPoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight;
            float top = startPos + (blackPoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight;
            canvas.drawBitmap(mBlackPiece, left, top, null);
            if (isDrawChessNum) {
                mPaint.setColor(Color.WHITE);
                float textTop = startPos + blackPoint.y * mLineHeight;
                float textBottom = textTop + mLineHeight;
                float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
                float centerX = startPos + (blackPoint.x + 0.5f) * mLineHeight;
                canvas.drawText(String.format("%S", 1 + i * 2), centerX, baseline, mPaint);
            }
        }
    }

    private void drawBoard(Canvas canvas) {
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(2.0f);
        int w = mPanelWidth;
        float lineHeight = mLineHeight;
        //画棋盘线
        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (startPos + lineHeight / 2);
            int endX = (int) (endPos - lineHeight / 2);
            int y = (int) (startPos + (0.5 + i) * lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);
        }
        for (int i = 0; i < MAX_LINE; i++) {
            int startY = (int) (startPos + lineHeight / 2);
            int endY = (int) (endPos - lineHeight / 2);
            int x = (int) (startPos + (0.5 + i) * lineHeight);
            canvas.drawLine(x, startY, x, endY, mPaint);
        }
        //画棋盘坐标
        mPaint.setTextSize(20);
        mPaint.setStrokeWidth(1.7f);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        for (int i = 0; i < MAX_LINE; i++) {
            float y = startPos + (0.5f + i) * lineHeight;
            float textTop = y - 0.5f * mLineHeight;
            float textBottom = y + 0.5f * mLineHeight;
            float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
            float x = 12;
            canvas.drawText(String.format("%S", MAX_LINE - i), x, baseline, mPaint);
        }
        for (int i = 0; i < MAX_LINE; i++) {
            float y = w - 12;
            float textTop = y - 0.5f * mLineHeight;
            float textBottom = y + 0.5f * mLineHeight;
            float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
            float x = startPos + (0.5f + i) * lineHeight;
            String text = String.valueOf((char) ('A' + i));
            canvas.drawText(text, x, baseline, mPaint);
        }
        //棋盘边缘线
        mPaint.setStrokeWidth(4.0f);
        int min = (int) lineHeight / 2 + 4, max = w - min;
        canvas.drawLine(min - 2, min, max + 2, min, mPaint);
        canvas.drawLine(min - 2, max, max + 2, max, mPaint);
        canvas.drawLine(min, min, min, max, mPaint);
        canvas.drawLine(max, min, max, max, mPaint);
        //画五个小黑点
        mPaint.setStrokeWidth(8f);
        final float startSize = 3.5f;
        final float endSize = MAX_LINE - 3.5f;
        final float middleSize = MAX_LINE / 2f;
        //左上
        canvas.drawCircle(startPos + startSize * lineHeight, startPos + startSize * lineHeight, 5, mPaint);
        //右上
        canvas.drawCircle(startPos + endSize * lineHeight, startPos + startSize * lineHeight, 5, mPaint);
        //左下
        canvas.drawCircle(startPos + startSize * lineHeight, startPos + endSize * lineHeight, 5, mPaint);
        //右下
        canvas.drawCircle(startPos + endSize * lineHeight, startPos + endSize * lineHeight, 5, mPaint);
        //中间
        canvas.drawCircle(startPos + middleSize * lineHeight, startPos + middleSize * lineHeight, 5, mPaint);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(4.0f);
        if (!mIsGameOver) {
            //标识最后一子
            if (chessArray.size() > 0) {
                drawCircle(canvas, chessArray.get(chessArray.size() - 1));
            }
            //标识建议位置
            if (suggestPoint != null) {
                mPaint.setColor(Color.GRAY);
                drawCircle(canvas, suggestPoint);
            }
        } else {
            //标识连五
            for (Point point : fiveArray) {
                drawCircle(canvas, point);
            }
        }
    }

    public void drawCircle(Canvas canvas, Point point) {
        float cx = startPos + (0.5f + point.x) * mLineHeight;
        float cy = startPos + (0.5f + point.y) * mLineHeight;
        float radius = ratioPieceOfLineHeight * mLineHeight / 2;
        canvas.drawCircle(cx, cy, radius, mPaint);
    }

    public void showSuggest(Point point) {
        this.suggestPoint = point;
        postInvalidate();
    }

    public void showChessNum() {
        isDrawChessNum = !isDrawChessNum;
        postInvalidate();
    }

    public void start() {
        for (int i = 0; i < MAX_LINE; i++) {
            for (int j = 0; j < MAX_LINE; j++) {
                mBoard[i][j] = 0;
            }
        }
        mBlackArray.clear();
        mWhiteArray.clear();
        chessArray.clear();
        suggestPoint = null;
        mIsGameOver = false;
        mWinner = 0;
        userChess = RenjuConstant.BLACK_CHESS;
        invalidate();
    }

    public boolean isFull() {
        for (int i = 0; i < MAX_LINE; i++) {
            for (int j = 0; j < MAX_LINE; j++) {
                if (mBoard[i][j] == RenjuConstant.N0_CHESS) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setUserChess(int userChess) {
        this.userChess = userChess;
    }

    public void setUserBout(boolean userBout) {
        isUserBout = userBout;
    }

    public void setCallBack(RenjuCallback callBack) {
        this.callBack = callBack;
    }

    public int getUserScore() {
        return userScore;
    }

    public int getAiScore() {
        return aiScore;
    }

    public boolean isGameOver() {
        return mIsGameOver;
    }

    public int getUserChess() {
        return userChess;
    }

    /**
     * 玩家是否执黑
     *
     * @return boolean
     */
    public boolean isUserBlack() {
        return userChess == (isHumanComputer() ? RenjuConstant.BLACK_CHESS : RenjuConstant.WHITE_CHESS);
    }

    /**
     * 是否人机
     *
     * @return 人机
     */
    public boolean isHumanComputer() {
        return gameMode == RenjuConstant.HUMAN_COMPUTER;
    }
}

```
主要都在onDraw方法中：
![坐标](https://img-blog.csdn.net/20180710105029760?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NDEzMjQ5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
![棋盘线和小黑点](https://img-blog.csdn.net/20180710105042151?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NDEzMjQ5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

用约束布局布置页面:
![布局页面](https://img-blog.csdn.net/20180710105347449?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NDEzMjQ5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

Ai实现类代码，也就是jar包代码，如果需要定制，可修改方法的访问修饰符:

```
package win.regin.renju

import android.graphics.Point
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * @author :Reginer in  2018/7/9 20:07.
 * 联系方式:QQ:282921012
 * 功能描述:五子棋ai
 */
class Ai(private val callBack: RenjuCallback) {
    var aiChess: Int = RenjuConstant.N0_CHESS
    private val aiObject: Long
    private var lastPoint: Point? = null
    private val timeMap = arrayListOf(2000, 5000, 10000, 30000, 60000)
    /**
     * ai是否正在思考
     */
    var isAiThing = false

    /**
     * 获取ai
     */
    private external fun getAiObject(): Long

    /**
     * 思考最佳点
     */
    private external fun getBestPoint(aiObject: Long, move: Int): Int

    /**
     * 重新开始
     */
    private external fun aiRestart(aiObject: Long)

    /**
     * 悔棋
     */
    private external fun aiUndo(aiObject: Long)

    /**
     * 玩家下一子
     */
    private external fun aiMove(aiObject: Long, move: Int)

    /**
     * 思考结束时间
     */
    private external fun setStepTime(aiObject: Long, time: Int)

    /**
     * 提示
     */
    private external fun getSuggest(aiObject: Long): Int

    init {
        this.aiObject = getAiObject()
    }

    /**
     * 当前回合轮到AI
     *
     * @param p p
     */
    fun aiBout(p: Point?) {
        launch {
            isAiThing = true
            lastPoint = p
            val lastMove: Int = if (null == lastPoint) {
                -1
            } else {
                lastPoint!!.x * 16 + lastPoint!!.y
            }

            val best = getBestPoint(aiObject, lastMove)
            val bestPoint = Point(getPointX(best), getPointY(best))
            launch(UI) {
                callBack.atBell(bestPoint, true, aiChess == RenjuConstant.BLACK_CHESS)
                isAiThing = false
            }
        }
    }

    fun addChess(p: Point) {
        lastPoint = p
        aiMove(aiObject, p.x * 16 + p.y)
    }

    /**
     * AI建议
     *
     * @return Point
     */
    fun suggest(): Point {
        isAiThing = true
        val p = getSuggest(aiObject)
        isAiThing = false
        return Point(getPointX(p), getPointY(p))
    }

    fun restart() {
        isAiThing = true
        aiRestart(aiObject)
        isAiThing = false
    }

    fun undo() {
        isAiThing = true
        aiUndo(aiObject)
        isAiThing = false
    }

    /**
     * 设置级别
     * @param level 0初级 1中级  2高级 3大师 4特大
     */
    fun setLevel(level: Int) {
        isAiThing = true
        setStepTime(aiObject, timeMap[level])
        isAiThing = false
    }

    /**
     * 获取玩家执子
     */
    fun getUserChess(): Int {
        return if (aiChess == RenjuConstant.WHITE_CHESS) RenjuConstant.BLACK_CHESS else RenjuConstant.WHITE_CHESS
    }

    private fun getPointX(p: Int): Int {
        return p shr 4
    }

    private fun getPointY(p: Int): Int {
        return p and 15
    }

    companion object {

        init {
            System.loadLibrary("renju-lib")
        }
    }
}

```

下面开始撸代码:

进入页面选择执子:
```
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
```

落子:

```
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
```

新局:

```
newRenju -> {
                if (rvRenju.isHumanComputer) {
                    mChooseChess?.showAtLocation(rvRenju, Gravity.CENTER, 0, 0)
                }
                mAi.restart()
                rvRenju.start()
            }
```

悔棋：

```
 undo -> {
                if (rvRenju.chessCount >= 2) {
                    mAi.undo()
                    rvRenju.undo()
                }
            }
```
Ai级别:
```
 level -> {
                var levelPosition = SpUtils[RenjuConstant.RENJU_LEVEL, 0] as Int
                levelPosition = if (levelPosition < 4) levelPosition + 1 else 0
                level.text = resources.getStringArray(R.array.renju_level)[levelPosition]
                SpUtils.put(RenjuConstant.RENJU_LEVEL,levelPosition)
                mAi.setLevel(levelPosition)
            }
```
提示:

```
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
```

模式：人机、人人:

```
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
```
游戏结束:

```
override fun gameOver(winner: Int) {
        SoundPlayUtils.play(HintConstant.GAME_OVER)
        toast(
                when (winner) {
                    RenjuConstant.BLACK_CHESS -> R.string.black_win
                    RenjuConstant.WHITE_CHESS -> R.string.white_win
                    else -> R.string.no_win
                })
    }
```