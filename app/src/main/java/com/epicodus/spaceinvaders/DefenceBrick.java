package com.epicodus.spaceinvaders;

import android.graphics.RectF;

/**
 * Created by Guest on 4/25/16.
 */
public class DefenceBrick {
    private RectF rect;
    private boolean isVisible;
    int brickHeight;

    public DefenceBrick(int row, int column, int shelterNumber, int screenX, int screenY) {
        int width = screenX/90;
        int height = screenY/40;

        isVisible = true;

        int brickPadding = 1;
        int shelterPadding = screenX/9;
        int startHeight = screenY - (screenY/8 * 2);
        brickHeight = (row*height)+brickPadding+startHeight;
        rect = new RectF(column * width + brickPadding + (shelterPadding * shelterNumber) + shelterPadding + shelterPadding*shelterNumber,
                row*height+brickPadding + startHeight,
                column*width + width - brickPadding + (shelterPadding*shelterNumber) + shelterPadding + shelterPadding*shelterNumber,
                row*height + height - brickPadding + startHeight);
    }

    public RectF getRect() {
        return this.rect;
    }

    public void setInvisible() {
        isVisible = false;
    }

    public boolean getVisibility() {
        return isVisible;
    }
}
