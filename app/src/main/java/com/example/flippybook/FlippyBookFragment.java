package com.example.flippybook;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class FlippyBookFragment extends Fragment {
    private Handler handler;
    private PillarsManager pillarsManager;
    private LinearInterpolator linearInterpolator;
    private ImageView book;
    private boolean fall = true;
    private ValueAnimator preValueAnimator;
    private int screenHeight;
    private int boxLeftMargin = 0;
    private int boxWidth = 0;
    private boolean inTheEnd = false;
    private boolean gameStart = false;
    private int pillarMoveSpeed = 2000;

    private int jumpRange = 200;

    private List<ValueAnimator> moveAnimList;

    private RelativeLayout mainLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        linearInterpolator = new LinearInterpolator();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.flippybook, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        book = (ImageView) getActivity().findViewById(R.id.flippyBook);
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) book.getLayoutParams();
        boxLeftMargin = rlp.leftMargin;
        boxWidth = rlp.width;

        mainLayout = (RelativeLayout) getActivity().findViewById(R.id.flippybook_main_lay);

        mainLayout.setOnTouchListener(onTouchListener);

        pillarsManager = new PillarsManager(getActivity(), mainLayout);

        moveAnimList = new ArrayList<ValueAnimator>();

    }

    private void movePillar(final ImageView v) {//this view must have been added to the RelativeLayout(Container)
        final RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) v.getLayoutParams();
        ValueAnimator va = ValueAnimator.ofInt(rlp.leftMargin, 0 - rlp.width);
        va.setDuration(pillarMoveSpeed);
        if (pillarMoveSpeed >= 800)
            pillarMoveSpeed -= 50;
        va.setInterpolator(linearInterpolator);
        va.addUpdateListener(new AnimatorUpdateListener() {
            int leftMargin = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                leftMargin = (Integer) animation.getAnimatedValue();
                if (leftMargin < boxLeftMargin + boxWidth && leftMargin > boxLeftMargin) {//must be checked conflict
                    RelativeLayout.LayoutParams bookrlp = (RelativeLayout.LayoutParams) book.getLayoutParams();
                    if (rlp.getRules()[RelativeLayout.ALIGN_PARENT_TOP] == -1) {//at top
                        if (bookrlp.topMargin <= rlp.height)
                            onCollision();
                    } else {//at bottom
                        if (bookrlp.topMargin + bookrlp.height >= screenHeight - (rlp.height))
                            onCollision();
                    }
                }
                rlp.setMargins(leftMargin, rlp.topMargin, rlp.rightMargin, rlp.bottomMargin);
                v.setLayoutParams(rlp);
            }
        });
        va.addListener(new AnimatorListener() {

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                pillarsManager.RestoreView(v);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }
        });
        va.start();
        moveAnimList.add(va);
    }

    private void fallBook() {
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) book.getLayoutParams();
        if (rlp.topMargin < screenHeight - rlp.height){
            rlp.setMargins(rlp.leftMargin, rlp.topMargin += 10, rlp.rightMargin, rlp.bottomMargin);
        }else{
            if (!inTheEnd)
                onCollision();
        }
        book.setLayoutParams(rlp);
    }

    private Runnable pillarGeneratorRunnable = new Runnable() {

        @Override
        public void run() {
            if (!inTheEnd){
                movePillar(pillarsManager.getImageView());
            }
            handler.postDelayed(pillarGeneratorRunnable, 1200);

        }
    };

    private Runnable bookFallRunnable = new Runnable() {

        @Override
        public void run() {
            if (fall){
                fallBook();
            }
            handler.postDelayed(bookFallRunnable, 10);
        }

    };

    private void setBookColorChange() {
        book = (ImageView) getActivity().findViewById(R.id.flippyBook);
        ValueAnimator va = ValueAnimator.ofInt(50, 100, 50);
        va.setInterpolator(new DecelerateInterpolator());
        va.setDuration(2000);
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                if (inTheEnd) {
                    animation.cancel();
                    return;
                }
                book.setBackgroundColor(val << 8 | 0xFF0000CC);

            }
        });
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.start();
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (inTheEnd) {
                    return false;
                }
                ;

                if (!gameStart) {
                    onGameStart();
                    gameStart = true;
                    return false;
                }

                fall = false;
                bookJump();
            }
            return false;
        }
    };

    private void bookJump() {

        final RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) book.getLayoutParams();


        if ((rlp.topMargin+book.getHeight())-jumpRange<0){
            fall = true;
            return;
        }

        ValueAnimator va = ValueAnimator.ofInt(rlp.topMargin, rlp.topMargin - jumpRange);
        va.setDuration(300);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rlp.setMargins(rlp.leftMargin, (Integer) animation.getAnimatedValue(), rlp.rightMargin, rlp.bottomMargin);
                book.setLayoutParams(rlp);

            }
        });
        va.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fall = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        if (preValueAnimator != null) {
            preValueAnimator.cancel();
            preValueAnimator = null;
        }
        preValueAnimator = va;
        va.start();
    }

    private void onGameStart() {
        ((AnimationDrawable)book.getBackground()).start();
        handler.post(pillarGeneratorRunnable);
        handler.post(bookFallRunnable);
        //setBookColorChange();
    }

    private void OnGameOver() {
        handler.removeCallbacks(pillarGeneratorRunnable);
        pauseAllMoveAnim();

        showGameOverEffect();
    }


    private void onCollision() {
        inTheEnd = true;
        book.setBackgroundColor(Color.RED);

        OnGameOver();
    }

    private void pauseAllMoveAnim() {
        for (ValueAnimator Anim : moveAnimList) {
            if (Anim.isRunning()){
                Anim.cancel();
            }
        }

        moveAnimList.clear();
    }

    private void showGameOverEffect(){

        //閃爍
        final View mView = new View(getActivity());
        mainLayout.addView(mView,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mView.setBackgroundColor(Color.WHITE);

        int fadeinoutDuration = 500;
        float minAlpha = 0f;
        float maxAlpha = 1f;

        Animation fadeIn = new AlphaAnimation(minAlpha, maxAlpha);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(fadeinoutDuration/5);

        Animation fadeOut = new AlphaAnimation(maxAlpha, minAlpha);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(fadeinoutDuration/2);
        fadeOut.setDuration(fadeinoutDuration/2);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainLayout.removeView(mView);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mView.startAnimation(animation);

    }

}
