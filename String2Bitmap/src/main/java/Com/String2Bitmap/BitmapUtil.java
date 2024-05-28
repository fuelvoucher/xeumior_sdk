package Com.String2Bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Dpuntu on 2017/3/8.
 */
public class BitmapUtil {
    public final String TAG = "BitmapUtil";
    private final static int WIDTH = 384;
//    private final static float SMALL_TEXT = 23;
//    private final static float LARGE_TEXT = 35;
    //private final static float SMALL_TEXT = 20;
    //private final static float LARGE_TEXT = 30;
    private final static int START_RIGHT = WIDTH;
    private final static int START_LEFT = 0;
    private final static int START_CENTER = WIDTH / 2;

    /**
     * 特殊需求：
     */
    public final static int IS_LARGE = 10;
    public final static int IS_SMALL = 11;
    public final static int IS_RIGHT = 100;
    public final static int IS_LEFT = 101;
    public final static int IS_CENTER = 102;


    private static float x = START_LEFT, y;



//    /**
//     * 生成图片
//     */
//    public Bitmap StringListtoBitmap(Context context, ArrayList<StringBitmapParameter> AllString) {
//        if (AllString.size() <= 0) return Bitmap.createBitmap(WIDTH, WIDTH / 4, Bitmap.Config.RGB_565);
//        ArrayList<StringBitmapParameter> mBreakString = new ArrayList<>();
//
//        Paint paint = new Paint();
//        paint.setAntiAlias(false);
//        paint.setTextSize(SMALL_TEXT);
//
//        Typeface typeface = Typeface.DEFAULT;
//        Typeface font = Typeface.create(typeface, Typeface.NORMAL);
//        //paint.setTypeface(font);
//
////        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/songti.TTF");// 仿宋打不出汉字
////        Typeface font = Typeface.create(typeface, Typeface.NORMAL);
////        paint.setTypeface(font);
//
//        for (StringBitmapParameter mParameter : AllString) {
//            int fontmark = Typeface.NORMAL;
//            if(mParameter.isBold())
//                fontmark = fontmark|Typeface.BOLD;
//            if(mParameter.isItalics())
//                fontmark = fontmark|Typeface.ITALIC;
//            font = Typeface.create(typeface, fontmark);
//            paint.setTypeface(font);
//
//
//            int ALineLength = paint.breakText(mParameter.getText(), true, WIDTH, null);//检测一行多少字
//            int lenght = mParameter.getText().length();
//            if (ALineLength < lenght) {
//
//                int num = lenght / ALineLength;
//                String ALineString = new String();
//                String RemainString = new String();
//
//                for (int j = 0; j < num; j++) {
//                    ALineString = mParameter.getText().substring(j * ALineLength, (j + 1) * ALineLength);
//                    mBreakString.add(new StringBitmapParameter(ALineString, mParameter.getIsRightOrLeft(), mParameter.getIsSmallOrLarge()));
//                }
//
//                RemainString = mParameter.getText().substring(num * ALineLength, mParameter.getText().length());
//                mBreakString.add(new StringBitmapParameter(RemainString, mParameter.getIsRightOrLeft(), mParameter.getIsSmallOrLarge()));
//            } else {
//                mBreakString.add(mParameter);
//            }
//        }
//
//
//        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
//        int FontHeight = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent) + (int) Math.abs(fontMetrics.descent) + getLineSpacing();
//
//        y = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent);
//        Log.d("BitmapUtil","SMALL_TEXT = " + SMALL_TEXT + " FontHeight = " + FontHeight + " Y=" + y);
//        int bNum = 0;
//        for (StringBitmapParameter mParameter : mBreakString) {
//            String bStr = mParameter.getText();
//            if (bStr.isEmpty() | bStr.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE)
//                bNum++;
//        }
//        //Bitmap bitmap = Bitmap.createBitmap(WIDTH, FontHeight * (mBreakString.size() + bNum), Bitmap.Config.RGB_565);
//        Bitmap bitmap = Bitmap.createBitmap(WIDTH, FontHeight * (mBreakString.size() + bNum), Bitmap.Config.RGB_565);
//        Log.d("BitmapUtil","Bitmap hight = " + FontHeight * (mBreakString.size() + bNum) + " bNum = " + bNum);
////        for (int i = 0; i < bitmap.getWidth(); i++) {
////            for (int j = 0; j < bitmap.getHeight(); j++) {
////                bitmap.setPixel(i, j, Color.WHITE);
////            }
////        }
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.GRAY);
//
//        for (StringBitmapParameter mParameter : mBreakString) {
//
//            String str = mParameter.getText();
//
//
//            if (mParameter.getIsSmallOrLarge() == IS_SMALL) {
//                paint.setTextSize(SMALL_TEXT);
//
//            } else if (mParameter.getIsSmallOrLarge() == IS_LARGE) {
//                paint.setTextSize(LARGE_TEXT);
//            }
//            if(mParameter.isItalics())
//                paint.setTextSkewX(-0.5f);     //float类型参数，负数表示右斜，整数左斜
//            else
//                paint.setTextSkewX(0);     //float类型参数，负数表示右斜，整数左斜
//            if(mParameter.isBold())
//                paint.setFakeBoldText(true);
//            else
//                paint.setFakeBoldText(false);
//
//            if(mParameter.isUnderline())
//                paint.setUnderlineText(true);
//            else
//                paint.setUnderlineText(false);
//
//            if (mParameter.getIsRightOrLeft() == IS_RIGHT) {
//                x = WIDTH - paint.measureText(str);
//            } else if (mParameter.getIsRightOrLeft() == IS_LEFT) {
//                x = START_LEFT;
//            } else if (mParameter.getIsRightOrLeft() == IS_CENTER) {
//                x = (WIDTH - paint.measureText(str)) / 2.0f;
//            }
//            Log.d("BitmapUtil","str = " + str);
//            if (str.isEmpty() | str.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE) {
//                canvas.drawText(str, x, y + FontHeight / 2, paint);
//                Log.d("BitmapUtil","drawText y = " + y + FontHeight / 2);
//                //y = y + FontHeight;
//            } else {
//                canvas.drawText(str, x, y, paint);
//                Log.d("BitmapUtil","drawText y = " + y );
//            }
//            y = y + FontHeight;
//        }
//        //canvas.save(Canvas.ALL_SAVE_FLAG);
//        canvas.save();
//        canvas.restore();
//        return bitmap;
//    }

    /**
     * 生成图片
     */
    public Bitmap StringListtoBitmap(Context context, ArrayList<StringBitmapParameter> AllString) {
        if (AllString.size() <= 0) return Bitmap.createBitmap(WIDTH, WIDTH / 4, Bitmap.Config.RGB_565);
        ArrayList<StringBitmapParameter> mBreakString = new ArrayList<>();
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, AllString.size() * 50, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        y = 0;
        int paperHeight = 0;
        for (StringBitmapParameter mParameter : AllString) {
            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(mParameter.getFontSize());
            String fontpath = mParameter.getFontsPath();
            if(fontpath.length()>0){
                Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontpath);//
                Typeface font = Typeface.create(typeface, Typeface.NORMAL);
                textPaint.setTypeface(font);
            }else{
                Typeface font = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
                textPaint.setTypeface(font);
            }
//textPaint.setTypeface()
            if(mParameter.isBold())
                textPaint.setFakeBoldText(true);
            else
                textPaint.setFakeBoldText(false);
            if(mParameter.isItalics())
                textPaint.setTextSkewX(-0.5f);
            else
                textPaint.setTextSkewX(0);

            if(mParameter.isUnderline())
                textPaint.setUnderlineText(true);
            else
                textPaint.setUnderlineText(false);
            textPaint.setColor(Color.BLACK);

            StaticLayout staticLayout = new StaticLayout(mParameter.getText(), textPaint, WIDTH, mParameter.getAlignment(), 1, mParameter.getLineSpacing(), false);
            int height = staticLayout.getHeight();
            Log.d("BitmapUtil","str=" + mParameter.getText());
            Log.d("BitmapUtil","height=" + height);
            if(mParameter.getLineSpacing()>0){
                canvas.translate(0,mParameter.getLineSpacing());
                height+=mParameter.getLineSpacing();
            }
            staticLayout.draw(canvas);
            canvas.translate(0,height);
            paperHeight+=height;
        }
        Log.d("BitmapUtil","paperHeight=" + paperHeight);
        canvas.clipRect(new RectF(0,0,paperHeight,WIDTH));
        canvas.save();
        canvas.restore();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, WIDTH, paperHeight);
        return bitmap;
    }

    /**
     * 合并图片
     */
    public Bitmap addBitmapInHead(Bitmap first, Bitmap second) {
        int width = Math.max(first.getWidth(), second.getWidth());
        int startWidth = (width - first.getWidth()) / 2;
        int height = first.getHeight() + second.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

//        for (int i = 0; i < result.getWidth(); i++) {
//            for (int j = 0; j < result.getHeight(); j++) {
//                result.setPixel(i, j, Color.WHITE);
//            }
//        }
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(first, startWidth, 0, null);
        canvas.drawBitmap(second, 0, first.getHeight(), null);
        return result;
    }

    /***
     * 使用两个方法的原因是：
     * logo标志需要居中显示，如果直接使用同一个方法是可以显示的，但是不会居中
     */
    public Bitmap addBitmapInFoot(Bitmap bitmap, Bitmap image) {
        int width = Math.max(bitmap.getWidth(), image.getWidth());
        int startWidth = (width - image.getWidth()) / 2;
        int height = bitmap.getHeight() + image.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

//        for (int i = 0; i < result.getWidth(); i++) {
//            for (int j = 0; j < result.getHeight(); j++) {
//                result.setPixel(i, j, Color.WHITE);
//            }
//        }
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(image, startWidth, bitmap.getHeight(), null);
        return result;
    }
    public Bitmap addTwoLogo(Bitmap bitmap1,Bitmap bitmap2){
        if(bitmap1.getWidth()>WIDTH/2)
            bitmap1 = fitBitmap(bitmap1,WIDTH/2);
        if(bitmap2.getWidth()>WIDTH/2)
            bitmap2 = fitBitmap(bitmap2,WIDTH/2);

        int height = Math.max(bitmap1.getHeight(), bitmap2.getHeight());
        Bitmap result = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap1, (WIDTH/2-bitmap1.getWidth())/2, 0, null);
        canvas.drawBitmap(bitmap2, WIDTH/2 + (WIDTH/2-bitmap2.getWidth())/2, 0, null);
        result = getSinglePic(result);
        return result;
    }

    public Bitmap fitBitmap(Bitmap target, int newWidth)
    {
        int width = target.getWidth();
        int height = target.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        // float scaleHeight = ((float)newHeight) / height;
        int newHeight = (int) (scaleWidth * height);
        matrix.postScale(scaleWidth, scaleWidth);
        // Bitmap result = Bitmap.createBitmap(target,0,0,width,height,
        // matrix,true);
        Bitmap bmp = Bitmap.createBitmap(target, 0, 0, width, height, matrix,true);
        if (target != null && !target.equals(bmp) && !target.isRecycled())
        {
            target.recycle();
            target = null;
        }
        return bmp;// Bitmap.createBitmap(target, 0, 0, width, height, matrix,
        // true);
    }

    public static Bitmap getSinglePic(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        Bitmap returBMP = Bitmap.createBitmap(inputBMP.getWidth(),inputBMP.getHeight(), Bitmap.Config.RGB_565);
        int lightNumber = 200;//曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高
        for(int j = 0;j<colorTemp.length; j++){
            colorTemp[j] = Color.rgb(Color.red(pix[j]),
                    Color.green(pix[j]), Color.blue(pix[j]));
        }
        for (int i = 0; i < colorTemp.length; i++) {
            int r = Color.red(pix[i]);
            int g = Color.green(pix[i]);
            int b = Color.blue(pix[i]);
            if(r+g+b>3*lightNumber){//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                colorTemp[i] = Color.rgb(255,255,255);
            }else{
                colorTemp[i] = Color.rgb(0,0,0);
            }
        }
        returBMP.setPixels(colorTemp, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        return returBMP;
    }

    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
        return resizeBmp;
    }


    public Bitmap addTextInBitmapFoot(Bitmap bitmap,String text,int fontsize) {
        int height = bitmap.getHeight();
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(fontsize);
        StaticLayout staticLayout = new StaticLayout(text, textPaint, WIDTH, Layout.Alignment.ALIGN_CENTER, 1,0, false);
        int staticLayout_height = staticLayout.getHeight();
        Bitmap result = Bitmap.createBitmap(WIDTH, staticLayout_height + bitmap.getHeight(), Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, (WIDTH-bitmap.getWidth())/2, 0, null);
        canvas.translate(0,bitmap.getHeight());
        staticLayout.draw(canvas);
        canvas.save();
        return result;
    }




}
