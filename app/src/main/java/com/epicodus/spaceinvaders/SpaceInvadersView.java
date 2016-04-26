    package com.epicodus.spaceinvaders;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
    private String gameState = "start";
    private Canvas canvas;
    private Paint paint;
    private long fps;

    private long timeThisFrame;
    private int screenX;
    private int screenY;
    private UFO ufo;
    private PlayerShip playerShip;
    private Bullet[] playerBullets = new Bullet[3];
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
    private long shotTimer;
    private boolean hasShot = false;
    private long lastMenaceTime = System.currentTimeMillis();
    private long hitInterval = 1500;
    private long lastHit;
    private long blinkInterval = 250;
    private long lastBlinkTime;
    private long shotInterval = 400;
    private boolean bumpedRecently = false;
    private long bumpedTimer;
    private long bumpedInterval = 100;
    private int remainingInvaders;

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
        ufo = new UFO((Context) context, screenX, screenY);
        lives = 3;
        score = 0;
        ufo = new UFO((Context) context, screenX, screenY);
        playerShip = new PlayerShip((Context) context, screenX, screenY);
        for(int i = 0; i < playerBullets.length; i++) {
            playerBullets[i] = new Bullet(screenY);
        }


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
        remainingInvaders = numberInvaders;

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

            if(!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        // Play Uh
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        // Play Oh
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
                if((startFrameTime - lastHit)>hitInterval) {
                    playerShip.setHit(false);
                    playerShip.setVisibility(true);
                } else {

                    if((startFrameTime - lastBlinkTime) > blinkInterval) {
                        if(playerShip.getVisibility()) {
                            playerShip.setVisibility(false);

                        } else {
                            playerShip.setVisibility(true);
                        }
                        lastBlinkTime = System.currentTimeMillis();
                    }
                }
                if((startFrameTime - shotTimer) > shotInterval){
                    hasShot = false;
                }
                if((startFrameTime - bumpedTimer) > bumpedInterval) {
                    bumpedRecently = false;
                }

            }


        }
    }

    private void update() {
        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;

        // Move the player's ship
        playerShip.update(fps);

        if(ufo.activate()) {
            ufo.setActive();
        }
        if(ufo.getStatus()) {
            ufo.update(fps);
        }


        // Update the invaders if visible
        for (int i = 0; i < numberInvaders; i++) {
            if (invaders[i].getVisibility()) {
                invaders[i].update(fps);
                for(int j=0; j < numBricks; j++){
                    if(RectF.intersects(bricks[j].getRect(), invaders[i].getRect())){
                        for(int k=0; k < numBricks; k++){
                            bricks[k].setInvisible();
                        }
                    }
                }
                if (invaders[i].takeAim(playerShip.getX(), playerShip.getLength())) {
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX() + invaders[i].getLength() / 2, invaders[i].getY(), invadersBullets[nextBullet].DOWN)) {
                        nextBullet++;
                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0;
                        }
                    }
                }
                if (invaders[i].getX() > screenX - invaders[i].getLength() || invaders[i].getX() < 0) {
                    if(!bumpedRecently) {
                        bumped = true;
                        bumpedRecently=true;
                        bumpedTimer = System.currentTimeMillis();
                    }

                }
            }
        }

        if (bumped) {
            for (int i = 0; i < numberInvaders; i++) {
                invaders[i].dropDownAndReverse();
                if (invaders[i].getY() > screenY - screenY / 15) {
                    lost = true;
                    gameState = "lost";
                }
            }
        }

        // Update all the invaders bullets if active
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }

        // Did an invader bump into the edge of the screen

        if (lost) {
            prepareLevel();
        }



        // Update the players playerBullets
        for(int i = 0; i < playerBullets.length; i++) {
            if (playerBullets[i].getStatus()) {
                playerBullets[i].update(fps);
            }
            // Has the player's playerBullets hit the top of the screen
            if (playerBullets[i].getImpactPointY() < 0) {
                playerBullets[i].setInactive();
            }
            if (playerBullets[i].getStatus()) {
                for (int j = 0; j < numBricks; j++) {
                    if (bricks[j].getVisibility()) {
                        if (RectF.intersects(playerBullets[i].getRect(), bricks[j].getRect())) {
                            playerBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }

                for (int j = 0; j < numberInvaders; j++) {
                    if (invaders[j].getVisibility()) {
                        if (RectF.intersects(playerBullets[i].getRect(), invaders[j].getRect())) {
                            invaders[j].setInvisible();
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            playerBullets[i].setInactive();
                            score = score + 10;
                            remainingInvaders--;
                            Log.d("remaining", "" + remainingInvaders);

                            if (remainingInvaders == 0) {
                                Log.d("it", "works");
                                paused = true;
                                score = 0;
                                lives = 3;
                                prepareLevel();
                                gameState = "won";
                            }
                        }
                    }
                }
                if(ufo.getStatus()) {
                    if(RectF.intersects(ufo.getRect(), playerBullets[i].getRect())) {
                        score += ufo.getPoints();
                        ufo.deactivate();
                        playerBullets[i].setInactive();
                    }
                }

            }
        }

        //Has the ufo gone offscreen
        if(ufo.getX() > screenX) {
            ufo.deactivate();
        }


        // Has an invaders playerBullets hit the bottom of the screen
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getImpactPointY() > screenY) {
                invadersBullets[i].setInactive();
            }
        }

        // Has the player's playerBullets hit an invader


        // Has an alien bullet hit a shelter brick
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                for (int j = 0; j < numBricks; j++) {
                    if (bricks[j].getVisibility()) {
                        if (RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())) {
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }
        }



        // Has a player playerBullets hit a shelter brick


        // Has an invader bullet hit the player ship
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) {
                    if(!playerShip.getHit()) {
                        invadersBullets[i].setInactive();
                        lives--;
                        soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
                        playerShip.setHit(true);
                        lastHit = System.currentTimeMillis();
                        lastBlinkTime = System.currentTimeMillis();
                        playerShip.setVisibility(false);

                        if (lives == 0) {
                            paused = true;
                            gameState = "lost";
                            lives = 3;
                            score = 0;
                            prepareLevel();
                        }
                    }
                }
            }
        }


    }

    private void draw(){
        if(ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.argb(255, 255, 255, 255));


            if (gameState.equals("start")) {
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(screenX / 10);
                canvas.drawText("Space Invaders", screenX / 2, screenY / 2, paint);
                paint.setTextSize(screenX / 30);
                canvas.drawText("Touch screen to start", screenX / 2, screenY / 2 + 100, paint);
            } else if (gameState.equals("won")) {
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(screenX / 10);
                canvas.drawText("You won!", screenX / 2, screenY / 2, paint);
            } else if (gameState.equals("lost")) {
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(screenX / 10);
                canvas.drawText("You lose", screenX / 2, screenY / 2, paint);
            } else if (gameState.equals("game")) {
                canvas.drawBitmap(ufo.getBitmap(), ufo.getX(), ufo.getY(),paint);
                paint.setTextAlign(Paint.Align.LEFT);
                // Draw the player spaceship
                if (playerShip.getVisibility()) {
                    canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - playerShip.getHeight() - 10, paint);
                }

                // Draw the invaders
                for (int i = 0; i < numberInvaders; i++) {
                    if (invaders[i].getVisibility()) {
                        if (uhOrOh) {
                            canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                        } else {
                            canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                        }
                    }
                }

                // Draw the bricks if visible
                for (int i = 0; i < numBricks; i++) {
                    if (bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                // Draw the players playerBullets if active
                paint.setColor(Color.CYAN);
                for (int i = 0; i < playerBullets.length; i++) {
                    if (playerBullets[i].getStatus()) {
                        canvas.drawRect(playerBullets[i].getRect(), paint);
                    }
                }


                // Draw the invaders bullets if active

                for (int i = 0; i < invadersBullets.length; i++) {
                    if (invadersBullets[i].getStatus()) {
                        canvas.drawRect(invadersBullets[i].getRect(), paint);
                    }
                }



                if(ufo.getStatus()){
                    canvas.drawBitmap(ufo.getBitmap(), ufo.getX(), ufo.getY(), paint);
                }

                // Draw the score and remaining lives
                // Change the brush color
                paint.setColor(Color.argb(255, 249, 129, 0));
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);


            }
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
        switch(motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                if(gameState.equals("start") || gameState.equals("lost") || gameState.equals("won")){
                    gameState = "game";
                    break;
                } else if (gameState.equals("game")){
                    paused = false;

                    if(motionEvent.getY() > screenY - screenY/8) {
                        if(motionEvent.getX() > screenX/2) {
                            playerShip.setMovementState(playerShip.RIGHT);
                        } else {
                            playerShip.setMovementState(playerShip.LEFT);
                        }
                    }

                    if(motionEvent.getY() < screenY - screenY/8) {
                        for(int i = 0; i < playerBullets.length; i++) {
                            if(!playerBullets[i].getStatus() && !hasShot) {
                                if (playerBullets[i].shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, playerBullets[i].UP)) {
                                    soundPool.play(shootID, 1, 1, 0, 0, 1);
                                    hasShot = true;
                                    shotTimer = System.currentTimeMillis();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }

            case MotionEvent.ACTION_UP:
                playerShip.setMovementState(playerShip.STOPPED);
                break;
        }
        return true;
    }
}




//TODO: different invaders
//TODO: UFO extra points

