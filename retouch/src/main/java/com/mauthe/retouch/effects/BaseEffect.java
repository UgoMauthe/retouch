package com.mauthe.retouch.effects;

import android.graphics.Bitmap;

/**
 * Created by Ugo on 14/09/2014.
 */
public abstract class BaseEffect {


    private EffectProperty fEffectProperty;
    private int fEffectThumbnail;
    private String fEffectName;
    private float fEffectAmount;
    private int fEffectIndex;


    public BaseEffect() {
        //fEffectProperty = new EffectProperty(0,0,0);
    }

    public int getEffectThumbnail() {
        return fEffectThumbnail;
    }
    public void setEffectThumbnail(int aValue){
        fEffectThumbnail = aValue;
    }

    public String getEffectName(){
        return fEffectName;
    }

    public void setEffectName(String aValue){
        fEffectName = aValue;
    }

    public void setEffectProperties(EffectProperty eP) {
        fEffectProperty = eP;
        fEffectAmount = fEffectProperty.getDefValue();
    }
    public EffectProperty getEffectProperties() {
        return fEffectProperty;
    }

    public float getEffectAmount() {
        return fEffectAmount;
    }

    public void setEffectAmount(float aValue) {

        fEffectAmount = Math.min(Math.max(aValue, fEffectProperty.getMinValue()), fEffectProperty.getMaxValue());

    }


    public BaseEffect setupEffect(String effectName, int thumbnail, EffectProperty properties) {
        setEffectName(effectName);
        setEffectThumbnail(thumbnail);
        setEffectProperties(properties);
        return this;
    }



    public abstract Bitmap doEffect(Bitmap b);

    public void setEffectIndex(int aValue) {
        fEffectIndex = aValue;

    }
    public int getEffectIndex() {
        return fEffectIndex;
    }



}
