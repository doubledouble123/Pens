package com.wst.pens;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import java.util.Random;

/**
 * Author:Double
 * Time:2019/4/22
 * Description:This is SplineCurveStrategy
 */
public class SplineCurveStrategy {
    public int curveIndex = 2;
    public float startWidth;
    public float endWidth;
    public SplineCurve splineCurve;
    public Paint blurryPaint;
    public Paint mMosaicPaint;
    public Paint eraserPaint;
    public final int eraserWidth = 50;
    protected Canvas canvas;
    protected Paint mPaint;
    public TimePoint lastTop,lastBottom;
    protected Path mPath;
    public SplineCurveStrategy(SplineCurve splineCurve, float startWidth, float endWidth, Canvas canvas, Paint mPaint){
        this.splineCurve = splineCurve;
        this.startWidth = startWidth;
        this.endWidth = endWidth;
        this.blurryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mMosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.eraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.eraserPaint.setColor(Color.WHITE);
        this.eraserPaint.setStrokeWidth(eraserWidth);
        this.eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        this.eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mMosaicPaint.setAlpha(80);
        mMosaicPaint.setStrokeCap(Paint.Cap.BUTT);
        mMosaicPaint.setStrokeJoin(Paint.Join.ROUND);
        //mMosaicPaint.setStyle(Paint.Style.FILL);
        mMosaicPaint.setStrokeWidth(12f);
        this.canvas = canvas;
        this.mPaint = mPaint;
        this.mPath = new Path();
    }

    public void updateData(float startWidth,float endWidth, SplineCurve splineCurve){
        this.splineCurve = splineCurve;
        this.startWidth = startWidth;
        this.endWidth = endWidth;
    }

