package com.mauthe.retouch;

/**
 * Created by Ugo on 15/09/2014.
 */
public class HistoryHelper {

    private int fIndex;
    private float fAmount;

    public HistoryHelper(int index, float amount) {
        fIndex = index;
        fAmount = amount;
    }

    public void setIndex(int aValue) {
        fIndex = aValue;
    }
    public int getIndex() {
       return fIndex;
    }

    public void setAmount(float aValue) {
        fAmount = aValue;
    }
    public float getAmount(){
        return fAmount;
    }
}
