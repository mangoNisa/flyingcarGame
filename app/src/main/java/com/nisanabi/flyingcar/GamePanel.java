package com.nisanabi.flyingcar;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import java.util.ArrayList;
import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 500;
    public static final int HEIGHT = 800;
    public static final int MOVESPEED = -5;
    public static final long startTime = System.nanoTime();
    private int countbird = 0;

    private long missileStartTime;
    private long rainbowStartTime;
    private long chirpStartTime;

    private int lastscore;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Bird> bird;
    private ArrayList<Rainbow> rainbow;

    Rect play;


    private static SoundPool sp;

    float scaleFactorX ;
    float scaleFactorY ;

    Rect clipBounds;
    private ArrayList<Integer> birds;

    int x1;
    int y1;

    private int chirp, magic, chirp2, magic2, rev;
    private static int crash, fail;

    private Random rand = new Random();


    private boolean newGameCreated;
    private boolean newHighest = false;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disapear;
    private boolean started;


    public GamePanel(Context context)
    {
        super(context);


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);

        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        chirp = sp.load(context, R.raw.chirp1, 1);
        chirp2 = sp.load(context, R.raw.chirp2, 1);
        crash = sp.load(context, R.raw.car_crash, 1);
        magic = sp.load(context, R.raw.magic1, 1);
        fail = sp.load(context, R.raw.fail, 2);
        magic2 = sp.load(context, R.raw.magic2, 1);
        rev = sp.load(context, R.raw.rev, 1);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            }catch(InterruptedException e){e.printStackTrace();}

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.car), 111, 70, 3);
        birds = new ArrayList<>();
        rainbow = new ArrayList<>();

        birds = new ArrayList<>();
        birds.add(R.drawable.bird);
        birds.add(R.drawable.bird2);
        birds.add(R.drawable.bird3);
        birds.add(R.drawable.bird4);
        birds.add(R.drawable.bird5);

        missileStartTime = System.nanoTime();
        rainbowStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN){

            //scale the location of the users finger to the background
            x1 = (int)(event.getX()/ scaleFactorX) + clipBounds.left;
            y1= (int)(event.getY()/scaleFactorY) + clipBounds.top;

            if(!player.getPlaying()&& newGameCreated && reset)
            {
                //User clicks play button
                if(play.contains(x1, y1)) {
                    player.setPlaying(true);
                    player.setUp(true);
                }
            }

            if(player.getPlaying()){

                if (!started) started = true;
                reset = false;
                player.setUp(true);

                if (!started && play.contains(x1,y1)) started = true;
            }
            return true;
        }

        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update(){

        if(player.getPlaying()) {

            /*
             * Setting up start time of each object
             */
            long missileElapsed = (System.nanoTime()-missileStartTime)/1000000;
            long rainbowElapsed = (System.nanoTime()-rainbowStartTime)/1000000;
            long chirptime = (System.nanoTime()-chirpStartTime)/1000000;

            bg.update();
            player.update();

            //add a rainbow
            if(rainbowElapsed > 1100) {

                rainbow.add(new Rainbow(BitmapFactory.decodeResource(getResources(), R.drawable.rainbow),
                        WIDTH + 500, (int) (rand.nextDouble() * (HEIGHT - (100)) + 30), 60, 60, player.getScore(), 1));
                rainbowStartTime = System.nanoTime();

            }

            //add a missile
            if (missileElapsed > (701)) {

                if (birds.size() == 0) {

                    bird.add(new Bird(BitmapFactory.decodeResource(getResources(), birds.get(countbird)),
                            WIDTH + 500, HEIGHT / 2, 44, 41, player.getScore(), 8));
                } else {

                    int position = (int) (rand.nextDouble() * (HEIGHT - (100)) + 30);
                    bird.add(new Bird(BitmapFactory.decodeResource(getResources(), birds.get(countbird)),
                            WIDTH + 500, position, 44, 41, player.getScore(), 8));
                }

                /**
                 * Play the chirping sounds at specified intervals
                 */
                if (countbird++ >= birds.size() - 1) {
                    countbird = 0;
                } else if (chirptime > 3500 && player.getScore() % 2 == 0) {
                    chirpStartTime = System.nanoTime();
                    sp.play(chirp, 1, 1, 1, 0, 0);
                } else if (chirptime > 3500){
                    chirpStartTime = System.nanoTime();
                    sp.play(chirp2, 1, 1, 1, 0, 0);
                }

                //reset timer
                 missileStartTime = System.nanoTime();
             }

            //loop through every missile and check collision and remove
            for(int i = 0; i< birds.size();i++){
                //update missile
                bird.get(i).update();

                if(collision(bird.get(i), player))
                {
                    playCrash();
                    player.setPlaying(false);
                    break;
                }

                //remove missile if it is way off the screen
                if(bird.get(i).getX()<-100|| bird.get(i).getY()> HEIGHT+100) {
                    birds.remove(i);
                    break;
                }
            }

            //loop through every rainbow and check collision and remove
            for(int i = 0; i<rainbow.size(); i++){
                //update missile
                rainbow.get(i).update();


                if (collision(rainbow.get(i), player)) {
                    rainbow.remove(i);
                    if(player.getScore()%2 == 0) {
                        sp.play(magic, 1, 1, 1, 1, 2);
                    }
                    else {
                        sp.play(magic2, 1, 1, 1, 1, 2);
                    }
                    player.setScore(1);
                    break;
                }

                //remove missile if it is way off the screen
                if(rainbow.get(i).getX()<-100)
                {
                    rainbow.remove(i);
                    break;
                }
            }

        }else{

            player.resetDY();

            //not reset yet and player is not playing
            if(!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion)
                        ,player.getX(), player.getY()-30, 140, 140, 35);
            }

            explosion.update();

            //time it waits before player can start game again
            long resetElapsed = (System.nanoTime() - startReset)/1000000;

            if(resetElapsed>2500 && !newGameCreated){
                //ad stuff

                if(SplashScreen.getGamesPlayed()>4){
                    SplashScreen.setGamesPlayed(0);
                    AdBuddiz.showAd((Activity) getContext());
                }else{
                    SplashScreen.setGamesPlayed(SplashScreen.getGamesPlayed() + 1);

                }
                newGame();
            }
        }
    }

    /**
     * checks for collision between two GameObject objects
     * @param a
     * @param b
     * @return true if there is a collision
     */
    public boolean collision(GameObject a, GameObject b) {
        return a.getRectangle().intersect(b.getRectangle()) ||
                b.getRectangle().intersect(a.getRectangle());
    }

    @Override
    public void draw(Canvas canvas) {


        if(canvas!=null) {

            scaleFactorX = getWidth()/(WIDTH*1.f);
            scaleFactorY = getHeight()/(HEIGHT*1.f);

            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);

            clipBounds = canvas.getClipBounds();

            bg.draw(canvas);
            drawText(canvas);

            //draw players car
            if(!disapear && started) {
                player.draw(canvas);
            }

            //draw birds
            for(Bird m: bird)
            {
                m.draw(canvas);
            }

            //draw rainbows
            for(Rainbow m: rainbow)
            {
                m.draw(canvas);
            }

            //draw explostion
            if(started){
                explosion.draw(canvas);
            }


            canvas.restoreToCount(savedState);


        }
    }

    /**
     * Clear all data structures and reset variables for a new game
     */
    public void newGame()
    {
        disapear = false;

        birds.clear();
        rainbow.clear();

        player.setPlaying(false);

        player.resetDY();
        started = false;
        lastscore = player.getScore();
        player.resetScore();
        player.setY(HEIGHT / 2);

        if(lastscore > SplashScreen.getHighScore()){
            newHighest = true;
            SplashScreen.setHighScore(lastscore);
        }

        newGameCreated = true;

    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);

        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        //if game is in progress only show the score
        if(player.getPlaying()){
            canvas.drawText("" + (player.getScore()), (WIDTH / 2) - 30, 60, paint);
        }else if(!player.getPlaying() && newGameCreated && reset) {

            /*
             * Display the menu page
             */
            String t;
            Rect rect = new Rect();
            rect.set(60, 30, WIDTH - 60, HEIGHT);
            paint.setColor(Color.WHITE);

            canvas.drawRect(100, 60, WIDTH - 100, 240,  paint);
            paint.setColor(Color.MAGENTA);
            paint.setTextSize(40);

            if(newHighest) {
                paint.setTextSize(60);
                newHighest = false;
            }

            //******************* SCORE BOX *************************//

            paint.setColor(Color.BLACK);
            RectF bounds = new RectF(rect);
            t = "Score";
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right)/2.0f;
            canvas.drawText(t, bounds.left, 100, paint);

            bounds = new RectF(rect);
            t = "" + lastscore;
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right)/2.0f;
            canvas.drawText(t, bounds.left, 140, paint);

            t = "Best"  ;
            bounds = new RectF(rect);
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right)/2.0f;
            canvas.drawText(t, bounds.left, 180, paint);

            t = "" + SplashScreen.getHighScore();
            bounds = new RectF(rect);
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right) / 2.0f;
            canvas.drawText(t, bounds.left, 220, paint);

            //******************* INSTUCTION BOX *************************//

            paint.setColor(Color.WHITE);
            canvas.drawRect(60, 300, WIDTH - 60, 490, paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            bounds = new RectF(rect);
            String h = "Flar: The Flying Car" ;
            bounds.right = paint.measureText(h, 0, h.length());
            bounds.left += (rect.width() - bounds.right)/2.0f;
            canvas.drawText(h, bounds.left, 340, paint);

            paint.setTextSize(25);
            t = "Press to go up" ;
            bounds = new RectF(rect);
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right)/2.0f;
            canvas.drawText(t, bounds.left, 380, paint);

            t = "Avoid the birds";
            bounds = new RectF(rect);
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right) / 2.0f;
            canvas.drawText(t, bounds.left, 420, paint);

            t = "Collect the rainbows";
            bounds = new RectF(rect);
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right) / 2.0f;
            canvas.drawText(t, bounds.left, 470, paint);

            paint.setColor(Color.WHITE);
            canvas.drawRect(100, 540, WIDTH - 100, 650,  paint);
            paint.setTextSize(35);

            //******************* PLAY BUTTON *************************//

            t = "PLAY";
            play = new Rect();
            play.set(100, 540, WIDTH - 100, 650);
            rect.set(100, 540, WIDTH - 100, 650);
            bounds = new RectF(rect);
            paint.setColor(Color.BLACK);
            bounds.right = paint.measureText(t, 0, t.length());
            bounds.left += (rect.width() - bounds.right) / 2.0f;
            canvas.drawText(t, bounds.left, 605, paint);
        }
    }

    public static void playCrash() {
        sp.play(crash, 1,1,1,1,0);
        sp.play(fail, 1, 1, 1, 1, 1);
    }
}