    public double mult(float x1,float y1,float x2,float y2,float x3,float y3){
        return (x1 - x3)*(y2 - y3) - (x2 - x3)*(y1 - y3);
    }
    public void initLastPoint(TimePoint lastTop,TimePoint lastBottom){
        this.lastTop = lastTop;
        this.lastBottom = lastBottom;
    }
    public void drawPen(DrawingStrokes drawingStrokes) {
        if(drawingStrokes.debug)
            mPaint.setStyle(Paint.Style.STROKE);
        else mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        //获得笔在两点间不同宽度的差值
        int drawSteps = (int)Math.floor(splineCurve.length());
        if(drawingStrokes.isUp) {
            //curveIndex = 1;
            if(drawSteps > 2)
                curveIndex = (drawSteps - 2)/2;
            else curveIndex = 1;
            if(curveIndex < 1) curveIndex = 1;
            if(drawSteps == 0) drawSteps = 2;
        }else if(drawingStrokes.isDown){
            curveIndex = 1;
            if(drawSteps == 0) drawSteps = 2;
        }else{
            if(drawSteps > 100) curveIndex = 40;
            else if(drawSteps > 80) curveIndex = 35;
            else if(drawSteps > 70) curveIndex = 30;
            else if(drawSteps > 60) curveIndex = 25;
            else if(drawSteps > 50) curveIndex = 20;
            else if(drawSteps > 40) curveIndex = 15;
            else if(drawSteps > 30) curveIndex = 13;
            else if(drawSteps > 20) curveIndex = 9;
            else if(drawSteps > 10) curveIndex = 7;
            else if(drawSteps >= 4) curveIndex = 3;
            else curveIndex = 1;
        }
        float widthDelta = endWidth - startWidth;
        //两点间实际轨迹距离
        float k = 0;
        TimePoint myPointC,myPointD,myPointA,myPointB;
        //危险
        boolean modify = false;
        if(drawSteps==0) {
            drawSteps = 1;
            modify = true;
        }
        Log.i("w_pen",drawSteps+" "+curveIndex);
//        curveIndex = drawSteps/2;
//        if(curveIndex==0) curveIndex=1;
        for(int i = 0,num = 1; i < drawSteps; i+=curveIndex,num++){
            mPath.reset();
            float t = (float)(i) / drawSteps;
            float tt = t * t;
            float ttt = tt * t;
            float u = 1 - t;
            float uu = u * u;
            float uuu = uu * u;
            float x = uuu * splineCurve.point1.x / 6.0f;
            x += (3*ttt-6*tt+4) * splineCurve.point2.x/ 6.0f;
            x += (-3*ttt+3*tt+3*t+1)* splineCurve.point3.x/ 6.0f;
            x += ttt * splineCurve.point4.x/ 6.0f;
            float y = uuu * splineCurve.point1.y/ 6.0f;
            y += (3*ttt-6*tt+4) * splineCurve.point2.y/ 6.0f;
            y += (-3*ttt+3*tt+3*t+1) * splineCurve.point3.y/ 6.0f;
            y += ttt * splineCurve.point4.y/ 6.0f;
            float currentWidth = startWidth + t * widthDelta ;
            if(!drawingStrokes.isUp)
                if(Math.abs(t*widthDelta)>0.2f*num) {
                    if(t*widthDelta>0)
                        currentWidth = startWidth + 0.2f*num;
                    else currentWidth = startWidth - 0.2f*num;
                }
            int currentState = 0;
            float numX = x - drawingStrokes.mLastX;
            float numY = y - drawingStrokes.mLastY;
            if(numX > 0 && numY >0) currentState = drawingStrokes.X_ADD_Y_ADD;
            if(numX > 0 && numY < 0) currentState = drawingStrokes.X_ADD_Y_DEC;
            if(numX > 0 && numY == 0) currentState = drawingStrokes.X_ADD_Y_SAM;
            if(numX < 0 && numY > 0) currentState = drawingStrokes.X_DEC_Y_ADD;
            if(numX < 0 && numY <0) currentState = drawingStrokes.X_DEC_Y_DEC;
            if(numX < 0 && numY == 0) currentState = drawingStrokes.X_DEC_Y_SAM;
            if(numX == 0 && numY > 0) currentState = drawingStrokes.X_SAM_Y_ADD;
            if(numX == 0 && numY < 0) currentState = drawingStrokes.X_SAM_Y_DEC;
            if(numX == 0 && numY == 0) currentState = drawingStrokes.X_SAM_Y_SAM;
//            if(drawingStrokes.state!=currentState&&modify&&!drawingStrokes.isUp&&!drawingStrokes.isDown){//保证转弯处不恶意修改值
//                    break;
//            }
            if( x != drawingStrokes.mLastX){
                k = (y - drawingStrokes.mLastY) / (x - drawingStrokes.mLastX);
                //上个点的上下端点MyPointA,MyPointB
                myPointA = new TimePoint( (drawingStrokes.mLastWidth / 2 )* (-k) / (float) Math.sqrt(k * k + 1) + drawingStrokes.mLastX,
                        (drawingStrokes.mLastWidth / 2 ) / (float) Math.sqrt(k * k + 1) + drawingStrokes.mLastY);
                myPointB = new TimePoint( (-drawingStrokes.mLastWidth / 2 )* (-k) / (float) Math.sqrt(k * k + 1) + drawingStrokes.mLastX,
                        (-drawingStrokes.mLastWidth / 2 ) / (float) Math.sqrt(k * k + 1) + drawingStrokes.mLastY);
                //当前点的上下端点MyPointC,MyPointD
                myPointC = new TimePoint( (currentWidth / 2 )* (-k) / (float) Math.sqrt(k * k + 1) + x,
                        (currentWidth / 2 ) / (float) Math.sqrt(k * k + 1) + y);
                myPointD = new TimePoint( (-currentWidth / 2 )* (-k) / (float) Math.sqrt(k * k + 1) + x,
                        (-currentWidth / 2 ) / (float) Math.sqrt(k * k + 1) + y);

            }else{
                myPointA = new TimePoint( drawingStrokes.mLastWidth / 2  + drawingStrokes.mLastX,
                        drawingStrokes.mLastY);
                myPointB = new TimePoint( -drawingStrokes.mLastWidth / 2  + drawingStrokes.mLastX,
                        drawingStrokes.mLastY);
                myPointC = new TimePoint( currentWidth / 2  + x,
                        y);
                myPointD = new TimePoint( -currentWidth / 2  + x,
                        y);
            }
            if(drawingStrokes.isDown){//起点  需要算AB
                //算出矩形的四个点
                TimePoint A,B,C,D;
                if( myPointA.x != myPointB.x){
                    k = (myPointA.y - myPointB.y) / (myPointA.x - myPointB.x);
                    A = new TimePoint( (drawingStrokes.mLastWidth  )* (-k) / (float) Math.sqrt(k * k + 1) + myPointA.x,
                            (drawingStrokes.mLastWidth  ) / (float) Math.sqrt(k * k + 1) + myPointA.y);
                    B = new TimePoint( (-drawingStrokes.mLastWidth  )* (-k) / (float) Math.sqrt(k * k + 1) + myPointA.x,
                            (-drawingStrokes.mLastWidth  ) / (float) Math.sqrt(k * k + 1) + myPointA.y);
                    //当前点的上下端点MyPointC,MyPointD
                    C = new TimePoint( (drawingStrokes.mLastWidth  )* (-k) / (float) Math.sqrt(k * k + 1) + myPointB.x,
                            (drawingStrokes.mLastWidth  ) / (float) Math.sqrt(k * k + 1) + myPointB.y);
                    D = new TimePoint( (-drawingStrokes.mLastWidth  )* (-k) / (float) Math.sqrt(k * k + 1) + myPointB.x,
                            (-drawingStrokes.mLastWidth ) / (float) Math.sqrt(k * k + 1) + myPointB.y);

                }else{
                    A = new TimePoint( drawingStrokes.mLastWidth   + myPointA.x,
                            myPointA.y);
                    B = new TimePoint( -drawingStrokes.mLastWidth   + myPointA.x,
                            myPointA.y);
                    C = new TimePoint( drawingStrokes.mLastWidth   + myPointB.x,
                            myPointB.y);
                    D = new TimePoint( -drawingStrokes.mLastWidth / 2  + myPointB.x,
                            myPointB.y);
                }
                TimePoint centerAC = new TimePoint((A.x+C.x)/2,(A.y+C.y)/2);
                TimePoint centerBD = new TimePoint((B.x+D.x)/2,(B.y+D.y)/2);
                boolean isAC = true;
                if( myPointA.x != myPointB.x){
                    float b = myPointA.y - k *myPointA.x;
                    if((centerAC.y - k* centerAC.x - b)*(drawingStrokes.mPoint.get(3).y - k*drawingStrokes.mPoint.get(3).x - b)<=0){
                        isAC = true;
                    }else {
                        isAC = false;
                    }
//                    if ((drawingStrokes.mLastY - centerAC.y) / (drawingStrokes.mLastX - centerAC.x) * k >0) {
//                        isAC = true;
//                    } else {
//                        isAC = false;
//                    }
                }else{
                    if((centerAC.y-myPointA.y)*(drawingStrokes.mPoint.get(3).y - myPointA.y)<=0){
                        isAC = true;
                    }else {
                        isAC = false;
                    }
                }
                if(!drawingStrokes.debug) {
                    //填充
//                    mPath.moveTo(myPointB.x, myPointB.y);
//                    mPath.lineTo(myPointD.x, myPointD.y);
////                    //**mPath.lineTo(myPointC.x, myPointC.y);
//                    mPath.lineTo(myPointC.x, myPointC.y);
//                    mPath.lineTo(myPointA.x, myPointA.y);
                    mPath.moveTo(myPointB.x, myPointB.y);
                    if(isAC)
                        mPath.quadTo(centerAC.x,centerAC.y,myPointA.x,myPointA.y);
                    else mPath.quadTo(centerBD.x,centerBD.y,myPointA.x,myPointA.y);
                    mPath.lineTo(myPointC.x, myPointC.y);
                    mPath.lineTo(myPointD.x, myPointD.y);
                    mPath.lineTo(myPointB.x, myPointB.y);
                    canvas.drawPath(mPath,mPaint);
                    mPath.reset();
                }else {
                    //测试
//                    mPath.moveTo(myPointB.x, myPointB.y);
//                    mPath.lineTo(myPointD.x, myPointD.y);
//                    //**mPath.lineTo(myPointC.x, myPointC.y);
//                    mPath.moveTo(myPointC.x, myPointC.y);
//                    mPath.lineTo(myPointA.x, myPointA.y);

                    mPath.moveTo(myPointB.x, myPointB.y);
                    if(isAC)
                        mPath.quadTo(centerAC.x,centerAC.y,myPointA.x,myPointA.y);
                    else mPath.quadTo(centerBD.x,centerBD.y,myPointA.x,myPointA.y);
                    mPath.lineTo(myPointC.x, myPointC.y);
                    mPath.moveTo(myPointB.x, myPointB.y);
                    mPath.lineTo(myPointD.x, myPointD.y);
                    mPath.lineTo(myPointB.x, myPointB.y);
                }

                drawingStrokes.isDown = false;

                drawingStrokes.strokesPath.moveTo(myPointB.x, myPointB.y);
                //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointB.x,myPointB.y));
                if(isAC) {
                    drawingStrokes.strokesPath.quadTo(centerAC.x, centerAC.y, myPointA.x, myPointA.y);
                    drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new TimePoint(centerAC.x, centerAC.y));
                }
                else {
                    drawingStrokes.strokesPath.quadTo(centerBD.x, centerBD.y, myPointA.x, myPointA.y);
                    drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new TimePoint(centerBD.x, centerBD.y));
                }
             //   drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointA.x, myPointA.y));
                drawingStrokes.strokesPath.lineTo(myPointC.x, myPointC.y);
               // drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointC.x, myPointC.y));
                drawingStrokes.lastLineX = myPointC.x;
                drawingStrokes.lastLineY = myPointC.y;
                //drawingStrokes.myPoints.add(new MyPoints(myPointA.x,myPointA.y));
                drawingStrokes.myPoints.add(new TimePoint(myPointB.x,myPointB.y));
                drawingStrokes.myPoints.add(new TimePoint(myPointD.x,myPointD.y));

            }else{
                //相交为180
                if((drawingStrokes.mLLastX == drawingStrokes.mLastX &&drawingStrokes.mLastX == x)
                        ||(drawingStrokes.mLLastX != drawingStrokes.mLastX &&drawingStrokes.mLastX != x && (k == drawingStrokes.mLastK || -k == drawingStrokes.mLastK))){
                    if (-k == drawingStrokes.mLastK&&k!=0) {//特殊情况
                        Log.d("123","特殊来了"+k);
                    }
                }else{
                    //判断外端点画弧
                    float degereeA = drawingStrokes.calculateDegree(drawingStrokes.mLastX,drawingStrokes.mLastY,
                            drawingStrokes.mLLastX, drawingStrokes.mLLastY, myPointA.x,myPointA.y);
                    float degereeB = drawingStrokes.calculateDegree(drawingStrokes.mLastX,drawingStrokes.mLastY,
                            drawingStrokes.mLLastX, drawingStrokes.mLLastY, myPointB.x,myPointB.y);
                    float degereeLT = drawingStrokes.calculateDegree(drawingStrokes.mLastX,drawingStrokes.mLastY,
                            x, y, lastTop.x,lastTop.y);
                    float degereeLB = drawingStrokes.calculateDegree(drawingStrokes.mLastX,drawingStrokes.mLastY,
                            x, y, lastBottom.x,lastBottom.y);
                    //谁大谁是外端点
                    if ((degereeA >= degereeB&&degereeLT >= degereeLB)||(degereeA <= degereeB&&degereeLT <= degereeLB)) {
                        if(!drawingStrokes.debug) {
                            //填充
                            mPath.moveTo(myPointA.x, myPointA.y);
                            mPath.lineTo(lastTop.x, lastTop.y);
                            mPath.lineTo(lastBottom.x, lastBottom.y);
                            mPath.lineTo(myPointB.x, myPointB.y);
                            mPath.lineTo(myPointA.x, myPointA.y);
                            canvas.drawPath(mPath,mPaint);
                            mPath.reset();
                        }else {
                            //测试
                            mPath.moveTo(myPointA.x, myPointA.y);
                            mPath.lineTo(lastTop.x, lastTop.y);
                            mPath.moveTo(lastBottom.x, lastBottom.y);
                            mPath.lineTo(myPointB.x, myPointB.y);
                        }
                        if(drawingStrokes.lastLineX == lastTop.x && drawingStrokes.lastLineY == lastTop.y){
                            drawingStrokes.strokesPath.lineTo(myPointA.x, myPointA.y);
                            //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointA.x, myPointA.y));
                            drawingStrokes.myPoints.add(new TimePoint(myPointB.x,myPointB.y));
                            drawingStrokes.lastLineX = myPointA.x;
                            drawingStrokes.lastLineY = myPointA.y;
                        }else{
                            drawingStrokes.strokesPath.lineTo(myPointB.x, myPointB.y);
                       //     drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointB.x, myPointB.y));
                            drawingStrokes.myPoints.add(new TimePoint(myPointA.x,myPointA.y));
                            drawingStrokes.lastLineX = myPointB.x;
                            drawingStrokes.lastLineY = myPointB.y;
                        }
                    } else {
                        //测试
                        if (drawingStrokes.intersect(myPointA.x, myPointA.y, lastBottom.x, lastBottom.y, x, y, drawingStrokes.mLastX, drawingStrokes.mLastY)
                                || drawingStrokes.intersect(myPointA.x, myPointA.y, lastBottom.x, lastBottom.y, drawingStrokes.mLLastX, drawingStrokes.mLLastY,
                                drawingStrokes.mLastX, drawingStrokes.mLastY)) {
                            //转弯了
                            if(drawingStrokes.state!=-1&&drawingStrokes.state != currentState) {
                                if(!drawingStrokes.debug) {
                                    //填充
                                    mPath.moveTo(myPointA.x, myPointA.y);
                                    mPath.lineTo(lastBottom.x, lastBottom.y);
                                    mPath.lineTo(lastTop.x, lastTop.y);
                                    mPath.lineTo(myPointB.x, myPointB.y);
                                    mPath.lineTo(myPointA.x, myPointA.y);
                                    canvas.drawPath(mPath,mPaint);
                                    mPath.reset();
                                }else {
                                    //测试
                                    mPath.moveTo(myPointA.x, myPointA.y);
                                    mPath.lineTo(lastBottom.x, lastBottom.y);
                                    mPath.moveTo(lastTop.x, lastTop.y);
                                    mPath.lineTo(myPointB.x, myPointB.y);
                                }
                                if(drawingStrokes.lastLineX == lastBottom.x && drawingStrokes.lastLineY == lastBottom.y){
                                    drawingStrokes.strokesPath.lineTo(myPointA.x, myPointA.y);
                                    //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointA.x, myPointA.y));
                                    drawingStrokes.myPoints.add(new TimePoint(myPointB.x,myPointB.y));
                                    drawingStrokes.lastLineX = myPointA.x;
                                    drawingStrokes.lastLineY = myPointA.y;
                                }else{
                                    drawingStrokes.strokesPath.lineTo(myPointB.x, myPointB.y);
                                   // drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointB.x, myPointB.y));
                                    drawingStrokes.myPoints.add(new TimePoint(myPointA.x,myPointA.y));
                                    drawingStrokes.lastLineX = myPointB.x;
                                    drawingStrokes.lastLineY = myPointB.y;
                                }
                            }else{
                                if(!drawingStrokes.debug) {
                                    //填充
                                    mPath.moveTo(myPointA.x, myPointA.y);
                                    mPath.lineTo(lastTop.x, lastTop.y);
                                    mPath.lineTo(lastBottom.x, lastBottom.y);
                                    mPath.lineTo(myPointB.x, myPointB.y);
                                    mPath.lineTo(myPointA.x, myPointA.y);
                                    canvas.drawPath(mPath,mPaint);
                                    mPath.reset();
                                }else {
                                    //测试
                                    mPath.moveTo(myPointA.x, myPointA.y);
                                    mPath.lineTo(lastTop.x, lastTop.y);
                                    mPath.moveTo(lastBottom.x, lastBottom.y);
                                    mPath.lineTo(myPointB.x, myPointB.y);
                                }
                                if(drawingStrokes.lastLineX == lastTop.x && drawingStrokes.lastLineY == lastTop.y){
                                    drawingStrokes.strokesPath.lineTo(myPointA.x, myPointA.y);
                                  //  drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointA.x, myPointA.y));
                                    drawingStrokes.myPoints.add(new TimePoint(myPointB.x,myPointB.y));
                                    drawingStrokes.lastLineX = myPointA.x;
                                    drawingStrokes.lastLineY = myPointA.y;
                                }else{
                                    drawingStrokes.strokesPath.lineTo(myPointB.x, myPointB.y);
                                  //  drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointB.x, myPointB.y));
                                    drawingStrokes.myPoints.add(new TimePoint(myPointA.x,myPointA.y));
                                    drawingStrokes.lastLineX = myPointB.x;
                                    drawingStrokes.lastLineY = myPointB.y;
                                }
                            }
                        }else{
                            if(!drawingStrokes.debug) {
                                //填充
                                mPath.moveTo(myPointA.x, myPointA.y);
                                mPath.lineTo(lastBottom.x, lastBottom.y);
                                mPath.lineTo(lastTop.x, lastTop.y);
                                mPath.lineTo(myPointB.x, myPointB.y);
                                mPath.lineTo(myPointA.x, myPointA.y);
                                canvas.drawPath(mPath,mPaint);
                                mPath.reset();
                            }else {
                                //测试
                                mPath.moveTo(myPointA.x, myPointA.y);
                                mPath.lineTo(lastBottom.x, lastBottom.y);
                                mPath.moveTo(lastTop.x, lastTop.y);
                                mPath.lineTo(myPointB.x, myPointB.y);
                            }
                            if(drawingStrokes.lastLineX == lastBottom.x && drawingStrokes.lastLineY == lastBottom.y){
                                drawingStrokes.strokesPath.lineTo(myPointA.x, myPointA.y);
                                //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointA.x, myPointA.y));
                                drawingStrokes.myPoints.add(new TimePoint(myPointB.x,myPointB.y));
                                drawingStrokes.lastLineX = myPointA.x;
                                drawingStrokes.lastLineY = myPointA.y;
                            }else{
                                drawingStrokes.strokesPath.lineTo(myPointB.x, myPointB.y);
                               // drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointB.x, myPointB.y));
                                drawingStrokes.myPoints.add(new TimePoint(myPointA.x,myPointA.y));
                                drawingStrokes.lastLineX = myPointB.x;
                                drawingStrokes.lastLineY = myPointB.y;
                            }
                        }
                    }
                }
                if(!drawingStrokes.debug) {
                    //填充
                    mPath.moveTo(myPointA.x, myPointA.y);
                    mPath.lineTo(myPointC.x, myPointC.y);
                    mPath.lineTo(myPointD.x, myPointD.y);
                    mPath.lineTo(myPointB.x, myPointB.y);
                    mPath.lineTo(myPointA.x, myPointA.y);
                    canvas.drawPath(mPath,mPaint);
                    mPath.reset();
                }else {
                    //测试
                    mPath.moveTo(myPointA.x, myPointA.y);
                    mPath.lineTo(myPointC.x, myPointC.y);
                    mPath.moveTo(myPointD.x, myPointD.y);
                    mPath.lineTo(myPointB.x, myPointB.y);
                }
                if(drawingStrokes.lastLineX == myPointA.x && drawingStrokes.lastLineY == myPointA.y){
                    drawingStrokes.strokesPath.lineTo(myPointC.x, myPointC.y);
                    //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointC.x, myPointC.y));
                    drawingStrokes.myPoints.add(new TimePoint(myPointD.x,myPointD.y));
                    drawingStrokes.lastLineX = myPointC.x;
                    drawingStrokes.lastLineY = myPointC.y;
                }else{
                    drawingStrokes.strokesPath.lineTo(myPointD.x, myPointD.y);
                    //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointD.x, myPointD.y));
                    drawingStrokes.myPoints.add(new TimePoint(myPointC.x,myPointC.y));
                    drawingStrokes.lastLineX = myPointD.x;
                    drawingStrokes.lastLineY = myPointD.y;
                }
            }
            if(drawingStrokes.isUp && i >= drawSteps -curveIndex ){
//                    mPath.moveTo(myPointC.x, myPointC.y);
//                    //控制点+结束点
//                    mPath.lineTo(myPointD.x,myPointD.y);
                drawingStrokes.isUp = false;
                TimePoint A,B,C,D;
                if( myPointC.x != myPointD.x){
                    k = (myPointC.y - myPointD.y) / (myPointC.x - myPointD.x);
                    A = new TimePoint( (currentWidth )* (-k) / (float) Math.sqrt(k * k + 1) + myPointC.x,
                            (currentWidth ) / (float) Math.sqrt(k * k + 1) + myPointC.y);
                    B = new TimePoint( (-currentWidth  )* (-k) / (float) Math.sqrt(k * k + 1) + myPointD.x,
                            (-currentWidth  ) / (float) Math.sqrt(k * k + 1) + myPointD.y);
                    //当前点的上下端点MyPointC,MyPointD
                    C = new TimePoint( (currentWidth )* (-k) / (float) Math.sqrt(k * k + 1) + myPointC.x,
                            (currentWidth ) / (float) Math.sqrt(k * k + 1) + myPointC.y);
                    D = new TimePoint( (-currentWidth )* (-k) / (float) Math.sqrt(k * k + 1) + myPointD.x,
                            (-currentWidth ) / (float) Math.sqrt(k * k + 1) + myPointD.y);

                }else{
                    A = new TimePoint( currentWidth   + myPointC.x,
                            myPointC.y);
                    B = new TimePoint( -currentWidth  + myPointD.x,
                            myPointD.y);
                    C = new TimePoint( currentWidth   + myPointC.x,
                            myPointC.y);
                    D = new TimePoint( -currentWidth  + myPointD.x,
                            myPointD.y);
                }
                TimePoint centerAC = new TimePoint((A.x+C.x)/2,(A.y+C.y)/2);
                TimePoint centerBD = new TimePoint((B.x+D.x)/2,(B.y+D.y)/2);
                boolean isAC = true;
                if( myPointC.x != myPointD.x){
                    float b = myPointC.y - k *myPointC.x;
                    if((centerAC.y - k* centerAC.x - b)*(drawingStrokes.mLastY - k*drawingStrokes.mLastX - b)<=0){
                        isAC = true;
                    }else {
                        isAC = false;
                    }
                }else{
                    if((centerAC.y-myPointC.y)*(drawingStrokes.mLastY - myPointC.y)<=0){
                        isAC = true;
                    }else {
                        isAC = false;
                    }
                }
                mPath.moveTo(myPointC.x, myPointC.y);
                if(isAC){
                    mPath.quadTo(centerAC.x,centerAC.y,myPointD.x,myPointD.y);
                    if(drawingStrokes.lastLineX == myPointC.x&&drawingStrokes.lastLineY == myPointC.y) {
                        drawingStrokes.strokesPath.quadTo(centerAC.x, centerAC.y, myPointD.x, myPointD.y);
                        //rawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(centerAC.x, centerAC.y));
                       // drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointD.x, myPointD.y));
                    }else{
                        drawingStrokes.strokesPath.quadTo(centerAC.x, centerAC.y, myPointC.x, myPointC.y);
                       // drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(centerAC.x, centerAC.y));
                        //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointC.x, myPointC.y));
                    }
                }else{
                    mPath.quadTo(centerBD.x,centerBD.y,myPointD.x,myPointD.y);
                    if(drawingStrokes.lastLineX == myPointC.x&&drawingStrokes.lastLineY == myPointC.y) {
                        drawingStrokes.strokesPath.quadTo(centerBD.x, centerBD.y, myPointD.x, myPointD.y);
                       // drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(centerBD.x, centerBD.y));
                        //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointD.x, myPointD.y));
                    }else{
                        drawingStrokes.strokesPath.quadTo(centerBD.x, centerBD.y, myPointC.x, myPointC.y);
                        //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(centerBD.x, centerBD.y));
                        //drawingStrokes.strokes.getMyPathList().get(drawingStrokes.strokes.getMyPathSize()-1).addPoint(new MyPoints(myPointC.x, myPointC.y));
                    }
                }
                mPath.lineTo(myPointC.x, myPointC.y);
                canvas.drawPath(mPath,mPaint);
                mPath.reset();
            }
            lastTop.x = myPointC.x;
            lastTop.y = myPointC.y;
            lastBottom.x = myPointD.x;
            lastBottom.y = myPointD.y;
            drawingStrokes.mLastWidth = currentWidth;
            drawingStrokes.mLLastX = drawingStrokes.mLastX;
            drawingStrokes.mLLastY = drawingStrokes.mLastY;
            drawingStrokes.mLastX = x;
            drawingStrokes.mLastY = y;
            drawingStrokes.mLastK = k;
            drawingStrokes.state = currentState;
        }
    }
    /*
   * 毛笔*/
    public void drawBrushPen(DrawingStrokes drawingStrokes) {
        // if(drawingStrokes.debug)
        int[] colors = new int[] {
                Color.parseColor("#66111111"),
                Color.parseColor("#77111111"),
                Color.parseColor("#88111111")};
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1f);
        blurryPaint.setStrokeWidth(1f);
        blurryPaint.setColor(Color.RED);
        //blurryPaint.setColor(drawingStrokes.context.getResources().getColor(R.color.pencil_nine));
        //  blurryPaint.setPathEffect(new DashPathEffect(new float[] {1f, 1f}, 0));
        blurryPaint.setStyle(Paint.Style.STROKE);
        //blurryPaint.setAlpha(100);
        //mPaint.setAlpha(250);
        // mPaint.setAntiAlias(false);
