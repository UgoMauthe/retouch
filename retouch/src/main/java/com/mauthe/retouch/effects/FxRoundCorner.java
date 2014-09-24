package com.mauthe.retouch.effects;

import android.graphics.Bitmap;

import com.mauthe.retouch.BitmapUtils;

/**
 * Created by Ugo on 15/09/2014.
 */
public class FxRoundCorner extends BaseEffect {
    @Override
    public Bitmap doEffect(Bitmap b) {
        return BitmapUtils.roundedCornerBitmap(b,(int)getEffectAmount());
    }
}
