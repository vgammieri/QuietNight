package v.sched.quite;

import v.sched.quite.intent.QuiteIntents;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;

public class QuiteNight extends PreferenceActivity {

	public static final String LOG_TAG = QuiteNight.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// AdManager.setPublisherId("a14c5e6a8c9f623");

		setContentView(R.layout.quite_night);
		addPreferencesFromResource(R.xml.settings);

		Preference p = findPreference("startNightTime");
		p.setDefaultValue("22:30");

		p = findPreference("endNightTime");
		p.setDefaultValue("7:00");

		p = findPreference("author");
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_SEND);
				// i.setType("text/plain"); //use this line for testing in the
				// emulator
				i.setType("message/rfc822"); // use from live device
				i.putExtra(Intent.EXTRA_EMAIL,
						new String[] { "vincenzo.gammieri@gmail.com" });
				i.putExtra(Intent.EXTRA_SUBJECT, "QuiteNight");
				i.putExtra(Intent.EXTRA_TEXT, "...");
				startActivity(Intent.createChooser(i, null));
				return true;

			}
		});

		
		findPreference("monitoringEnabled").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						Boolean b = (Boolean) newValue;
						setTimers(b);

						return true;
					}
				});

		findPreference("timesOnSameDay").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						boolean sameDay = ((Boolean) newValue).booleanValue();

						if(!isTimeSequenceOk(sameDay)){
							showInvalidDatesDialog(QuiteNight.this);
							return false;
						}else
							return true;
					}
				});

//		findPreference("startNightTime").setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			
//			@Override
//			public boolean onPreferenceClick(Preference preference) {
//				showWarningIfChangingTimeInBadSequence();
//				return true;
//			}
//		});
//		
//		
//		
//		findPreference("endNightTime").setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			
//			@Override
//			public boolean onPreferenceClick(Preference preference) {
//				showWarningIfChangingTimeInBadSequence();
//				return true;
//			}
//		});

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if (key.equals("endNightTime") || key.equals("startNightTime")){
					showWarningIfChangingTimeInBadSequence();
				}
				
			}
		});
		
		AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });

		AdView adView = (AdView) findViewById(R.id.ad);
		adView.requestFreshAd();

	}

	protected void showWarningIfChangingTimeInBadSequence() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		boolean sameDay = sp.getBoolean("timesOnSameDay", false);

		if (!isTimeSequenceOk(sameDay)){
			new AlertDialog.Builder(this).setTitle(
					android.R.string.dialog_alert_title).setMessage(
					R.string.timesOnSameDay_badTimeSequence_DisablingSameDay).setPositiveButton(
					android.R.string.ok, null).show();
			((CheckBoxPreference)findPreference("timesOnSameDay")).setChecked(false);
			
		}
	}

	boolean isTimeSequenceOk(boolean sameDay) {

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(QuiteNight.this);

		String startNight = sp.getString("startNightTime", "");
		String endNight = sp.getString("endNightTime", "");

		if (sameDay) {
			long start = QuiteReceiver.getStringAsTime(startNight, true);
			long end = QuiteReceiver.getStringAsTime(endNight, true);
			if (start > end) {
				
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			setTimers(null);
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	private void setTimers(Boolean b) {
		boolean monitoringEnabled;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (b == null) {
			monitoringEnabled = sp.getBoolean("monitoringEnabled", false);
		} else {
			monitoringEnabled = b.booleanValue();
		}

		boolean sameDay = sp.getBoolean("timesOnSameDay", false);

		String startNight = sp.getString("startNightTime", "");
		String endNight = sp.getString("endNightTime", "");

		if (sameDay) {
			long start = QuiteReceiver.getStringAsTime(startNight, true);
			long end = QuiteReceiver.getStringAsTime(endNight, true);
			if (start > end) {
				// errore
				// showInvalidDatesDialog(this);
				return;
			}
		}

		if (monitoringEnabled)
			QuiteReceiver.setNotificationAlarms(getApplicationContext());
		else {
			QuiteReceiver.deleteNotificationAlarms(getApplicationContext(),
					QuiteIntents.START_NIGHT_INTENT);
			QuiteReceiver.deleteNotificationAlarms(getApplicationContext(),
					QuiteIntents.END_NIGHT_INTENT);
		}
	}

	private static void showInvalidDatesDialog(Context context) {
		new AlertDialog.Builder(context).setTitle(
				android.R.string.dialog_alert_title).setMessage(
				R.string.timesOnSameDay_badTimeSequence).setPositiveButton(
				android.R.string.ok, null).show();

	}

	// private void setNotificationAlarms(Context context) {
	//		
	// SharedPreferences sp = PreferenceManager
	// .getDefaultSharedPreferences(context);
	//		
	// String startNight = sp.getString("startNightTime","");
	// String endNight = sp.getString("endNightTime","");
	//		
	//		
	// String s ="";
	// String slabel = getResources().getString(R.string.toast_start_night);
	// String elabel = getResources().getString(R.string.toast_end_night);
	//		
	// s = slabel;
	// s += ":";
	// s+=startNight;
	// s+="\n";
	// s+=elabel;
	// s+=":";
	// s+=endNight;
	// Toast.makeText(getApplicationContext(), s, 2000).show();
	//		
	// QuiteReceiver.scheduleAction(context, QuiteIntents.START_NIGHT_INTENT,
	// startNight, true);
	// QuiteReceiver.scheduleAction(context, QuiteIntents.END_NIGHT_INTENT,
	// endNight, false);
	//		
	//		
	// }

}