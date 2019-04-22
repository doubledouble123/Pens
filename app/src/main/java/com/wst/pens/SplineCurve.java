package com.wst.pens;


/**
 * Author:Double
 * Time:2019/4/22
 * Description:This is SplineCurve
 */
public class SplineCurve {
    public TimePoint point1;
    public TimePoint point2;//起点
    public TimePoint point3;//结束点
    public TimePoint point4;
    public int steps = 10;//设定在曲线上取的点数

    public SplineCurve(TimePoint point1, TimePoint point2,
                       TimePoint point3, TimePoint point4){
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
        this.point4 = point4;
    }
    //获得贝塞尔曲线中取得的10个点的相邻点距离和
    public float length(){
        int length = 0;
        float perStep;
        double cx, cy, px = 0, py = 0, xdiff, ydiff;
        for(int i = 0;i <= steps; i++){
            perStep = (float)i / steps;
            cx = point(perStep, this.point1.x, this.point2.x, this.point3.x,
                    this.point4.x);
            cy = point(perStep, this.point1.y, this.point2.y, this.point3.y,
                    this.point4.y);
            if(i > 0){//计算与上一个点的距离
                xdiff = cx - px;
                ydiff = cy - py;
                length += Math.sqrt(xdiff * xdiff + ydiff * ydiff);
            }
            px = cx;
            py = cy;
        }
        return length;
    }
    //通过贝塞尔算法返回每个点的x或者y值
    public double point(float perStep, float point1, float point2, float point3, float point4){
        return point1 * (1.0 - perStep) * (1.0 - perStep) * (1.0 - perStep) / 6.0
                +  point2 * (3 * perStep * perStep * perStep - 6 * perStep * perStep + 4) / 6.0
                +  point3 * (-3 * perStep * perStep * perStep + 3 * perStep * perStep + 3 * perStep + 1)/6.0
                +point4 * perStep * perStep * perStep / 6.0;
    }
}
