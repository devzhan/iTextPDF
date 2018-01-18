/**********************************************************************
 * <pre>
 * FILE : PositionRenderListener.java
 * CLASS : PositionRenderListener
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

package com.itext.pdf;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.pdf.DocumentFont;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * pdf渲染监听,当找到渲染的文本时，得到文本的坐标x,y,w,h
 * @user : owen_zhan
 * @date : 2017年11月9日
 */
public class PositionRenderListener implements RenderListener{
    
    private List<String> findText;
    private float defaultH;     ///出现无法取到值的情况，默认为12
    private float fixHeight;    //可能出现无法完全覆盖的情况，提供修正的参数，默认为2
    private int currentPage;    //当前页面
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public PositionRenderListener(List<String> findText, float defaultH,float fixHeight) {
        this.findText = findText;
        this.defaultH = defaultH;
        this.fixHeight = fixHeight;
    }

    public PositionRenderListener(List<String> findText) {
        this.findText = findText;
        this.defaultH = 12;
        this.fixHeight = 2;
    }
    public PositionRenderListener(List<String> findText,int currentPage) {
        this.findText = findText;
        this.defaultH = 12;
        this.fixHeight = 2;
        this.currentPage=currentPage;
    }
    @Override
    public void beginTextBlock() {
        
    }

    @Override
    public void endTextBlock() {
        
    }

    @Override
    public void renderImage(ImageRenderInfo imageInfo) {
    }

    ArrayList<ReplaceRegion> replaceRegions=new ArrayList<>();
    @Override
    public void renderText(TextRenderInfo textInfo) {
        String text = textInfo.getText();
        for (String keyWord : findText) {
            if (null != text && text.contains(keyWord)){
                int currentPage=getCurrentPage();
               
                
                
                
                
                ArrayList<Integer> positions=getPositions(text, keyWord);
                if (text.length()!=0&&keyWord.length()!=0) {
                    //平均每个字符所占的长度
                    for(int i=0;i<positions.size();i++) {
                        Float bound = textInfo.getBaseline().getBoundingRectange();
                        float x=bound.x;
                        float y=bound.y-this.fixHeight;
                        float w=bound.width;
                        float h=bound.height;
                        float avgX=w/(text.length());
                        ReplaceRegion region = new ReplaceRegion(text);
                        int pos=positions.get(i);
                        float realX=x+pos*avgX;
                        float realW=keyWord.length()*avgX;
                        region.setH(bound.height == 0 ? defaultH : bound.height);
                        region.setW(realW);
                        region.setX(realX);
                        region.setY(bound.y-this.fixHeight);
                        region.setAliasName(text);
                        region.setPage(currentPage);
                        System.out.println("avgX==="+ avgX + "====postion==="+pos);
                        System.out.println("text==="+ text + "====x===" + x + "===y===" + y + "===w===" + w + "===h===" + h);
                        System.out.println("text==="+ text + "====realX===" + realX + "===y===" + y + "===realW===" + realW + "===h===" + h);

                        replaceRegions.add(region);
                    }
                }
                
                
                
                
                
                
                
                
                
                
            }
        }
    }

    public static ArrayList<Integer> getPositions(String text, String keyWord) {
        ArrayList<Integer> positions = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.indexOf(keyWord, start) >= 0 && i < text.length()) {
                start = text.indexOf(keyWord, start) + keyWord.length();
                int position = start - keyWord.length();
                positions.add(position);
            }
        }
        return positions;
    }
    
    
    public ArrayList<ReplaceRegion> getPageReplaceRegions(){
        return replaceRegions;
    }
}
