package woyou.aidlservice.jiuiv5;

/**
 * Callback of print service execution results
 */
interface ICallback {

	/**
	* Return the result of the interface execution
	* Note: This callback only indicates whether the interface execution is successful but does not
	* indicate the working result of the printer. If you need to obtain the printer result,
	* please use the transaction mode.
	* @param isSuccess:	 true - execution succeeded, false - execution failed
	*/
	oneway void onRunResult(boolean isSuccess);

	/**
	* Returns the result of the interface execution (string data)
	* @param result:	As a result, the print length, etc. since the printer was powered on (in mm)
	*/
	oneway void onReturnString(String result);

	/**
	* Returns the specific cause of an exception when the interface fails to execute.
	* code：	Exception code
	* msg:	Exception description
	*/
	oneway void  onRaiseException(int code, String msg);

	/**
	* Return to printer results
	* code：	Exception code: 0 success, 1 failure
	* msg:	Exception description
	*/
	oneway void  onPrintResult(int code, String msg);
	
}