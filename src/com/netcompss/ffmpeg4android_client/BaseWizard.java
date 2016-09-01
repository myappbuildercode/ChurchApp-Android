package com.netcompss.ffmpeg4android_client;



import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.churchapp.church.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netcompss.ffmpeg4android.IFfmpgefRemoteServiceBridge;
import com.netcompss.ffmpeg4android.LicenseCheckJNI;

import org.apache.cordova.PluginResult;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

/**
 * This class use the Template Method design pattern, 
 * the invokeService() virtual method is implemented at children level and called in this class template method onServiceConnected()
 * @author ehasson
 *
 */
public class BaseWizard extends Base {

	protected IFfmpgefRemoteServiceBridge remoteService;
	protected boolean started = false;
	protected RemoteServiceConnection conn = null;
	protected boolean invokeFlag = false;
	protected ProgressDialog progressDialog;
	
	
	protected Button convertButton;
	protected Button playButton;
	protected Button shareButton;
	protected Button selectButton;
	protected int PICK_REQUEST_CODE = 0;
	
	protected static String workingFolder;
	protected String outputFile;
	protected String outputFilePath;
	protected String inputFilePath;
	protected Prefs _prefs = null;
	

	protected String commandStr, returnPath = null;
	
	
	private String progressDialogMessage;
	private String progressDialogTitle;
	
	private int notificationIcon;
	private String notificationTitle = null;
	private String notificationMessage = null;
	private String notificationfinishedMessageTitle = null;
	private String notificationStoppedMessage = null;
	private String notificationfinishedMessageDesc = null;
	
	public void setNotificationfinishedMessageDesc(
			String notificationfinishedMessageDesc) {
		this.notificationfinishedMessageDesc = notificationfinishedMessageDesc;
	}


	protected String[] commandComplex;
	
	
	public String[] getCommandComplex() {
		return commandComplex;
	}
	public void setCommandComplex(String[] commandComplex) {
		this.commandComplex = commandComplex;
	}
	public String getNotificationfinishedMessageTitle() {
		return notificationfinishedMessageTitle;
	}
	public void setNotificationfinishedMessageTitle(String notificationfinishedMessage) {
		this.notificationfinishedMessageTitle = notificationfinishedMessage;
	}
	public String getNotificationStoppedMessage() {
		return notificationStoppedMessage;
	}
	public void setNotificationStoppedMessage(String notificationStoppedMessage) {
		this.notificationStoppedMessage = notificationStoppedMessage;
	}
	public void setNotificationTitle(String notificationTitle) {
		this.notificationTitle = notificationTitle;
	}
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	public void setNotificationIcon(int notificationIcon) {
		this.notificationIcon = notificationIcon;
	}
	public String getProgressDialogMessage() {
		return progressDialogMessage;
	}
	
	public void setProgressDialogMessage(String progressDialogMessage) {
		this.progressDialogMessage = progressDialogMessage;
	}
	public String getProgressDialogTitle() {
		return progressDialogTitle;
	}
	public void setProgressDialogTitle(String progressDialogTitle) {
		this.progressDialogTitle = progressDialogTitle;
	}
	
	

	public String getCommand() {
		return commandStr;
	}
	public void setCommand(String commandStr) {
		Log.i(Prefs.TAG, "Command is set");
		this.commandStr = commandStr;
	}
	
	
	public static String getWorkingFolder() {
		return workingFolder;
	}
	public String getOutputFile() {
		return outputFile;
	}
	public String getInputFilePath() {
		return inputFilePath;
	}
	
	private void setRemoteNotificaitonIcon() {
		if (notificationIcon != -1)
			Prefs.setRemoteNotificationIconId(getApplicationContext(), notificationIcon);
	}
	
	private void setRemoteNotificationInfo() {
		try {
			if (remoteService != null) {
				Log.i(Prefs.TAG, "setting remote notification info");
				if (notificationTitle != null)
					remoteService.setNotificationTitle(notificationTitle);
				if (notificationMessage != null)
					remoteService.setNotificationMessage(notificationMessage);
			}
			else {
				Log.w(Prefs.TAG, "remoteService is null, can't set remote notification info");
			}
		} catch (RemoteException e1) {
			Log.w(Prefs.TAG, e1.getMessage(), e1);
		}
	}
	
