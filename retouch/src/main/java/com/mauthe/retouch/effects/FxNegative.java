package com.mauthe.retouch.effects;

import android.graphics.Bitmap;

import com.mauthe.retouch.BitmapUtils;

/**
 * Created by Ugo on 14/09/2014.
 */
public class FxNegative extends BaseEffect {

    @Override
    public Bitmap doEffect(Bitmap b) {
        return BitmapUtils.setBitmapNegative(b);
    }
}
