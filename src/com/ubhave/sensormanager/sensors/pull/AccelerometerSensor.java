package com.ubhave.sensormanager.sensors.pull;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.data.pullsensor.AccelerometerData;
import com.ubhave.sensormanager.logs.ESLogger;
import com.ubhave.sensormanager.sensors.SensorList;

/**
 * Accelerometer sensor monitor.
 */
public class AccelerometerSensor extends AbstractPullSensor
{

	private static final String TAG = "AccelerometerSensor";

	private SensorEventListener listener; // accelerometer data listener
	private SensorManager sensorManager; // Controls the hardware sensor

	private ArrayList<float[]> sensorReadings;

	private static AccelerometerSensor accelerometerSensor;
	private static Object lock = new Object();

	public static AccelerometerSensor getAccelerometerSensor(Context context)
	{
		if (accelerometerSensor == null)
		{
			synchronized (lock)
			{
				if (accelerometerSensor == null)
				{
					accelerometerSensor = new AccelerometerSensor(context);
				}
			}
		}
		return accelerometerSensor;
	}

	private AccelerometerSensor(Context context)
	{
		super(context);
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		listener = new SensorEventListener()
		{

			// This method is required by the API and is called when the
			// accuracy of the
			// readings being generated by the accelerometer changes.
			// We don't do anything when this happens.
			public void onAccuracyChanged(Sensor sensor, int accuracy)
			{
			}

			// This method is called when the accelerometer takes a reading:
			// despite the name, it is called whether even if it's the same as
			// the previous one
			public void onSensorChanged(SensorEvent event)
			{
				try
				{
					if (isSensing)
					{
						synchronized (sensorReadings)
						{
							if (isSensing)
							{
								float[] data = new float[3];

								for (int i = 0; i < 3; i++)
								{
									data[i] = event.values[i];
								}

								sensorReadings.add(data);

							}
						}
					}
				}
				catch (Exception e)
				{
					ESLogger.error(TAG, e);
				}
			}
		};
		ESLogger.log(TAG, "new listener created " + listener.toString());
	}

	protected String getLogTag()
	{
		return TAG;
	}

	public int getSensorType()
	{
		return SensorList.SENSOR_TYPE_ACCELEROMETER;
	}

	protected AccelerometerData getMostRecentRawData()
	{
		AccelerometerData accelerometerData;
		synchronized (sensorReadings)
		{
			accelerometerData = new AccelerometerData(pullSenseStartTimestamp, sensorReadings);
		}
		return accelerometerData;
	}

	protected boolean startSensing(SensorConfig sensorConfig)
	{
		sensorReadings = new ArrayList<float[]>();

		boolean registrationSuccess = sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		return registrationSuccess;
	}

	protected void stopSensing()
	{
		sensorManager.unregisterListener(listener);
	}

}
