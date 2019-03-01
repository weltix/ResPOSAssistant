package com.respos.android.assistant.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Map;

public class BitmapMaker {
    private final Map<Byte, Bitmap> charsCP866Map;

    public BitmapMaker(Bitmap charactersCP866Bitmap, int indicatorCharWidth){
        charsCP866Map = getCharsCP866Map(charactersCP866Bitmap, indicatorCharWidth);
    }

    /**
     * @param sourceBitmap must contain row of all symbols from CP866 code table in range
     *                     (0x20-0xAF)&(0xE0-0xFF) in one line with full height of one print place
     */
    public Map<Byte, Bitmap> getCharsCP866Map(Bitmap sourceBitmap, int charWidth) {
        int charHeight = sourceBitmap.getHeight();
        int x = 0;
        byte charNum = 0x20;    // start from Space symbol
//        0xB0 = byte(-80)
        for (; charNum != -80; charNum++, x += charWidth) {
            Bitmap charBitmap = Bitmap.createBitmap(sourceBitmap, x, 0, charWidth, charHeight);
            charsCP866Map.put(charNum, charBitmap);
        }
        charNum += 48;          // skip 48 useless symbols
//        (0xFF = byte(-1)) + 1 = 0
        for (; charNum != 0; charNum++, x += charWidth) {
            Bitmap charBitmap = Bitmap.createBitmap(sourceBitmap, x, 0, charWidth, charHeight);
            charsCP866Map.put(charNum, charBitmap);
        }
        return charsCP866Map;
    }

    public Bitmap getOutputBitmap(Bitmap sourceBitmap, byte[] bytesData) {
        Canvas canvas = new Canvas(sourceBitmap);
        int startPixelLeft = 0;
        int startPixelTop = 0;
        int charWidth = charsCP866Map.get((byte) 0x20).getWidth();    // get from Space char
        int charHeight = charsCP866Map.get((byte) 0x20).getHeight();  // get from Space char
        int lineLength = sourceBitmap.getWidth() / charWidth;
        for (int i = 0; i < bytesData.length; startPixelLeft = 0, startPixelTop += charHeight) {
            for (int j = 0; j < lineLength; j++, startPixelLeft += charWidth) {
                Bitmap charBitmap = charsCP866Map.get(bytesData[i]);
                if (charBitmap == null)
                    charBitmap = charsCP866Map.get((byte) 0x20);  // if null, then Space char is used
                try {
                    canvas.drawBitmap(charBitmap, startPixelLeft, startPixelTop, null);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    break;
                }
                i++;
            }
        }
        Bitmap outputBitmap = sourceBitmap;
        return outputBitmap;
    }
}
