package com.dw;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by dw on 18-11-22.
 */

public class LayoutBean extends BaseObservable {

    public String getFirstText() {
        return firstText;
    }


    public void setFirstText(String firstText) {
        this.firstText = firstText;
        notifyPropertyChanged(BR.firstText);
    }

    @Bindable
    public String firstText;
}
