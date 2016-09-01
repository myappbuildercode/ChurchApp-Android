package org.apache.cordova.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class Echo_Capture extends CordovaPlugin {

	public static CallbackContext callbackContext1;
	public Context context;

	private static final int PERMISSION_REQUEST_CODE = 1;
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		Log.d("result", "result");
		context = cordova.getActivity().getApplicationContext();
		callbackContext1 = callbackContext;
		Constant.videoCapture = null;
		Start("");
		PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
		result.setKeepCallback(true);
		callbackContext1.sendPluginResult(result);
		return true;
	}

	private void Start(String string) {
		Intent intent = null;
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (checkPermission_storage()) {
			intent = new Intent(context, Gallery_Activity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
		}
		else
		{
			requestPermission_storage();
		}




	}


	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d("result", "result");
		if (requestCode == 0) {
			try {
				PluginResult result = null;
				Log.d("result", "result");
				if (resultCode == android.app.Activity.RESULT_OK) {
					Toast.makeText(context, "calledded", Toast.LENGTH_LONG).show();
					JSONObject jsonObj = new JSONObject();
					if(Constant.febCapture != null)
						jsonObj.put("frame", Constant.febCapture);
					else
						jsonObj.put("frame", "null");
					if(Constant.videoCapture != null)
						jsonObj.put("video", Constant.videoCapture);
					else
						jsonObj.put("video", "null");
					callbackContext1.success(jsonObj);
					result = new PluginResult(PluginResult.Status.OK, jsonObj);
				} else {
					if (intent != null) {
						if (intent.hasExtra("result")) {
							JSONObject jsonObj = new JSONObject();
							jsonObj.put("frame", "null");
							if(Constant.videoCapture != null)
								jsonObj.put("video", Constant.videoCapture);
							else
								jsonObj.put("video", "null");
							Toast.makeText(context, "failure",
									Toast.LENGTH_LONG).show();
							callbackContext1.error(jsonObj);
							result = new PluginResult(
									PluginResult.Status.ERROR, jsonObj);
						}
					}

				}
				if (result != null) {
					result.setKeepCallback(false);
					if (callbackContext1 != null) {
						callbackContext1.sendPluginResult(result);
						callbackContext1 = null;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}*/

	private boolean checkPermission_storage(){
		int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (result == PackageManager.PERMISSION_GRANTED ){

			return true;

		} else {

			return false;

		}
	}

	private void requestPermission_storage(){

		ActivityCompat.requestPermissions(cordova.getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

	}


	@Override
	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		Intent intent = null;
		switch (requestCode) {
			case PERMISSION_REQUEST_CODE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


				} else {
					Toast.makeText(context,"Permission Denied, You cannot access Video.",Toast.LENGTH_LONG).show();

				}
				//this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
		}
	}
}
