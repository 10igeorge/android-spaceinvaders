package com.epicodus.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

/**
 * Created by Guest on 4/25/16.
 */
public class PlayerShip {

    RectF rect;

    private Bitmap bitmap;
    private float length;
    private float height;
    private float x;
    private float y;
    private float shipSpeed;
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;
    private int shipMoving = STOPPED;

    public PlayerShip(Context context, int screenX, int screenY) {
        rect = new RectF();
        length = screenX/10;
        height = screenY/10;
        x = screenX/2;
        y = screenY-20;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (length), (int) (height), false);
        shipSpeed = 350;
    }

    public RectF getRect() {
        return rect;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public float getX() {
        return x;
    }

    public float getLength() {
        return length;
    }

    public float getHeight() {
        return height;
    }

    public void setMovementState(int state) {
        shipMoving = state;
    }

    public void update(long fps) {
        if(shipMoving == LEFT) {
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT) {
            x = x + shipSpeed / fps;
        }

        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;
    }

}
