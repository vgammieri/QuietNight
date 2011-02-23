package v.sched.actions;

import v.sched.quite.DimmingActivity;
import v.sched.quite.QuiteNight;
import v.sched.quite.intent.QuiteIntents;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class ManageBacklightAction implements QuiteActions {

	private static final String ORIGINAL_BRIGHT_LEVEL = "ORIGINAL_BRIGHT_LEVEL";
	private static final String ORIGINAL_BRIGHT_MODE = "ORIGINAL_BRIGHT_MODE";

	@Override
	public void handleIntent(Context context, Intent intent) {
		
		int ib = Settings.System.getInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, -1);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor spe = sp.edit();

		if (intent.getAction().equals(QuiteIntents.START_NIGHT_INTENT)) {
			// riduco la luminosita'
			if (ib > 10) {
				Log.i(QuiteNight.LOG_TAG, "Set backlight level to 10");
				
				spe.putInt(ORIGINAL_BRIGHT_LEVEL, ib);
				spe.putBoolean(ORIGINAL_BRIGHT_MODE, DimmingActivity.isAutoBacklightEnabled(context));
				spe.commit();
//				Settings.System.putInt(context.getContentResolver(),
//						Settings.System.SCREEN_BRIGHTNESS, 0);
//				
				setBrightness(context,10,false);
			} else {
				Log.i(QuiteNight.LOG_TAG,
						"Backlight level = 10, setting it is useless");
			}
		} else {
			// ripristino
			if (ib <= 10) {
				ib = sp.getInt(ORIGINAL_BRIGHT_LEVEL, -1);
				boolean backlight = sp.getBoolean(ORIGINAL_BRIGHT_MODE, false);
				if (ib != -1) {
					Log.i(QuiteNight.LOG_TAG, "Set backlight level to " + ib);
					spe.putInt(ORIGINAL_BRIGHT_LEVEL, -1);
					spe.putBoolean(ORIGINAL_BRIGHT_MODE, false);
					spe.commit();
//					Settings.System.putInt(context.getContentResolver(),
//							Settings.System.SCREEN_BRIGHTNESS, ib);
					setBrightness(context, ib, backlight);
				}
			} else {
				Log.i(QuiteNight.LOG_TAG,
						"Backlight level > 10, setting it is useless");
			}
		}
	}

	private void setBrightness(Context context, int brightness, boolean enableAuto) {

		Intent changeActivity = new Intent(context, DimmingActivity.class);
//		changeActivity.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		changeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		changeActivity.putExtra("brightness", brightness);
		changeActivity.putExtra("enable_auto", enableAuto);
		// changeActivity.setClassName("v.custombright", "DimmingActivity");
		context.startActivity(changeActivity);
		
	}

}
