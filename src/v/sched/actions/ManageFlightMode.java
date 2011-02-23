package v.sched.actions;

import v.sched.quite.QuiteNight;
import v.sched.quite.intent.QuiteIntents;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class ManageFlightMode implements QuiteActions {

	@Override
	public void handleIntent(Context context, Intent intent) {

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor spe = sp.edit();

		if (intent.getAction().equals(QuiteIntents.START_NIGHT_INTENT)) {
			if (isAirplaneModeOn(context)){
				Log.i(QuiteNight.LOG_TAG,"Airplane mode already on, setting it is useless");
			}else{
				Log.i(QuiteNight.LOG_TAG,"Setting airplane mode on");
				setAirplaneMode(context, true);
				
			}
		}else{
			if (isAirplaneModeOn(context)){
				
				Log.i(QuiteNight.LOG_TAG,"Setting airplane mode off");
				setAirplaneMode(context, false);
			}else{
				Log.i(QuiteNight.LOG_TAG,"Airplane mode already off, setting it is useless");
			}
		}

	}

	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	/**
	 * 
	 * @param status
	 */
	public static void setAirplaneMode(Context context, boolean status) {
		boolean isAirplaneModeOn = isAirplaneModeOn(context);

		if (isAirplaneModeOn && status) {
			return;
		}
		if (!isAirplaneModeOn && !status) {
			return;
		}
		if (isAirplaneModeOn && !status) {

			Settings.System.putInt(context.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0);
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", 0);
			context.sendBroadcast(intent);
			return;
		}
		if (!isAirplaneModeOn && status) {

			Settings.System.putInt(context.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 1);
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", 1);
			context.sendBroadcast(intent);
			return;
		}
	}

}
