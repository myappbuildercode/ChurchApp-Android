package org.apache.cordova.plugin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.churchapp.church.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class Video_Activity extends Activity {

	@Override
	public void onBackPressed() {
	/*	if (bar.isShowing() || bar != null) {
			bar.setCancelable(true);
			bar.dismiss();
			bar.cancel();
		}

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
				}

				if (bar != null) {
					bar.setCancelable(true);
					bar.dismiss();
					bar.cancel();
				}

				Video_Activity.this.finish();

			}
		});

		alert_dialog.show();*/
		Video_Activity.this.finish();
	}

	private Boolean ispause = false;
	static ProgressDialog bar = null;
	private String[] vid_tits = { ".afl", ".AFL", ".mp4", ".MP4", ".avi",
			".AVI", ".3gp", ".3GP", ".mpeg", ".MPEG", ".flv", ".FLV", ".mkv",
			".MKV", ".mov", ".MOV", ".wmv", ".WMV", ".asf", ".ASF", ".moov",
			".mov", ".movie", ".mpe", ".MOOV", ".MOV", ".MOVIE", ".MPE", ".mv",
			".qt", ".qtc", ".gl", ".MV", ".QT", ".QTC", ".GL", ".isu", ".rv",
			".vdo", ".viv", ".ISU", ".RV", ".VDO", ".VIV", ".vivo", ".vos",
			"xsr", ".xdr", ".VIVO", ".VOS", ".XSR", ".XDR", ".scm", ".m1v",
			".m2v", ".mjpg", ".SCM", ".M1V", ".M2V", ".MJPG", ".fli", ".fmf",
			".asf", ".asx", ".FLI", ".FMF", ".ASF", ".ASX", ".avs", ".afl",
			".dl", ".dif", "AVS", ".AFL", ".DL", ".DIF", ".dv", ".DV", ".webm",
			".WEBM" };

	public static InputStream inputStreamVideo = null;
	public Context context;
	private VideoView mVideoView = null;
	static MediaPlayer mp = null;
	private MediaController mediaController;
	private DisplayMetrics dm;
	private int pos = 0;
	private String vid = null, path = null;

	public static Boolean isInternetPresent = false;
	public static ConnectionDetector cd;

	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;

	@Override
	protected void onStart() {
		super.onStart();
		try {
			cd = new ConnectionDetector(context);
			isInternetPresent = cd.isConnectingToInternet();
			if (isInternetPresent) {
				mVideoView.setSoundEffectsEnabled(true);
				mVideoView.setDrawingCacheEnabled(true);
				mediaController.setAnchorView(mVideoView);
				mVideoView.setMediaController(mediaController);
				boolean boo_vid = false;
				if (vid.contains("www.youtube.com")) {
					
					/*String data = vid.replace(
							"http://www.youtube.com/watch?v=", "");
					Intent i = new Intent(getApplicationContext(),
							Youtube_Activity.class);
					Bundle bun = new Bundle();
					bun.putString("youtube", data);
					i.putExtras(bun);
					Video_Activity.this.startActivityForResult(i, 1111);*/
				} else if (vid.contains(".m4v") || vid.contains(".m4v")) {
					if (bar.isShowing()) {
						bar.setCancelable(true);
						bar.cancel();
						bar.dismiss();
					}
					new download1().execute(vid);
				} else if (vid.contains(".mp4") || vid.contains(".MP4")) {
					mVideoView.setVideoURI(Uri.parse(vid));
					mVideoView.requestFocus();
					if (bar.isShowing()) {
						bar.setCancelable(true);
						bar.cancel();
						bar.dismiss();
					}
					new download1().execute(vid);
					mVideoView.setOnPreparedListener(PreparedListener);
				} else if (vid.contains("iPhone")) {
					mVideoView.setVideoURI(Uri.parse(vid));
					// new download1().execute(vid);
				} else {
					for (int i = 0; i < vid_tits.length; i++) {
						if (vid.contains(vid_tits[i])) {
							boo_vid = true;
							break;
						}
					}
					if (boo_vid) {
						if (path.contentEquals("path")) {
							mVideoView.setVideoPath(vid);
							mVideoView.requestFocus();
							if (bar.isShowing()) {
								bar.setCancelable(true);
								bar.cancel();
								bar.dismiss();
							}
							mVideoView.setOnPreparedListener(PreparedListener);
						} else if (path.contentEquals("uri")) {
							mVideoView.setVideoURI(Uri.parse(vid));
							mVideoView.requestFocus();
							if (bar.isShowing()) {
								bar.setCancelable(true);
								bar.cancel();
								bar.dismiss();
							}
							mVideoView.setOnPreparedListener(PreparedListener);
						}
					} else {
						toastsettext("This format could not support, Please try another");
					}
				}
			} else {
				toastsettext("No Internet Connection");
			}
		} catch (Exception e) {
			if (mVideoView != null) {
				mVideoView.stopPlayback();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1111) {
			if (resultCode == RESULT_OK) {
				finish();
			}
		}
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videosurface);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		context = this;
		try {
		/*	bar = new ProgressDialog(Video_Activity.this);
			bar.setTitle("Video Streaming");
			bar.setMessage("Buffering...");
			bar.setIndeterminate(false);
			bar.setCancelable(false);
			bar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			bar.show();*/
			Bundle bun = getIntent().getExtras();
			if (bun != null) {
				vid = bun.getString("vid");
				path = bun.getString("img");
				Log.d("vid", vid);
				Log.d("path", path);
				mVideoView = (VideoView) findViewById(R.id.video_view);
				mediaController = new MediaController(this);
				dm = new DisplayMetrics();
				int height = dm.heightPixels;
				int width = dm.widthPixels;
				new download1().execute(vid);
				mVideoView.setMinimumWidth(width);
				mVideoView.setMinimumHeight(height);
				this.getWindowManager().getDefaultDisplay().getMetrics(dm);
			}
		} catch (Exception e) {

		}
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
			setFullScreen(Video_Activity.this);

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			setFullScreen1(Video_Activity.this);

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
				onStart();
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

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

	class download1 extends AsyncTask<String, String, String> {
		String path = null;
		ProgressDialog mProgressDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (bar != null) {
				bar.setCancelable(true);
				bar.cancel();
				bar.dismiss();
			}
			mProgressDialog = new ProgressDialog(Video_Activity.this);
			mProgressDialog.setMessage("Buffering..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;
			File file = null;
			try {
				File directory = Environment.getExternalStorageDirectory();
				file = new File(directory + "/church" + "0" + ".mp4");

				if (file.exists()) {
					file.delete();
					file = new File(directory + "/church" + "0" + ".mp4");
				}
				path = file.getAbsolutePath();

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(file);

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
			}
			return null;

		}

		protected void onProgressUpdate(String... progress) {
			Log.d("ANDRO_ASYNC", progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String unused) {
			if (mProgressDialog != null) {
				mProgressDialog.setCancelable(true);
				mProgressDialog.cancel();
				mProgressDialog.dismiss();
			}
			mVideoView.setVideoPath(path);
			mVideoView.requestFocus();
			mVideoView.setOnPreparedListener(PreparedListener);
		}
	}
}