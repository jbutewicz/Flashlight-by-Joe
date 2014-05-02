package com.jbutewicz.flashlightbyjoe;

import java.io.IOException;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

public class Flashlight extends Activity implements SurfaceHolder.Callback {

	// This class turns the LED on and off for phones. Some phones require
	// require a camera preview to be drawn to a SurfaceView, so this class
	// implements the SurfaceHolder.Callback.

	int count = 0;
	static Camera mCameraActivity;
	static Button flashlight_button;
	Parameters params;
	SurfaceView preview;
	SurfaceHolder mHolder;

	// In onCreate we are cleaning things up and opening the view and setting
	// the clickListener for the button.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flashlight);
		preview = (SurfaceView) findViewById(R.id.preview);
		mHolder = preview.getHolder();

		if (AppGlobals.getIsFlashOn()) {
			if (FlashlightWidgetReceiver.getmCameraWidget() != null) {

				flashOffWidget();
				setWidgetTo(R.drawable.light_on);

			}

			if (getmCameraActivity() != null) {

				try {
					flashOffApp();
				} catch (Exception e) {
					e.printStackTrace();
				}

				setWidgetTo(R.drawable.light_on);

			}

			count = 0;
			turnMotorolaOff();

		}

		try {
			setmCameraActivity(Camera.open());
		} catch (Exception e) {
			e.printStackTrace();
		}

