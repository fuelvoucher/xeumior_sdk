package Com.String2Bitmap;

import android.support.annotation.Nullable;
import android.text.Layout;

/**
 * Created by Dpuntu on 2017/3/8.
 */

public class StringBitmapParameter {
    @Nullable
    private String text;
    private final int DefaultFontSize = 20;
    private int fontSize=DefaultFontSize;


    private boolean isBold=false;
    private boolean isItalics=false;
    private boolean isUnderline=false;
    private Layout.Alignment alignment=Layout.Alignment.ALIGN_LEFT;

    /*
        行间距
        line spacing
         */
    private int lineSpacing = 0;
    /*
    path of the ttf file
     */
    private String fontsPath="";


    public int getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }


    public StringBitmapParameter() {

    }

    /**
     * @param text          字段
     */
    public StringBitmapParameter(String text) {
        this.text = text;

    }

    /**
     * @param text           字段
     * @param alignment  可空，默认左边
     * @param fontSize 可空，字体大小
     */
    public StringBitmapParameter(String text, Layout.Alignment alignment, int fontSize) {
        this.text = text;
        this.fontSize = fontSize;
        this.alignment = alignment;
    }

    public StringBitmapParameter(String text, Layout.Alignment alignment, int fontSize, int lineSpacing) {
        this.text = text;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.lineSpacing = lineSpacing;
    }

    public StringBitmapParameter(String text, Layout.Alignment alignment) {
        this.text = text;
        this.alignment = alignment;
    }
    public StringBitmapParameter(String text, Layout.Alignment alignment, int fontSize, int lineSpacing,boolean isBold,boolean isItalics,boolean isUnderline) {
        this.text = text;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.lineSpacing = lineSpacing;
        this.isBold = isBold;
        this.isItalics = isItalics;
        this.isUnderline = isUnderline;
    }

    public StringBitmapParameter(String text, Layout.Alignment alignment, int fontSize, int lineSpacing,boolean isBold,boolean isItalics,boolean isUnderline,String fontsPath) {
        this.text = text;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.lineSpacing = lineSpacing;
        this.isBold = isBold;
        this.isItalics = isItalics;
        this.isUnderline = isUnderline;
        this.fontsPath = fontsPath;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public boolean isItalics() {
        return isItalics;
    }

    public void setItalics(boolean italics) {
        isItalics = italics;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setUnderline(boolean underline) {
        isUnderline = underline;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Layout.Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Layout.Alignment alignment) {
        this.alignment = alignment;
    }

    public String getFontsPath() {
        return fontsPath;
    }

    public void setFontsPath(String fontsPath) {
        this.fontsPath = fontsPath;
    }
}
