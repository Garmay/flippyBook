package com.example.flippybook;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FlippyBookFragment extends Fragment {
	private Context context;
	private Handler handler;
	private PillarsManager pillarsManager;
	private LinearInterpolator linearInterpolator;
	private ImageView book;
	private boolean fall = true;
	private ValueAnimator preValueAnimator;
	private int screenHeight;
	private int boxLeftMargin=0;
	private int boxWidth=0;
	private boolean inTheEnd = false;
	private boolean gameStart = false;
	private int pillarMoveSpeed = 2000; 
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		linearInterpolator = new LinearInterpolator();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.flippybook, container,false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		book = (ImageView) ((Activity)context).findViewById(R.id.flippyBook);
		RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)book.getLayoutParams();
		boxLeftMargin = rlp.leftMargin;
		boxWidth = rlp.width;
		
		RelativeLayout mainRel = (RelativeLayout)((Activity)context).findViewById(R.id.flippybook_main_lay);
		
		mainRel.setOnTouchListener(onTouchListener);
		
		pillarsManager = new PillarsManager(context,mainRel);
		
	}
	
	private void movePillar(final ImageView v){//this view must have been added to the RelativeLayout(Container)
		final RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)v.getLayoutParams();
		ValueAnimator va = ValueAnimator.ofInt(rlp.leftMargin,0-rlp.width);
		va.setDuration(pillarMoveSpeed);
		if(pillarMoveSpeed>=800)
			pillarMoveSpeed-=50;
		va.setInterpolator(linearInterpolator);
		va.addUpdateListener(new AnimatorUpdateListener() {
			int leftMargin=0;
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				leftMargin = (Integer) animation.getAnimatedValue();
				if(leftMargin<boxLeftMargin+boxWidth && leftMargin>boxLeftMargin){//must be checked conflict
					RelativeLayout.LayoutParams bookrlp = (RelativeLayout.LayoutParams)book.getLayoutParams();
					if(rlp.getRules()[RelativeLayout.ALIGN_PARENT_TOP]==-1){//at top
						if(bookrlp.topMargin<=rlp.height)
							onCollision();
					}
					else{//at bottom
						if(bookrlp.topMargin+bookrlp.height>=screenHeight-(rlp.height))
							onCollision();
					}
				}
				rlp.setMargins(leftMargin, rlp.topMargin, rlp.rightMargin, rlp.bottomMargin);
				v.setLayoutParams(rlp);
			}
		});
		va.addListener(new AnimatorListener(){

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
			}});
		va.start();		
	}

	private void fallBook(){
		RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)book.getLayoutParams();
		if(rlp.topMargin<screenHeight-rlp.height)
			rlp.setMargins(rlp.leftMargin, rlp.topMargin+=10, rlp.rightMargin, rlp.bottomMargin);
		book.setLayoutParams(rlp);
	}
	
	private Runnable pillarGeneratorRunnable = new Runnable(){

		@Override
		public void run() {
			movePillar(pillarsManager.getImageView());
			handler.postDelayed(pillarGeneratorRunnable,1200);
			
		}};
	
	private Runnable bookFallRunnable = new Runnable(){

		@Override
		public void run() {
			if(fall)
				fallBook();
			handler.postDelayed(bookFallRunnable, 10);
		}
		
	};
	
	private void setBookColorChange(){
		book = (ImageView) ((Activity)context).findViewById(R.id.flippyBook);
		ValueAnimator va = ValueAnimator.ofInt(50,100,50);
		va.setInterpolator(new DecelerateInterpolator());
		va.setDuration(2000);
		va.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int val = (Integer) animation.getAnimatedValue();
				if(inTheEnd){
					animation.cancel();
					return;
				}
				book.setBackgroundColor(val<<8 | 0xFF0000CC);
				
			}
		});
		va.setRepeatCount(ValueAnimator.INFINITE);
		va.start();
	}

	private OnTouchListener onTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
				if(!gameStart){
					onGameStart();
					gameStart=true;
					return false;
				}
				
				fall=false;
				bookJump();				
			}
			return false;
		}
	};
	
	private void bookJump(){
		
		final RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)book.getLayoutParams();
		ValueAnimator va = ValueAnimator.ofInt(rlp.topMargin,rlp.topMargin-200);
		va.setDuration(300);
		va.setInterpolator(new DecelerateInterpolator());
		va.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				rlp.setMargins(rlp.leftMargin, (Integer) animation.getAnimatedValue(), rlp.rightMargin,rlp.bottomMargin );
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
				fall=true;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
		if(preValueAnimator!=null){
			preValueAnimator.cancel();
			preValueAnimator=null;
		}
		preValueAnimator = va;
		va.start();
	}

	private void onGameStart(){
		handler.post(pillarGeneratorRunnable);
		handler.post(bookFallRunnable);
		setBookColorChange();
	}

	private void onCollision(){
		inTheEnd=true;
		book.setBackgroundColor(Color.RED);
	}
}
