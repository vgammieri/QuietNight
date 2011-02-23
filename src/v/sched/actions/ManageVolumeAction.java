package v.sched.actions;

import v.sched.quite.QuiteNight;
import v.sched.quite.intent.QuiteIntents;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class ManageVolumeAction implements QuiteActions {

	private static final String ORIGINAL_RING_MODE = "ORIGINAL_RING_MODE";

	@Override
	public void handleIntent(Context context, Intent intent) {

		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor spe = sp.edit();

		if (intent.getAction().equals(QuiteIntents.START_NIGHT_INTENT)) {
			// abbassare il volume!
			
			int ringerMode = am.getRingerMode();
			if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
				spe.putInt(ORIGINAL_RING_MODE, ringerMode);
				spe.commit();
				//am.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Log.i(QuiteNight.LOG_TAG, "Set ringher mode to silent");
			}else{
				Log.i(QuiteNight.LOG_TAG, "Ringer mode already silent, setting it is useless");
			}

		}else{
			// ripristino il volume
			int ringerMode = am.getRingerMode();
			
			if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
				
				ringerMode = sp.getInt(ORIGINAL_RING_MODE, -1);
				spe.putInt(ORIGINAL_RING_MODE, -1);
				spe.commit();
				if (ringerMode!=-1 ){
					//am.setStreamVolume(AudioManager.STREAM_RING, ringVolume, 0);
					am.setRingerMode(ringerMode);
					Log.i(QuiteNight.LOG_TAG, "Set ringer mode to original");
				}
			}else{
				Log.i(QuiteNight.LOG_TAG, "Ringher mode not silent, setting it is useless");
			}
		}

	}

}
