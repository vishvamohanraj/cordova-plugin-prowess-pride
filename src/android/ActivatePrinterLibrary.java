package com.vmr.prowess.pride;

import org.apache.cordova.CordovaActivity;

import com.prowesspride.api.Setup;

import android.content.Intent;
import android.os.Bundle;

public class ActivatePrinterLibrary extends CordovaActivity
{
	public static boolean IS_Activate = false;
	public static Setup setup = null;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try{
			Intent launchIntent = this.getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName());
			setup=new Setup();
			IS_Activate = setup.blActivateLibrary(this,this.getResources().getIdentifier("licencefull_pride_gen", "raw", getApplicationContext().getPackageName()));
			String className = launchIntent.getComponent().getClassName();
			Intent intent = new Intent(this,Class.forName(className));
	        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        startActivity(intent);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}