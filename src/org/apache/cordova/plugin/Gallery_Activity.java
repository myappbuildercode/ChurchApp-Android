package org.apache.cordova.plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import com.churchapp.church.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//import android.widget.Toast;

public class Gallery_Activity extends Activity implements OnClickListener {

	private GridView gridGallery;
	private Context context;
	private Handler handler;
	private GLSurfaceView audio_surfaceview;
	private ImageView img_edt;
	private ViewFlipper vf_main;
	private Button btn_galbck, btn_vidback, btn_donebtn;
	private ImageLoader imageLoader;
	private RelativeLayout rl_capture;
	private NativeGalleryAdapter adapter;
	private ProgressDialog pd = null;
	private Boolean ispause = false;
	private String[] vid_tits = { ".afl", ".AFL", ".mp4", ".MP4", ".avi",
			".AVI", ".3gp", ".3GP", ".mpeg", ".MPEG", ".flv", ".FLV", ".mkv",
			".MKV", ".mov", ".MOV", ".wmv", ".WMV", ".asf", ".ASF", ".moov",
			".mov", ".movie", ".mpe", ".MOOV", ".MOV", ".MOVIE", ".MPE", ".mv",
			".qt", ".qtc", ".gl", ".MV", ".QT", ".QTC", ".GL", ".isu", ".rv",
			".vdo", ".viv", ".ISU", ".RV", ".VDO", ".VIV", ".vivo", ".vos",
			"xsr", ".xdr", ".VIVO", ".VOS", ".XSR", ".XDR", ".scm", ".m1v",
			".m2v", ".mjpg", ".SCM", ".M1V", ".M2V", ".MJPG", ".fli", ".fmf",
			".asf", ".asx", ".FLI", ".FMF", ".ASF", ".ASX", ".avs", ".afl",
			".dl", ".dif", "AVS", ".AFL", ".DL", ".DIF", ".dv", ".DV" };

	public static InputStream inputStreamVideo = null;
	private VideoView mVideoView = null;
	private ImageView iv_set;
	private MediaMetadataRetriever mediaMetadataRetriever;
	static MediaPlayer mp = null;
	private MediaController mediaController;
	private DisplayMetrics dm;
	private int pos = 0;
	private String vid = null;

	public static Boolean isInternetPresent = false;
	public static ConnectionDetector cd;

	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;

	// String getVideo =
	// "http://s3.amazonaws.com/iPhoneBooks/uploaded_videos/14529/one2ffs.mp4?1419318083";

	@Override
	public void onBackPressed() {
		if (vf_main.getDisplayedChild() == 0) {
			failed();
		} else if (vf_main.getDisplayedChild() == 1) {
			video();
		}
	}

