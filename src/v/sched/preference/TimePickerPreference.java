package v.sched.preference;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class TimePickerPreference extends DialogPreference implements
		OnTimeChangedListener {

	private static final String VALIDATION_EXPRESSION = "[0-2]*[0-9]:[0-5]*[0-9]";
	private String defaultValue = new SimpleDateFormat("HH:mm")
			.format(new Date());
	private int hour;
	private int mins;
	private TimePicker timepicker;

	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public TimePickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	private void initialize() {
		setPersistent(true);

	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		setDisplayTimeAndSave(false);
		return super.onCreateView(parent);
	}

	@Override
	protected View onCreateDialogView() {
		TimePicker timepicker = new TimePicker(getContext());
		timepicker.setIs24HourView(true);
		timepicker.setOnTimeChangedListener(this);
		int h = getHour();
		int m = getMinutes();
		if (h >= 0 && m >= 0) {
			timepicker.setCurrentHour(h);
			timepicker.setCurrentMinute(m);
		}

		this.timepicker = timepicker;
		return timepicker;

	}

	private int getMinutes() {
		String time = getPersistedString(defaultValue);
		if (time == null || !time.matches(VALIDATION_EXPRESSION)) {
			return -1;
		}

		return Integer.valueOf(time.split(":")[1]);
	}

	private int getHour() {
		String time = getPersistedString(defaultValue);
		if (time == null || !time.matches(VALIDATION_EXPRESSION)) {
			return -1;
		}

		return Integer.valueOf(time.split(":")[0]);
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		hour = hourOfDay;
		mins = minute;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		timepicker.requestFocus();
		if (positiveResult) {
			if (isPersistent()) {
				
				setDisplayTimeAndSave(true);
			}
		}
	}

	private void setDisplayTimeAndSave(boolean useFields) {
		int h = useFields?hour:getHour();
		int m = useFields?mins:getMinutes();
		String summary = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m);
		setSummary(summary);

		persistString(summary);

	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		super.setDefaultValue(defaultValue);
		this.defaultValue = (String) defaultValue;
	}
}
