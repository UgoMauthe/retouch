package com.mauthe.retouch.effects;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.mauthe.retouch.BitmapUtils;

/**
 * Created by Ugo on 15/09/2014.
 */
public class FxColorize extends BaseEffect {

    @Override
    public Bitmap doEffect(Bitmap b) {
        float[] hsvVal = new float[3];
        hsvVal[0] = getEffectAmount();
        hsvVal[1] = 1.0f;
        hsvVal[2] = 1.0f;
        int c = Color.HSVToColor(hsvVal);
        //return BitmapUtils.setBitmapColorFilter(b,c);
        return BitmapUtils.setBitmapColorize(b,c);
    }





}
