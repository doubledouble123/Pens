package com.wst.pens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
/**
 * Author:Double
 * Time:2019/4/22
 * Description:This is StrokesView
 */
public class StrokesView extends View{
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //笔划
    private Strokes strokes;
    private  DrawingStrokes mDrawing;
    private PenType penType;
    public StrokesView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        strokes = new Strokes();
        mDrawing = new DrawingStrokes(this, strokes);
        mDrawing.setMaxWidth(2);//刚笔
        penType = new PenType();
        penType.setPenType(PenType.PEN);
        mDrawing.setPenType(penType);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if(mDrawing != null) {
            mDrawing.setSize(canvas.getWidth(),canvas.getHeight(),mPaint);
            mDrawing.draw(canvas, mPaint);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mDrawing.moveTo(event.getX(), event.getY(), event.getPressure());
        } else if(action == MotionEvent.ACTION_MOVE){
            int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                float historicalX = event.getHistoricalX(i);
                float historicalY = event.getHistoricalY(i);
                //判断两点之间的距离是否太短
                double distance = Math.sqrt((historicalX - mDrawing.mPoint.get(mDrawing.mPoint.size() - 1).getX())
                        * (historicalX - mDrawing.mPoint.get(mDrawing.mPoint.size() - 1).getX())
                        + (historicalY - mDrawing.mPoint.get(mDrawing.mPoint.size() - 1).getY())
                        * (historicalY - mDrawing.mPoint.get(mDrawing.mPoint.size() - 1).getY()));
                if(mDrawing.mPoint.size() > 0 && distance > 0.2)
                    mDrawing.lineTo(historicalX, historicalY, event.getHistoricalPressure(i),false);
            }
        }else if(action == MotionEvent.ACTION_UP) {
            mDrawing.lineTo(event.getX(), event.getY(), event.getPressure(),true);
        }
        invalidate();
        return true;
    }

    public void reDo() {
        mDrawing.reDo();
    }

    public void unDo() {
        mDrawing.unDo();
    }

    public void recover() {
        mDrawing.recover();
    }

    public void setPenType(int penType) {
        this.penType.setPenType(penType);
        switch (penType) {
            case PenType.BRUSH:
                mDrawing.setMaxWidth(8);//毛笔
                break;
            case PenType.PEN:
                mDrawing.setMaxWidth(2);//刚笔
                break;
        }
    }

    public void onDestroy() {
        mDrawing.onDestroy();
    }
}

