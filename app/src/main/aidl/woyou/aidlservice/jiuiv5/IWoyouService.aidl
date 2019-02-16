//Mini series models

package woyou.aidlservice.jiuiv5;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.ILcdCallback;
import android.graphics.Bitmap;
import woyou.aidlservice.jiuiv5.ITax;

interface IWoyouService
{

	/**
	* Replace the original printer upgrade firmware interface (void updateFirmware())
    * Now changed to the data interface of the load package name, only the system call
    * Supported version: 4.0.0 or higher
	*/
    boolean postPrintData(String packageName, in byte[] data, int offset, int length);

	/**
	* Printer firmware status
	* return:
	* 0--unknown，
	* A5--bootloader,
	* C3--print
	*/
	int getFirmwareStatus();

	/**
	* Take the WoyouService service version
	*/
	String getServiceVersion();

	/**
	 * Initialize the printer, reset the printer's logic, but not clear the buffer data, so
     * Unfinished print jobs will continue after reset
	 */
	void printerInit(in ICallback callback);

	/**
	* The printer self-test, the printer will print a self-test page
	*/
	void printerSelfChecking(in ICallback callback);

	/**
	* Get the printer board serial number
	*/
	String getPrinterSerialNo();

	/**
	* Get the printer firmware version number
	*/
	String getPrinterVersion();

	/**
	* Get the printer model
	*/
	String getPrinterModal();

	/**
	* Get printhead print length
	*/
	int getPrintedLength();

	/**
	 * The printer feeds paper (forced line feed, after the end of the print content, the paper is n lines)
     * n: Number of paper lines
	 */
	void lineWrap(int n, in ICallback callback);

	/**
	* Print using the original instructions
    * data: instruction
	*/
	void sendRAWData(in byte[] data, in ICallback callback);

	/**
	* Set the alignment mode, which has an effect on printing afterwards, unless initialized
    * alignment:
    * 0--left,
    * 1--centered,
    * 2--right
	*/
	void setAlignment(int alignment, in ICallback callback);

	/**
	* Set the print font, temporarily only the system call, the external call is invalid
	*/
	void setFontName(String typeface, in ICallback callback);

	/**
	* Set the font size, which has an effect on printing afterwards, unless initialized
    * Note: The font size is printed beyond the standard international directives.
    * Adjusting the font size will affect the character width, and the number of characters per line will also change.
    * Therefore, layouts formed in monospaced fonts may be confusing
    * fontsize: font size
	*/
	void setFontSize(float fontsize, in ICallback callback);

	/**
	* Print text, the text width is one line, and the line is automatically wrapped and typeset.
	* If the line is not full, it will not print unless it is forced to wrap.
    * text: the text string to be printed
	*/
	void printText(String text, in ICallback callback);

	/**
	* Print the text of the specified font, the font setting is only valid for this time.
    * text: to print text
    * typeface: font name (temporarily only system call, invalid external call)
    * fontsize: font size
	*/
	void printTextWithFont(String text, String typeface, float fontsize, in ICallback callback);

