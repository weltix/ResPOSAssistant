package woyou.aidlservice.jiuiv5;

/**
 * Callback of print service execution results
 */
interface ITax {

	oneway void onDataResult(in byte [] data);
	
}