package com.netcompss.ffmpeg4android_client;

import android.os.Bundle;
import android.os.Environment;

import org.apache.cordova.plugin.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class NewAct extends BaseWizard {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			File source = new File(Constant.command);
			File in = new File(Environment.getExternalStorageDirectory()+"/videokit");

			if(!in.exists()) {
				if(in.mkdir()); //directory is created;
				//Toast.makeText(getApplicationContext(),"directory is created",Toast.LENGTH_LONG).show();
			}
			else
			{
				//Toast.makeText(getApplicationContext(),"else",Toast.LENGTH_LONG).show();
			}

			File desi = new File(in+"/final.mp4");
			try {
				copyFile(source, desi);
			} catch (IOException e) {
				e.printStackTrace(); 
			}
		} else {
			Transfer();
		}
		
	}

	private void Transfer() {

		copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();
		setCommand(Constant.commandStr);
		String outputPath = FileUtils
				.getOutputFileFromCommandStr(Constant.commandStr);
		setOutputFilePath(outputPath);
		setProgressDialogTitle("Uploading...");
		setProgressDialogMessage("Depends on your video size, Uploading can take a few minutes");
		runTranscoing();
		
	}

	private void copyFile(File sourceFile, File destFile) throws IOException {
		if (!sourceFile.exists()) {
			return;
		}

		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null) {
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
			source.close();
		}
		if (destination != null) {
			destination.close();
		}
		Transfer();
	}

	public static void finished() {
		// finish();
	}

	@Override
	public void onBackPressed() {

	}

}