//        mPaint.setPathEffect(new DashPathEffect(new float[] {2f, 2f}, 0));
        //mPaint.setPathEffect(new DiscretePathEffect(3.0f, 5.0f));
        //mPaint.setPathEffect(new CornerPathEffect(10));
        //  mPaint.setColor(drawingStrokes.context.getResources().getColor(R.color.pencil_two));
        mPaint.setColor(Color.parseColor("#111111"));
        //获得笔在两点间不同宽度的差值
        int drawSteps = (int) Math.floor(splineCurve.length());
        if (drawingStrokes.isUp) {
            //curveIndex = 1;
            if (drawSteps > 2)
                curveIndex = (drawSteps - 2) / 2;
            else curveIndex = 1;
            if (curveIndex < 1) curveIndex = 1;
            if (drawSteps == 0) drawSteps = 2;
        } else if (drawingStrokes.isDown) {
            curveIndex = 1;
            if (drawSteps == 0) drawSteps = 2;
        } else {
            if (drawSteps > 100) curveIndex = 40;
            else if (drawSteps > 80) curveIndex = 35;
            else if (drawSteps > 70) curveIndex = 30;
            else if (drawSteps > 60) curveIndex = 25;
            else if (drawSteps > 50) curveIndex = 20;
            else if (drawSteps > 40) curveIndex = 15;
            else if (drawSteps > 30) curveIndex = 13;
            else if (drawSteps > 20) curveIndex = 9;
            else if (drawSteps > 10) curveIndex = 7;
            else if (drawSteps >= 4) curveIndex = 3;
            else curveIndex = 1;
        }
        float widthDelta = endWidth - startWidth;
        //两点间实际轨迹距离
        if (drawSteps == 0) {
            drawSteps = 1;
        }
        Log.i("endWidth", " " + endWidth);
