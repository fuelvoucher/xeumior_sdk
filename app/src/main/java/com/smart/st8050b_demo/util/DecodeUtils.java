package com.smart.st8050b_demo.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Administrator on 2018/2/8.
 */

public class DecodeUtils {
    Context context = null;
    public DecodeUtils(Context con)
    {
        context = con;
    }
    /**
     * 获取图片资源
     * @param name
     * @return
     */
    public Bitmap getRes(String name) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        int resID = context.getResources().getIdentifier(name, "drawable", appInfo.packageName);
        return BitmapFactory.decodeResource(context.getResources(), resID);
    }

    public byte[] decodeBarcodeYUV(Bitmap barcode) {
        if (null == barcode) {
            return null;
        }
        int width = barcode.getWidth();
        int height = barcode.getHeight();
        //以argb方式存放图片的像素
        int[] argb = new int[width * height];
        barcode.getPixels(argb, 0, width, 0, 0, width, height);
        //将argb转换为yuv
        byte[] yuv = new byte[width * height * 3 / 2];
        encodeYUV420SP(yuv, argb, width, height);
        //解析YUV编码方式的二维码
        //Result result = decodeBarcodeYUV(yuv, width, height);
        barcode.recycle();
        barcode = null;
        return yuv;
    }

    /**
     * RGB转YUV的公式是:
     * Y=0.299R+0.587G+0.114B;
     * U=-0.147R-0.289G+0.436B;
     * V=0.615R-0.515G-0.1B;
     *
     * @param yuv
     * @param argb
     * @param width
     * @param height
     */
    private static void encodeYUV420SP(byte[] yuv, int[] argb, int width, int height) {
        // 帧图片的像素大小
        final int frameSize = width * height;
        // ---YUV数据---
        int Y, U, V;
        // Y的index从0开始
        int yIndex = 0;
        // UV的index从frameSize开始
        int uvIndex = frameSize;
        // ---颜色数据---
        int R, G, B;
        int rgbIndex = 0;
        // ---循环所有像素点，RGB转YUV---
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                R = (argb[rgbIndex] & 0xff0000) >> 16;
                G = (argb[rgbIndex] & 0xff00) >> 8;
                B = (argb[rgbIndex] & 0xff);
                //
                rgbIndex++;
                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
                Y = Math.max(0, Math.min(Y, 255));
                U = Math.max(0, Math.min(U, 255));
                V = Math.max(0, Math.min(V, 255));
                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other
                // pixel AND every other scan line.
                // ---Y---
                yuv[yIndex++] = (byte) Y;
                // ---UV---
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    //
                    yuv[uvIndex++] = (byte) V;
                    //
                    yuv[uvIndex++] = (byte) U;
                }
            }
        }
    }
}
