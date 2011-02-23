package v.sched.quite;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

public class DimmingActivity extends Activity {

	public static String SCREEN_BRIGHTNESS_MODE = "screen_brightness_mode";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		
		int val = getIntent().getIntExtra("brightness", 0);
		boolean enable_auto = getIntent().getBooleanExtra("enable_auto", false);
		
		if (enable_auto)
			Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE, 1);
		else
			Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE, 0);
		
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, val);
		getWindow().getAttributes().screenBrightness = val;
		//finish();
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					
				}
				
				finish();
				
			}
		});
		t.start();
		
	}
	
	public static boolean isAutoBacklightEnabled(Context context){
		return Settings.System.getInt(context.getContentResolver(), SCREEN_BRIGHTNESS_MODE, -1)==1;
	}

}