	// called from onServiceConnected
	public void invokeService() {
		
		Log.i(Prefs.TAG, "invokeService called");
		
		// needed for demo client for grasfull fail
		int rc = isLicenseValid(getApplicationContext());
		
		if (rc < 0) {
			String message = "";
			if (rc == -1) 
				message = "Trial expired, please contact support@netcompss.com";
			else
				message = "License Validation failed, please contact support@netcompss.com";
				
			
			new AlertDialog.Builder(this)
		    .setTitle("License Validation failed")
		    .setMessage(message)
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            
		        }
		     })
		     .show();
			
			return;
		}
		
		
		 setRemoteNotificationInfo();
		  
		  // this call with handle license gracefully.
		  // If it will be removed, the fail will be in the native code, causing the progress dialog to start.
		  //if (! isLicenseValid()) return;
		  
		  if (invokeFlag) {
			  if(conn == null) {
				  Toast.makeText(this, "Cannot invoke - service not bound", Toast.LENGTH_SHORT).show();
			  } else {
				  try {
					  String command = getCommand();
					  
					  if (remoteService != null) {
						  if (command != null)
							  remoteService.setFfmpegCommand(command);
						  else {
							  remoteService.setComplexFfmpegCommand(commandComplex);
						  }
						  remoteService.setWorkingFolder(Prefs.getWorkFolder());
						  runWithCommand(command);
						  
					  }
					  else {
						  Log.w( Prefs.TAG, "Invoke failed, remoteService is null." );
					  }

				  } catch (android.os.DeadObjectException e) {
					  Log.d( Prefs.TAG, "ignoring DeadObjectException (FFmpeg process exit)");
				  } catch (RemoteException re) {
					  Log.e( Prefs.TAG, re.getMessage(), re );
				  }
			  }
			  invokeFlag = false;
		  }
		  else {
			  Log.d(Prefs.TAG, "Not invoking");
			  
		  }
	}
	
	
	protected boolean invokeFileInfoServiceFlag = false;
	private TranscodeBackground _transcodeBackground;
	
	public void getInputFileAndOutputFileFromCommand(String workingFolder, String inputFileName) {

	 }
	
	public IFfmpgefRemoteServiceBridge getRemoteService() {
		return remoteService;
	}
	
	public void setWorkingFolder(String workingFolder) {
		Prefs.setWorkFolder(workingFolder);
	}
	
	
	public int isLicenseValid(Context ctx) {
		 LicenseCheckJNI lm = new LicenseCheckJNI();
		  int rc = lm.licenseCheck(Prefs.getWorkingFolderForNative(), ctx);
		  if (rc < 0) {
			  if (rc == -1)
				  Toast.makeText(this, "Trail Expired. contact support.", Toast.LENGTH_SHORT).show();
			  else if (rc == -2) 
				  Toast.makeText(this, "License invalid contact support", Toast.LENGTH_SHORT).show();
			  else 
				  Toast.makeText(this, "License check failed. contact support." + rc, Toast.LENGTH_SHORT).show();
		  }

		  return rc;
			  
		 
	}
	

	public void runTranscoing() {
		setRemoteNotificaitonIcon();
		releaseService();
		stopService();
		startService();
		invokeFlag = true;
		bindService();
	}
	
	 public void startAct(Class act) {
		 Intent intent = new Intent(this, act);
		 Log.d(Prefs.TAG, "Starting act:" + act);
		 this.startActivity(intent);
	 }
	
	public void runWithCommand(String command) {
		  Prefs p = new Prefs();
		  p.setContext(getApplicationContext());

		  deleteLogs();
		  FileUtils.writeToLocalLog("command: " + command);
		  FileUtils.writeToLocalLog("Input file size: " + Prefs.inputFileSize);
		  Log.d( Prefs.TAG, "Client invokeService()" );
		  Random rand = new Random();
		  int randInt  = rand.nextInt(1000);
		  _transcodeBackground = new TranscodeBackground(this, remoteService, randInt);
		  _transcodeBackground.setProgressDialogTitle(progressDialogTitle);
		  _transcodeBackground.setProgressDialogMessage(progressDialogMessage);
		  _transcodeBackground.setNotificationIcon(notificationIcon);
		  _transcodeBackground.setNotificationfinishedMessageTitle(notificationfinishedMessageTitle);
		  _transcodeBackground.setNotificationfinishedMessageDesc(notificationfinishedMessageDesc);
		  _transcodeBackground.setNotificationStoppedMessage(notificationStoppedMessage);
		  _transcodeBackground.execute();
	}
	
	
	public void copyLicenseAndDemoFilesFromAssetsToSDIfNeeded() {
		_prefs = new Prefs();
	    _prefs.setContext(this);
		File destVid = null;
		File destLic = null;
		//String workingFolderPath = Environment.getExternalStorageDirectory() + Prefs.WORKING_DIRECTORY;
		String workingFolderPath = Prefs.getWorkFolder();
		Log.i(Prefs.TAG, "workingFolderPath: " + workingFolderPath);
		//try {
			if (!FileUtils.checkIfFolderExists(workingFolderPath)) {
				
				boolean isFolderCreated = FileUtils.createFolder(workingFolderPath);
				Log.i(Prefs.TAG, workingFolderPath + " created? " + isFolderCreated);


			}
			else {
				Log.d(Prefs.TAG, "Working directory exists, not coping assests (license file and demo videos)");
				//Toast.makeText(this, "Sample videos located at: " + workingFolderPath , Toast.LENGTH_SHORT).show();
			}
			


			if (!FileUtils.checkIfFolderExists(_prefs.getOutFolder())) {
				boolean isFolderCreated = FileUtils.createFolder(_prefs.getOutFolder());
				Log.i(Prefs.TAG, _prefs.getOutFolder() + " created? " + isFolderCreated);
			}
			else {
				Log.d(Prefs.TAG, "output directory exists.");
				
			}


		
		//========= License Copy ===================================================================
		InputStream is = null;
		BufferedOutputStream o = null;
		boolean copyLic = true;
		try {
			is = getApplication().getAssets().open("ffmpeglicense.lic");
		} catch (Exception e) {
			Log.i(Prefs.TAG, "License file does not exist in the assets.");
			copyLic = false;
		}

		if (copyLic) {
			destLic = new File(workingFolderPath + "ffmpeglicense.lic");
			Log.i(Prefs.TAG, "Adding lic file at " + destLic.getAbsolutePath());

			o = null;
			try {
				byte[] buff = new byte[10000];
				int read = -1;
				o = new BufferedOutputStream(new FileOutputStream(destLic), 10000);
				while ((read = is.read(buff)) > -1) { 
					o.write(buff, 0, read);
				}
				Log.i(Prefs.TAG, "Copy " + destLic.getAbsolutePath() + " from assets to SDCARD finished succesfully");
			}
			catch (Exception e) {
				Log.e(Prefs.TAG, "Error when coping license file from assets to working folder: " + e.getMessage());
			}
			finally {
				try {
					is.close();
					if (o != null) o.close();
				} catch (IOException e) {
					Log.w(Prefs.TAG, "Error when closing license file io: " + e.getMessage());
				}  

			}

		}
		else {
			Log.i(Prefs.TAG, "Not coping license");
		}
		
		//========License Copy ======================================================================
		
	}
	
	
	private int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	protected void startService(){
		if (started) {
		//	Toast.makeText(this, "Service already started", Toast.LENGTH_SHORT).show();
		} else {
			
			Intent i = new Intent("com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
			if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				i.setPackage("com.netcompss.ffmpeg4android");


				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> services = packageManager.queryIntentServices(i, 0);
				//Log.i(Prefs.TAG, "!!!!!!!!!!!!!!!!!!services.size(): " +  services.size());



					//ResolveInfo service = services.get(0);
					i.setClassName("com.churchapp.church", "com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
					i.setAction("com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
					if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
						i.setPackage("com.netcompss.ffmpeg4android");
					}
					if (!invokeFileInfoServiceFlag) {
						i.addCategory("Base");
						Log.i(Prefs.TAG, "putting Base categoty");
					}
					else {
						i.addCategory("Info");
						Log.i(Prefs.TAG, "putting Info categoty");
					}

					ComponentName cn = startService(i);
					Log.d(Prefs.TAG, "started: " + cn.getClassName());

				posted();
			}
			else
			{
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> services = packageManager.queryIntentServices(i, 0);
				Log.i(Prefs.TAG, "!!!!!!!!!!!!!!!!!!services.size(): " +  services.size());


				if (services.size() > 0) {
					ResolveInfo service = services.get(0);
					i.setClassName(service.serviceInfo.packageName, service.serviceInfo.name);
					i.setAction("com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
					if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
						i.setPackage("com.netcompss.ffmpeg4android");
					}
					if (!invokeFileInfoServiceFlag) {
						i.addCategory("Base");
						Log.i(Prefs.TAG, "putting Base categoty");
					}
					else {
						i.addCategory("Info");
						Log.i(Prefs.TAG, "putting Info categoty");
					}

					ComponentName cn = startService(i);
					Log.d(Prefs.TAG, "started: " + cn.getClassName());
				}
			}

			started = true;
			Log.d( Prefs.TAG, "Client startService()" );
		}

	}






	private void posted() {
		try {
			JSONArray arry = new JSONArray(Constant.json_array);
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
				File path = new File(Environment.getExternalStorageDirectory()+"/videokit/final.mp4");
				params.put("video", path);
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
							String getString = getFileNameByUri(getApplicationContext(),
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
			//posted(arry, params);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}




	// this is not working, not stopping the remote service.
	protected void stopService() {
		Log.d( Prefs.TAG, "Client stopService()" );
		//Intent i = new Intent();
		Intent i = new Intent("com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
		if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			i.setPackage("com.netcompss.ffmpeg4android");
		} 
		stopService(i);
		started = false;
	}
	
	protected void bindService() {
		Log.d(Prefs.TAG," bindService() called");
		if(conn == null) {
			conn = new RemoteServiceConnection();
			///Intent i = new Intent();
			Intent i = new Intent("com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
			if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				i.setPackage("com.netcompss.ffmpeg4android");
			} 
			bindService(i, conn, Context.BIND_AUTO_CREATE);
			Log.d( Prefs.TAG, "Client bindService()" );
		} else {
			Log.d(Prefs.TAG," Client Cannot bind - service already bound");
			//Toast.makeText(this, "Client Cannot bind - service already bound", Toast.LENGTH_SHORT).show();
		}
	}
  
	protected void releaseService() {
		if(conn != null) {
			unbindService(conn);
			conn = null;
			Log.d( Prefs.TAG , "releaseService()" );
		} else {
			//Toast.makeText(this, "Client Cannot unbind - service not bound", Toast.LENGTH_SHORT).show();
			Log.d( Prefs.TAG , "Client Cannot unbind - service not bound");
		}
	}
	
	
	
	 public class RemoteServiceConnection implements ServiceConnection {
	    	public void onServiceConnected(ComponentName className, 
	    			IBinder boundService ) {
	    		Log.d( Prefs.TAG, "Client onServiceConnected()" );
	    		remoteService = IFfmpgefRemoteServiceBridge.Stub.asInterface((IBinder)boundService);
	    		
	    		
	    		if (invokeFileInfoServiceFlag)
	    			invokeFileInfoService(inputFilePath);
	    		else
	    			invokeService();

	    	}

	        public void onServiceDisconnected(ComponentName className) {
	          remoteService = null;
			  Log.d( Prefs.TAG, "onServiceDisconnected" );
	        }
	    };
	    
	    

	    
	    public void handleServiceFinished() {
	    	Log.i(Prefs.TAG, "FFMPEG finished.");
	    	//Toast.makeText(this, getString(R.string.notif_message_ok), Toast.LENGTH_LONG).show();
	    	
	    	//remove the sticky notification
	    	// fix 4.4.2 bug, should not effect other versions.
	    	releaseService();
    		stopService();

	    }
	    
	    protected void handleInfoServiceFinished() {
	    	Log.i(Prefs.TAG, "FFMPEG finished (info).");
	    	removeDialog(FILE_INFO_DIALOG);
	    	showDialog(FILE_INFO_DIALOG);
	    	invokeFileInfoServiceFlag = false;
	    	
	    	
	    }
	    
	   
	    
	    private String getRealPathFromURI(Uri contentUri) {
	        String[] proj = { MediaStore.Images.Media.DATA };
	        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        return cursor.getString(column_index);
	    }
	    
	    public void invokeFileInfoService(String inputFilePath) {
			  Log.i(Prefs.TAG, "invokeFileInfoService called");
			  
			  if (invokeFlag) {

				  if(conn == null) {
					 // Toast.makeText(this, "Cannot invoke - service not bound", Toast.LENGTH_SHORT).show();
				  } else {
					  try {
						  //FileUtils.deleteFile(workingFolder + outputFile);
						  String command = "ffmpeg -i " + inputFilePath;
						  if (remoteService != null) {
							  deleteLogs();
							  FileUtils.writeToLocalLog("command: " + command);
							  Log.i(Prefs.TAG, "command: " + command);
							  remoteService.setFfmpegCommand(command);
							  Log.d( Prefs.TAG, "Client invokeService()" );
							  remoteService.runTranscoding();
						  }
						  else {
							  Log.w( Prefs.TAG, "Invoke failed, remoteService is null." );
						  }

					  } catch (android.os.DeadObjectException e) {
						 
						  Log.d( Prefs.TAG, "ignoring DeadObjectException (FFmpeg process exit)");

					  } catch (RemoteException re) {
						  Log.e( Prefs.TAG, re.getMessage(), re );
					  }
				  }
				  handleInfoServiceFinished();
				  invokeFlag = false;
			  }
			  else {
				  Log.d(Prefs.TAG, "Not invoking");
				  
			  }
		  }
	    
	    
	    public void deleteLogs() {
		      FileUtils.deleteFile(Prefs.getVkLogFilePath());
		      FileUtils.deleteFile(Prefs.getFfmpeg4androidLogFilePath());
		      FileUtils.deleteFile(Prefs.getVideoKitLogFilePath());
	    }
		public void setOutputFilePath(String outputFilePath) {
			this.outputFilePath = outputFilePath;
			this.outputFile = FileUtils.getFileNameFromFilePath(outputFilePath);
		}
		
		
	public void stopTranscoding() {
		Log.d(Prefs.TAG, "stopTranscoding called");
		if (_transcodeBackground != null) {
			_transcodeBackground.forceCancel();
		}
	}
	    
	    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(Prefs.TAG, "BaseWizard onDestroy");
		stopTranscoding();
	}
	

	private int srcBgId = R.drawable.bg;
	static int w = 80;
	static int h = 80;
	File video_thumbpath = null;
	
	public void uploadtoserver() {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		try {
			JSONArray arry = new JSONArray(BaseVideo.jsondata);
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
							String getString = getFileNameByUri(getApplicationContext(),
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
	
	public static String getFileNameByUri(Context context, Uri uri) {
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
	
	String strMyImagePath = null;
	File f = null;

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
				BitmapFactory.decodeResource(getApplicationContext().getResources(), srcBgId,
						options);
				options.inJustDecodeBounds = false;
				options.inSampleSize = calculateInSampleSize(options, w, h);
				Bitmap unbgbtmp = BitmapFactory.decodeResource(
						getApplicationContext().getResources(), srcBgId, options);
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

	public String createVideoThumbnail(String fDescriptor) {
		Bitmap thumb = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(fDescriptor);
			thumb = retriever.getFrameAtTime(-1);
			int width = thumb.getWidth();
			int height = thumb.getHeight();
			int max = Math.max(width, height);
			if (max > 512) {
				float scale = 512f / max;
				int w = Math.round(scale * width);
				int h = Math.round(scale * height);
				thumb = Bitmap.createScaledBitmap(thumb, w, h, true);
			}

			FileOutputStream fos = null;
			String extr = Environment.getExternalStorageDirectory().toString();
			File mFolder = new File(extr + "/CHURCH");
			if (!mFolder.exists()) {
				mFolder.mkdir();
			} else {
				mFolder.delete();
				mFolder.mkdir();
			}

			String s = "churchthumbs.png";

			video_thumbpath = new File(mFolder.getAbsolutePath(), s);
			returnPath = video_thumbpath.getAbsolutePath();

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

		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
			Log.e("e",
					"Failed to create video thumbnail for file description: "
							+ fDescriptor.toString());
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
			Log.e("e",
					"Failed to create video thumbnail for file description: "
							+ fDescriptor.toString());
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		return returnPath;
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
			Log.e("message", arg0);
			if (arg0 != null) {
				Log.e("response_image", arg0);
				try {
					JSONObject obj = new JSONObject(arg0);
					deleted();
					PluginResult progressResult = new PluginResult(
							PluginResult.Status.OK, obj);
					progressResult.setKeepCallback(true);
					finish();
					BaseVideo.callbackContext.sendPluginResult(progressResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}

		private void deleted() {
			String extr = Environment.getExternalStorageDirectory().toString();
			File mFolder = new File(extr + "/CHURCH");
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
			File mFolders = new File(extr + "/CHURCH");
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

			File mFolders1 = new File(extr + "/CHURCH");
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
				finish();
				PluginResult progressResult = new PluginResult(
						PluginResult.Status.ERROR, arg1);
				progressResult.setKeepCallback(true);
				BaseVideo.callbackContext
						.sendPluginResult(progressResult);
			}
		};

	};

	public void uploadtoservers() {

		JSONObject object=new JSONObject();
		try {
			object.put("statues", "Error");
			object.put("message", "Try again later.....");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
		PluginResult progressResult = new PluginResult(	PluginResult.Status.ERROR, object.toString());
		progressResult.setKeepCallback(true);
		BaseVideo.callbackContext.sendPluginResult(progressResult);
		
	}


}
