package com.infusion.jirareconciler.reconciliation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.util.Hashtable;

/**
 * Created by rcieslak on 22/04/2015.
 */
public class IssueIdDecoder {
    private static final String TAG = "IssueIdDecoder";

    public static String[] decode(byte[] lane) {
        return decode(lane, 100);
    }

    public static String[] decode(byte[] lane, int cropPercentage) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(lane, 0, lane.length, opt);
        try {
            Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, "QR_CODE");
            int height = bitmap.getHeight() * cropPercentage / 100;
            int[] pixels = new int[bitmap.getWidth() * height];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, (bitmap.getHeight() - height) / 2, bitmap.getWidth(), height);

            LuminanceSource source = new RGBLuminanceSource(
                    bitmap.getWidth(),
                    height,
                    pixels);

            QRCodeMultiReader multiReader = new QRCodeMultiReader();
            Result[] results = multiReader.decodeMultiple(new BinaryBitmap(new HybridBinarizer(source)), hints);

            String[] issueIds = new String[results.length];
            for (int i = 0; i < results.length; i++) {
                issueIds[i] = results[i].getText();
            }
            Log.d(TAG, "Found: " + results.length);

            return issueIds;
        }
        catch (NotFoundException e) {
            Log.d(TAG, "No codes found");
            return new String[0];
        }
        finally {
            bitmap.recycle();
        }
    }
}