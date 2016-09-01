package com.netcompss.ffmpeg4android_client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.churchapp.church.R;
import com.netcompss.ffmpeg4android.IFfmpgefRemoteServiceBridge;
import com.nua.New.New;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TranscodeBackground extends AsyncTask<Void, Integer, Integer>
{
	private BaseWizard _act;
	ProgressDialog progressDialog;
	IFfmpgefRemoteServiceBridge _remote;
	int _notificationId;
	private SimpleDateFormat _simpleDateFormat;
	long _timeRef = -1;
	int  _prevProgress = 0;
	WakeLock _wakeLock;
	private Prefs _prefs = null;
	
	private long _lastVklogSize = -1;
	private int _vkLogNoChangeCounter = 0;
	
	private String progressDialogMessage = null;
	private String progressDialogTitle = null;
	
	private int notificationIcon = -1;
	
	private String notificationfinishedMessageTitle = null;
	private String notificationfinishedMessageDesc = null;
	private String notificationStoppedMessage = null;
	private int _transcodingStatus = 0;
	
	
	public String getNotificationfinishedMessageTitle() {
		return notificationfinishedMessageTitle;
	}
	
	public void setNotificationfinishedMessageTitle(String notificationfinishedMessageTitle) {
		this.notificationfinishedMessageTitle = notificationfinishedMessageTitle;
	}
	
	public void setNotificationStoppedMessage(String notificationStoppedMessage) {
		this.notificationStoppedMessage = notificationStoppedMessage;
	}
	
	public int getNotificationIcon() {
		return notificationIcon;
	}
	public void setNotificationIcon(int notificationIcon) {
		this.notificationIcon = notificationIcon;
	}
	public String getProgressDialogMessage() {
		return progressDialogMessage;
	}
	public void setProgressDialogMessage(String progressDialogMessage) {
		//Log.d(Prefs.TAG, "!!!!!!!setProgressDialogMessage");
		this.progressDialogMessage = progressDialogMessage;
	}
	public String getProgressDialogTitle() {
		//Log.d(Prefs.TAG, "!!!!!!!getProgressDialogTitle");
		return progressDialogTitle;
	}
	public void setProgressDialogTitle(String progressDialogTitle) {
		this.progressDialogTitle = progressDialogTitle;
	}
	
	public TranscodeBackground(BaseWizard act, IFfmpgefRemoteServiceBridge remote, int notificationId) {
		_act = act;
		_remote = remote;
		_notificationId = notificationId;
		_simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SS");
		
		//_prefs = new Prefs(_act);
		_prefs = new Prefs();
		_prefs.setContext(_act);
		
		try {
			Date ref = _simpleDateFormat.parse("00:00:00.00");
			ref.setYear(112);
			_timeRef = ref.getTime();
		} catch (ParseException e) {
			Log.w(Prefs.TAG, "failed to set _timeRef");
		}
	}
	
	
	
	@Override
	protected void onPreExecute() {
		PowerManager powerManager = (PowerManager)_act.getSystemService(_act.POWER_SERVICE);
		_wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK"); 
		Log.d(Prefs.TAG, "Acquire wake lock");
		_wakeLock.acquire();
	    Prefs.durationOfCurrent = null;
		Prefs.transcodingIsRunning = true;
		progressDialog = new ProgressDialog(_act);
		//progressDialog.setCancelable(true);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		
		if (progressDialogTitle != null)
			progressDialog.setTitle(progressDialogTitle);
		else
			progressDialog.setTitle(_act.getString(R.string.progress_dialog_title));
		if (progressDialogMessage != null)
			progressDialog.setMessage(progressDialogMessage);
		else
			progressDialog.setMessage(_act.getString(R.string.progress_dialog_message));
		
		
		progressDialog.setCancelable(false);
		/*progressDialog.setButton("Cancel", 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.i(Prefs.TAG, "Cancel clicked");
				if (_remote != null) {
	    			try {
	    				Log.i(Prefs.TAG, "Setting Prefs.forceStopFlag to true");
	    			    Prefs.forceStopFlag = true;
	    				_remote.fexit();
	    				_remote.setTranscodingProgress(100);
	    				if (_wakeLock.isHeld())
	    					_wakeLock.release();
	    				else{
	    					Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
	    				}
	    				
	    			} catch (Exception e) {
	    				Log.i(Prefs.TAG, "DeadObject Exception after fexit()");
	    				
	    			}
	    		}
	    		else {
	    			Log.e(Prefs.TAG, "Remote service is null, can't force stop");
	    			Prefs.forceStopFlag = false;
	    			
	    		}
			}
		}
		);*/
		progressDialog.setMax(100);
		progressDialog.setProgress(0);
		progressDialog.show();
	}

	protected Integer doInBackground(Void... paths)  {
		Log.d(Prefs.TAG, "TranscodeBackground doInBackground started");
		
		try {
			_remote.runTranscoding();
			//Log.i(Prefs.TAG, "======= Waiting 2 secs for transcoding to start before starting progress notification==========");
			try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
			waitTillEnds();
		} catch (android.os.DeadObjectException e) {
		    Log.d( Prefs.TAG, "ignoring DeadObjectException (FFmpeg process exit)");
		}
		catch (RemoteException e) {
			 Log.e( Prefs.TAG, e.getMessage(), e );
		}
		
		
		return new Integer(0);
	}

	protected void onProgressUpdate(Integer... progress) {
		int p = progress[0];
		Log.i(Prefs.TAG, "onProgressUpdate: " + p);
		progressDialog.setProgress(p);
	}
	
	public void forceCancel() {
		Log.i(Prefs.TAG, "TranscodeBAckground forceCancel");
		if (progressDialog != null)
			progressDialog.dismiss();
		if (_remote != null) {
			try {
				Log.i(Prefs.TAG, "Setting Prefs.forceStopFlag to true");
			    Prefs.forceStopFlag = true;
				_remote.fexit();
				_remote.setTranscodingProgress(100);
				if (_wakeLock.isHeld())
					_wakeLock.release();
				else{
					Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
				}
				
			} catch (Exception e) {
				Log.i(Prefs.TAG, "DeadObject Exception after fexit()");
				
			}
		}
		else {
			Log.e(Prefs.TAG, "Remote service is null, can't force stop");
			Prefs.forceStopFlag = false;
			
		}
	}

	@Override
	protected void onCancelled() {
		Log.d(Prefs.TAG, "TranscodeBackground onCancelled");
		super.onCancelled();
		if (progressDialog != null)
			progressDialog.dismiss();
		
	}


	@Override
	protected void onPostExecute(Integer result) {
		Log.d(Prefs.TAG, "Releasing wake lock");
		if (_wakeLock.isHeld())
			_wakeLock.release();
		else{
			Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
		}
		Log.d(Prefs.TAG, "TranscodeBackground onPostExecute");
		Prefs.transcodingIsRunning = false;
		FileUtils.writeToLocalLog("TranscodeBackground onPostExecute");
		
		try {
			progressDialog.dismiss();
			_remote.setTranscodingProgress(100);
		} catch (Exception e) {
			Log.w(Prefs.TAG, "progressDialog failed to dismiss: " + e.getMessage(), e);
		}
		//FileUtils.writeSystemLogPart();
		if (_act != null) {
			String outputFileURL = _prefs.getOutFolder() + _act.getOutputFile();
			long outputSize = FileUtils.checkIfFileExistAndNotEmptyReturnSize(outputFileURL); 
	    	Prefs.outputFileSize = outputSize;
			showFinishNotification();
			_act.handleServiceFinished();
			if(outputSize>10){
					_act.deleteLogs();
					_act.uploadtoserver();
			}else{
				_act.deleteLogs(); 
				_act.uploadtoservers();
			}
			Prefs.outputFileSize = outputSize;
			showFinishNotification();
			_act.handleServiceFinished();
		}
		else {
			String m = "Act is null in TranscodeBackground onPostExecute, not showing notification";
			Log.e(Prefs.TAG, m );
			FileUtils.writeToLocalLog(m);
		}

	}
	
	
	private void showFinishNotification () {
		Log.d(Prefs.TAG, "showNotifications");
		
		//boolean transcodingOK = FileUtils.checkIfFileExistAndNotEmpty(_prefs.getOutFolder() + _act.getOutputFile()); 
		
		
		NotificationManager nm = (NotificationManager) _act.getSystemService(_act.NOTIFICATION_SERVICE);
		int icon =android.R.drawable.stat_notify_call_mute;
		if (notificationIcon != -1)
			icon = notificationIcon;
		long when = System.currentTimeMillis();
		
		String notifMessage = null;
		
		if (Prefs.forceStopFlag) 
			notifMessage = "notif_message_stopping";
		else {
			 notifMessage = "notif_message_ok";
		}
		Notification notification = new Notification(icon, notifMessage, when);

		if (Prefs.forceStopFlag) {
			Intent intent = new Intent(_act, New.class);
			PendingIntent pIntent = PendingIntent.getActivity(_act, 0, intent, 0);
			
			String notifTitle ="notif_message_stopped";
			if (notificationStoppedMessage != null)
				notifTitle = notificationStoppedMessage;
			
			//notification.setLatestEventInfo(_act, notifTitle, "", pIntent);
			FileUtils.writeToLocalLog("Transcoding force stopped");
			Log.i(Prefs.TAG, "setting Prefs.forceStopFlag to false for the next time");
			Prefs.forceStopFlag = false;
		}
		else if (_transcodingStatus == 0)  {
			if (_act.getOutputFile() == null) {
				Log.w(Prefs.TAG, "output file is not set use the setOutputFilePath method to set the full output file path");
				Intent intent = new Intent(_act, New.class);
				PendingIntent pIntent = PendingIntent.getActivity(_act, 0, intent, 0);
				String notifTitle = "notif_message_ok";
				if (notificationfinishedMessageTitle != null)
					notifTitle = notificationfinishedMessageTitle;
				//notification.setLatestEventInfo(_act, notifTitle, "", pIntent);
			}
			else {


				int fileType = FileUtils.getFileTypeFromFile(_act.getOutputFile());
				Log.d(Prefs.TAG, "got file type: " + fileType + " from file: " + _act.getOutputFile());
				String outputFileURL = _prefs.getOutFolder() + _act.getOutputFile();
				Log.d(Prefs.TAG, "outputFileURL (from TranscodeBackgroud): " + outputFileURL);
				Intent intent = null;
				switch (fileType) {
				case Prefs.FILE_TYPE_VIDEO:

					ContentValues content = new ContentValues(4);
					content.put(Video.VideoColumns.TITLE, _act.getOutputFile());
					content.put(Video.VideoColumns.DATE_ADDED,
							System.currentTimeMillis() / 1000);
					content.put(Video.Media.MIME_TYPE, "video/mp4");
					content.put(MediaStore.Video.Media.DATA, outputFileURL);
					ContentResolver resolver = _act.getContentResolver();
					Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							content);
					
					Log.d(Prefs.TAG, "content uri: " + uri);
	        	    if (uri == null) {
	        	    	Log.w(Prefs.TAG, "content uri is null, 4.3 fix attempt 1");
	        	    	String cUri = FileUtils.getVideoContentUriFromFilePath(_act, outputFileURL);
	        	    	if (cUri != null) {
	        	    		uri = Uri.parse(cUri);
	        	    		Log.d(Prefs.TAG, "content uri(after fix 1): " + uri);
	        	    	}
	        	    }
	        		
	        		intent = new Intent(Intent.ACTION_VIEW);
	        		boolean isKitkatOrAbove = Prefs.isAndroidVersionKitkatOrAbove();
	        		//isKitkatOrAbove = false;
	        		if (uri == null || isKitkatOrAbove) {
	        			Log.w(Prefs.TAG, "content uri is null, 4.3 fix attempt 2, is isKitkatOrAbove: " + isKitkatOrAbove);
	        			outputFileURL = "file://" + outputFileURL;
	        	    	uri = Uri.parse(outputFileURL);
	        	    	Log.d(Prefs.TAG, "content uri(after fix 2): " + uri);
	        		}
	        		intent.setDataAndType(uri, "video/mp4");
					break;

				case Prefs.FILE_TYPE_PIC:
					intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse("file://" + outputFileURL), "image/*");
					break;

				case Prefs.FILE_TYPE_AUDIO:
					intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse("file://" + outputFileURL), "audio/*");
					break;
				}


				float compressPrecent = Math.round((((float)(Prefs.inputFileSize - Prefs.outputFileSize)) / Prefs.outputFileSize) * 100);
				Log.d(Prefs.TAG, "input size: " + Prefs.inputFileSize  + " output size: " + Prefs.outputFileSize + " compressed(%): " + compressPrecent);


				PendingIntent pIntent = PendingIntent.getActivity(_act, 0, intent, 0);
				String defaultTitle =  _act.getOutputFile() + " compressed " + compressPrecent + " %";
				String title = (notificationfinishedMessageTitle == null) ? defaultTitle : notificationfinishedMessageTitle;
				String defaultDesc = "Play Result";
				String desc = (notificationfinishedMessageDesc == null) ? defaultDesc : notificationfinishedMessageDesc;
				
				/*notification.setLatestEventInfo(_act, title,
						desc,
						pIntent);*/
			}
			
			
		}
		else {
			Log.i(Prefs.TAG, "============ Transcoding Failed, caling fexist");
			try {_remote.fexit();} catch (RemoteException e) {Log.e(Prefs.TAG, "fexit remote excetion");}
			
			
		}
		

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(_notificationId, notification);
		
		
	
    }
    
	
	
	private int _durationOfCurrentWaitIndex = 0;
	private final int DURATION_OF_CURRENT_WAIT_INDEX_LIMIT = 6;
			
	private int calcProgress() {
		//Log.i(Prefs.TAG, "========calc progress=======");
		int progress  = 0;
		if (Prefs.durationOfCurrent == null) {
			String dur = FileUtils.getDutationFromVCLogRandomAccess();
			Log.d(Prefs.TAG, "dur: " + dur);
			if (dur == null || dur.equals("") || dur.equals("null") ) {
				Log.i(Prefs.TAG, "dur is not good, not setting ");
				if (_durationOfCurrentWaitIndex < DURATION_OF_CURRENT_WAIT_INDEX_LIMIT) {
					Log.i(Prefs.TAG, "waiting for real duration, going out of calcProgress with 0");
					_durationOfCurrentWaitIndex ++;
					return 0;
				}
				else {
					Log.i(Prefs.TAG, "_durationOfCurrentWaitIndex is equal to: " + DURATION_OF_CURRENT_WAIT_INDEX_LIMIT + " reseting.");
					_durationOfCurrentWaitIndex = 0;
					Log.i(Prefs.TAG, "setting fake Prefs.durationOfCurrent");

					Prefs.durationOfCurrent = "00:03:00.00";
					Log.w(Prefs.TAG, "setting fake Prefs.durationOfCurrent (Cant get from file): " + Prefs.durationOfCurrent);

				}
			}
			else {
				Prefs.durationOfCurrent = FileUtils.getDutationFromVCLogRandomAccess();
				Log.i(Prefs.TAG, "Got real duration: " + Prefs.durationOfCurrent);
			}
		}
		
		if (Prefs.durationOfCurrent != null) {
			long currentVkLogSize = -1;
			if (Prefs.noFfmpeg4androidLog) { // this means the new transcoding engine
				currentVkLogSize = FileUtils.getVKLogSizeRandomAccess();
				Log.i(Prefs.TAG, "==== getting currentVkLogSize from VK");
			}
			else {
				currentVkLogSize = FileUtils.getFFMpeg4AndroidLogSizeRandomAccess();
				Log.i(Prefs.TAG, "==== getting currentVkLogSize from FFmpeg4Android log");
			}
			
			
			if (currentVkLogSize > _lastVklogSize) {
				_lastVklogSize = currentVkLogSize;
				_vkLogNoChangeCounter = 0;
				Log.d(Prefs.TAG, "currentVkLogSize: " + currentVkLogSize);
			}
			else {
				Log.w(Prefs.TAG, "Looks like Vk log is not increasing in size");
				_vkLogNoChangeCounter++;
			}

			
			String currentTimeStr = FileUtils.readLastTimeFromFFmpegLogFileUsingRandomAccess();
			Log.d(Prefs.TAG, "currentTimeStr: " + currentTimeStr);
			if (currentTimeStr.equals("exit")) {
				Log.d(Prefs.TAG, "============Found one of the exit tokens in the log============");
				return 100;
			}
			else if (currentTimeStr.equals("error") && _prevProgress == 0) {
				Log.d(Prefs.TAG, "============Found error in the log============");
				FileUtils.writeToLocalLog("maybe_error stops transcoding with fail!");
				_transcodingStatus = -1;
				return 100;
			}
			else if (_vkLogNoChangeCounter > 16) {
				Log.e(Prefs.TAG, "VK log is not changing in size, and no exit token found");
				FileUtils.writeToLocalLog("VK log is not changing in size, and no exit token found");
				_transcodingStatus = -1;
				return 100;
			}
			try {
				Date durationDate = _simpleDateFormat.parse(Prefs.durationOfCurrent);
				Date currentTimeDate = _simpleDateFormat.parse(currentTimeStr);
				currentTimeDate.setYear(112);
				durationDate.setYear(112);
				//Log.d(Prefs.TAG, " durationDate: " + durationDate + " currentTimeDate: " + currentTimeDate);
				
				long durationLong = durationDate.getTime() - _timeRef;
				long currentTimeLong = currentTimeDate.getTime() - _timeRef;
				//Log.d(Prefs.TAG, " durationLong: " + durationLong + " currentTimeLong: " + currentTimeLong + " diff: " + (durationLong - currentTimeLong));
				progress  = Math.round(((float)currentTimeLong / durationLong) * 100);
				if (progress >= 100) {
					Log.w(Prefs.TAG, "progress is 100, but can't find exit in the log, probably fake progress, still running...");
					progress = 99;
				}
				_prevProgress = progress;
				
				try {
					if (progress != 0)
						_remote.setTranscodingProgress(progress);
					else {
						Log.i(Prefs.TAG, "progress is 0, not setting");
					}
				} catch (RemoteException e) {
					Log.w(Prefs.TAG, "failed _remote.setTranscodingProgress");
				}
				
				
			} catch (ParseException e) {
				Log.w(Prefs.TAG, e.getMessage());
			}
		}
		
		return progress;
	}
	
	
	
	private void waitTillEnds() {
		int progress = 0;
		while (true) {
            try {Thread.sleep(300);} catch (InterruptedException e) {}
            progress = calcProgress();
            if (progress != 0)
            	publishProgress(progress);
            else {
            	Log.i(Prefs.TAG, "progress is 0, not publishing");
            }
            if ((progress == 100) || Prefs.forceStopFlag || !Prefs.transcodingIsRunning ) {
            	//Log.i(Prefs.TAG, "======showNotificationInProgress() stop condition met - progress: " + progress + " Prefs.forceStopFlag: " + Prefs.forceStopFlag + " Prefs.transcodingIsRunning: " + Prefs.transcodingIsRunning );
            	break;
            }
            //Log.d(Prefs.TAG, "======Still no exit in the log=====");
        }
		
	}

	public String getNotificationfinishedMessageDesc() {
		return notificationfinishedMessageDesc;
	}

	public void setNotificationfinishedMessageDesc(
			String notificationfinishedMessageDesc) {
		this.notificationfinishedMessageDesc = notificationfinishedMessageDesc;
	}

}
