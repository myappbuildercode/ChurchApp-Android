package com.netcompss.ffmpeg4android_client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.churchapp.church.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netcompss.ffmpeg4android.LicenseCheckJNI;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.plugin.ConnectionDetector;
import org.apache.cordova.plugin.Constant;
import org.apache.cordova.plugin.ScalingUtilities;
import org.apache.cordova.plugin.ScalingUtilities.ScalingLogic;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class BaseVideo extends CordovaPlugin {

	protected ProgressDialog progressDialog;
	public static Boolean isInternetPresent = false;
	public static ConnectionDetector cd;
	String Tag = "Video_Compress", sgn = null;
	public static String jsondata;
	String post_imgpth = null;
	public static CallbackContext callbackContext;
	public Context context;
	public static Context contexts;
	public int wid = 80, hig = 80;

	// private ProgressDialog pd = null;

	public boolean execute(String actiosaveImagen, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		context = this.cordova.getActivity().getApplicationContext();
		contexts = this.cordova.getActivity().getApplicationContext();
		_prefs = new Prefs();
		_prefs.setContext(this.cordova.getActivity().getApplicationContext());
		jsondata = args.toString();
		Constant.json_array = jsondata;
		Log.e("json", args.toString() + " ddddd " + args.length());
		cd = new ConnectionDetector(context);
		isInternetPresent = cd.isConnectingToInternet();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		// pd = new
		// ProgressDialog(this.cordova.getActivity().getApplicationContext());
		// pd.setCancelable(true);
		// pd.setMessage("Please Wait...");
		if (isInternetPresent) {
			if (args != null && args.length() > 0) {
				JSONArray arry = new JSONArray(jsondata);
				JSONObject obj = arry.getJSONObject(2);
				sgn = obj.getString("video");

				if (obj.has("video_thumbnail")) {
					String image = obj.getString("video_thumbnail");
					if (image != null) {
						Log.d("image", image);
						if (image.contains("content") || image.contains("file")) {
							Log.d("content", "content");
							String getString = getFileNameByUri(context,
									Uri.parse(image));
							String thumb = reporteds(getString);
							video(thumb, true);
						} else {
							Log.d("image", "image");
							String thumb = reporteds(image);
							video(thumb, true);
						}
					} else {
						Log.d("one", "videopath1");
						Log.d("two-2", video_thumbpath.getAbsolutePath());
						String thumb = reporteds(video_thumbpath
								.getAbsolutePath());
						if (thumb != null) {
							Log.d("thumb", thumb);
							video(thumb, true);
							Log.d("image path32",
									video_thumbpath.getAbsolutePath());
						}
					}
				} else {
					Log.d("one", "videopath2");
					Log.d("two-2", video_thumbpath.getAbsolutePath());
					String thumb = reporteds(video_thumbpath.getAbsolutePath());
					if (thumb != null) {
						Log.d("thumb", thumb);
						video(thumb, true);
						Log.d("image path32", video_thumbpath.getAbsolutePath());
					}
				}

			} else {
				PluginResult progressResult = new PluginResult(
						PluginResult.Status.ERROR,
						"parameter required or incorrect");
				progressResult.setKeepCallback(true);
				callbackContext.sendPluginResult(progressResult);
			}

		} else {
			toastsettext("No Internet Connection");
		}

		return true;// super.execute(action, args, callbackContext);
	}

	String strMyImagePath = null;
	File f = null;
	private int srcBgId = R.drawable.bg;
	static int w = 80;
	static int h = 80;

	@SuppressWarnings("unused")
	private String reporteds(String path) {

		ExifInterface exif = null;
		try {
			exif = new ExifInterface(path);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);
		Matrix matrix = new Matrix();
		if (orientation == 6) {
			matrix.postRotate(90);
		} else if (orientation == 3) {
			matrix.postRotate(180);
		} else if (orientation == 8) {
			matrix.postRotate(270);
		}

		if (path != null) {

			if (path.contains("http")) {
				try {
					URL url = new URL(path);
					HttpGet httpRequest = null;

					httpRequest = new HttpGet(url.toURI());

					HttpClient httpclient = new DefaultHttpClient();
					HttpResponse response = (HttpResponse) httpclient
							.execute(httpRequest);

					HttpEntity entity = response.getEntity();
					BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(
							entity);
					InputStream input = bufHttpEntity.getContent();

					Bitmap bitmap = BitmapFactory.decodeStream(input);
					input.close();
					return getPath(bitmap);
				} catch (MalformedURLException e) {
					Log.e("ImageActivity", "bad url", e);
				} catch (Exception e) {
					Log.e("ImageActivity", "io error", e);
				}
			} else {

				Options options = new Options();
				options.inSampleSize = 2;
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(context.getResources(), srcBgId,
						options);
				options.inJustDecodeBounds = false;
				options.inSampleSize = calculateInSampleSize(options, w, h);
				Bitmap unbgbtmp = BitmapFactory.decodeResource(
						context.getResources(), srcBgId, options);
				Bitmap unrlbtmp = ScalingUtilities.decodeFile(path, w, h,
						ScalingLogic.FIT);
				unrlbtmp.recycle();
				Bitmap rlbtmp = null;
				if (unrlbtmp != null) {
					rlbtmp = ScalingUtilities.createScaledBitmap(unrlbtmp, w,
							h, ScalingLogic.FIT);
				}
				if (unbgbtmp != null && rlbtmp != null) {
					Bitmap bgbtmp = ScalingUtilities.createScaledBitmap(
							unbgbtmp, w, h, ScalingLogic.FIT);
					Bitmap newscaledBitmap = ProcessingBitmapTwo(bgbtmp, rlbtmp);
					unbgbtmp.recycle();
					return getPath(newscaledBitmap);
				}
			}
		}
		return path;

	}

	private void video(String thumb, boolean boo_img) {

		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("api_key", "d4b2e8f5473bd5023797436ce9556620");
		params.put("id", "2225");
		// pd.show();
		try {
			params.put("image", new File(thumb));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		;
		client.addHeader("Accept", "appliction/json");
		if (boo_img) {
			client.post(
					"http://build.myappbuilder.com/api/elements/images.json?",
					params, response_Image);
		} else {
			client.post(
					"http://build.myappbuilder.com/api/elements/images.json?",
					params, response_Frame);
		}

	}

	public AsyncHttpResponseHandler response_Frame = new AsyncHttpResponseHandler() {
		@Override
		public void onFinish() {
			super.onFinish();
			Log.d("response_Frame", " response_image finish");
			video_posted();/*
							 * if(pd.isShowing()){ pd.cancel(); pd.dismiss(); }
							 */
		}

		@Override
		public void onSuccess(String arg0) {
			super.onSuccess(arg0);
			if (arg0 != null) {
				Log.e("response_image", arg0);
				try {
					JSONObject obj = new JSONObject(arg0);
					Constant.frm_url = obj.getString("url");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			if (arg1 != null) {
				Log.d("response_Frame", arg1);
			}
		};

	};

	public AsyncHttpResponseHandler response_Image = new AsyncHttpResponseHandler() {
		@Override
		public void onFinish() {
			super.onFinish();
			video_posted();
			Log.d("response_image", " response_image finish");

		}

		@Override
		public void onSuccess(String arg0) {
			super.onSuccess(arg0);
			if (arg0 != null) {
				Log.e("response_image", arg0);
				try {
					JSONObject obj = new JSONObject(arg0);
					Constant.img_url = obj.getString("url");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			if (arg1 != null) {
				Log.d("response_image", arg1);
			}
		};

	};

	private String getPath(Bitmap bitmap) {
		FileOutputStream fos = null;
		String extr = Environment.getExternalStorageDirectory().toString();
		File mFolder = new File(extr + "/CHURCH");
		if (!mFolder.exists()) {
			mFolder.mkdir();
		} else {
			mFolder.delete();
			mFolder.mkdir();
		}

		String s = "churchthumb.png";

		f = new File(mFolder.getAbsolutePath(), s);

		strMyImagePath = f.getAbsolutePath();
		Log.d("f", strMyImagePath);
		try {
			fos = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return strMyImagePath;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public void uploadtoserver() {
		try {
			JSONArray arry = new JSONArray(jsondata);
			RequestParams params = new RequestParams();
			JSONObject obj = arry.getJSONObject(2);

			if (obj.has("button_id")) {
				Log.d("buttonid", String.valueOf(obj.getInt("button_id")));
				params.put("button_id", String.valueOf(obj.getInt("button_id")));
			}

			if (obj.has("id")) {
				Log.d("id", obj.getString("id"));
				params.put("id", obj.getString("id"));
			}

			if (obj.has("title")) {
				Log.d("title", obj.getString("title"));
				params.put("title", obj.getString("title").toString());
			}

			if (obj.has("text")) {
				Log.d("text", obj.getString("text"));
				String txt = obj.getString("text").toString()
						.replace("<p>", "").replace("</p>", "");
				params.put("text", txt);
				// Toast.makeText(context, txt, Toast.LENGTH_LONG).show();
			}
			// Html.fromHtml(obj.getString("description")).toString());
			if (obj.has("description")) {
				Log.d("description", obj.getString("description"));
				params.put("description", obj.getString("description")
						.toString());
			}

			if (obj.has("api_key")) {
				Log.d("api_key", obj.getString("api_key"));
				params.put("api_key", obj.getString("api_key"));
			}

			if (obj.has("video")) {
				Log.d("video", "/sdcard/videokit/final.mp4");
				params.put("video", new File("/sdcard/videokit/final.mp4"));
			}

			if (Constant.img_url != null) {
				Log.d("video_thumbnail_url", Constant.img_url);
				params.put("video_thumbnail_url", Constant.img_url);
			} else {
				// video_thumbnail
				if (obj.has("video_thumbnail")) {
					String image = obj.getString("video_thumbnail");
					if (image != null) {
						Log.d("video_thumbnail", image);
						if (image.contains("content") || image.contains("file")) {
							String getString = getFileNameByUri(context,
									Uri.parse(image));
							String thumb = reporteds(getString);
							params.put("video_thumbnail", new File(thumb));
						} else {
							String thumb = reporteds(image);
							params.put("video_thumbnail", new File(thumb));
						}
					} else {
						String thumb = reporteds(video_thumbpath
								.getAbsolutePath());
						params.put("video_thumbnail", new File(thumb));
						Log.d("image path32", video_thumbpath.getAbsolutePath());
					}
				} else {
					String thumb = reporteds(video_thumbpath.getAbsolutePath());
					params.put("video_thumbnail", new File(thumb));
					Log.d("image path33", video_thumbpath.getAbsolutePath());
				}
			}

			if (obj.has("video_frame")) {
				String images = obj.getString("video_frame");
				if (images != null) {
					Log.d("video_frame_url", images);
					if (images.contentEquals("null")) {

					} else if (images.contains("http")
							|| images.contains("https")) {
						params.put("video_frame_url", images);

					} else if (Constant.frm_url != null) {
						params.put("video_frame_url", Constant.frm_url);
					}
				}
			}

			posted(arry, params);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void posted(JSONArray arry, RequestParams params)
			throws JSONException {
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(200000);
		client.addHeader("Accept", "appliction/json");
		Log.d("api", arry.getString(0));
		Log.d("method", arry.getString(1));
		if (arry.getString(1).equalsIgnoreCase("post")) {
			client.post(arry.getString(0).replace("?", ""), params,
					responseHandler);
		} else if (arry.getString(1).equalsIgnoreCase("put")) {
			client.put(arry.getString(0).replace("?", ""), params,
					responseHandler);
		}

	}

	public AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
		@Override
		public void onFinish() {
			super.onFinish();
			Log.d("arg", "finish");
		}

		@Override
		public void onSuccess(String arg0) {
			// TODO Auto-generated method stub
			super.onSuccess(arg0);
			Log.e("mesage", arg0);

			deleted();

			PluginResult progressResult = new PluginResult(
					PluginResult.Status.OK, arg0);
			progressResult.setKeepCallback(true);
			BaseVideo.this.callbackContext.sendPluginResult(progressResult);
			// }
		}

		private void deleted() {
			String extr = Environment.getExternalStorageDirectory().toString();
			File mFolder = new File(extr + "/MYAPPBUILDER2");
			if (mFolder.exists()) {
				File[] files = mFolder.listFiles();
				if (files == null) {

				}
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {

					} else {
						files[i].delete();
					}
				}
				if (files.length == 0) {
					mFolder.delete();
				}
			}
			File mFolders = new File(extr + "/MYAPPBUILDER1");
			if (mFolders.exists()) {
				File[] files = mFolders.listFiles();
				if (files == null) {

				}
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {

					} else {
						files[i].delete();
					}
				}
				if (files.length == 0) {
					mFolders.delete();
				}
			}

			File mFolders1 = new File(extr + "/MYAPPBUILDER");
			if (mFolders1.exists()) {
				File[] files = mFolders1.listFiles();
				if (files == null) {

				}
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {

					} else {
						files[i].delete();
					}
				}
				if (files.length == 0) {
					mFolders1.delete();
				}
			}

		}

		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			if (arg1 != null) {
				Log.d("aa", arg1);
				PluginResult progressResult = new PluginResult(
						PluginResult.Status.ERROR, arg1);
				progressResult.setKeepCallback(true);
				BaseVideo.this.callbackContext.sendPluginResult(progressResult);
			}
		};

	};

	File video_thumbpath = null;

	private String[] getVideoPath(String name) {
		// TODO Auto-generated method stub
		Log.e(Tag, "file name:" + name);
		Uri mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] cloumns = { MediaStore.Video.VideoColumns.DATA,
				MediaStore.Video.VideoColumns._ID,
				MediaStore.Video.VideoColumns.TITLE,
				MediaStore.Video.Thumbnails.DATA };
		String selection = MediaStore.Video.Media.DATA + " Like '%" + name
				+ "'";
		Cursor videocursor = BaseVideo.this.cordova.getActivity().managedQuery(
				mUri, cloumns, selection, null, null);
		if (videocursor.getCount() > 0) {
			videocursor.moveToFirst();
			while (!videocursor.isAfterLast()) {
				String path = videocursor.getString(0);
				String thumbpath = videocursor.getString(3);
				Log.e(Tag, "path:" + path + "  thumb:" + thumbpath);
				String[] temp = path.split("/");
				if (temp[temp.length - 1].equalsIgnoreCase(name)) {
					Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
							MediaStore.Images.Thumbnails.MINI_KIND);
					FileOutputStream fos = null;
					String extr = Environment.getExternalStorageDirectory()
							.toString();
					File mFolder = new File(extr + "/MYAPPBUILDER2");
					if (!mFolder.exists()) {
						mFolder.mkdir();
					} else {
						mFolder.delete();
						mFolder.mkdir();
					}

					String s = "myappbuilder.png";

					video_thumbpath = new File(mFolder.getAbsolutePath(), s);
					// returnPath = video_thumbpath.getAbsolutePath();

					try {
						fos = new FileOutputStream(video_thumbpath);
						thumb.compress(Bitmap.CompressFormat.PNG, 100, fos);
						fos.flush();
						fos.close();
					} catch (FileNotFoundException e) {

						e.printStackTrace();
					} catch (Exception e) {

						e.printStackTrace();
					}
					return new String[] { "success", path };
				}
				videocursor.moveToNext();
			}

		} else {
			return new String[] { "error", "Video not Founded..." };
		}
		return new String[] { "error", "Video not Founded..." };
	}

	private Bitmap ProcessingBitmapTwo(Bitmap bm1, Bitmap bm2) {
		Bitmap newBitmap = null;

		int w;
		if (bm1.getWidth() >= bm2.getWidth()) {
			w = bm1.getWidth();
		} else {
			w = bm2.getWidth();
		}

		int h;
		if (bm1.getHeight() >= bm2.getHeight()) {
			h = bm1.getHeight();
		} else {
			h = bm2.getHeight();
		}

		Config config = bm1.getConfig();
		if (config == null) {
			config = Bitmap.Config.ARGB_4444;
		}

		newBitmap = Bitmap.createBitmap(w, h, config);
		Canvas newCanvas = new Canvas(newBitmap);
		newCanvas.drawColor(Color.WHITE);
		if (bm2.getWidth() == bm2.getHeight()) {
			newCanvas.drawBitmap(bm2, (w - bm2.getWidth()) / 2,
					(h - bm2.getHeight()) / 2, null);
		} else {
			newCanvas.drawBitmap(bm2, (w / 2) - (bm2.getWidth() / 2), (h / 2)
					- (bm2.getHeight() / 2), null);
		}

		return newBitmap;
	}

	public String getFileNameByUri(Context context, Uri uri) {
		String fileName = "unknown";// default fileName
		Uri filePathUri = uri;
		if (uri.getScheme().toString().compareTo("content") == 0) {
			ParcelFileDescriptor parcelFileDescriptor;
			String filename = null;
			try {
				FileOutputStream fos = null;
				parcelFileDescriptor = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor
						.getFileDescriptor();
				Bitmap image = BitmapFactory
						.decodeFileDescriptor(fileDescriptor);
				String extr = Environment.getExternalStorageDirectory()
						.toString();
				File mFolder = new File(extr + "/CHURCH");
				if (!mFolder.exists()) {
					mFolder.mkdir();
				} else {
					mFolder.delete();
					mFolder.mkdir();
				}

				String s = "rough.png";

				File f = new File(mFolder.getAbsolutePath(), s);

				filename = f.getAbsolutePath();
				Log.d("f", filename);
				try {
					fos = new FileOutputStream(f);
					image.compress(Bitmap.CompressFormat.PNG, 100, fos);
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}
				parcelFileDescriptor.close();
				return filename;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (uri.getScheme().compareTo("file") == 0) {
			fileName = filePathUri.getPath();
			File fill = new File(fileName);
			if (fill.exists()) {
				Log.d("exitst", "exist");
				ParcelFileDescriptor parcelFileDescriptor;
				String filename = null;
				FileOutputStream fos = null;
				Bitmap image = BitmapFactory.decodeFile(fileName);
				String extr = Environment.getExternalStorageDirectory()
						.toString();
				File mFolder = new File(extr + "/CHURCH");
				if (!mFolder.exists()) {
					mFolder.mkdir();
				} else {
					mFolder.delete();
					mFolder.mkdir();
				}

				String s = "rough.png";

				File f = new File(mFolder.getAbsolutePath(), s);

				filename = f.getAbsolutePath();
				Log.d("f", filename);
				try {
					fos = new FileOutputStream(f);
					image.compress(Bitmap.CompressFormat.PNG, 100, fos);
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}
			} else {
				Log.d("not file exitst", "not exist");
			}
			Log.d("file", "file");
		} else {
			fileName = filePathUri.getPath();
			Log.d("else", "else");
		}

		return fileName;

	}

	protected Prefs _prefs = null;

	private void video_posted() {

		LicenseCheckJNI lic = new LicenseCheckJNI();
		int licenseCheckRC = lic.licenseCheck(
				Prefs.getWorkingFolderForNative(), context);
		String licenseText = "";
		if (licenseCheckRC == 4) {
			licenseText = " Personal";
		} else if (licenseCheckRC == 1) {
			licenseText = " OEM";

		} else if (licenseCheckRC == 0 || licenseCheckRC == 2) {
			licenseText = " Trial";
		} else if (licenseCheckRC == -1) {
			licenseText = " Trial Expired";
		} else if (licenseCheckRC == -3) {
			licenseText = " Trial (not validated) ";
			Log.w(Prefs.TAG, "License file not created, possible first time");
		} else {
			licenseText = "Not Valid";
		}

		Log.d("license", licenseText);
		File files = new File(sgn);
		// JSONObject obj = args.getJSONObject(2).getJSONObject("video");
		String[] temp = null;// args.getString(0).split("/");
		temp = getVideoPath(files.getName());
		if (temp[0].equalsIgnoreCase("success")) {
			File file = new File(temp[1]);
			String hrSize = "";
			long size = file.length();
			double m = size / 1048576;
			if (m < 75) {

				File in = new File(Environment.getExternalStorageDirectory()+"/videokit");

				if(!in.exists()) {
					if(in.mkdir()); //directory is created;
					//Toast.makeText(context,"directory is created",Toast.LENGTH_LONG).show();
				}
				else
				{
				//	Toast.makeText(context,"else",Toast.LENGTH_LONG).show();
				}

				Intent intent = new Intent(context, NewAct.class);
				Log.d(Prefs.TAG, "Starting demo act");
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
					Constant.command = temp[1];
					//Toast.makeText(context,"temp file"+temp[1],Toast.LENGTH_LONG).show();
					Log.d("temp_file",temp[1]);
					Constant.commandStr = "ffmpeg -y -i "+temp[1]+" -strict experimental -s 160x120 -r 25 -aspect 4:3 -vcodec mpeg4 -b 97152 -ab 48000 -ac 2 -ar 22050 /sdcard0/videokit/final.mp4";
				} else {
					Constant.command = temp[1];
					Constant.commandStr = "ffmpeg -y -i "
							+ temp[1]
							+ " -strict experimental -s 160x120 -r 25 -aspect 4:3 -vcodec mpeg4 -b 97152 -ab 48000 -ac 2 -ar 22050 /sdcard/videokit/final.mp4";
				}
				Log.d("command", Constant.commandStr);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} else {
				JSONObject object = new JSONObject();
				try {
					object.put("statues", "Error");
					object.put("message",
							"file size should not greater than 75MB....");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				PluginResult progressResult = new PluginResult(
						PluginResult.Status.ERROR, object);
				progressResult.setKeepCallback(true);
				callbackContext.sendPluginResult(progressResult);
			}
		} else {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.ERROR, temp[0]);
			progressResult.setKeepCallback(true);
			callbackContext.sendPluginResult(progressResult);
		}
	}

	public void toastsettext(String string1) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_activity,
				(ViewGroup) ((Activity) context).findViewById(R.id.toast_rl));
		TextView txt = (TextView) layout.findViewById(R.id.toast_txt);
		txt.setText(string1);
		Toast tst = new Toast(context);
		tst.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		tst.setDuration(Toast.LENGTH_SHORT);
		tst.setView(layout);
		tst.show();
	}

}