	/**
	* Print a row of the table, you can specify the column width, alignment
    * colsTextArr array of text strings for each column
    * colsWidthArr array of widths of columns (in English characters, each Chinese character occupies two English characters, each width is greater than 0)
    * colsAlign alignment of each column (0 left, 1 center, 2 right)
    * Note: The array length of the three parameters should be the same. If the width of colsText[i] is greater than colsWidth[i], the text is wrapped.
	*/
	void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);


	/**
	* Print picture
    * bitmap: image bitmap object
    * Note: The maximum width is 576 pixels.
    * If the width is exceeded, the display will be incomplete; the picture size is long * width <8M;
	*/
	void printBitmap(in Bitmap bitmap, in ICallback callback);

	/**
	*Print one-dimensional barcode
    * data: barcode data
    * symbology: barcode type
    * 0 -- UPC-A,
    * 1 -- UPC-E,
    * 2 -- JAN13 (EAN13),
    * 3 -- JAN8 (EAN8),
    * 4 -- CODE39,
    * 5 -- ITF,
    * 6 -- CODABAR,
    * 7 -- CODE93,
    * 8 -- CODE128
    * height: bar code height, value 1 to 255, default 162
    * width: barcode width, value 2 to 6, default 2
    * textposition: text position 0--do not print text, 1--text is above the barcode, 2--text is below the barcode, 3--barcode is printed above and below
	*/
	void printBarCode(String data, int symbology, int height, int width, int textposition,  in ICallback callback);

	/**
	* Print 2D barcode
    * data: QR code data
    * modulesize: QR code block size (unit: point, value 1 to 16)
    * errorlevel: QR code error correction level (0 to 3),
    * 0 -- error correction level L ( 7%),
    * 1 -- error correction level M (15%),
    * 2 -- error correction level Q (25%),
    * 3 -- Error correction level H (30%)
	*/
	void printQRCode(String data, int modulesize, int errorlevel, in ICallback callback);

	/**
	* Print text, the text width is one line, and the line is automatically wrapped and typeset.
	* If the line is not full, it will not print unless it is forced to wrap.
    * Text is output as it is in vector text width, ie each character is not equal width
    * text: the text string to be printed
	*/
	void printOriginalText(String text, in ICallback callback);

	/**
	* Print buffer content
    * Supported version: T1mini-v2.4.1 or higher
    * T2mini-v1.0.0 or higher
	*/
	void commitPrinterBuffer();

	/**
	* Cut paper
	*/
	void cutPaper(in ICallback callback);

	/**
	* Get the number of cutters
	*/
	int getCutPaperTimes();

	/**
	* Open the cashbox
	*/
	void openDrawer(in ICallback callback);

	/**
	* Cash drawer cumulative opening times
	*/
	int getOpenDrawerTimes();

	/**
	* Enter transaction mode, all print calls will be cached;
    * Call commitPrinterBuffe(), exitPrinterBuffer(true), commitPrinterBufferWithCallback(),
    * exitPrinterBufferWithCallback(true) before printing;
    * clean: whether to clear the cached buffer contents if the transaction mode has not been exited before
    * Supported version: T1mini-v2.4.1 or higher
    * T2mini-v1.0.0 or higher
	*/
	void enterPrinterBuffer(in boolean clean);

	/**
	* Exit buffer mode
    * commit: whether to print out the contents of the buffer
    * Supported version: T1mini-v2.4.1 or higher
    * T2mini-v1.0.0 or higher
	*/
	void exitPrinterBuffer(in boolean commit);

   /**
    * Send CNC instructions
    * data: tax control order
    */
	void tax(in byte [] data,in ITax callback);

	/**
	* Get current printer mode
    * Back:
    * 0 Normal mode
    * 1 Black mark mode
	*/
	int getPrinterMode();

	/**
	* Get the black paper mode printer automatic paper distance
    * Back: Continue to move forward after detecting the black mark
	*/
	int getPrinterBBMDistance();

	/**
	* Print a row of the table, you can specify the column width, alignment
    * colsTextArr array of text strings for each column
    * colsWidthArr column width weight is the proportion of each column
    * colsAlign alignment of each column (0 left, 1 center, 2 right)
    * Note: The array length of the three parameters should be the same.
    * If the width of colsText[i] is greater than colsWidth[i], the text is wrapped.
	*/
	void printColumnsString(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);

	/**
	* Get the latest status of the printer
    * Return value:
    * 1 Printer is OK
    * 2 Printer update status
    * 3 Get status error
    * 4 Out of paper
    * 5 Overheat
    * 6 Open cover
    * 7 Cutter abnormality
    * 8 Cutter recovery
    * 505 No printer detected
	**/
	int updatePrinterState();

	/*
	*   Send a command
    * flag:
    * 1 Initialization
    * 2 Wake up LCD
    * 3 Sleep LCD
    * 4 Clear screen
	*/
	void sendLCDCommand(in int flag);

    /**
    *   Send a single line of solid content string
    * string: solidified display string
    */
	void sendLCDString(in String string, ILcdCallback callback);

    /**
    *   Send a solid picture
    * bitmap: The content of the displayed image is 128*40 pixels.
    */
	void sendLCDBitmap(in Bitmap bitmap, ILcdCallback callback);

	/**
	* With feedback print buffer content
    * Supported version: T1mini-v2.4.1 or higher
    * T2mini-v1.0.0 or higher
	*/
    void commitPrinterBufferWithCallback(in ICallback callback);

	/**
	* With feedback exit buffer print mode
    * commit: whether to submit the buffer content when exiting
    * Supported version: T1mini-v2.4.1 or higher
    * T2mini-v1.0.0 or higher
	*/
    void exitPrinterBufferWithCallback(in boolean commit, in ICallback callback);

    /**
    *   Send a double-line fixed content string
    * string: solidified display string
    * Supported version: T1mini-v2.4.1 or higher
    * T2mini-v1.0.0 or higher
    */
    void sendLCDDoubleString(in String topText, in String bottomText, ILcdCallback callback);

    /**
    *   Custom print image
    * bitmap: image bitmap object (maximum width 384 pixels, pictures over 1M cannot be printed)
    * type: There are currently two printing methods: 0, the same printBitmap 1, the threshold of 200 black and white pictures 2, grayscale pictures
    * Supported version: T1mini-v2.4.1 or higher
    * T2mini-v1.0.0 or higher
    */
    void printBitmapCustom(in Bitmap bitmap, in int type, in ICallback callback);

   /**
   *   Get the status of the cash box
   * return: true the cash box is open false the cash box is closed
   */
   boolean getDrawerStatus();

   /**
   *   Send a single-line solid content string that sets the font size
   * string: display content
   * size: the font size of the displayed content. The larger the font, the less content can be displayed.
   * fill: Whether the font fills the screen height but the width does not change when the font height is not enough true Fills false Not fills up (default)
   * Supported version: T1mini-v4.0.0 or higher
   * T2mini-v4.0.0 or higher
   */
   void sendLCDFillString(in String string, int size, boolean fill, ILcdCallback callback);

   /**
    * Send multiple lines of solid content strings
    * text: Each line of content that is solidified by multiple lines, each line of content can be empty, and only occupy space at this time
    * align: weight of each line of fixed content
    * Supported version: T1mini-v4.0.0 or higher
    * T2mini-v4.0.0 or higher
    */
    void sendLCDMultiString(in String[] text, in int[] align, ILcdCallback callback);
}