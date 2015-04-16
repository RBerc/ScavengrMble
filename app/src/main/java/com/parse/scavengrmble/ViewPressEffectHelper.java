package com.parse.scavengrmble;

/**
 * Created by Rob on 4/14/2015.
 */
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;

import static android.os.Build.VERSION.SDK_INT;
import static android.view.View.OnTouchListener;

/**
 *
 * View Press Effect Helper
 * - usage : do some simple press effect like iOS
 *
 * Simple Usage:
 * ImageView img = (ImageView) findViewById(R.id.img);
 * ViewPressEffectHelper.attach(img)
 *
 * @author Lam @ HongKong
 *
 */
public class ViewPressEffectHelper {
    public static void attach(View view){
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (SDK_INT < 11) {
                            final AlphaAnimation animation = new AlphaAnimation(1.0f, 0.5f);
                            animation.setDuration(100);
                            animation.setFillAfter(true);
                            v.startAnimation(animation);
                        } else
                            v.setAlpha(0.5f);
                    }
                    break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:{
                        if (SDK_INT < 11) {
                            final AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
                            animation.setDuration(100);
                            animation.setFillAfter(true);
                            v.startAnimation(animation);
                        } else
                            v.setAlpha(1.0f);
                    }
                    break;
                }
                return false;
            }
        });
    }
}