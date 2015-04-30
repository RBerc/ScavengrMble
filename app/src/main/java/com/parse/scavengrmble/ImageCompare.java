package com.parse.scavengrmble;

import android.graphics.Bitmap;
import android.widget.Toast;

/**
 * Created by Kevin on 4/22/2015.
 */
public class ImageCompare {
    // compares the rgb values of two bitmaps and returns a double of the % difference
    // if the images are of the same object, but one picture is shifted, the two will be very different
    public static double compareImagesRGBPixel(Bitmap img1, Bitmap img2) {
        int width1 = img1.getWidth();
        int height1 = img1.getHeight();

        int width2 = img2.getWidth();
        int height2 = img2.getHeight();

        if ((width1 != width2) || (height1 != height2)) {
            // error
        }

        double diff = 0;
        for (int y=0; y<height1; y++) {
            for (int x=0; x<width1; x++) {
                int rgb1 = img1.getPixel(x, y);
                int rgb2 = img2.getPixel(x, y);
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >>  8) & 0xff;
                int b1 = (rgb1      ) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >>  8) & 0xff;
                int b2 = (rgb2      ) & 0xff;
                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
            }
        }
        double n = width1 * height1 * 3;
        double p = diff / n / 255.0;
        return p*100.0;
    }

    public static double compareImagesRGBAll(Bitmap img1, Bitmap img2) {
        int width1 = img1.getWidth();
        int height1 = img1.getHeight();

        int width2 = img2.getWidth();
        int height2 = img2.getHeight();

        if ((width1 != width2) || (height1 != height2)) {
            return 100.0;
        }

        double diff = 0;
        double r1=0, r2=0, g1=0, g2=0, b1=0, b2=0;
        for (int y=0; y<height1; y++) {
            for (int x=0; x<width1; x++) {
                int rgb1 = img1.getPixel(x, y);
                int rgb2 = img2.getPixel(x, y);
                r1 += (rgb1 >> 16) & 0xff;
                g1 += (rgb1 >>  8) & 0xff;
                b1 += (rgb1      ) & 0xff;
                r2 += (rgb2 >> 16) & 0xff;
                g2 += (rgb2 >>  8) & 0xff;
            }
        }
        diff += Math.abs(r1 - r2);
        diff += Math.abs(g1 - g2);
        diff += Math.abs(b1 - b2);
        double n = width1 * height1 * 3;
        double p = diff / n / 255.0;
        return p*100.0;
    }
}
