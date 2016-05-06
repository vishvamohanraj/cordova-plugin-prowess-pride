var exec = require('cordova/exec');
var ProwessPride={
	activatePrinterLibrary: function(){
		exec(null, null, "ProwessPride", "activatePrinterLibrary", []);
	},
	isBluetoothConnected: function(fnSuccess, fnError){
		exec(fnSuccess, fnError, "ProwessPride", "isBluetoothConnected", []);
	},
	getBluetoothDevicesList: function(fnSuccess, fnError){
		exec(fnSuccess, fnError, "ProwessPride", "getBluetoothDevicesList", []);
	},
	connectWithBluetoothDevice: function(fnSuccess, fnError,deviceName){
		exec(fnSuccess, fnError, "ProwessPride", "connectWithBluetoothDevice", [deviceName]);
	},
	print: function(fnSuccess, fnError,dataForPrint){
		exec(fnSuccess, fnError, "ProwessPride", "print", dataForPrint);
	}
};
module.exports = ProwessPride;