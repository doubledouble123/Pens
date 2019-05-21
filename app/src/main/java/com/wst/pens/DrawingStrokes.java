package com.wst.pens;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.util.Vector;

/**
 * Author:Double
 * Time:2019/4/22
 * Description:This is DrawingStrokes
 */

public class DrawingStrokes {
    private final static String TAG = DrawingStrokes.class.getSimpleName();
    public Paint mPaint;
    public Vector<TimePoint> myPoints = new Vector<>();
    public Path strokesPath = null;
    public float lastLineX;
    public float lastLineY;

    public float mLastX;
    public float mLastY;
    public float mLLastX;
    public float mLLastY;
    public Bitmap myBitmap;
    public Canvas myCanvas;
    public Vector<TimePoint> mPoint;

    public TimePoint lastTop= new TimePoint();
    public TimePoint lastBottom=new TimePoint();
    public boolean isDown = false,isUp = false;
    public float mLastK = 0;
    public View strokeView;
    public Strokes strokes = null;
    //抬笔和操作笔划时的绘图
    public Canvas canvasStroke;
    public Bitmap bitmapStroke;

    public boolean isUnDo = false;
    public SplineCurveStrategy splineCurveStrategy;

    public int state = -1;
    public final static int X_ADD_Y_ADD = 0X00;
    public final static int X_ADD_Y_DEC = 0X01;
    public final static int X_ADD_Y_SAM = 0X02;
    public final static int X_DEC_Y_ADD = 0X03;
    public final static int X_DEC_Y_DEC = 0X04;
    public final static int X_DEC_Y_SAM = 0X05;
    public final static int X_SAM_Y_ADD = 0X06;
    public final static int X_SAM_Y_DEC = 0X07;
    public final static int X_SAM_Y_SAM = 0X08;
    public boolean debug = false;
    public float mLastWidth ;
    private float width, height;
    private float maxWidth;
    private PenType penType;
    public DrawingStrokes(View strokeView, Strokes strokes){
        this.strokes = strokes;
        this.strokeView = strokeView;
        this.strokesPath = new Path();
        mPoint = new Vector<>();
    }
    public void setSize(float width,float height,Paint mPaint){
        if (myBitmap != null) return;
        this.width = width;
        this.height = height;
        initBitmap();
        initBitmapStroke();
        if(this.mPaint == null) {
            this.mPaint = mPaint;
        }
    }
    public void setPenType(PenType penType) {
        this.penType = penType;
    }
    private void initBitmap(){
        if(myBitmap == null){
            myBitmap = Bitmap.createBitmap((int)width,(int)height, Bitmap.Config.ARGB_8888);
            myCanvas = new Canvas(myBitmap);
        }
    }

    private void initBitmapStroke(){
        if(bitmapStroke == null){
            bitmapStroke = Bitmap.createBitmap((int)width,(int)height, Bitmap.Config.ARGB_8888);
            canvasStroke = new Canvas(bitmapStroke);
        }
    }

    public float strokeWidth(float press,float widthDelta){
        float width = Math.min(maxWidth   , (0.1f * (1 + press * (maxWidth * 10 - 1) ))) * 0.9f + mLastWidth * 0.1f;
        if(width>mLastWidth)
            return Math.min(width  , mLastWidth + widthDelta);
        else
            return Math.max(width , mLastWidth - widthDelta);
    }

    public void setMaxWidth(float maxWidth){
        this.maxWidth = maxWidth;
        Log.i(TAG, "maxWidth " + maxWidth);
    }

