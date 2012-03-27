package com.lukekorth.screennotifications;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;

public class NotificationsCaptureService extends AccessibilityService {
	
	SensorManager mySensorManager;
	Sensor myProximitySensor;
	boolean close, sensor;
	SharedPreferences mPrefs;
	
	 public void onServiceConnected()
	  {
		close = false;
	    AccessibilityServiceInfo localAccessibilityServiceInfo = new AccessibilityServiceInfo();
	    localAccessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
	    localAccessibilityServiceInfo.feedbackType = 16;
	    localAccessibilityServiceInfo.notificationTimeout = 0L;
	    localAccessibilityServiceInfo.flags = 1;
	    setServiceInfo(localAccessibilityServiceInfo);

		mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		myProximitySensor = mySensorManager
				.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (myProximitySensor == null) {
			sensor = false;
		} else {
			mySensorManager.registerListener(proximitySensorEventListener,
					myProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
			sensor = true;
		}
	  }

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {	
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		if(sensor && !close && !pm.isScreenOn()) {
			mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			int time = mPrefs.getInt("time", 10);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
			wl.acquire();
			System.out.println("Screen on!");
			try {
				Thread.sleep(time * 1000);
			}
			catch (Exception e) {}
			wl.release(); 
		}
		
		
	}

	@Override
	public void onInterrupt() {}
	
	SensorEventListener proximitySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
				System.out.println(event.values[0]);
				if(event.values[0] == 0.0)
					close = true;
				else
					close = false;
				System.out.println(String.valueOf(close));
			}
		}
	};
}