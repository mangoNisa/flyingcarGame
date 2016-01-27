package com.nisanabi.flyingcar;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by hello on 11/12/2015.
 */
public class Bird extends GameObject{

    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spritesheet;
    private Boolean drop;

    public Bird(Bitmap res, int x, int y, int w, int h, int s, int numFrames){
        super.x = x;
        super.y = y;
        width = w;
        height = h;
        score = s;
        drop = false;

        speed = 10 + (int) (rand.nextDouble()*(score-10));

        //cap missle speed
        if(speed >= 15){
            speed = 15;
        }

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        //loop through the images assignment element of array to a missile animation
        for(int i = 0; i<image.length; i++){
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }

        animation.setFrames(image);
        //if missle is faster, then delay will decreate so animation will be faster
        animation.setDelay(20);
    }

    public void update(){
        if(score > 10){
            x-= speed;
        }else {
            x -= 10;
        }
        animation.update();
    }

    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(animation.getImage(), x,y,null);
        }catch(Exception e){}
    }

    @Override
    public int getWidth(){

        //offset a bit for more realisitic collision detection
        return width-10;
    }


}
