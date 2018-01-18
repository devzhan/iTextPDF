package com.itext.pdf;

/**********************************************************************
 * <pre>
 * FILE : ReplaceRegion.java
 * CLASS : ReplaceRegion
 *
 * AUTHOR : owen_zhan
 *
 * FUNCTION : TODO
 *
 *
 *======================================================================
 * CHANGE HISTORY LOG
 *----------------------------------------------------------------------
 * MOD. NO.|   DATE   |   NAME  | REASON  | CHANGE REQ.
 *----------------------------------------------------------------------
 *          |2017年11月9日|owen_zhan| Created |
 * DESCRIPTION:
 * </pre>
 ***********************************************************************/


/**
 * 需要替换的区域
 * @user : owen_zhan
 * @date : 2017年11月9日
 */
public class ReplaceRegion {

    private String aliasName;
    private int page;//当前页面
    private Float x;
    private Float y;
    private Float w;
    private Float h;
    
    public ReplaceRegion(String aliasName){
        this.aliasName = aliasName;
    }
    
    /**
     * 替换区域的别名
     * @user : owen_zhan
     * @date : 2017年11月9日
     * @return
     */
    public String getAliasName() {
        return aliasName;
    }
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Float getX() {
        return x;
    }
    public void setX(Float x) {
        this.x = x;
    }
    public Float getY() {
        return y;
    }
    public void setY(Float y) {
        this.y = y;
    }
    public Float getW() {
        return w;
    }
    public void setW(Float w) {
        this.w = w;
    }
    public Float getH() {
        return h;
    }
    public void setH(Float h) {
        this.h = h;
    }
}
