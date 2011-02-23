package v.sched.quite;

import java.util.Calendar;
import java.util.Date;

import v.sched.actions.ManageBacklightAction;
import v.sched.actions.ManageFlightMode;
import v.sched.actions.ManageVolumeAction;
import v.sched.quite.intent.QuiteIntents;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class QuiteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Toast.makeText(context, "Received " + intent.getAction(),
		// 2000).show();

		Log.i(QuiteNight.LOG_TAG, "Received " + intent.getAction());

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			setNotificationAlarms(context);
			return;
		}

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean manageVolume = sp.getBoolean("manageVolume", false);
		boolean manageBacklight = sp.getBoolean("manageBacklight", false);
		boolean manageFlight = sp.getBoolean("manageFlight", false);

		if (manageFlight)
			new ManageFlightMode().handleIntent(context, intent);

		if (manageVolume)
			new ManageVolumeAction().handleIntent(context, intent);

		if (manageBacklight)
			new ManageBacklightAction().handleIntent(context, intent);

		scheduleActionNextDay(context, intent.getAction());
	}

	public static void setNotificationAlarms(Context context) {

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);

		String startNight = sp.getString("startNightTime", "");
		String endNight = sp.getString("endNightTime", "");

		if (!isTime(startNight) || !isTime(endNight)) {
			return;
		}

		String s = "";
		String slabel = context.getResources().getString(
				R.string.toast_start_night);
		String elabel = context.getResources().getString(
				R.string.toast_end_night);

		s = slabel;
		s += ": ";
		s += startNight;
		s += "\n";
		s += elabel;
		s += ": ";
		s += endNight;
		Toast.makeText(context, s, 2000).show();

		boolean sameDay = sp.getBoolean("timesOnSameDay", false);

		long start = getStringAsTime(startNight, true);
		long end = getStringAsTime(endNight, true);

		boolean setStartAndEndToday = false;

		if (sameDay) {
			if (start > end) {
				// errore
				// showInvalidDatesDialog(context);
				return;
			} else {
				long now = System.currentTimeMillis();
				if (now > end) {
					// inutile settare i timer oggi... imposto a domani
					setStartAndEndToday = false;
				} else {
					setStartAndEndToday = true;
				}
			}
		}

		if (sameDay) {
			QuiteReceiver.scheduleAction(context,
					QuiteIntents.START_NIGHT_INTENT, startNight, setStartAndEndToday);

			QuiteReceiver.scheduleAction(context,
					QuiteIntents.END_NIGHT_INTENT, endNight, setStartAndEndToday);
		} else {

			QuiteReceiver.scheduleAction(context,
					QuiteIntents.START_NIGHT_INTENT, startNight, true);

			QuiteReceiver.scheduleAction(context,
					QuiteIntents.END_NIGHT_INTENT, endNight, false);
		}

	}

	public static boolean isTime(String time) {
		try {
			if (time.length() == 0)
				return false;

			String[] comps = time.split(":");
			if (comps.length != 2)
				return false;

			int v = Integer.valueOf(comps[0]);
			if (v < 0 || v > 24)
				return false;

			v = Integer.valueOf(comps[1]);
			if (v < 0 || v > 59)
				return false;

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void scheduleActionNextDay(Context context, String action) {

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		String time = "";
		if (action.equals(QuiteIntents.START_NIGHT_INTENT))
			time = sp.getString("startNightTime", "");
		else
			time = sp.getString("endNightTime", "");

		if (time.length() > 0) {
			scheduleAction(context, action, time, false);
		}

	}

	public static void scheduleAction(Context ctx, String action, String time,
			boolean thisDay) {
		long startTime = getStringAsTime(time, thisDay);
		AlarmManager am = (AlarmManager) ctx
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctx, QuiteReceiver.class);
		i.setAction(action);
		PendingIntent operation = PendingIntent.getBroadcast(ctx, 0, i, 0);
		am.set(AlarmManager.RTC_WAKEUP, startTime, operation);
		Log.i(QuiteNight.LOG_TAG, "Schedule " + action + " at "
				+ new Date(startTime).toString());
	}

	public static long getStringAsTime(String startNight, boolean thisDay) {
		String[] dateComp = startNight.split(":");
		int h = Integer.valueOf(dateComp[0]);
		int m = Integer.valueOf(dateComp[1]);

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.HOUR_OF_DAY, h);
		c.set(Calendar.MINUTE, m);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		if (thisDay == true) {
			return c.getTimeInMillis();
		} else {
			// andiamo avanti di un giorno
			c.add(Calendar.DATE, 1);
			return c.getTimeInMillis();
		}

	}

	public static void deleteNotificationAlarms(Context context, String action) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, QuiteReceiver.class);
		i.setAction(action);
		PendingIntent operation = PendingIntent.getBroadcast(context, 0, i, 0);
		am.cancel(operation);
		Log.i(QuiteNight.LOG_TAG, "Deleting action " + action);
	}

}
