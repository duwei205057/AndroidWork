package com.dw;

import android.view.View;
import android.widget.Toast;

/**
 * Created by dw on 17-9-26.
 */

public class DyHelper {

    private String mToastMessage = "default toast";
//    private String mToastMessage = "modify toast!!!";

    public void handleButtonClicked(View view){
        Toast.makeText(view.getContext(),mToastMessage,Toast.LENGTH_LONG).show();
    }
}
