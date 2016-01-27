package com.nisanabi.flyingcar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class SplashScreen extends AppCompatActivity {

    public static SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        pref = this.getSharedPreferences("cargame", Context.MODE_PRIVATE);

        AdBuddiz.setPublisherKey("5058e9c5-53ec-418a-8142-20f2312de78d");
        AdBuddiz.cacheAds(this);
        setGamesPlayed(0) ;

        Thread openMenu = new Thread(){

            @Override
            public void run(){
                try {

                    Intent i = new Intent(getApplicationContext(), Game.class );



                    sleep(1000);
                    startActivity(i);

                } catch (InterruptedException e) {

                }

                finish();
            }
        };
        openMenu.start();
    }

    public static void setHighScore(int s){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("highscore", s);
        editor.commit();
    }

    public static int getHighScore(){
        int i = pref.getInt("highscore", 0);
        return i;
    }

    public static void setGamesPlayed(int s){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("played", s);
        editor.commit();
    }

    public static int getGamesPlayed(){
        int i = pref.getInt("played", 0);
        return i;
    }
}
