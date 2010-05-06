// Code39BarCodeDecoder.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

import java.util.*;

/**
  * Code 3 of 9 bar code decoding class.
  * This class performs Code39 barcode decoding using relative widths
  * of alternating bars and spaces, starting with the width of the first
  * suspected bar.
  *
  * @author Ronald B. Cemer
  */
public class Code39BarCodeDecoder
	extends BarCodeDecoder
	implements Code39Constants {

	/**
      * Decode a Code39 barcode given relative widths of alternating bars and
      * spaces, starting with the width of the first suspected bar.
      * @param widths The relative widths of alternating bars and spaces,
      * starting with the width of the first suspected bar.  There must be at
      * least 19 elements in this array.
      * @param numBarsAndSpaces The total number of bars and spaces.Must be
      * less than or equal to <code>widths.length</code>.  This allows
      * applications to pre-allocate a buffer of a fixed size, and to load
      * widths into the front of the buffer, thus making the buffer re-usable.
      * NOTE: There must be at least 19 total bars and spaces in a Code39 bar
      * code.
      * @param includeCheckDigits <code>true</code> to return check digits;
      * <code>false</code> to strip them off.  Since Code39 format does not
      * include check digits, this argument has no meaning.
      * @param listener The <code>BarCodeDecoderListener</code> to be notified
      * each time a bar code is decoded, or <code>null</code> if none.
      * @return A <code>String</code> array containing all decoded bar codes.
      * This array will be of zero length if no bar codes were successfully
      * decoded.
      */
	public String[] decode(
		int[] widths,
		int numBarsAndSpaces,
		boolean includeCheckDigits,
		BarCodeDecoderListener listener) {

		abortedByListener = false;
		int[] widthThresh = new int[6];
		int[] charBarWidths = new int[10];
		ArrayList barCodes = new ArrayList();
		StringBuffer sb = new StringBuffer(32);
		// A space on the end doesn't do us any good.
		// Since the first width is always a bar, we need an odd number
		// of widths in order to also end on a bar.
		if ((numBarsAndSpaces & 0x01) == 0) numBarsAndSpaces--;
		int nMinus1 = numBarsAndSpaces - 1;
		for (int dir = 0; dir < 2; dir++) {
			boolean reversed = (dir > 0);
			for (int startIdx = 0; (startIdx + 19) <= numBarsAndSpaces; startIdx += 2) {
				// Calculate width thresholds based on first bar and
				// look for start code.
				if (reversed) {
					int revIdx = nMinus1 - startIdx;
					calculateWidthThresh(widthThresh, widths, revIdx, startEndCode, true);
					getNextSamplesReverse(charBarWidths, widthThresh, widths, revIdx, 10);
				} else {
					calculateWidthThresh(widthThresh, widths, startIdx, startEndCode, false);
					getNextSamples(charBarWidths, widthThresh, widths, startIdx, 10);
				}
///for (int i = 0; i < 10; i++) System.out.print(charBarWidths[i]);
///System.out.println();
				if (!compare(charBarWidths, startEndCode, 10)) {
					continue;
				}
///System.out.println("*** FOUND START CODE reversed="+reversed);
				for (int i = startIdx + 10; (i + 9) <= numBarsAndSpaces; i += 10) {
					// Look for end code.
					if (reversed) {
						int revIdx = nMinus1 - i;
						getNextSamplesReverse(charBarWidths, widthThresh, widths, revIdx, 9);
					} else {
						getNextSamples(charBarWidths, widthThresh, widths, i, 9);
					}
					if (compare(charBarWidths, startEndCode, 9)) {
///System.out.println("*** FOUND END CODE reversed="+reversed);
						if (sb.length() > 0) {
							String bc = sb.toString();
							barCodes.add(bc);
							if (listener != null) {
								if (!listener.barCodeFound(bc, this)) {
									abortedByListener = true;
									dir = 2;
									startIdx = numBarsAndSpaces;
									break;
								}
							}
						}
						sb.setLength(0);
						startIdx = (i + 10) & 0xfffffffe;
						break;
					}
					if ((i + 9) >= numBarsAndSpaces) break;
					if (reversed) {
						charBarWidths[9] =
							normalizeWidth(widthThresh, widths[nMinus1 - (i + 9)], false);
					} else {
						charBarWidths[9] =
							normalizeWidth(widthThresh, widths[i + 9], false);
					}
///for (int j = 0; j < 10; j++) System.out.print(charBarWidths[j]);
///System.out.println();
					boolean validChar = false;
					for (int j = 0; j < codePatterns.length; j++) {
						if (compare(charBarWidths, codePatterns[j], 10)) {
							validChar = true;
							sb.append(codeChars.charAt(j));
///
///System.out.println(sb.toString());
						}
					}
					if (!validChar) {
///
///if (sb.length() > 0) {
///    System.out.print("                ");
///    for (int j = 0; j < 10; j++) System.out.print(charBarWidths[j]);
///    System.out.println();
///}
						sb.setLength(0);
						break;
					}
///         if (reversed) {
///         int revIdx = nMinus1-i;
///         calculateWidthThresh
///             (widthThresh, widths, revIdx, charBarWidths, true);
///         } else {
///         calculateWidthThresh
///             (widthThresh, widths, i, charBarWidths, false);
///         }
				}
			}					// for (int startIdx = 0; ...
		}						// for (int dir = 0; dir < 2; dir++)
		String[]result = new String[barCodes.size()];
		barCodes.toArray(result);
		return result;
	}

	private final void getNextSamples
		(int[]dest, int[]widthThresh, int[]widths, int startIdx, int n) {

		int i = 0;
		while ((i + 1) < n) {
			dest[i++] = normalizeWidth(widthThresh, widths[startIdx++], true);
			dest[i++] = normalizeWidth(widthThresh, widths[startIdx++], false);
		}
		if (i < n) {
			dest[i++] = normalizeWidth(widthThresh, widths[startIdx++], true);
		}
	}

	private final void getNextSamplesReverse
		(int[]dest, int[]widthThresh, int[]widths, int startIdx, int n) {

		int i = 0;
		while ((i + 1) < n) {
			dest[i++] = normalizeWidth(widthThresh, widths[startIdx--], true);
			dest[i++] = normalizeWidth(widthThresh, widths[startIdx--], false);
		}
		if (i < n) {
			dest[i++] = normalizeWidth(widthThresh, widths[startIdx--], true);
		}
	}

	private final int divRound(int num, int denom) {
		return (num + (denom / 2)) / denom;
	}

	private final void calculateWidthThresh(
		int[]widthThresh,
		int[]widths,
		int startIdx,
		int[]code,
		boolean reversed) {

		int wideBarWidth = 0, wideSpaceWidth = 0;
		int dir = reversed ? -1 : 1;
		for (int i = 0; i < 10;) {
			// bar
			int sample = widths[startIdx];
			startIdx += dir;
			if (code[i++] == 1)
				sample *= 2;
			wideBarWidth += sample;
			// space
			sample = widths[startIdx];
			startIdx += dir;
			if (code[i++] == 1)
				sample *= 2;
			wideSpaceWidth += sample;
		}
		wideBarWidth = divRound(wideBarWidth, 5);
		wideSpaceWidth = divRound(wideSpaceWidth, 5);
		int ref = wideBarWidth;
		for (int i = 0; i < 6; i += 3) {
			widthThresh[i] = divRound(ref, 4);
			widthThresh[i + 1] = divRound((ref * 3), 4);
			widthThresh[i + 2] = ref + divRound(ref, 2);
			ref = wideSpaceWidth;
		}
	}

	private final int normalizeWidth(int[]widthThresh, int width, boolean isBar) {

		int ofs = (isBar ? 0 : 3);
		if (width < widthThresh[ofs]) return 0;
		if (width >= widthThresh[ofs + 2]) return 3;
		if (width >= widthThresh[ofs + 1]) return 2;
		return 1;
	}

	private final boolean compare(int[]charBarWidths, int[]candidate, int n) {
		for (int i = 0; i < n; i++) {
			if (charBarWidths[i] != candidate[i]) return false;
		}
		return true;
	}

/*
    public static void main(String[] args) {
		int[] widths = new int[] {
	    	1,0,1,1,1,1,0,1,0,0,// leading garbage (must be even # of entries)
	    	1,1,1,	// start code
	    	3,2,1,1,	// 0
	    	1,1,3,2,	// 4
	    	1,4,1,1,	// 3
	    	3,2,1,1,	// 0
	    	3,2,1,1,	// 0
	    	3,2,1,1,	// 0
	    	1,1,1,1,1,	// middle divider
	    	2,2,2,1,	// 1
	    	1,2,1,3,	// 8
	    	2,2,2,1,	// 1
	    	1,3,1,2,	// 7
	    	3,2,1,1,	// 0
	    	1,1,1,4,	// 6
	    	1,1,1,	// stop code
	    	1,0,1,1,1,1,0,1,0,0,// trailing garbage (must be even # of entries)
		};
		int[] revWidths = new int[widths.length];
		for (int i = 0; i < widths.length; i++) revWidths[(widths.length-1)-i] = widths[i];
		Code39BarCodeDecoder decoder = new Code39BarCodeDecoder();
		for (int pass = 0; pass < 8; pass++) {
	    	System.out.println(decoder.decode(widths, true, null));
	    	System.out.println(decoder.decode(revWidths, true, null));
	    	for (int i = 0; i < widths.length; i++) {
				widths[i] *= 2;
				revWidths[i] *= 2;
	    	}
		}
    }
*/
}