		flashlight_button = (Button) findViewById(R.id.flashlight_button);
		flashlight_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (AppGlobals.getIsFlashOn()) {
					processOffClick();
				} else {
					processOnClick();
				}

			}
		});

	}

	// This is a design decision. When the user hits the home button, leave the
	// light on if it is on. We go through the processOffClick if the light is
	// off just to make sure everything is in sync.
	@Override
	protected void onPause() {
		super.onPause();

		if (!AppGlobals.getIsFlashOn()) {
			processOffClick();
		}

	}

	// This is a design decision. When the user hits the back button, turn the
	// light off.
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		processOffClick();
	}

	// When the app resumes turn the LED off. This is a design decision. I think
	// the user will likely want to open the app when it is on only to turn it
	// off.
	@Override
	protected void onResume() {
		super.onResume();

		if (AppGlobals.getIsFlashOn()) {
			if (FlashlightWidgetReceiver.getmCameraWidget() != null) {

				flashOffWidget();
				setWidgetTo(R.drawable.light_on);

			}

			if (getmCameraActivity() != null) {

				try {
					flashOffApp();
				} catch (Exception e) {
					e.printStackTrace();
				}

				setWidgetTo(R.drawable.light_on);

			}

			count = 0;
			turnMotorolaOff();

		}

		if (AppGlobals.getIsFlashOn()) {
			flashlight_button.setBackgroundResource(R.drawable.light_off);
		} else if (!AppGlobals.getIsFlashOn()) {
			flashlight_button.setBackgroundResource(R.drawable.light_on);
		}

	}

	// Any time the widget or the button in the app is pressed to turn the LED
	// on we process this off click. On method that is deprecated is needed for
	// earlier than Android 3.0 devices.
	@SuppressWarnings("deprecation")
	private void processOnClick() {

		flashlight_button.setBackgroundResource(R.drawable.light_off);
		setWidgetTo(R.drawable.light_off);

		if (getmCameraActivity() == null) {
			try {
				mHolder.addCallback(this);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
					mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				}
				setmCameraActivity(Camera.open());
				try {
					if (mHolder != null) {
						getmCameraActivity().setPreviewDisplay(mHolder);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (getmCameraActivity() != null) {
			flashOnApp();
		}

		if (FlashlightWidgetReceiver.getmCameraWidget() != null) {
			flashOnWidget();
		}

		turnMotorolaOn();

	}

	// Turns the LED on when the button on the app is pressed.
	private void flashOnApp() {
		setParams(getmCameraActivity().getParameters());

		List<String> flashModes = getParams().getSupportedFlashModes();

		if (flashModes == null) {
			return;
		} else {
			if (count == 0) {
				getParams().setFlashMode(Parameters.FLASH_MODE_OFF);
				getmCameraActivity().setParameters(getParams());
				preview = (SurfaceView) findViewById(R.id.preview);
				mHolder = preview.getHolder();
				mHolder.addCallback(this);

				try {
					getmCameraActivity().startPreview();
				} catch (Exception e) {
					e.printStackTrace();

				}

				AppGlobals.setIsFlashOn(true);
			}

			String flashMode = getParams().getFlashMode();

			if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

				if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
					getParams().setFlashMode(Parameters.FLASH_MODE_TORCH);
					getmCameraActivity().setParameters(getParams());
				} else {
					getParams().setFlashMode(Parameters.FLASH_MODE_ON);

					getmCameraActivity().setParameters(getParams());
					try {
						getmCameraActivity().autoFocus(new AutoFocusCallback() {
							public void onAutoFocus(boolean success,
									Camera camera) {
								count = 1;
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				AppGlobals.setIsFlashOn(true);

			}
		}
	}

	// Turns the LED on when the widget is pressed.
	private void flashOnWidget() {
		FlashlightWidgetReceiver.setParamsWidget(FlashlightWidgetReceiver
				.getmCameraWidget().getParameters());

		List<String> flashModes = FlashlightWidgetReceiver.getParamsWidget()
				.getSupportedFlashModes();

		if (flashModes == null) {
			return;
		} else {
			if (count == 0) {
				FlashlightWidgetReceiver.getParamsWidget().setFlashMode(
						Parameters.FLASH_MODE_OFF);
				FlashlightWidgetReceiver.getmCameraWidget().setParameters(
						FlashlightWidgetReceiver.getParamsWidget());

				try {
					FlashlightWidgetReceiver.getmCameraWidget().startPreview();
				} catch (Exception e) {
					e.printStackTrace();

				}

				AppGlobals.setIsFlashOn(true);
			}

			String flashMode = FlashlightWidgetReceiver.getParamsWidget()
					.getFlashMode();

			if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

				if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
					FlashlightWidgetReceiver.getParamsWidget().setFlashMode(
							Parameters.FLASH_MODE_TORCH);
					FlashlightWidgetReceiver.getmCameraWidget().setParameters(
							FlashlightWidgetReceiver.getParamsWidget());
				} else {
					FlashlightWidgetReceiver.getParamsWidget().setFlashMode(
							Parameters.FLASH_MODE_ON);

					FlashlightWidgetReceiver.getmCameraWidget().setParameters(
							FlashlightWidgetReceiver.getParamsWidget());
					try {
						FlashlightWidgetReceiver.getmCameraWidget().autoFocus(
								new AutoFocusCallback() {
									public void onAutoFocus(boolean success,
											Camera camera) {
										count = 1;
									}
								});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				AppGlobals.setIsFlashOn(true);

			}
		}
	}

	static void turnMotorolaOn() {
		DroidLED led;
		try {
			led = new DroidLED();
			led.enable(true);
			AppGlobals.setIsFlashOn(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Any time the widget or the button in the app is pressed to turn the LED
	// off we process this off click.
	private void processOffClick() {

		flashlight_button.setBackgroundResource(R.drawable.light_on);
		setWidgetTo(R.drawable.light_on);

		if (getmCameraActivity() != null) {
			count = 0;
			flashOffApp();
		}

		if (FlashlightWidgetReceiver.getmCameraWidget() != null) {
			count = 0;
			flashOffWidget();
		}

		turnMotorolaOff();

	}

	// Turns the LED off when the button on the app is pressed.
	private void flashOffApp() {
		getmCameraActivity().stopPreview();
		getmCameraActivity().release();
		setmCameraActivity(null);
		AppGlobals.setIsFlashOn(false);
	}

	// Turns the LED off when the widget is pressed.
	private void flashOffWidget() {
		FlashlightWidgetReceiver.getmCameraWidget().stopPreview();
		FlashlightWidgetReceiver.getmCameraWidget().release();
		FlashlightWidgetReceiver.setmCameraWidget(null);
		AppGlobals.setIsFlashOn(false);

	}

	// Turns the LED off for some Motorola phones.
	static void turnMotorolaOff() {
		DroidLED led;
		try {
			led = new DroidLED();
			led.enable(false);
			AppGlobals.setIsFlashOn(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// setWidgetTo changes the widget view to either off or on.
	private void setWidgetTo(int drawable) {
		Context context = this;
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		ComponentName thisWidget = new ComponentName(context,
				FlashlightWidgetProvider.class);
		remoteViews.setImageViewResource(R.id.flashlight_widget_imageview,
				drawable);
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
	}

	// The following three methods are needed to implement SurfaceView.Callback.
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		mHolder.addCallback(this);

		if (getmCameraActivity() != null) {

			try {
				getmCameraActivity().setPreviewDisplay(mHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mHolder = holder;
		mHolder.addCallback(this);

		if (getmCameraActivity() != null) {
			try {
				getmCameraActivity().setPreviewDisplay(mHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHolder = null;
	}

	// Getters and setters for mCameraActivity.
	public static Camera getmCameraActivity() {
		return mCameraActivity;
	}

	public static void setmCameraActivity(Camera mCameraActivity) {
		Flashlight.mCameraActivity = mCameraActivity;
	}

	// Getters and setters for params.
	public Parameters getParams() {
		return params;
	}

	public void setParams(Parameters params) {
		this.params = params;
	}

}