	private void failed() {
		try {
			PluginResult resultS = null;
			Log.d("result", "result123");
			//Toast.makeText(context, "called", Toast.LENGTH_LONG).show();
			JSONObject jsonObj = new JSONObject();
			if(Constant.videoCapture != null)
				jsonObj.put("video", Constant.videoCapture);
			else
				jsonObj.put("video", "null");
			jsonObj.put("frame", "null");
			Echo_Capture.callbackContext1.success(jsonObj);
			resultS = new PluginResult(PluginResult.Status.ERROR, jsonObj);
			resultS.setKeepCallback(false);
			if (Echo_Capture.callbackContext1 != null) {
				Echo_Capture.callbackContext1.sendPluginResult(resultS);
				Echo_Capture.callbackContext1 = null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			finish();
		}
		finish();
	}

	private void video() {

		final Button cancel_btn, logout_btn;
		AlertDialog.Builder build_dialog;
		final AlertDialog alert_dialog;

		build_dialog = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.altdialog_videoexit,
				(ViewGroup) findViewById(R.id.videoexit_sample));
		cancel_btn = (Button) layout.findViewById(R.id.vdoext_btn_cancel);
		logout_btn = (Button) layout.findViewById(R.id.vdoext_btn_ok);
		build_dialog.setView(layout);
		alert_dialog = build_dialog.create();
		alert_dialog.setView(layout, 0, 0, 0, 0);

		cancel_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert_dialog.dismiss();

			}
		});

		logout_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert_dialog.dismiss();

				if (mp != null) {
					mp.pause();
					ispause = true;
					mVideoView.stopPlayback();
					mVideoView.clearAnimation();
					mVideoView.clearFocus();
				}
				vf_main.setDisplayedChild(0);
			}
		});

		alert_dialog.show();

	}

	MediaPlayer.OnPreparedListener PreparedListener = new MediaPlayer.OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer m) {
			try {
				mp = m;
				if (mp.isPlaying()) {
					mp.stop();
					mp.release();
					mp = new MediaPlayer();
				}
				mVideoView.seekTo(pos);
				mp.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		pos = mVideoView.getCurrentPosition();
		outState.putInt("pos", mVideoView.getCurrentPosition());

	};

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		pos = savedInstanceState.getInt("pos");
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setFullScreen(Gallery_Activity.this);

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			setFullScreen1(Gallery_Activity.this);

		}
	}

	protected void setFullScreen(Context currContext) {
		((Activity) currContext).getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		((Activity) currContext).getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	}

	protected void setFullScreen1(Context currContext) {
		((Activity) currContext).getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		((Activity) currContext).getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	SharedPreferences settings = null;

	@Override
	protected void onResume() {
		super.onResume();

		if (mVideoView != null) {
			int sdkVersion = Build.VERSION.SDK_INT;
			pos = mVideoView.getCurrentPosition();
			if (sdkVersion > Build.VERSION_CODES.HONEYCOMB) {
				if (mp != null) {
					if (ispause) {
						mVideoView.seekTo(pos);
						mVideoView.start();
						ispause = false;
					}
				}
			} else {
				if (mp != null) {
					if (ispause) {
						mVideoView.seekTo(pos);
						mVideoView.start();
						ispause = false;
					}
				} else {

				}
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVideoView != null) {
			int sdkVersion = Build.VERSION.SDK_INT;
			pos = mVideoView.getCurrentPosition();
			if (sdkVersion > Build.VERSION_CODES.HONEYCOMB) {
				if (mp != null) {
					if (ispause) {
						mVideoView.seekTo(pos);
						mVideoView.pause();
						ispause = false;
					}
				}
			} else {
				if (mp != null) {
					if (ispause) {
						mVideoView.seekTo(pos);
						mVideoView.pause();
						ispause = false;
					}
				}
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		findViewById();
		declaration();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (vf_main.getDisplayedChild() == 0) {
			initImageLoader();
			init();
		}
	};

	private void findViewById() {
		vf_main = (ViewFlipper) findViewById(R.id.gal_viewflipper);
		gridGallery = (GridView) findViewById(R.id.gal_Gallery);
		btn_galbck = (Button) findViewById(R.id.gal_clsbtn);
		mVideoView = (VideoView) findViewById(R.id.vid_vvmain);
		iv_set = (ImageView) findViewById(R.id.vid_ivmain);
		rl_capture = (RelativeLayout) findViewById(R.id.vid_navibtm);
		btn_vidback = (Button) findViewById(R.id.vid_backbtn);
		btn_donebtn = (Button) findViewById(R.id.vid_donebtn);
	}

	private void declaration() {
		context = this;
		
		vf_main.setDisplayedChild(0);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		rl_capture.setOnClickListener(this);
		btn_vidback.setOnClickListener(this);
		btn_galbck.setOnClickListener(this);
		btn_donebtn.setOnClickListener(this);

		try {
			mediaController = new MediaController(this);
			mediaMetadataRetriever = new MediaMetadataRetriever();
			dm = new DisplayMetrics();
			int height = dm.heightPixels;
			int width = dm.widthPixels;
			mVideoView.setMinimumWidth(width);
			mVideoView.setMinimumHeight(height);
			this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		} catch (Exception e) {

		}
	}

	private void initImageLoader() {

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(defaultOptions).memoryCache(
				new WeakMemoryCache());

		ImageLoaderConfiguration config = builder.build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
	}

	private void init() {

		handler = new Handler();
		adapter = new NativeGalleryAdapter(getApplicationContext(), imageLoader);
		gridGallery.setAdapter(adapter);
		new Thread() {

			@Override
			public void run() {
				Looper.prepare();
				handler.post(new Runnable() {

					@Override
					public void run() {
						adapter.addAll(getGalleryPhoto());
					}

				});
				Looper.loop();
			};

		}.start();

		gridGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				vid = Constant.data.get(position).sdcardPath.toString();
				Constant.videoCapture = vid;
				vf_main.setDisplayedChild(1);
				Log.d("vid", vid);
				try {
					String extr = Environment.getExternalStorageDirectory()
							.toString();
					File mFolder = new File(extr + "/CHURCH");
					if (!mFolder.exists()) {
						mFolder.mkdir();
					} else {
						mFolder.delete();
						mFolder.mkdir();
					}
					Calendar c = Calendar.getInstance();
					SimpleDateFormat ormat1 = new SimpleDateFormat("HH:mm:ss");
					String formatdate = ormat1.format(c.getTime());
					String s = "Church" + formatdate + ".png";
					Log.d("s", s);

					Constant.file2 = new File(mFolder.getAbsolutePath(), s);

					Constant.febCapture = Constant.file2.getAbsolutePath();
					Log.d("path", Constant.febCapture);

					mVideoView.setSoundEffectsEnabled(true);
					mVideoView.setDrawingCacheEnabled(true);
					mediaController.setAnchorView(mVideoView);
					mVideoView.setMediaController(mediaController);
					mediaMetadataRetriever.setDataSource(vid);
					boolean boo_vid = false;

					/*
					 * for (int i = 0; i < vid_tits.length; i++) { if
					 * (vid.contains(vid_tits[i])) { boo_vid = true; } }
					 */
					// if (boo_vid) {
					// if (path.contentEquals("path")) {
					mVideoView.setVideoPath(vid);
					mVideoView.requestFocus();
					mVideoView.setOnPreparedListener(PreparedListener);
					// }
					// }
				} catch (Exception e) {
					if (mVideoView != null) {
						mVideoView.stopPlayback();
					}
				}
			}
		});

	}

	public void toastsettext(String string1) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_activity,
				(ViewGroup) findViewById(R.id.toast_rl));
		TextView txt = (TextView) layout.findViewById(R.id.toast_txt);
		txt.setText(string1);
		Toast tst = new Toast(getApplicationContext());
		tst.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		tst.setDuration(Toast.LENGTH_SHORT);
		tst.setView(layout);
		tst.show();
	}

	@SuppressLint("NewApi")
	private ArrayList<CustomGallery> getGalleryPhoto() {
		ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();

		try {
			String[] projection = { MediaStore.Files.FileColumns._ID,
					MediaStore.Files.FileColumns.DATA,
					MediaStore.Files.FileColumns.DATE_ADDED,
					MediaStore.Files.FileColumns.MEDIA_TYPE,
					MediaStore.Files.FileColumns.MIME_TYPE,
					MediaStore.Files.FileColumns.TITLE };

			String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
					+ MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

			// Log.d("selection", selection);
			Uri queryUri = null;
			Cursor imagecursor = null;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				/*
				 * final String[] columns = { MediaStore.Images.Media.DATA,
				 * MediaStore.Images.Media._ID }; final String orderBy =
				 * MediaStore.Images.Media._ID; imagecursor = managedQuery(
				 * MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
				 * null, orderBy);
				 */
			} else {

				queryUri = MediaStore.Files.getContentUri("external");
				Log.d("query", queryUri.toString());
				Log.d("android", "general");
				CursorLoader cursorLoader = new CursorLoader(this, queryUri,
						projection, selection, null,
						MediaStore.Files.FileColumns.DATE_ADDED + " DESC"

				);

				imagecursor = cursorLoader.loadInBackground();

			}

			if (imagecursor != null && imagecursor.getCount() > 0) {

				for (int index = 0; index < imagecursor.getCount(); index++) {
					imagecursor.moveToPosition(index);
					CustomGallery item = new CustomGallery();
					item.sdcardPath = imagecursor.getString(imagecursor
							.getColumnIndex(MediaStore.Video.Thumbnails.DATA));

					galleryList.add(item);
					Log.d("TAG", "mediaPath: " + item.sdcardPath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return galleryList;
	}

	Bitmap bmFrame = null;

	@Override
	public void onClick(View v) {
		if (rl_capture == v) {
			int currentPosition = mVideoView.getCurrentPosition(); // in
																	// millisecond
			if (bmFrame != null) {
				bmFrame.recycle();
				bmFrame = null;
			}

			bmFrame = mediaMetadataRetriever
					.getFrameAtTime(currentPosition * 1000); // unit in

			if (bmFrame == null) {
				toastsettext("Try again later");
			} else {
				if (Constant.bit_map != null) {
					Constant.bit_map.recycle();
					Constant.bit_map = null;
				}
				Constant.bit_map = bmFrame;
				iv_set.setImageBitmap(bmFrame);
			}

		} else if (v == btn_vidback) {
			video();
		} else if (v == btn_galbck) {
			//Intent intent = new Intent(Gallery_Activity.this, MainAct.class);
		//	startActivity(intent);
			failed();
		} else if (v == btn_donebtn) {
			FileOutputStream fos = null;
			if (mp != null) {
				mp.pause();
				ispause = true;
			}

			if (bmFrame != null || Constant.bit_map != null) {
				Log.d("capt", Constant.febCapture);
				try {
					fos = new FileOutputStream(Constant.file2);
					bmFrame.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}

				if (Constant.file2.exists()) {
					System.out.println("file exists" + Constant.file2);
				} else {
					System.out
							.println("No such Fileeeeeeeeee" + Constant.file2);
				}
				cd = new ConnectionDetector(context);
				isInternetPresent = cd.isConnectingToInternet();
				if (isInternetPresent) {
					video(Constant.febCapture);
				} else {
					toastsettext("No Internet Connection");
				}
			} else {
				failed();
			}
		}

	}

	private void video(String thumb) {
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.setMessage("Please Wait...");
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("api_key", "d4b2e8f5473bd5023797436ce9556620");
		params.put("id", "2225");
		try {
			params.put("image", new File(thumb));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		pd.show();
		client.addHeader("Accept", "appliction/json");
		client.post("http://build.myappbuilder.com/api/elements/images.json?",
				params, response_Frame);

	}

	public AsyncHttpResponseHandler response_Frame = new AsyncHttpResponseHandler() {
		@Override
		public void onFinish() {
			super.onFinish();
			try {
				PluginResult resultS = null;
				Log.d("result", "result123");
				//Toast.makeText(context, "called", Toast.LENGTH_LONG).show();
				JSONObject jsonObj = new JSONObject();
				if(Constant.frm_url != null){
					jsonObj.put("frame", Constant.frm_url);
				}else{
					jsonObj.put("frame", Constant.febCapture);
				}
				jsonObj.put("video", Constant.videoCapture);
				if (Echo_Capture.callbackContext1 != null) {
					Echo_Capture.callbackContext1.success(jsonObj);
					resultS = new PluginResult(PluginResult.Status.OK, jsonObj);
					resultS.setKeepCallback(false);
					Echo_Capture.callbackContext1.sendPluginResult(resultS);
					Echo_Capture.callbackContext1 = null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				if(pd.isShowing()){
					pd.cancel();
					pd.dismiss();
				}
			}
			if(pd.isShowing()){
				pd.cancel();
				pd.dismiss();
			}
			finish();
			
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
				if(pd.isShowing()){
					pd.cancel();
					pd.dismiss();
				}
			}
		};

	};
}
