package com.vmr.prowess.pride;

import org.apache.cordova.CordovaWebView;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import com.prowesspride.api.Printer_GEN;

public class ProwessPride extends CordovaPlugin {
	Printer_GEN ptrGen;
	int iRetVal;
	private static BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();
	private static BluetoothSocket mbsSocket = null;
	public static InputStream misIn = null;
	public static OutputStream mosOut = null;
	public static JSONArray jsonDeviceArray;
	public static boolean bTConnected = false;
	public static List<BluetoothDevice> mBTDevices;
	public static BluetoothDevice mBTDeviceConnected;
	public final static String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
	private ProgressDialog mProgressDlg;
	/**
	* Constructor.
	*/
	public ProwessPride() {}
	/**
	* Sets the context of the Command. This can then be used to do things like
	* get file paths associated with the Activity.
	*
	* @param cordova The context of the main Activity.
	* @param webView The CordovaWebView Cordova is running in.
	*/
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}
	
	public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		final Context context=this.cordova.getActivity().getApplicationContext();
		if (action.equals("activatePrinterLibrary")) {
			this.activatePrinterLibrary(context);
			return true;
		}
		else if (action.equals("isBluetoothConnected")) {
			this.isBluetoothConnected(callbackContext);
			return true;
		}
		else if (action.equals("getBluetoothDevicesList")) {
			this.getBluetoothDevicesList(callbackContext);
			return true;
		}
		else if (action.equals("connectWithBluetoothDevice")) {
			this.connectWithBluetoothDevice(callbackContext,args.getString(0));
			return true;
		}
		else if (action.equals("print")) {
			this.sendDataForPrint(callbackContext,args);
			return true;
		}
		return false;
	}
	
	public void activatePrinterLibrary(final Context context)
	{
		cordova.getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	activateLibrary(context);
	        }
	    });
	}
	
	public void activateLibrary(Context context)
	{
		if(!ActivateLibrary.IS_Activate)
		{
			Intent intent=new Intent(context,ActivateLibrary.class);
			cordova.getActivity().startActivity(intent);
		}
	}
	
	public void isBluetoothConnected(final CallbackContext callbackContext)
	{
		cordova.getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	try{
					if (mBT==null ||!mBT.isEnabled())
					{
						mbsSocket=null;
						ptrGen=null;
						mBTDeviceConnected=null;
						callbackContext.success(Boolean.toString(Boolean.FALSE));
					}
	        		else if(mbsSocket!=null)
	        			callbackContext.success(Boolean.toString(mbsSocket.isConnected()));
	        		else
	        			callbackContext.success(Boolean.toString(Boolean.FALSE));
	        	}
	        	catch(Exception e)
	        	{
	        		callbackContext.error("Bluetooth device not connected");
	        	}
	        }
	    });
	}
	
	public void checkBluetoothAdapter(CallbackContext callbackContext)
	{
		if (mBT == null) {
			callbackContext.error("Bluetooth adapter not available");
			return;
		}
		if (!mBT.isEnabled()) {
			mbsSocket=null;
			ptrGen=null;
			mBTDeviceConnected=null;
			jsonDeviceArray = new JSONArray();
	    	mBTDevices=new ArrayList<BluetoothDevice>();
			Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			cordova.getActivity().startActivityForResult(enableBluetooth, 0);
		}
	}
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {	    	
	        String action = intent.getAction();

	        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
	        	Log.v("Prowess Pride Printer  ", "In Discovery start");
				mProgressDlg.show();
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	Log.v("Prowess Pride Printer  ", "In Discovery end");
	        	mProgressDlg.hide();
	        	//webView.getContext().unregisterReceiver(mReceiver);
	        }
	        else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	// Get the BluetoothDevice object from the Intent
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        	// Add the name and address to an array adapter to show in a JSON Array
	        	jsonDeviceArray.put(device.getName());
	            Log.v("Prowess Pride Printer  ", device.getName()+"        "+device.getAddress());
	            mBTDevices.add(device);
	        }
	    }
	};
	public void getBluetoothDevicesList(final CallbackContext callbackContext)
	{
		cordova.getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	    		try {
	    			checkBluetoothAdapter(callbackContext);
					Thread.sleep(5000);
	    			jsonDeviceArray = new JSONArray();
	    			mBTDevices=new ArrayList<BluetoothDevice>();
	    			if(mBT.isEnabled()){
						if(mbsSocket==null || !mbsSocket.isConnected())
						{
							Set<BluetoothDevice> pairedDevices = mBT.getBondedDevices();
							if (pairedDevices.size() > 0) {
								for (BluetoothDevice device : pairedDevices) {
									jsonDeviceArray.put(device.getName());
									mBTDevices.add(device);
								}
							}
							if(jsonDeviceArray.length()>0){
								callbackContext.success(jsonDeviceArray);
							}
							else {
								callbackContext.error("No Bluetooth Device Found");
							}
						}
						else
						{
							callbackContext.error("Bluetooth already connected or in use");
						}
	    			}
	    			else
	    			{
	    				callbackContext.error("Bluetooth adapter not enabled");
	    			}
	    		} catch (Exception e) {
	    			callbackContext.error("Unable to find bluetooth devices");
	    		}
	        }
	    });
	}
	
	public boolean createBond(BluetoothDevice device)throws Exception  
    { 
        Method createBondMethod = device.getClass().getMethod("createBond",(Class[])null);  
        Boolean returnValue = (Boolean) createBondMethod.invoke(device,(Object[])null);  
        return returnValue.booleanValue();  
    }  
	
	public void connectWithBluetoothDevice(final CallbackContext callbackContext,final String deviceName)
	{
		cordova.getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	try {
	        		checkBluetoothAdapter(callbackContext);
	        		if(mBT.isEnabled()){
						if(mbsSocket==null || !mbsSocket.isConnected())
						{
							if(mBTDevices!=null && mBTDevices.size()>0)
							{
								for (BluetoothDevice device : mBTDevices) {
									if (device.getName().equalsIgnoreCase(deviceName)) {
										boolean bondDevice=false;
										mBTDeviceConnected=device;
										if(BluetoothDevice.BOND_BONDED==mBTDeviceConnected.getBondState()){
											bondDevice=true;
										}
										else{
											bondDevice=createBond(mBTDeviceConnected);
										}
										if(bondDevice){
											final UUID uuidComm = UUID.fromString(UUID_STR);
											mbsSocket = mBTDeviceConnected.createRfcommSocketToServiceRecord(uuidComm);
											Thread.sleep(3000);
											mbsSocket.connect();
											Thread.sleep(3000);
											mosOut = mbsSocket.getOutputStream();//Get global output stream object
											misIn = mbsSocket.getInputStream();
											callbackContext.success("Bluetooth device successfully connected");
											bTConnected=true;
											return;
										}
										else{
											mBTDeviceConnected=null;
											callbackContext.error("Unable to connect with bluetooth device");
										}
									}
									else{
										callbackContext.error("Selected device not found now");
									}
								}
							}
						}
						else
						{
							callbackContext.error("Bluetooth already connected or in use");
						}
	    			}
	    			else
	    			{
	    				callbackContext.error("Bluetooth adapter not enabled");
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			callbackContext.error("Unable to connect with bluetooth device");
	    		}
	        }
	    });
	}
	
	public void sendDataForPrint(final CallbackContext callbackContext, final JSONArray args)
	{
		cordova.getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	try 
	        	{
	        		if(!ActivateLibrary.IS_Activate)
	        		{
	        			activateLibrary(cordova.getActivity().getApplicationContext());
	        			callbackContext.error("Printer library not activated");
	        			return;
	        		}
					if (mBT == null) {
						callbackContext.error("Bluetooth adapter not available");
						return;
					}
					if (!mBT.isEnabled()) {
						mbsSocket=null;
						ptrGen=null;
						mBTDeviceConnected=null;
						jsonDeviceArray = new JSONArray();
						mBTDevices=new ArrayList<BluetoothDevice>();
						Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						cordova.getActivity().startActivityForResult(enableBluetooth, 0);
						Thread.sleep(3000);
					}	        		
	        		ptrGen = new Printer_GEN(ActivateLibrary.setup, mosOut, misIn);
	        		if(ptrGen!=null)
	    			{
	    				ptrGen.iFlushBuf();
	    				if(args.length()>0)
	    				{
	    					for(int i=0;i<args.length();i++)
	    					{
	    						ptrGen.iAddData((byte) 0x01,args.getString(i));
	    					}
	    				}
	    				ptrGen.iAddData((byte) 0x01, "\n\n\n\n\n");
	    				iRetVal= ptrGen.iStartPrinting(1);
	    				String msgReturnByPrinter=msgReturnByPrinter(iRetVal);
	    				Toast.makeText(cordova.getActivity().getApplicationContext(), msgReturnByPrinter, Toast.LENGTH_SHORT).show();
	    			}
	    			else
	    			{
	    				callbackContext.error("Printer not connected");
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			callbackContext.error("Printer not connected");
	    		}
	        }
	    });
	}
	
	public String msgReturnByPrinter(Integer result) {
		String msgReturnByPrinter;
		if (iRetVal == -100) {
			msgReturnByPrinter="Device not connected";
		} else if (iRetVal == Printer_GEN.SUCCESS) {
			msgReturnByPrinter="Print Successfully";
		} else if (iRetVal == Printer_GEN.PLATEN_OPEN) {
			msgReturnByPrinter="Platen open";
		} else if (iRetVal == Printer_GEN.PAPER_OUT) {
			msgReturnByPrinter="Paper out";
		} else if (iRetVal == Printer_GEN.IMPROPER_VOLTAGE) {
			msgReturnByPrinter="Printer at improper voltage";
		} else if (iRetVal == Printer_GEN.FAILURE) {
			msgReturnByPrinter="Printing failed";
			mbsSocket=null;
			ptrGen=null;
			mBTDeviceConnected=null;
		} else if (iRetVal == Printer_GEN.PARAM_ERROR) {
			msgReturnByPrinter="Parameter error";
		}else if (iRetVal == Printer_GEN.NO_RESPONSE) {
			msgReturnByPrinter="No response from Pride device";
			mbsSocket=null;
			ptrGen=null;
			mBTDeviceConnected=null;
		}else if (iRetVal== Printer_GEN.DEMO_VERSION) {
			msgReturnByPrinter="Library in demo version";
		}else if (iRetVal==Printer_GEN.INVALID_DEVICE_ID) {
			msgReturnByPrinter="Connected  device is not authenticated.";
		}else if (iRetVal==Printer_GEN.NOT_ACTIVATED) {
			msgReturnByPrinter="Library not activated";
		}else if (iRetVal==Printer_GEN.NOT_SUPPORTED) {
			msgReturnByPrinter="Not Supported";
		}else{
			msgReturnByPrinter="Unknown Response from Device";
		}
		return msgReturnByPrinter;
	}
}