//        curveIndex = drawSteps/2;
//        if(curveIndex==0) curveIndex=1;
        for (float i = 0, num = 1; i < drawSteps; i += 0.5f, num++) {
            mPath.reset();
            float t = (float) (i) / drawSteps;
            float tt = t * t;
            float ttt = tt * t;
            float u = 1 - t;
            float uu = u * u;
            float uuu = uu * u;
            float x = uuu * splineCurve.point1.x / 6.0f;
            x += (3 * ttt - 6 * tt + 4) * splineCurve.point2.x / 6.0f;
            x += (-3 * ttt + 3 * tt + 3 * t + 1) * splineCurve.point3.x / 6.0f;
            x += ttt * splineCurve.point4.x / 6.0f;
            float y = uuu * splineCurve.point1.y / 6.0f;
            y += (3 * ttt - 6 * tt + 4) * splineCurve.point2.y / 6.0f;
            y += (-3 * ttt + 3 * tt + 3 * t + 1) * splineCurve.point3.y / 6.0f;
            y += ttt * splineCurve.point4.y / 6.0f;
            float currentWidth = startWidth + t * widthDelta;
//            if (!drawingStrokes.isUp)
//                if (Math.abs(t * widthDelta) > 0.4f * num) {
//                    if (t * widthDelta > 0)
//                        currentWidth = startWidth + 0.4f * num;
//                    else currentWidth = startWidth - 0.4f * num;
//                }
            //  mPaint.setMaskFilter(new BlurMaskFilter(currentWidth, BlurMaskFilter.Blur.SOLID));
            // mPaint.setShadowLayer(currentWidth / 2, 0, 0, drawingStrokes.context.getResources().getColor(R.color.pencil_nine));
            if (drawingStrokes.isDown) {
//                canvas.drawLine(drawingStrokes.mLastX + currentWidth / 2, drawingStrokes.mLastY - currentWidth / 2, x + currentWidth / 2, y - currentWidth/ 2, blurryPaint);
//                mPaint.setStrokeWidth(1f);
//                mPaint.setColor(drawingStrokes.context.getResources().getColor(R.color.pencil_two));
                //canvas.drawOval(new RectF(x - currentWidth / 2, y - currentWidth / 2, x + currentWidth / 2, y + currentWidth / 2), blurryPaint);
                canvas.drawOval(new RectF(x - currentWidth / 2, y - currentWidth / 2, x + currentWidth / 2, y + currentWidth / 2), mPaint);
                drawingStrokes.mLastWidth = currentWidth;
                drawingStrokes.mLastX = x;
                drawingStrokes.mLastY = y;
                drawingStrokes.isDown = false;
            } else {
//                float width = currentWidth / 2;
//                while (width < currentWidth) {
//                    canvas.drawLine(drawingStrokes.mLastX + width, drawingStrokes.mLastY - width , x + width, y - width, blurryPaint);
//                    width += 0.5f;
//                }
                LinearGradient linearGradient = new LinearGradient(x - currentWidth / 2, y - currentWidth / 2, x + currentWidth / 2, y + currentWidth / 2, colors, null, Shader.TileMode.REPEAT);
//                RadialGradient radialGradient = new RadialGradient(x, y, currentWidth / 2, colors[1], colors[0], Shader.TileMode.MIRROR);
                blurryPaint.setShader(linearGradient);
                //mPaint.setAlpha(0);
                float k;
                if( x != drawingStrokes.mLastX) {
                    k = Math.abs((drawingStrokes.mLastY - y) / (drawingStrokes.mLastX - x));
                    Log.d("123","myk: " + k);
                    if(k < 0.005) {
                        canvas.drawLine(x , y - currentWidth, x , y - currentWidth / 4, blurryPaint);
                    } else if(k < 0.05) {
                        canvas.drawLine(x , y - currentWidth * 15 / 16, x , y - currentWidth / 4, blurryPaint);
                    } else if( k  < 0.1) {
                        canvas.drawLine(x , y - currentWidth * 14 / 16, x , y - currentWidth / 4, blurryPaint);
                    } else if(k < 0.3) {
                        canvas.drawLine(x , y - currentWidth * 13 / 16, x , y - currentWidth / 4, blurryPaint);
                    } else if(k < 0.5) {
                        canvas.drawLine(x , y - currentWidth * 3 / 4, x , y - currentWidth / 4, blurryPaint);
                    } else if(k > 100) {
                        canvas.drawLine(x + currentWidth, y , x + currentWidth / 4, y, blurryPaint);
                    } else if(k > 50) {
                        canvas.drawLine(x + currentWidth * 15 / 16, y , x + currentWidth / 4, y, blurryPaint);
                    } else if(k > 20) {
                        canvas.drawLine(x + currentWidth * 14 / 16, y , x + currentWidth / 4, y, blurryPaint);
                    } else if(k > 10) {
                        canvas.drawLine(x + currentWidth * 13 / 16, y , x + currentWidth / 4, y, blurryPaint);
                    } else if(k > 5) {
                        canvas.drawLine(x + currentWidth * 3 / 4, y , x + currentWidth / 4, y, blurryPaint);
                    } else {
                        k = (drawingStrokes.mLastY - y) / (drawingStrokes.mLastX - x);
                        //根据斜率计算另一点
                        TimePoint myPointC = new TimePoint( (currentWidth * 11 / 16 )* (-k) / (float) Math.sqrt(k * k + 1) + x,
                                (currentWidth * 11 / 16 ) / (float) Math.sqrt(k * k + 1) + y);
                        TimePoint myPointD = new TimePoint( (-currentWidth * 11 / 16 )* (-k) / (float) Math.sqrt(k * k + 1) + x,
                                (-currentWidth * 11 / 16 ) / (float) Math.sqrt(k * k + 1) + y);
                        canvas.drawLine(myPointC.x, myPointC.y, x, y, blurryPaint);
                        canvas.drawLine(myPointD.x, myPointD.y, x, y, blurryPaint);
                    }
                } else {
                    canvas.drawLine(x , y - currentWidth, x , y - currentWidth / 4, blurryPaint);
                }

                //canvas.drawOval(new RectF(x + currentWidth , y - currentWidth , x , y ), blurryPaint);
                //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
                canvas.drawOval(new RectF(x - currentWidth / 2, y - currentWidth / 2, x + currentWidth / 2, y + currentWidth / 2), mPaint);
                drawingStrokes.mLastWidth = currentWidth;
                drawingStrokes.mLastX = x;
                drawingStrokes.mLastY = y;
            }
        }
    }
}
