package com.jbutewicz.flashlightbyjoe;

import java.util.List;
import java.util.Locale;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class Flashlight extends Activity {
	private boolean isFlashOn = false;
	int count = 0;
	String manuName = android.os.Build.MANUFACTURER.toLowerCase(Locale
			.getDefault());
	Camera mCamera;
	Button flashlight_button;
	Parameters params;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flashlight);

		flashlight_button = (Button) findViewById(R.id.flashlight_button);
		flashlight_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isFlashOn) {
					processOffClick();
					isFlashOn = false;
				} else {
					processOnClick();
					isFlashOn = true;
				}

			}
		});

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mCamera != null) {
			mCamera.release();
		}
		finish();
	}

	private void processOnClick() {

		flashlight_button.setText("Off");

		if (manuName.contains("motorola")) {
			DroidLED led;
			try {
				led = new DroidLED();
				led.enable(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (mCamera == null) {
				try {
					mCamera = Camera.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (mCamera != null) {

				params = mCamera.getParameters();

				List<String> flashModes = params.getSupportedFlashModes();

				if (flashModes == null) {
					return;
				} else {
					if (count == 0) {
						params.setFlashMode(Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(params);
						mCamera.startPreview();
					}

					String flashMode = params.getFlashMode();

					if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

						if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
							params.setFlashMode(Parameters.FLASH_MODE_TORCH);
							mCamera.setParameters(params);
						} else {
							params.setFlashMode(Parameters.FLASH_MODE_ON);

							mCamera.setParameters(params);
							try {
								mCamera.autoFocus(new AutoFocusCallback() {
									public void onAutoFocus(boolean success,
											Camera camera) {
										count = 1;
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		if (mCamera == null) {
			return;
		}
	}

	private void processOffClick() {

		flashlight_button.setText("On");

		if (manuName.contains("motorola")) {
			DroidLED led;
			try {
				led = new DroidLED();
				led.enable(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (mCamera != null) {
				count = 0;

				params = mCamera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(params);
				mCamera.stopPreview();
			}
		}
	}

}