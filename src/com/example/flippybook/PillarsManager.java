package com.example.flippybook;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class PillarsManager{
	private ArrayList<ImageView> restoredViewList;
	private Context context;
	private ViewGroup container;
	private Random random;
	private int screenWidth;
	private int heightRandomHeighten;
	private int heightRandomMin;
	public PillarsManager(Context context,ViewGroup container){
		restoredViewList = new ArrayList<ImageView>();
		this.context = context;
		this.container = container;
		random = new Random();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenWidth = displaymetrics.widthPixels;
		heightRandomHeighten = (int) (displaymetrics.heightPixels*0.2);
		heightRandomMin = (int) (displaymetrics.heightPixels*0.3);
		
	}
	public ImageView getImageView(){
		if(restoredViewList.isEmpty()){
			ImageView iv = new ImageView(context);
			container.addView(iv);
			return setUpPillar(iv);
		}
		else{
			return setUpPillar(restoredViewList.remove(0));
		}
		
	}
	public void RestoreView(ImageView restoredView){
		restoredViewList.add(restoredView);
	}
	
	//set parameter of Pillar(just like width、height)
	private ImageView setUpPillar(ImageView pillar){
		RelativeLayout.LayoutParams rlp = (LayoutParams) pillar.getLayoutParams();
		if(random.nextBoolean()){
			rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);// 0 means false
		}
		else{
			rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);// 0 means false
		}
		
		rlp.width=100;
		rlp.height=random.nextInt(heightRandomHeighten)+heightRandomMin;
		
		rlp.setMargins(screenWidth, rlp.topMargin, rlp.rightMargin, rlp.bottomMargin);
		pillar.setLayoutParams(rlp);
		
		pillar.setBackgroundColor(Color.BLACK);
		
		return pillar;
	}

	
}
