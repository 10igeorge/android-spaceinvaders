package com.epicodus.spaceinvaders;

import android.app.Activity;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

public class MainActivity extends Activity {

    SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Log.d("DISPLAY", ""+display);
        Point size = new Point();
        Log.d("POINT", ""+size);
        display.getSize(size);
        Log.d("SIZE", ""+size);
        spaceInvadersView = new SpaceInvadersView(this, size.x, size.y);
        setContentView(spaceInvadersView);
    }

    @Override
    protected void onResume(){
        super.onResume();
        spaceInvadersView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        spaceInvadersView.pause();
    }
}
