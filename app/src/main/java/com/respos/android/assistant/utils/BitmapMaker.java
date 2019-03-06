/*
 * Copyright (c) RESONANCE JSC, Bludov Dmitriy, 05.03.2019
 */

package com.respos.android.assistant.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.HashMap;
import java.util.Map;

/*
 * Class for creating bitmaps that show receipt view or indicator view as a picture.
 * Monospace fonts may be used only.
 * Necessary data:
 *   1. Bitmap with letters.
 *   2. Char width in pixels.
 *   3. Dimensions for ready picture (bottom may has undetermined margin).
 *   4. Text data (in bytes), from which picture is building.
 * */

public class BitmapMaker {
    private final Map<Byte, Bitmap> charsCP866Map = new HashMap<>();

    public BitmapMaker(Bitmap charactersCP866Bitmap, int charWidth) {
        getCharsCP866Map(charactersCP866Bitmap, charWidth);
    }

    /**
     * @param sourceBitmap must contain row of all symbols from CP866 code table in range
     *                     (0x20-0xAF)&(0xE0-0xFF) in one line with full height of one print place
     */
    public void getCharsCP866Map(Bitmap sourceBitmap, int charWidth) {
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
    }

    /**
     * @param sourceBitmap "sheet" with certain dimensions for painting on it.
     * @param isShrinkable true - image will be truncated from below, if it has enough margin
     *                     and free space left below. It may be actual for receipts.
     *                     false - for indicator with stable dimensions, for example.
     */
    public Bitmap getOutputBitmap(Bitmap sourceBitmap, boolean isShrinkable, byte[] bytesData) {
        Canvas canvas = new Canvas(sourceBitmap);

        int charWidth = charsCP866Map.get((byte) 0x20).getWidth();    // get from Space char
        int charHeight = charsCP866Map.get((byte) 0x20).getHeight();  // get from Space char
        int firstPixX = 0;
        int firstPixY = 0;
        int lineLength = sourceBitmap.getWidth() / charWidth;         // chars number

        for (int i = 0; i < bytesData.length; firstPixX = 0, firstPixY += charHeight) {
            for (int j = 0; j < lineLength; j++, firstPixX += charWidth) {
                Bitmap charBitmap;
                try {
                    charBitmap = charsCP866Map.get(bytesData[i]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    charBitmap = null;  // also equals null if we try to get Bitmap for unknown char from our map
                }
                if (charBitmap == null)
                    charBitmap = charsCP866Map.get((byte) 0x20);  // if null, then Space char is used
                canvas.drawBitmap(charBitmap, firstPixX, firstPixY, null);
                i++;
            }
        }

        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        if (isShrinkable) {
            if ((firstPixY < height) && (firstPixY > 0))
                height = firstPixY;
        }
        Bitmap outputBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, width, height);

        return outputBitmap;
    }
}