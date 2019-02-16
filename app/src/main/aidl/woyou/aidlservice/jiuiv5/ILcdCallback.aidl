package woyou.aidlservice.jiuiv5;

/**
 * Gu Xian feedback results
 */
interface ILcdCallback {

	/**
	* Return execution result
	* @param show: true - display success, false - display failure
	*/
	oneway void onRunResult(boolean show);	
}