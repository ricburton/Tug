package com.jjoe64.graphview.compatible;

import android.content.Context;
//import android.view.ScaleGestureDetector;
import com.jjoe64.graphview.compatible.ScaleGestureDetector;

//import android.

/**
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 */
public class RealScaleGestureDetector extends ScaleGestureDetector {
	public RealScaleGestureDetector(Context context, final com.jjoe64.graphview.compatible.ScaleGestureDetector fakeScaleGestureDetector, final com.jjoe64.graphview.compatible.ScaleGestureDetector.SimpleOnScaleGestureListener fakeListener) {
		super(context, new com.jjoe64.graphview.compatible.ScaleGestureDetector.SimpleOnScaleGestureListener() {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				return fakeListener.onScale(fakeScaleGestureDetector);
			}
		});
	}
}
