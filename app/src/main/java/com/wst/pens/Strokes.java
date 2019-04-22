package com.wst.pens;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Vector;

/**
 * Author:Double
 * Time:2019/4/22
 * Description:This is Strokes
 */
public class Strokes {
    //当前笔划列表
    private Vector<Stroke> myPath = null;
    //保存删除  移动 旋转等更新后回收的笔划
    private Vector<Stroke> recycleStrokesList = null;
    //恢复操作保存的笔划
    private Vector<Stroke> unDoStrokesList = null;
    public Strokes(){
        myPath = new Vector<>();
        recycleStrokesList = new Vector<>();
        unDoStrokesList = new Vector<>();
    }
    public int getRecycleStrokesListSize(){
        return recycleStrokesList.size();
    }
    public int getMyPathSize(){
        return myPath.size();
    }
    public Vector<Stroke> getRecycleStrokesList(){
        return recycleStrokesList;
    }
    public Vector<Stroke> getMyPathList(){
        return myPath;
    }
    public Vector<Stroke> getUnDoStrokesList(){
        return unDoStrokesList;
    }


    public void addMyPath(int location, int priority) {
        myPath.add(new Stroke(location,priority));
    }

    public void draw(Canvas canvas,Paint paint){
        paint.setStyle(Paint.Style.FILL);
        for(int i = 0;i < myPath.size(); i++){
            if(myPath.size() == 0) break;
            Path stroke = myPath.elementAt(i).getStroke();
            if(!stroke.isEmpty()){
                canvas.drawPath(stroke,paint);
            }
        }
    }
    public void deleteMyPath(int index){
        myPath.remove(index);
    }
    public void deleteRecycleStrokesList(int index){
        recycleStrokesList.remove(index);
    }
    public void deleteUnDoStrokesList(int index){
        unDoStrokesList.remove(index);
    }
    public void clearUnDoStrokesList(){
        checkUnDoNull();
        unDoStrokesList.clear();
    }
    public void checkUnDoNull(){
        if(unDoStrokesList==null){
            unDoStrokesList = new Vector<>();
        }
    }
    public void addUnDoStrokes(Stroke stroke){
        checkUnDoNull();
        unDoStrokesList.add(stroke);
    }
    public class Stroke{
        private Path stroke;
        private int location;//删除笔划原来所在笔划列表中的位置
        private int priority;//所有笔划执行撤销动作的优先级
        private Vector<TimePoint> points; //拟合后的几个点
        private Vector<TimePoint> originPoints;//最原始的几个点
        private Vector<Float> originWidth;
        public Stroke(int index,int priority){
            this.stroke = new Path();
            this.location = index;
            this.priority = priority;
            this.points = new Vector<>();
            originWidth = new Vector<>();
            originPoints = new Vector<>();
        }
        public Path getStroke(){
            return  stroke;
        }
        public void setStroke(Path path){
            stroke = new Path(path);
        }
        public Vector<TimePoint> getOriginPoints(){
            return originPoints;
        }
        public void setOriginPoints(Vector<TimePoint> timePoints){
            this.originPoints = timePoints;
        }
        public void setOriginWidth(Vector<Float> originWidth){
            this.originWidth = originWidth;
        }
        public void addOriginPoint(TimePoint myPoints){
            originPoints.add(myPoints);
        }
        public Vector<Float> getOriginWidth(){
            return originWidth;
        }
        public void addOriginWidth(float width){
            originWidth.add(width);
        }
        public void addPoint(TimePoint point){
            points.add(point);
        }
        public Vector<TimePoint> getPoints(){
            return points;
        }
        public int getLocation() {
            return location;
        }

        public void setLocation(int location) {
            this.location = location;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }

}