    public float getMaxWidth() {
        return maxWidth;
    }
    public void addPoint(TimePoint timePoint,float pressure){
        mPoint.add(timePoint);

        if(mPoint.size() > 3){
            SplineCurve splineCurve = new SplineCurve(mPoint.get(0),
                    mPoint.get(1), mPoint.get(2), mPoint.get(3));
            float velocity = splineCurve.point3.velocityFrom(splineCurve.point2);
            float widthDelta = 0;
            float newWidth;
            if (false) {
                if (velocity > 3) {
                    splineCurve.steps = 4;
                    widthDelta = 0.8f;
                } else if (velocity > 2) {
                    splineCurve.steps = 3;
                    widthDelta = 0.7f;
                } else if (velocity > 1) {
                    splineCurve.steps = 3;
                    widthDelta = 0.6f;
                } else if (velocity > 0.5) {
                    splineCurve.steps = 2;
                    widthDelta = 0.5f;
                } else if (velocity > 0.2) {
                    splineCurve.steps = 2;
                    widthDelta = 0.4f;
                } else if (velocity > 0.1) {
                    splineCurve.steps = 1;
                    widthDelta = 0.3f;
                } else {
                    splineCurve.steps = 1;
                    widthDelta = 0.2f;
                }
                Log.i(TAG, "pressure: " + pressure);
                if (pressure < 0.4)
                    newWidth = strokeWidth(pressure, 1 - pressure);
                else
                    newWidth = strokeWidth(pressure, widthDelta);
            } else {
                if (velocity > 3) {
                    splineCurve.steps = 4;
                    widthDelta = 3.0f;
                } else if (velocity > 2) {
                    splineCurve.steps = 3;
                    widthDelta = 2.0f;
                } else if (velocity > 1) {
                    splineCurve.steps = 3;
                    widthDelta = 1.0f;
                } else if (velocity > 0.5) {
                    splineCurve.steps = 2;
                    widthDelta = 0.8f;
                } else if (velocity > 0.2) {
                    splineCurve.steps = 2;
                    widthDelta = 0.6f;
                } else if (velocity > 0.1) {
                    splineCurve.steps = 1;
                    widthDelta = 0.3f;
                } else {
                    splineCurve.steps = 1;
                    widthDelta = 0.2f;
                }
                newWidth = strokeWidth(pressure, widthDelta) ;
            }
            newWidth = Float.isNaN(newWidth) ? mLastWidth : newWidth;
            Log.i(TAG, "newWidth" + newWidth);
            if(strokes.getMyPathSize() >= 1) {
                strokes.getMyPathList().elementAt(strokes.getMyPathSize() - 1).addOriginPoint(new TimePoint(timePoint.x, timePoint.y));
                strokes.getMyPathList().elementAt(strokes.getMyPathSize() - 1).addOriginWidth(newWidth);
            }
            if(isUp){
                strokes.getMyPathList().elementAt(strokes.getMyPathSize() - 1).addOriginPoint(new TimePoint(timePoint.x, timePoint.y));
                strokes.getMyPathList().elementAt(strokes.getMyPathSize() - 1).addOriginWidth(newWidth);
            }
            if (splineCurveStrategy == null) {
                splineCurveStrategy = new SplineCurveStrategy(splineCurve, mLastWidth, newWidth, myCanvas, mPaint);
                splineCurveStrategy.initLastPoint(lastTop, lastBottom);
            } else {
                splineCurveStrategy.updateData(mLastWidth, newWidth, splineCurve);
            }
            Log.i("penType", penType.getPenType() + "");
            switch (penType.getPenType()) {
                case PenType.PEN:
                    splineCurveStrategy.drawPen(this);//钢笔
                    break;
                case PenType.BRUSH:
                    splineCurveStrategy.drawBrushPen(this);//毛笔
                    break;
            }
            mPoint.remove(0);
        }
    }

