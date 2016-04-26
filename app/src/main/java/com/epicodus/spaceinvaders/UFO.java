package com.epicodus.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

/**
 * Created by Guest on 4/26/16.
 */
public class UFO {
    RectF rect;
    Random randomGenerator = new Random();
    private Bitmap bitmap;
    private float length;
    private float height;
    private float x;
    private float y;
    private float ufoSpeed = 150;
    boolean isActive = false;

    public UFO(Context context, int screenX, int screenY){
        rect = new RectF();
        length = screenX/10;
        height = screenY/15;
        x = -length;
        y = 30;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ufo);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (length), (int) (height), false);

    }

    public boolean getStatus(){
        return isActive;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public boolean activate(){
        int randomNumber;
        randomNumber = randomGenerator.nextInt(5000);
        if(randomNumber == 0) {
            return true;
        }
        return false;
    }

    public void setActive() {
        isActive = true;
    }

    public void update(long fps){
        x = x + ufoSpeed/fps;
        rect.left = x;
        rect.right = x+length;
        rect.top = y;
        rect.bottom = y+height;
    }

    public RectF getRect() {
        return rect;
    }

    public int getPoints() {
        int randomNumber = randomGenerator.nextInt(3);
        if(randomNumber == 0) {
            return 50;
        } else if (randomNumber == 1) {
            return 100;
        } else {
            return 200;
        }
    }

    public void deactivate() {
        isActive = false;
        x = -length;
        y = 30;
    }
}
