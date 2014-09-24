package com.mauthe.retouch.effects;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;

import com.mauthe.retouch.BitmapUtils;

/**
 * Created by Ugo on 14/09/2014.
 */
public class FxColorGreen extends BaseEffect {

    @Override
    public Bitmap doEffect(Bitmap b) {
        return BitmapUtils.combineBitmaps(b,BitmapUtils.setBitmapColorFilter(b, Color.argb(255, 0, 255, 0)), PorterDuff.Mode.OVERLAY);
    }
}