    public double mult(float x1,float y1,float x2,float y2,float x3,float y3){
        return (x1 - x3)*(y2 - y3) - (x2 - x3)*(y1 - y3);
    }
    public boolean intersect(float x1,float y1,float x2,float y2,float x3,float y3,
                             float x4,float y4){
        if(Math.max(x1,x2)<Math.min(x3,x4)){
            return  false;
        }
        if(Math.max(y1,y2)<Math.min(y3,y4)){
            return false;
        }
        if(Math.max(x3,x4)<Math.min(x1,x2)){
            return false;
        }
        if(Math.max(y3,y4)<Math.min(y1,y2)){
            return false;
        }
        if(mult(x3,y3,x2,y2,x1,y1)*mult(x2,y2,x4,y4,x1,y1)<0){
            return false;
        }
        if(mult(x1,y1,x4,y4,x3,y3)*mult(x4,y4,x2,y2,x3,y3)<0){
            return false;
        }
        return true;
    }
    public float calculateDegree(float x1,float y1,float x2,float y2,float x3,float y3){
        float b = (float)Math.sqrt((x1 - x2)*( x1 - x2) + (y1 - y2) * (y1 - y2));
        float c = (float)Math.sqrt((x2 - x3)*( x2 - x3) +
                (y2 - y3) * (y2 - y3));
        float a = (float)Math.sqrt((x1 - x3)*( x1 - x3) +
                (y1 - y3) * (y1 - y3));
        if(c==0||b==0) return 0;
        float sum = (b * b + c * c - a * a)/(2*b*c);
        float degree =(float) Math.acos(sum) * 180 / (float)Math.PI;
        Log.i(TAG, "degree : " + degree);
        if(Float.isNaN(degree)) degree = 0;
        return  degree;
    }
    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //当所有笔划都画到bitmapStroke上时
            //清除myBitmap上的笔划  这步很重要
            if(myCanvas!=null) {
                myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                myCanvas.drawBitmap(bitmapStroke, 0, 0, mPaint);
                strokeView.invalidate();
            }
        }
    };
    public void updatePathToCanvas(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                canvasStroke.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                strokes.draw(canvasStroke,mPaint);
                myHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    public void moveTo(float x,float y,float pressure){
        strokesPath.reset();
        myPoints.clear();
        mPoint.clear();
        isDown = true;
        isUp = false;
        mLLastX = x;
        mLLastY = y;
        mLastX = x;
        mLastY = y;
        mLastWidth = Math.min(maxWidth  , 0.1f * (1 + (pressure+0.2f) * (maxWidth * 10 - 1) ));
        mLastK = 0;
        strokes.addMyPath(strokes.getMyPathSize(), strokes.getMyPathSize() + strokes.getRecycleStrokesListSize());
        addPoint(new TimePoint(x, y), pressure);
        addPoint(new TimePoint(x, y), pressure);
        strokes.getMyPathList().elementAt(strokes.getMyPathSize()-1).addOriginPoint(new TimePoint(x, y));
        strokes.getMyPathList().elementAt(strokes.getMyPathSize()-1).addOriginPoint(new TimePoint(x,y));
        strokes.getMyPathList().elementAt(strokes.getMyPathSize()-1).addOriginWidth(mLastWidth);
        strokes.getMyPathList().elementAt(strokes.getMyPathSize()-1).addOriginWidth(mLastWidth);
    }
    public void lineTo(float x,float y,float pressure,boolean isUp){
        if (isUp) {
            addPoint(new TimePoint(x, y), pressure);
            this.isUp = true;
            addPoint(new TimePoint(x, y), pressure);
            for(int i = myPoints.size() - 1;i>=0;i--){
                strokesPath.lineTo(myPoints.elementAt(i).getX(),myPoints.elementAt(i).getY());
                strokes.getMyPathList().get(strokes.getMyPathSize()-1).addPoint(new TimePoint(myPoints.elementAt(i).getX(),myPoints.elementAt(i).getY()));
            }
            myPoints.clear();
            strokes.getMyPathList().elementAt(strokes.getMyPathSize()-1).setStroke(strokesPath);
            setUnDo(false);
            Log.i(TAG, "拟合前的点数量" +  strokes.getMyPathList().elementAt(strokes.getMyPathSize() - 1).getOriginPoints().size());
            Log.i(TAG,"拟合后的点数量" + strokes.getMyPathList().elementAt(strokes.getMyPathSize() - 1).getPoints().size());
        } else {
            addPoint(new TimePoint(x, y), pressure);
        }

    }

    public void draw(Canvas canvas,Paint mPaint) {
        // TODO Auto-generated method stub
        canvas.drawBitmap(myBitmap, 0, 0, mPaint);
    }

    //撤销
    public void unDo(){
        Log.i(TAG, "unDo");
        setUnDo(true);
        //首先判断两个vector中笔划的优先级
        int recycleVectorPriority = -1;
        int myVectorPriority = -1;
        int myPicturePriority = -1;
        int recyclePicturePriority = -1;
        if(strokes.getMyPathSize() > 0){
            myVectorPriority = strokes.getMyPathList().elementAt(strokes.getMyPathSize()-1).getPriority();
        }
        if(strokes.getRecycleStrokesListSize() > 0){
            recycleVectorPriority = strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getPriority();
        }
        //说明有可以撤销的笔划
        if(recycleVectorPriority != -1 || myVectorPriority != -1||myPicturePriority!=-1||recyclePicturePriority!=-1){
            if(myVectorPriority >= recycleVectorPriority && myVectorPriority >= myPicturePriority &&myVectorPriority>=recyclePicturePriority){
                //将笔划压入撤销栈
                strokes.addUnDoStrokes(strokes.getMyPathList().elementAt(strokes.getMyPathSize()-1));
                //移除myPath最后一个
                strokes.deleteMyPath(strokes.getMyPathSize()-1);
            }

            if(recycleVectorPriority>=myPicturePriority&&recycleVectorPriority>=myVectorPriority&&recycleVectorPriority>=recyclePicturePriority){//可能一次会撤销很多笔划   因为当初可能一次删除多个笔划  所以要循环
                int priority = -1;
                do {
                    //降低回收栈中笔划的优先级
                    strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).setPriority(
                            strokes.getMyPathList().elementAt(strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getLocation()).getPriority());
                    //设置撤销栈中笔划的优先级继承回收站的  方便恢复多个笔划
                    strokes.getMyPathList().elementAt(strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getLocation()).setPriority(
                            recycleVectorPriority);
                    //将myPath对应位置的笔划压入撤销栈
                    Log.i(TAG,strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getLocation()+" ");
                    strokes.addUnDoStrokes(strokes.getMyPathList().elementAt(strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getLocation()));
                    //移除myPath对应位置的笔划
                    strokes.deleteMyPath(strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getLocation());
                    strokes.getMyPathList().add(strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getLocation(),
                            strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1));
                    strokes.deleteRecycleStrokesList(strokes.getRecycleStrokesListSize() - 1);
                    //继续判断下一个笔划的优先级
                    priority = -1;
                    if(strokes.getRecycleStrokesListSize() > 0)
                        priority = strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).getPriority();
                }while(priority == recycleVectorPriority);
            }
            updatePathToCanvas();
        }

    }
    public void reDo(){
        Log.i(TAG, "reDo");
        //对三个栈都做清空
        if(isUnDo) setUnDo(false);
        strokes.getMyPathList().clear();
        strokes.getRecycleStrokesList().clear();
        updatePathToCanvas();
    }
    public void clear(){
        //对三个栈都做清空
        if(isUnDo) setUnDo(false);
        strokes.getMyPathList().clear();
        strokes.getRecycleStrokesList().clear();
    }

    public void recover(){
        //上一步必须是撤销
        if(isUnDo){
            Log.i(TAG, "recover");
            int strokePriority = -1;
            int strokeSize = strokes.getUnDoStrokesList().size();
            if(strokeSize>0)
                strokePriority = strokes.getUnDoStrokesList().elementAt(strokeSize - 1).getPriority();
            int picturePriority = -1;
            Log.i(TAG,strokePriority+" "+picturePriority);
            if(strokePriority!=-1||picturePriority!=-1) {
                int addPriority = strokes.getMyPathSize() + strokes.getRecycleStrokesListSize();
                if ((picturePriority!=-1&&strokePriority <= picturePriority && strokePriority!=-1)||
                        (picturePriority==-1&&strokePriority!=-1)) {
                    //可能是恢复多个
                    int priority = -1;
                    int finalPriority = strokePriority;
                    do {
                        //恢复的位置是插入
                        if (strokes.getUnDoStrokesList().elementAt(strokeSize - 1).getLocation() < strokes.getMyPathSize()) {
                            //降低撤销栈中笔划的优先级
                            strokes.getUnDoStrokesList().elementAt(strokeSize - 1).setPriority(
                                    strokes.getMyPathList().elementAt(strokes.getUnDoStrokesList().elementAt(strokeSize - 1).getLocation()).getPriority()
                            );
                            //将myPath对应位置上的压入回收栈
                            strokes.getRecycleStrokesList().add(strokes.getMyPathList().elementAt(strokes.getUnDoStrokesList().elementAt(strokeSize - 1).getLocation()));
                            //增加优先级
                            strokes.getRecycleStrokesList().elementAt(strokes.getRecycleStrokesListSize() - 1).setPriority(addPriority);
                            //删除myPath对应的笔划
                            strokes.deleteMyPath(strokes.getUnDoStrokesList().elementAt(strokeSize - 1).getLocation());
                        }
                        //将撤销栈中最后一个压入myPath对应的位置上
                        strokes.getMyPathList().add(strokes.getUnDoStrokesList().elementAt(strokeSize - 1).getLocation(),
                                strokes.getUnDoStrokesList().elementAt(strokeSize - 1));
                        //删除撤销栈最后一个
                        strokes.deleteUnDoStrokesList(strokeSize - 1);
                        //继续判断下一个笔划的优先级
                        priority = -1;
                        strokeSize = strokes.getUnDoStrokesList().size();
                        if (strokeSize > 0)
                            priority = strokes.getUnDoStrokesList().elementAt(strokeSize - 1).getPriority();
                    } while (priority == finalPriority);
                    updatePathToCanvas();
                }
                if ((strokePriority!=-1&&strokePriority >= picturePriority&&picturePriority!=-1)||
                        (strokePriority==-1&&picturePriority!=-1)) {
                    //可能是恢复多个
                    int priority = -1;
                    int finalPriority = picturePriority;
                    updatePathToCanvas();
                }
            }
        }
    }

    public boolean isUnDo() {
        return isUnDo;
    }

    public void setUnDo(boolean unDo) {
        isUnDo = unDo;
        if(!unDo){
            strokes.clearUnDoStrokesList();
        }
    }
    public void onDestroy(){
        if(myBitmap!=null&&!myBitmap.isRecycled()){
            myCanvas = null;
            myBitmap.recycle();
            myBitmap = null;
        }
        if(bitmapStroke != null && !bitmapStroke.isRecycled()){
            canvasStroke = null;
            bitmapStroke.recycle();
            bitmapStroke = null;
        }
        setUnDo(false);
        clear();
        myPoints.clear();
    }
}
