package com.jbutewicz.flashlightbyjoe;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.widget.RemoteViews;

public class FlashlightWidgetReceiver extends BroadcastReceiver {

	// This class turns the LED on and off for phones when the widget is
	// pressed.

	static Camera mCameraWidget;
	static Parameters paramsWidget;
	int count = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);

		if (AppGlobals.getIsFlashOn()) {
			views.setImageViewResource(R.id.flashlight_widget_imageview,
					R.drawable.light_on);
		} else {
			views.setImageViewResource(R.id.flashlight_widget_imageview,
					R.drawable.light_off);
		}

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		appWidgetManager.updateAppWidget(new ComponentName(context,
				FlashlightWidgetProvider.class), views);

		if (AppGlobals.getIsFlashOn()) {
			if (getmCameraWidget() != null) {
				flashOffWidget();

			}

			if (Flashlight.getmCameraActivity() != null) {

				flashOffApp();
				Flashlight.flashlight_button
						.setBackgroundResource(R.drawable.light_on);

			}

			Flashlight.turnMotorolaOff();

		} else {
			try {
				setmCameraWidget(Camera.open());
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (getmCameraWidget() == null) {
			} else {
				setParamsWidget(getmCameraWidget().getParameters());

				List<String> flashModes = getParamsWidget()
						.getSupportedFlashModes();

				if (flashModes == null) {
					return;
				} else {
					if (count == 0) {
						getParamsWidget().setFlashMode(
								Parameters.FLASH_MODE_OFF);
						getmCameraWidget().setParameters(getParamsWidget());
						getmCameraWidget().startPreview();
						AppGlobals.setIsFlashOn(true);
					}

					String flashMode = getParamsWidget().getFlashMode();

					if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

						if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
							getParamsWidget().setFlashMode(
									Parameters.FLASH_MODE_TORCH);
							getmCameraWidget().setParameters(getParamsWidget());
						} else {
							getParamsWidget().setFlashMode(
									Parameters.FLASH_MODE_ON);

							getmCameraWidget().setParameters(getParamsWidget());
							try {
								getmCameraWidget().autoFocus(
										new AutoFocusCallback() {
											public void onAutoFocus(
													boolean success,
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

			Flashlight.turnMotorolaOn();
		}

	}

	// Turns the LED off when the button on the app is pressed.
	private void flashOffApp() {
		Flashlight.getmCameraActivity().stopPreview();
		Flashlight.getmCameraActivity().release();
		Flashlight.setmCameraActivity(null);
		AppGlobals.setIsFlashOn(true);
		count = 0;

	}

	// Turns the LED off when the widget is pressed.
	private void flashOffWidget() {
		FlashlightWidgetReceiver.getmCameraWidget().stopPreview();
		FlashlightWidgetReceiver.getmCameraWidget().release();
		FlashlightWidgetReceiver.setmCameraWidget(null);
		AppGlobals.setIsFlashOn(false);
		count = 0;

	}

	// Getters and setters for mCameraWidget.
	public static Camera getmCameraWidget() {
		return mCameraWidget;
	}

	public static void setmCameraWidget(Camera mCameraWidget) {
		FlashlightWidgetReceiver.mCameraWidget = mCameraWidget;
	}

	// Getters and setters for paramsWidget.
	public static Parameters getParamsWidget() {
		return paramsWidget;
	}

	public static void setParamsWidget(Parameters paramsWidgetSet) {
		paramsWidget = paramsWidgetSet;
	}

}