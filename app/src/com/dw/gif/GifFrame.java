package com.dw.gif;

import android.graphics.Bitmap;

public class GifFrame {
	
	public GifFrame(Bitmap im, int del, int frameIndex) {
		image = im;
		delay = del;
		index = frameIndex;
	}
	
	public Bitmap image;

	public int index;

	public int delay;
	
	public GifFrame nextFrame = null;
}
