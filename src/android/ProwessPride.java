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
	
	public void checkBluetoothAdapter(CallbackContext callbackContext)
	{
		if (mBT == null) {
			callbackContext.error("Bluetooth adapter not available");
			return;
		}
		if (!mBT.isEnabled()) {
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
	    			jsonDeviceArray = new JSONArray();
	    			mBTDevices=new ArrayList<BluetoothDevice>();
	    			if(mBT.isEnabled() && (mbsSocket==null || !mbsSocket.isConnected())){
	    				/*mProgressDlg = new ProgressDialog(cordova.getActivity());
	    				mProgressDlg.setMessage("Scanning...");
	    				mProgressDlg.setCancelable(false);
	    				mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
	    				    @Override
	    				    public void onClick(DialogInterface dialog, int which) {
	    				        dialog.dismiss();
	    				        mBT.cancelDiscovery();
	    				    }
	    				});
	    				Context context = (Context)cordova.getActivity().getApplicationContext();
	    				
	    				 // Register for broadcasts when a device is discovered
	    		       IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    		       context.registerReceiver(mReceiver, filter);
	    		       
	    		       
	    		       // Register for broadcasts when discovery starts
	    		       filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
	    		       context.registerReceiver(mReceiver, filter);

	    		       
	    		       // Register for broadcasts when discovery has finished
	    		       filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	    		       context.registerReceiver(mReceiver, filter);  
	    		       
	    		       Intent discoverableIntent = new
	    		    		   Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	    		    		   discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	    		    		   cordova.getActivity().startActivity(discoverableIntent);
	    		              
	    		       // Register for broadcasts when connectivity state changes
	    		       filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
	    		       context.registerReceiver(mReceiver, filter);  
	    		       Log.v("Prowess Pride Printer  ", "Intent Fileter Start");
	    		       mBT.startDiscovery();
	    		       Log.v("Prowess Pride Printer  ", "After Discovery");
	    		       Thread.sleep(5000);*/
		    			Set<BluetoothDevice> pairedDevices = mBT.getBondedDevices();
		    			if (pairedDevices.size() > 0) {
		    				for (BluetoothDevice device : pairedDevices) {
		    					jsonDeviceArray.put(device.getName());
		    					mBTDevices.add(device);
		    				}
		    			}
		    			if(jsonDeviceArray.length()>0){
		    				callbackContext.success(jsonDeviceArray);
		    				//cordova.getActivity().getBaseContext().unregisterReceiver(mReceiver);
		    			}
		    			else {
		    				callbackContext.error("No Bluetooth Device Found");
		    				//cordova.getActivity().getBaseContext().unregisterReceiver(mReceiver);
		    			}
	    			}
	    			else
	    			{
	    				callbackContext.error("Bluetooth not enabled");
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			callbackContext.error(e.getMessage());
	    			//cordova.getActivity().getBaseContext().unregisterReceiver(mReceiver);
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
	        		if(mBT.isEnabled() && (mbsSocket==null || !mbsSocket.isConnected()))
	    			{
	        			if(mBTDevices!=null && mBTDevices.size()>0)
	        			{
	        				for (BluetoothDevice device : mBTDevices) {
		    					if (device.getName().equalsIgnoreCase(deviceName)) {
		    						boolean bondDevice=false;
		        					if(BluetoothDevice.BOND_BONDED==device.getBondState())
		        					{
		        						bondDevice=true;
		        					}
		        					else
		        					{
		        						bondDevice=createBond(device);
		        					}
		        					if(bondDevice)
		        					{
			    						final UUID uuidComm = UUID.fromString(UUID_STR);
			    						mbsSocket = device.createRfcommSocketToServiceRecord(uuidComm);
			    						Thread.sleep(2000);
			    						mbsSocket.connect();
			    						Thread.sleep(2000);
			    						mosOut = mbsSocket.getOutputStream();//Get global output stream object
			    						misIn = mbsSocket.getInputStream();
			    						callbackContext.success("Bluetooth device successfully connected");
			    						bTConnected=true;
			    						return;
		        					}
		        					else
		        					{
		        						callbackContext.success("Bluetooth device not paired");
		        					}
		    					}
		    				}
	        			}
	    			}
	    			else
	    			{
	    				callbackContext.error("Bluetooth not enabled");
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			callbackContext.error(e.getMessage());
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
	        		if(!mbsSocket.isConnected())
	        		{
	        			callbackContext.error("Printer not connected");
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
	    				Toast.makeText(cordova.getActivity().getApplicationContext(), "Printer not connected", Toast.LENGTH_SHORT).show();
	    				callbackContext.error("Printer not connected");
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			callbackContext.error(e.getMessage());
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
		} else if (iRetVal == Printer_GEN.PARAM_ERROR) {
			msgReturnByPrinter="Parameter error";
		}else if (iRetVal == Printer_GEN.NO_RESPONSE) {
			msgReturnByPrinter="No response from Pride device";
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