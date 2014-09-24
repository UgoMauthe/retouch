package com.mauthe.retouch.effects;

/**
 * Created by Ugo on 14/09/2014.
 */
public class EffectProperty {

    private float fMaxValue;
    private float fMinValue;
    private float fDefValue;

    public EffectProperty(float maxValue, float minValue, float defValue) {
        fMaxValue = maxValue;
        fMinValue = minValue;
        fDefValue = defValue;
    }

    public float getMaxValue() {
        return fMaxValue;
    }

    public float getMinValue() {
        return fMinValue;
    }

    public float getDefValue() {
        return fDefValue;
    }

}

