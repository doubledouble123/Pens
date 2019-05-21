package com.wst.pens;

/**
 * Created by BJ-00314 on 2019/5/20.
 */

public class PenType {
    /**笔的类型*/
    public static final int PEN = 0x00;
    public static final int BRUSH = 0x01;
    /**记录当前笔类型  默认刚开始都是钢笔*/
    private int penType = PEN;
    public void setPenType(int penType) {
        this.penType = penType;
    }
    public int getPenType() {
        return penType;
    }
}
