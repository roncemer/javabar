// ImageUtils.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.util;

import java.io.*;
import java.util.*;

/**
  * This class contains miscellaneous image processing methods.
  *
  * This class should never be instantiated, since all methods are static.
  *
  * @author Ronald B. Cemer
  */
public class ImageUtils {
	private static final int[][] sobelHorizMatrix = {
		{-1, 0, 1},
		{-2, 0, 2},
		{-1, 0, 1}
	};

	private static final int[][] sobelVertMatrix = {
		{-1, -2, -1},
		{0, 0, 0},
		{1, 2, 1}
	};

	private static final int convolve3x3
		(int[]src, int w, int x, int y, int[][]matrix3x3) {
/// int result = 0;
/// x--;
/// y--;
/// for (int yy = 0; yy <= 2; yy++) {
///     int idx = ((y+yy)*w)+x;
///     for (int xx = 0; xx <= 2; xx++, idx++) {
///     result += src[idx]*matrix3x3[yy][xx];
///     }
/// }
/// return result;
		int xm = x - 1, xp = x + 1;
		int idx1 = y * w;
		int idx0 = idx1 - w, idx2 = idx1 + w;
		return
			(src[idx0 + xm] * matrix3x3[0][0]) +
			(src[idx0 + x] * matrix3x3[0][1]) +
			(src[idx0 + xp] * matrix3x3[0][2]) +
			(src[idx1 + xm] * matrix3x3[1][0]) +
			(src[idx1 + x] * matrix3x3[1][1]) +
			(src[idx1 + xp] * matrix3x3[1][2]) +
			(src[idx2 + xm] * matrix3x3[2][0]) +
			(src[idx2 + x] * matrix3x3[2][1]) +
			(src[idx2 + xp] * matrix3x3[2][2]);
	}

	/**
      * Perform Sobel edge detection on an image.
      * @param src An array containing the monochrome source pixels.  Each
      * element in this array should be in the range of 0-255.
      * @param dest An array to receive the monochrome destination pixels.
      * @param w The width of the image, in pixels.
      * @param h The height of the image, in pixels.
      * @param thresh The threshold for edge detection, in the range of 0-255.
      */
	public static final void sobelEdgeDetect
		(int[]src, int[]dest, int w, int h, int thresh) {

		thresh *= thresh;
		int wMinus1 = w - 1;
		int hMinus1 = h - 1;
		int lastLineOffset = hMinus1 * w;
		for (int x = 0; x < w; x++) {
			dest[x] = 0;
			dest[lastLineOffset + x] = 0;
		}
		int sx, sy;
		for (int y = 1; y < hMinus1; y++) {
			int idx = y * w;
			dest[idx++] = 0;
			for (int x = 1; x < wMinus1; x++, idx++) {
				sx = convolve3x3(src, w, x, y, sobelHorizMatrix);
				sy = convolve3x3(src, w, x, y, sobelVertMatrix);
				if (((sx * sx) + (sy * sy)) >= thresh) {
					dest[idx] = 255;
				} else {
					dest[idx] = 0;
				}
			}
			dest[idx] = 0;
		}
	}

	/**
      * Perform edge enhancement using Sobel edge detection on an image.
      * @param src An array containing the monochrome source pixels.  Each
      * element in this array should be in the range of 0-255.
      * @param dest An array to receive the monochrome destination pixels.
      * @param w The width of the image, in pixels.
      * @param h The height of the image, in pixels.
      * @param thresh The threshold for edge detection, in the range of 0-255.
      * @param intensity The intensity of the edge enhancement (0-255).
      */
	public static final void sobelEnhance
		(int[]src, int[]dest, int w, int h, int thresh, int intensity) {

		thresh *= thresh;
		int wMinus1 = w - 1;
		int hMinus1 = h - 1;
		int lastLineOffset = hMinus1 * w;
		for (int x = 0; x < w; x++) {
			dest[x] = src[x];
			int idx = lastLineOffset + x;
			dest[idx] = idx;
		}
		int sx, sy, pix;
		for (int y = 1; y < hMinus1; y++) {
			int idx = y * w;
			dest[idx] = src[idx];
			idx++;
			int thisLineStartIdx = idx;
			boolean foundEdge = false, prevFoundEdge = false;
			boolean isWhite = false;
			for (int x = 1; x < wMinus1; x++, idx++) {
				sx = convolve3x3(src, w, x, y, sobelHorizMatrix);
				sy = convolve3x3(src, w, x, y, sobelVertMatrix);
				if (((sx * sx) + (sy * sy)) >= thresh) {
					foundEdge = true;
					isWhite = ((sx + sy) > 0);
				}
				if (foundEdge) {
					if (!prevFoundEdge) {
						prevFoundEdge = true;
						for (int idx2 = thisLineStartIdx; idx2 < idx;
							 idx2++) {
							if (!isWhite) {
								pix = src[idx2] + intensity;
							} else {
								pix = src[idx2] - intensity;
							}
							if (pix < 0) pix = 0; else if (pix > 255) pix = 255;
							dest[idx2] = pix;
						}
					}
					if (isWhite) {
						pix = src[idx] + intensity;
					} else {
						pix = src[idx] - intensity;
					}
					if (pix < 0) pix = 0; else if (pix > 255) pix = 255;
					dest[idx] = pix;
				} else {
					dest[idx] = src[idx];
				}
			}
			dest[idx] = src[idx];
		}
	}

	/**
      * Convert an rgb triplet to grayscale.
      * @param rgb An <code>int</code> containing red, green, and blue color
      * values, each in the range of 0-255.  Red must be shifted left 16 bits,
      * green must be shifted left 8 bits, and blue must be unshifted.
      * @return The resulting monochrome luminance value, in the range 0-255.
      */
	public static final int rgbToGrayscale(int pix) {
		int r = (pix >> 16) & 0xff;
		int g = (pix >> 8) & 0xff;
		int b = pix & 0xff;
		int Y = ((r * 306) + (g * 601) + (b * 117)) >> 10;
		if (Y < 0) Y = 0; else if (Y > 255) Y = 255;
		return Y;
	}
}
