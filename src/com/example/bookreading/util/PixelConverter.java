package com.example.bookreading.util;

import android.content.Context;

/**
 * This is a utility class useful for Pixel conventions which are supported by Andriod.
 *
 */
public class PixelConverter {
	
	/**Converts actual pixel value into Scale Independent Pixel value using activity context.
	 * @param context Activity context
	 * @param pixelValue Pixel value
	 * @return Scale Independent Pixel value
	 */
	public static float getScaleIndependentPixelFromPixelValue(Context context, float pixelValue){
		 float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		 return pixelValue/scaledDensity;
	}

}
