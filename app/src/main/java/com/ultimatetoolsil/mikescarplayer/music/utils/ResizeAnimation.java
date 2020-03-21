package com.ultimatetoolsil.mikescarplayer.music.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {
    final int targetWidth;
    View view;
    int startWidth;

    public ResizeAnimation(View view, int targetWidth, int startWidth) {
        this.view = view;
        this.targetWidth = startWidth;
        this.startWidth = startWidth;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight = (int) (startWidth + startWidth * interpolatedTime);
        //to support decent animation, change new heigt as Nico S. recommended in comments
        //int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
        view.getLayoutParams().width = newHeight;

        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
