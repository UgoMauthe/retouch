package com.mauthe.retouch;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Ugo on 13/09/2014.
 */



public class BitmapUtils {

    public static int MAX_IMAGE_SIZE = 1920;

    public static String getRealImagePathFromURI(Context context, Uri contentUri,String ColumnData) {
        Cursor cursor = null;
        String ris = "";
        try {
            try {
                String[] mediaData = {ColumnData};
                cursor = context.getContentResolver().query(contentUri, mediaData, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(ColumnData);
                if (column_index > -1) {
                    cursor.moveToFirst();
                    ris = cursor.getString(column_index);
                }
            }
            catch (Exception e) { ris = ""; }


        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ris;
    }


    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }




    public static Bitmap decodeSampledBitmapFromResource(String filename, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        options.inInputShareable = true;


        File theFile = new File(filename);
        byte byteArray[] = new byte[(int) theFile.length()];
        FileInputStream fInStream = null;
        try {
            fInStream = new FileInputStream(theFile);
            fInStream.read(byteArray);
            fInStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;

        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,options);


        int maxSize = Math.min(MAX_IMAGE_SIZE,Math.max(reqWidth,reqHeight));
        int height = 0;
        int width = 0;
        int inHeight = bitmap.getHeight();
        int inWidth = bitmap.getWidth();
        if(inWidth > inHeight) { // photo is landscape
            height =  (inHeight * maxSize) / inWidth;
            width = maxSize;
        } else { // photo is portrait
            height = maxSize;
            width =  (inWidth  * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }


    /**
     *
     * @param bmp input bitmap
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    public static Bitmap setBitmapBrightness(Bitmap bmp, float brightness)
    {

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        1, 0, 0, 0, brightness,
                        0, 1, 0, 0, brightness,
                        0, 0, 1, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }


    /**
     *
     * @param bmp input bitmap
     * @param contrast -255..255 0 is default
     * @return new bitmap
     */
    public static Bitmap setBitmapContrast(Bitmap bmp, float contrast)
    {

        contrast = contrast / 255;

        float scale = contrast + 1.f;

        float translate = (-.5f * scale + .5f) * 255.f;
        ColorMatrix cm = new ColorMatrix(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0 });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    /**
     *
     * @param bmp input bitmap
     * @param saturation 0..200 100 is default
     * @return new bitmap
     */
    public static Bitmap setBitmapSaturation(Bitmap bmp,float saturation)
    {
        ColorMatrix cm = new ColorMatrix();

        saturation = saturation / 100;
        cm.setSaturation(saturation);

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }


    /**
     *
     * @param bmp input bitmap
     * @param color the selected color filter
     * @return new bitmap
     */

    public static Bitmap setBitmapColorFilter(Bitmap bmp,int color) {
        final ColorMatrix matrixA = new ColorMatrix();
        // making image B&W


        final ColorMatrix matrixB = new ColorMatrix();
        // applying scales for RGB color values

        float a = Color.alpha(color) == 0 ? 0 : 255 / Color.alpha(color);
        float r = Color.red(color) == 0 ? 0 : 255 / Color.red(color);
        float g = Color.green(color) == 0 ? 0 : 255 / Color.green(color);
        float b = Color.blue(color) == 0 ? 0 : 255 / Color.blue(color);

        matrixB.setScale(r,g,b,a);
        matrixA.setConcat(matrixB, matrixA);

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrixA));

        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }


    /**
     *
     * @param bmp input bitmap
     * @param value hue -180..180  0 is default
     * @return new bitmap
     */

    public static Bitmap setBitmapHue(Bitmap bmp, float value)
    {

        ColorMatrix cm = new ColorMatrix();

        value =  Math.min(180f,Math.max(-180f,value)) / 180f * (float) Math.PI;
        if (value == 0)
        {
            return bmp;
        }
        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = new float[]
                {
                        lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                        0f, 0f, 0f, 1f, 0f,
                        0f, 0f, 0f, 0f, 1f };
        cm.postConcat(new ColorMatrix(mat));

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;

    }

    /**
     *
     * @param bmp input bitmap
     * @param color color tint
     * @return new bitmap
     */

    public static Bitmap setBitmapColorize(Bitmap bmp, int color) {
        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint(color);
        ColorFilter filter = new LightingColorFilter(color, 1);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bmp, 0, 0, paint);
        return ret;
    }

    /**
     *
     * @param base first bitmap used as background
     * @param overlay second bitmap the overlay.
     * @param selectedMode PorterDuff overlap method
     * @return new bitmap
     */


    public static Bitmap combineBitmaps(Bitmap base,Bitmap overlay, PorterDuff.Mode selectedMode) {

        int w;
        if(base.getWidth() >= overlay.getWidth()){
            w = base.getWidth();
        }else{
            w = overlay.getWidth();
        }

        int h;
        if(base.getHeight() >= overlay.getHeight()){
            h = base.getHeight();
        }else{
            h = overlay.getHeight();
        }

        Bitmap.Config config = base.getConfig();
        if(config == null){
            config = Bitmap.Config.ARGB_8888;
        }

        Bitmap newBitmap = Bitmap.createBitmap(w, h, config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(base, 0, 0, null);

        Paint paint = new Paint();

        paint.setXfermode(new PorterDuffXfermode(selectedMode));
        newCanvas.drawBitmap(overlay, 0, 0, paint);

        return newBitmap;

    }


    /**
     *
     * @param originalImage bitmap to add reflect effect
     * @return new bitmap
     */


    public static Bitmap ReflectBitmap(final Bitmap originalImage) {


        final float imageReflectionRatio = 0.5f;
        final int reflectionGap = 1;
        final int width = originalImage.getWidth();
        final int height = originalImage.getHeight();
        final Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        final Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, (int) (height * imageReflectionRatio), width, (int) (height - height * imageReflectionRatio), matrix, false);
        final Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (int) (height + height * imageReflectionRatio), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(originalImage, 0, 0, null);
        final Paint deafaultPaint = new Paint();
        deafaultPaint.setColor(Color.TRANSPARENT);
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
        final Paint paint = new Paint();
        final LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);
        return bitmapWithReflection;
    }


    /**
     *
     * @param bitmap input bitmap to round corners
     * @param pixels corner size in pixel
     * @return new bitmap
     */
    public static Bitmap roundedCornerBitmap(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }






    /**
     *
     * @param bmp input bitmap to make negate
     * @return new bitmap
     */

    public static Bitmap setBitmapNegative(Bitmap bmp) {
        //To generate negative image
        float[] colorMatrix_Negative = {
                -1.0f, 0, 0, 0, 255, //red
                0, -1.0f, 0, 0, 255, //green
                0, 0, -1.0f, 0, 255, //blue
                0, 0, 0, 1.0f, 0 //alpha
        };

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix_Negative));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return ret;

    }


}
