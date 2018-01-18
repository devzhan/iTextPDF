package com.itext.pdf;

/**********************************************************************
 * <pre>
 * FILE : PdfTextReplacer.java
 * CLASS : PdfTextReplacer
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
 *          |2017年11月8日|owen_zhan| Created |
 * DESCRIPTION:
 * </pre>
 ***********************************************************************/

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * 替换PDF文件某个区域内的文本
 * 
 * @user : owen_zhan
 * @date : 2017年11月8日
 */
public class PdfMain {
    private static final Logger logger = LoggerFactory.getLogger(PdfMain.class);

    private int fontSize;

    private Map<String, ReplaceRegion> replaceRegionMap = new HashMap<String, ReplaceRegion>();

    HashMap<Integer, IdentityHashMap<String, ReplaceRegion>> pageResultMap = new HashMap<>();

    ArrayList<ReplaceRegion> pageParseResult = new ArrayList<>();

    private Map<String, Object> replaceTextMap = new HashMap<String, Object>();

    private ByteArrayOutputStream output;

    private PdfReader reader;

    private PdfStamper stamper;

    private PdfContentByte canvas;

    private Font font;

    public PdfMain(byte[] pdfBytes) throws DocumentException, IOException {
        init(pdfBytes);
    }

    public PdfMain(String fileName) throws IOException, DocumentException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            byte[] pdfBytes = new byte[in.available()];
            in.read(pdfBytes);
            init(pdfBytes);
        } finally {
            in.close();
        }
    }

    private void init(byte[] pdfBytes) throws DocumentException, IOException {
        logger.info("初始化开始");
        reader = new PdfReader(pdfBytes);
        output = new ByteArrayOutputStream();
        stamper = new PdfStamper(reader, output);
        
        setFont(10);
        AcroFields acroFields=stamper.getAcroFields();
        logger.info("初始化成功");
    }

    private void close() throws DocumentException, IOException {
        if (reader != null) {
            reader.close();
        }
        if (output != null) {
            output.close();
        }

        output = null;
        replaceRegionMap = null;
        replaceTextMap = null;
    }

    public void replaceText(float x, float y, float w, float h, String text) {
        ReplaceRegion region = new ReplaceRegion(text); // 用文本作为别名
        region.setH(h);
        region.setW(w);
        region.setX(x);
        region.setY(y);
        addReplaceRegion(region);
        this.replaceText(text, text);
    }

    public void replaceText(String name, String text) {
        this.replaceTextMap.put(name, text);
    }

    /**
     * 替换文本
     * 
     * @throws IOException
     * @throws DocumentException
     * @user : owen_zhan
     * @date : 2017年11月9日
     */
    private void process() throws DocumentException, IOException {
        try {
            parseReplaceText();

            for (int i = 0; i < pageParseResult.size(); i++) {
                ReplaceRegion value = pageParseResult.get(i);
                int page = value.getPage();

                float x = value.getX();
                float y = value.getY();
                float w = value.getW();
                float h = value.getH();

                String aliasName = value.getAliasName();

                canvas = stamper.getOverContent(page);

                canvas.setColorFill(BaseColor.RED);
                canvas.rectangle(value.getX(), value.getY(), value.getW(), 1);
                // canvas.moveText(x, y);
                // canvas.lineTo(x+w, y);
                // canvas.stroke();
                canvas.saveState();
                canvas.fill();
                canvas.restoreState();
                // 开始写入文本
                canvas.beginText();

                // 设置字体
                canvas.setFontAndSize(font.getBaseFont(), getFontSize());
                canvas.setTextMatrix(value.getX(),
                        value.getY() + 2/* 修正背景与文本的相对位置 */);

                System.out.println("aliasName===" + aliasName + "x===" + x + "===y===" + y + "===w===" + w + "===h===" + h);
                canvas.setAction(new PdfAction("https://www.sekorm.com/"), x, y, x + w, y + h);
                // canvas.showText(aliasName);
                canvas.endText();

            }
        } finally {
            if (stamper != null) {
                stamper.close();
            }
        }
    }

    /**
     * 未指定具体的替换位置时，系统自动查找位置
     * 
     * @user : owen_zhan
     * @date : 2017年11月9日
     */
    private void parseReplaceText() {
        PdfPositionParse parse = new PdfPositionParse(reader);
        Set<Entry<String, Object>> entrys = this.replaceTextMap.entrySet();
        for (Entry<String, Object> entry : entrys) {
            if (this.replaceRegionMap.get(entry.getKey()) == null) {
                parse.addFindText(entry.getKey());
            }
        }

        try {
            this.pageParseResult = parse.parse();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            System.out.println("e======" + e.toString());
        }

    }

    /**
     * 生成新的PDF文件
     * 
     * @user : owen_zhan
     * @date : 2017年11月9日
     * @param fileName
     * @throws DocumentException
     * @throws IOException
     */
    public void toPdf(String fileName) throws DocumentException, IOException {
        FileOutputStream fileOutputStream = null;
        try {
            process();
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(output.toByteArray());
            fileOutputStream.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            close();
        }
        logger.info("文件生成成功");
    }

    /**
     * 将生成的PDF文件转换成二进制数组
     * 
     * @user : owen_zhan
     * @date : 2017年11月9日
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    public byte[] toBytes() throws DocumentException, IOException {
        try {
            process();
            logger.info("二进制数据生成成功");
            return output.toByteArray();
        } finally {
            close();
        }
    }

    /**
     * 添加替换区域
     * 
     * @user : owen_zhan
     * @date : 2017年11月9日
     * @param replaceRegion
     */
    public void addReplaceRegion(ReplaceRegion replaceRegion) {
        this.replaceRegionMap.put(replaceRegion.getAliasName(), replaceRegion);
    }

    /**
     * 通过别名得到替换区域
     * 
     * @user : owen_zhan
     * @date : 2017年11月9日
     * @param aliasName
     * @return
     */
    public ReplaceRegion getReplaceRegion(String aliasName) {
        return this.replaceRegionMap.get(aliasName);
    }

    public int getFontSize() {
        return fontSize;
    }

    /**
     * 设置字体大小
     * 
     * @user : owen_zhan
     * @date : 2017年11月9日
     * @param fontSize
     * @throws DocumentException
     * @throws IOException
     */
    public void setFont(int fontSize) throws DocumentException, IOException {
        if (fontSize != this.fontSize) {
            this.fontSize = fontSize;
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
            font = new Font(bf, this.fontSize, Font.BOLD);
        }
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new NullPointerException("font is null");
        }
        this.font = font;
    }

    private static String dir="D:\\pdf\\";
    private static String result_file = "D:\\pdf\\mergePDF.pdf";
    private static String FILE_PATH = "D:\\pdf\\5.pdf";

    private static String FILE_DIR_TEMP = "D:\\pdf\\4.pdf";

    public static void main(String[] args) throws IOException, DocumentException {
        logger.info("main");
        try {
            splitPDF();
            mergePDF();
            PdfMain textReplacer = new PdfMain(result_file);
            textReplacer.replaceText("2", "世强先进");
            // textReplacer.replaceText("本科", "社会大学");
            // textReplacer.replaceText("0755-29493863", "15112345678");
            textReplacer.toPdf(FILE_DIR_TEMP);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
    }
    
    
    
    
    private static void splitPDF() throws Exception {

      PdfReader reader = new PdfReader(FILE_PATH);
      Document dd = new Document();
      PdfWriter writer = PdfWriter.getInstance(dd, new FileOutputStream(dir + "temp1.pdf"));
      dd.open();
      PdfContentByte cb = writer.getDirectContent();
      dd.newPage();
      cb.addTemplate(writer.getImportedPage(reader, 1), 0, 0);
      dd.newPage();
      cb.addTemplate(writer.getImportedPage(reader, 2), 0, 0);
      dd.close();
      writer.close();

      Document dd2 = new Document();
      PdfWriter writer2 = PdfWriter.getInstance(dd2, new FileOutputStream(dir + "temp2.pdf"));
      dd2.open();
      PdfContentByte cb2 = writer2.getDirectContent();
      dd2.newPage();
      cb2.addTemplate(writer2.getImportedPage(reader, 3), 0, 0);
      dd2.newPage();
      cb2.addTemplate(writer2.getImportedPage(reader, 4), 0, 0);
      dd2.close();
      writer2.close();
  }
    
    
    private static void mergePDF() throws Exception {

        PdfReader reader1 = new PdfReader(dir + "temp1.pdf");
        PdfReader reader2 = new PdfReader(dir + "temp2.pdf");
        
        FileOutputStream out = new FileOutputStream(result_file);
        
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        int totalPages = 0;
        totalPages += reader1.getNumberOfPages();
        totalPages += reader2.getNumberOfPages();
        
        java.util.List<PdfReader> readers = new ArrayList<PdfReader>();
        readers.add(reader1);
        readers.add(reader2);
        
        int pageOfCurrentReaderPDF = 0;
        Iterator<PdfReader> iteratorPDFReader = readers.iterator();

        // Loop through the PDF files and add to the output.
        while (iteratorPDFReader.hasNext()) {
            PdfReader pdfReader = iteratorPDFReader.next();

            // Create a new page in the target for each source page.
            while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                document.newPage();
                pageOfCurrentReaderPDF++;
                PdfImportedPage page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                cb.addTemplate(page, 0, 0);
            }
            pageOfCurrentReaderPDF = 0;
        }
        out.flush();
        document.close();
        out.close();
    }
    
}
