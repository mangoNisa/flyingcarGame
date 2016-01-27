package com.nisanabi.flyingcar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;

/**
 * Created by hello on 24/12/2015.
 */
public class ShowAd extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set screen to fullscreen & no title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //finish();
    }
}
