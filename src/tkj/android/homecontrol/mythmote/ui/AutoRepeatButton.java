/*
 * Copyright (C) 2010 Rob Elsner
 *
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package tkj.android.homecontrol.mythmote.ui;

import tkj.android.homecontrol.mythmote.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Borrowed from Carl http://stackoverflow.com/questions/4284224
 * /android-hold-button-to-repeat-action
 * 
 * @author Carl and robelsner
 * 
 */
public class AutoRepeatButton extends Button {

	public static final int DEFAULT_INITIAL_DELAY = 500;
	public static final int DEFAULT_REPEAT_INTERVAL = 100;
	private static boolean sAutoRepeatEnabled = true;
	private static int sRepeatInterval = DEFAULT_REPEAT_INTERVAL;
	private long initialRepeatDelay = 500;
	private long repeatIntervalInMilliseconds = 100;
	private boolean wasLongClick = false;
	
	private Runnable repeatClickWhileButtonHeldRunnable = new Runnable() {
		@Override
		public void run() {
			wasLongClick = true;
			
			performLongClick();

			// Schedule the next repetitions of the click action, using a faster
			// repeat
			// interval than the initial repeat delay interval.
			if(sAutoRepeatEnabled) {
				postDelayed(repeatClickWhileButtonHeldRunnable, getActiveDelay());
			}
		}
	};

	private void init() {
		
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					// Just to be sure that we removed all callbacks,
					// which should have occurred in the ACTION_UP
					removeCallbacks(repeatClickWhileButtonHeldRunnable);
					
					//enable drawing of button pressed state
					v.setPressed(true);

					// Schedule the start of repetitions after a one half second
					// delay.
					postDelayed(repeatClickWhileButtonHeldRunnable, initialRepeatDelay);
				} else if (action == MotionEvent.ACTION_UP){
					//perform click if its was not a long press
					if ( !wasLongClick) 
						performClick();
					
					//return button to non pressed state
					v.setPressed(false);
					
					// Cancel any repetition in progress.
					removeCallbacks(repeatClickWhileButtonHeldRunnable);
				}else if(action == MotionEvent.ACTION_CANCEL) {
					//return button to non pressed state
					v.setPressed(false);
					
					// Cancel any repetition in progress.
					removeCallbacks(repeatClickWhileButtonHeldRunnable);
				}
				
				wasLongClick = false;
				// Returning true here prevents performClick() from getting
				// called in the usual manner, which would be redundant, given
				// that we are already calling it above.
				return true;
			}
		});
	}

	public AutoRepeatButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AutoRepeatButton(Context context) {
		super(context);
		init();
	}

	public AutoRepeatButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoRepeatButton);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);

			switch (attr) {
			case R.styleable.AutoRepeatButton_initial_delay:
				initialRepeatDelay = a.getInt(attr, DEFAULT_INITIAL_DELAY);
				break;
			case R.styleable.AutoRepeatButton_repeat_interval:
				repeatIntervalInMilliseconds = a.getInt(attr,
						DEFAULT_REPEAT_INTERVAL);
				break;
			}
		}
		init();
	}
	
	public static void SetAutoRepeatEnalbed(boolean enabled){
		sAutoRepeatEnabled = enabled;
	}
	
	public static boolean GetAutoRepeatEnalbed(){
		return sAutoRepeatEnabled;
	}
	
	public static void SetRepeatInterval(int interval){
		sRepeatInterval = interval;
	}
	
	public static int GetRepeatInterval(){
		return sRepeatInterval;
	}
	
	
	private long getActiveDelay(){
		//if we have read in a non-default delay attribute use it
		if(this.repeatIntervalInMilliseconds != DEFAULT_REPEAT_INTERVAL) {
			return this.repeatIntervalInMilliseconds;
		}
		
		//use system wide set delay
		return sRepeatInterval;
	}
}