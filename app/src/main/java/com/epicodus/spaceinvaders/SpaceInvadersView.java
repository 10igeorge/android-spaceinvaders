package com.epicodus.spaceinvaders;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Guest on 4/25/16.
 */
public class SpaceInvadersView extends SurfaceView implements Runnable {
    private final Object context;
    private Thread gameThread = null;
    private SurfaceHolder ourHolder;
    private volatile boolean playing;
    private boolean paused = true;
    private Canvas canvas;
    private Paint paint;
    private long fps;
    private long timeThisFrame;
    private int screenX;
    private int screenY;
    private PlayerShip playerShip;
    private Bullet bullet;
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;
    Invader[] invaders = new Invader[60];
    int numberInvaders = 0;
    private DefenceBrick[] bricks = new DefenceBrick[400];
    private int numBricks;
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;
    int score = 0;
    private int lives = 3;
    private long menaceInterval = 1000;
    private boolean uhOrOh;
    private long lastMenaceTime = System.currentTimeMillis();

    public SpaceInvadersView(Context context, int x, int y){
        super(context);
        this.context = context;

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        } catch(IOException e){
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    private void prepareLevel(){
       playerShip = new PlayerShip((Context) context, screenX, screenY);
        bullet = new Bullet(screenY);

        for(int i= 0; i < invadersBullets.length; i++){
            invadersBullets[i] = new Bullet(screenY);
        }

        numberInvaders = 0;
        for(int column = 0; column < 6; column++){
            for(int row=0; row<5; row++){
                invaders[numberInvaders] = new Invader((Context) context, row, column, screenX, screenY);
                numberInvaders ++;
            }
        }

        numBricks = 0;
        for(int shelterNumber = 0; shelterNumber < 4; shelterNumber++) {
            for(int column = 0; column < 10; column++) {
                for(int row = 0; row < 5; row++) {
                    bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                    numBricks++;
                }
            }
        }

    }

    @Override
    public void run(){
        while(playing){
            long startFrameTime = System.currentTimeMillis();

            if(!paused){
                update();
            }

            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if(timeThisFrame >= 1){
                fps = 1000/timeThisFrame;
            }

            //TODO: do something new
        }
    }

    private void update(){
        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;

        // Move the player's ship
        playerShip.update(fps);

        // Update the invaders if visible
        for(int i = 0; i<numberInvaders; i++){
            if(invaders[i].getVisibility()){
                invaders[i].update(fps);
                if(invaders[i].takeAim(playerShip.getX(), playerShip.getLength())){
                    if(invadersBullets[nextBullet].shoot(invaders[i].getX() + invaders[i].getLength()/2, invaders[i].getY(), bullet.DOWN)){
                        nextBullet++;
                        if(nextBullet == maxInvaderBullets){
                            nextBullet = 0;
                            Log.d("hey", "made it");
                        }
                    }
                } if(invaders[i].getX() > screenX - invaders[i].getLength() || invaders[i].getX() < 0){
                    bumped = true;
                }
            }
        }

        if(bumped){
            for(int i = 0; i < numberInvaders; i ++){
                invaders[i].dropDownAndReverse();
                if(invaders[i].getY() > screenY - screenY/10){
                    lost = true;
                }
            }
        }

        // Update all the invaders bullets if active
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                invadersBullets[i].update(fps);
            }
        }

        // Did an invader bump into the edge of the screen

        if(lost){
            prepareLevel();
        }

        // Update the players bullet
        if(bullet.getStatus()){
            bullet.update(fps);
        }
        // Has the player's bullet hit the top of the screen

        // Has an invaders bullet hit the bottom of the screen

        // Has the player's bullet hit an invader

        // Has an alien bullet hit a shelter brick

        // Has a player bullet hit a shelter brick

        // Has an invader bullet hit the player ship

    }

    private void draw(){
        if(ourHolder.getSurface().isValid()){
            canvas = ourHolder.lockCanvas();
            canvas.drawColor(Color.argb(255, 26, 128, 182));
            paint.setColor(Color.argb(255,  255, 255, 255));
            // Draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - playerShip.getHeight() - 10, paint);

            // Draw the invaders
            for( int i=0; i < numberInvaders; i++){
                if(invaders[i].getVisibility()){
                    if(uhOrOh) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    } else {
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }

            // Draw the bricks if visible
            for(int i = 0; i < numBricks; i++) {
                if(bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            // Draw the players bullet if active
            if(bullet.getStatus()){
                canvas.drawRect(bullet.getRect(), paint);
            }

            // Draw the invaders bullets if active
            for(int i=0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()){
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            // Draw the score and remaining lives
            // Change the brush color
            paint.setColor(Color.argb(255,  249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10,50, paint);

            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause(){
        playing = false;
        try{
            gameThread.join();
        } catch (InterruptedException e){
            Log.e("Error:", "joining thread");
        }
    }

    public void resume(){
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        Log.v("motion", ""+motionEvent);
        switch(motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                paused = false;

                if(motionEvent.getY() > screenY - screenY/8) {
                    if(motionEvent.getX() > screenX/2) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else {
                        playerShip.setMovementState(playerShip.LEFT);
                    }
                }

                if(motionEvent.getY() < screenY - screenY/8) {
                    if (bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, bullet.UP)) {
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }
                break;
        }
        return true;
    }
}
