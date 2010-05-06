// UPCABarCodeDecoder.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

import java.util.*;

/**
  * UPC-A and EAN-13 bar code decoding class.
  * This class performs UPC-A and EAN-13 barcode decoding using relative widths
  * of alternating bars and spaces, starting with the width of the first
  * suspected bar.
  * Note that if the decoded bar code is in EAN-13 format, the returned bar code
  * will not contain the extra leading zero.  This is because the bars are
  * identical between both formats, so there is no way for the decoder to tell
  * which of the two formats the bar code is.
  *
  * @author Ronald B. Cemer
  */
public class UPCABarCodeDecoder
	extends BarCodeDecoder
	implements UPCAConstants {

	/**
      * Decode a UPC-A barcode given relative widths of alternating bars and
      * spaces, starting with the width of the first suspected bar.
      * @param widths The relative widths of alternating bars and spaces,
      * starting with the width of the first suspected bar.  There must be at
      * least 59 elements in this array.
      * @param numBarsAndSpaces The total number of bars and spaces.Must be
      * less than or equal to <code>widths.length</code>.  This allows
      * applications to pre-allocate a buffer of a fixed size, and to load
      * widths into the front of the buffer, thus making the buffer re-usable.
      * NOTE: There must be at least 59 total bars and spaces in a UPC-A bar
      * code.
      * @param includeCheckDigits <code>true</code> to return check digits;
      * <code>false</code> to strip them off.
      * @param listener The <code>BarCodeDecoderListener</code> to be notified
      * each time a bar code is decoded, or <code>null</code> if none.
      * @return A <code>String</code> array containing all decoded bar codes.
      * This array will be of zero length if no bar codes were successfully
      * decoded.
      */
	public String[] decode(
		int[]widths,
		int numBarsAndSpaces,
		boolean includeCheckDigits,
		BarCodeDecoderListener listener) {

		abortedByListener = false;
		int[] widthThresh = new int[10];
		int[] charWidths = new int[4];
		ArrayList barCodes = new ArrayList();
		StringBuffer sb = new StringBuffer(12);
		for (int startIdx = 0; (startIdx + 59) <= numBarsAndSpaces; startIdx += 2) {
			// Calculate width thresholds based on start code.
			calculateWidthThresh(widthThresh, widths, startIdx, startEndCode);
			// Look for start code.
			if (normalizeWidth(widthThresh, widths[startIdx + 1], false) != 1) continue;
			if (normalizeWidth(widthThresh, widths[startIdx + 2], true) != 1) continue;
			sb.setLength(0);
			int idx = startIdx + 3;
			boolean reversed = false;
			boolean allValidChars = true;
			for (int charCount = 0; charCount < 12; charCount++) {
				if (charCount == 6) {
					// Check for center 1-1-1-1-1 pattern.
					for (int i = 0; i < 5; i++, idx++) {
						if (normalizeWidth
							(widthThresh, widths[idx],
							 ((idx & 0x01) == 0)) != 1) {
							allValidChars = false;
							break;
						}
					}
					if (!allValidChars) break;
///         calculateWidthThresh
///         (widthThresh, widths, idx, centerDivider);
				}
				for (int i = 0; i < 4; i++, idx++) {
					charWidths[i] = normalizeWidth(widthThresh, widths[idx], ((idx & 0x01) == 0));
				}
				boolean validChar = false;
				for (int i = 0; i <= 9; i++) {
					validChar = true;
					if ((reversed) || (charCount == 0)) {
						boolean validCharReversed = true;
						for (int j = 0; j < 4; j++) {
							if (charWidths[j] != codePatterns[i][3 - j]) {
								if (reversed) validChar = false;
								validCharReversed = false;
								break;
							}
						}
						if (validCharReversed) reversed = true;
					}
					if (!reversed) {
						for (int j = 0; j < 4; j++) {
							if (charWidths[j] != codePatterns[i][j]) {
								validChar = false;
								break;
							}
						}
					}
					if (validChar) {
						sb.append((char) ((int) '0' + i));
///
///if ( (sb.length() >= 7) && (sb.toString().startsWith("01111085358")) )
///if (sb.length() >= 3)
///    System.out.println(sb.toString());
						break;
					}
				}				// for (int i = 0; i <= 9; i++)
				if (!validChar) {
///
///if ( (sb.length() >= 7) && (sb.toString().startsWith("01111085358")) ) {
///if (sb.length() >= 3) {
///    System.out.print("                ");
///    for (int i = 0; i < 4; i++) System.out.print(charWidths[i]);
///    System.out.println();
///}
					allValidChars = false;
					break;
				}
///int[] save = new int[10];
///System.arraycopy(widthThresh, 0, save, 0, 10);
///     calculateWidthThresh(widthThresh, widths, idx-4, charWidths);
///boolean changed = false;
///for (int i = 0; i < 10; i++) {
///    if (Math.abs(widthThresh[i]-save[i]) >= 3) changed = true;
///}
///if (changed) {
///    System.out.print("                     w:");
///    for (int i = 0; i < 10; i++) {
/// System.out.print(" "+save[i]+":"+widthThresh[i]);
///    }
///    System.out.println();
///}
///System.arraycopy(save, 0, widthThresh, 0, 10);
			}
			if (!allValidChars) continue;
			if (normalizeWidth(widthThresh, widths[idx], ((idx & 0x01) == 0)) != 1) continue;
			idx++;
			if (normalizeWidth(widthThresh, widths[idx], ((idx & 0x01) == 0)) != 1) continue;
			idx++;
			if (normalizeWidth(widthThresh, widths[idx], ((idx & 0x01) == 0)) != 1) continue;
			idx++;
			if (reversed) sb.reverse();
			int csOdd =
				(sb.charAt(0) - '0') +
				(sb.charAt(2) - '0') +
				(sb.charAt(4) - '0') +
				(sb.charAt(6) - '0') +
				(sb.charAt(8) - '0') + (sb.charAt(10) - '0');
			int csEven =
				(sb.charAt(1) - '0') +
				(sb.charAt(3) - '0') +
				(sb.charAt(5) - '0') +
				(sb.charAt(7) - '0') + (sb.charAt(9) - '0');
			int cs = (10 - (((csOdd * 3) + csEven) % 10)) % 10;
			if (cs == (sb.charAt(11) - '0')) {
				if (!includeCheckDigits) sb.setLength(11);
				String bc = sb.toString();
				barCodes.add(bc);
				if (listener != null) {
					if (!listener.barCodeFound(bc, this)) {
						abortedByListener = true;
						startIdx = numBarsAndSpaces;
						break;
					}
				}
				startIdx = (idx - 2) & 0xfffffffe;
			}
		}						// for (int startIdx = 0; ...
		String[]result = new String[barCodes.size()];
		barCodes.toArray(result);
		return result;
	}

	private final int divRound(int num, int denom) {
		return (num + (denom / 2)) / denom;
	}

	private final void calculateWidthThresh(
		int[]widthThresh,
		int offset,
		int totalWidths,
		int totalNormWidths) {

		int halfWidth = divRound(totalWidths, (totalNormWidths * 2));
		if (halfWidth < 1) halfWidth = 1;
		// 0.5 of calculated narrow width is too narrow to be valid.
		widthThresh[offset] = halfWidth;
		// 1.5 or more of calculated narrow width is considered 2-wide.
		widthThresh[offset + 1] = divRound(totalWidths, totalNormWidths) + halfWidth;
		// 2.5 or more of calculated narrow width is considered 3-wide.
		widthThresh[offset + 2] = divRound((totalWidths * 2), totalNormWidths) + halfWidth;
		// 3.5 or more of calculated narrow width is considered 4-wide.
		widthThresh[offset + 3] = divRound((totalWidths * 3), totalNormWidths) + halfWidth;
		// 5 or more of calculated narrow width is too wide to be valid.
		widthThresh[offset + 4] = divRound((totalWidths * 5), totalNormWidths);
	}

	private final void calculateWidthThresh
		(int[]widthThresh, int[]widths, int startIdx, int[]code) {

		boolean odd = ((startIdx & 0x01) != 0);
		int nWidths = widths.length, nCodes = code.length;
		int w = 0, c = 0;
		// Bar if even; space if odd.
		for (int i = 0, idx = startIdx; ((i < nCodes) && (idx < nWidths)); i += 2, idx += 2) {
			w += widths[idx];
			c += code[i];
		}
		calculateWidthThresh(widthThresh, odd ? 5 : 0, w, c);
		// Space if even; bar if odd.
		w = c = 0;
		for (int i = 1, idx = startIdx + 1; ((i < nCodes) && (idx < nWidths)); i += 2, idx += 2) {
			w += widths[idx];
			c += code[i];
		}
		calculateWidthThresh(widthThresh, odd ? 0 : 5, w, c);
	}

	private final int normalizeWidth(int[]widthThresh, int width, boolean isBar) {
		int ofs = (isBar) ? 0 : 5;
		if (width < widthThresh[ofs]) return 0;			// too narrow
		if (width >= widthThresh[ofs + 4]) return 5;	// too wide
		if (width >= widthThresh[ofs + 3]) return 4;
		if (width >= widthThresh[ofs + 2]) return 3;
		if (width >= widthThresh[ofs + 1]) return 2;
		return 1;
	}

	public static void main(String[]args) {
		int[] widths = new int[] {
			1, 0, 1, 1, 1, 1, 0, 1, 0, 0,	// leading garbage (must be even # of entries)
			1, 1, 1,			// start code
			3, 2, 1, 1,			// 0
			1, 1, 3, 2,			// 4
			1, 4, 1, 1,			// 3
			3, 2, 1, 1,			// 0
			3, 2, 1, 1,			// 0
			3, 2, 1, 1,			// 0
			1, 1, 1, 1, 1,		// middle divider
			2, 2, 2, 1,			// 1
			1, 2, 1, 3,			// 8
			2, 2, 2, 1,			// 1
			1, 3, 1, 2,			// 7
			3, 2, 1, 1,			// 0
			1, 1, 1, 4,			// 6
			1, 1, 1,			// stop code
			1, 0, 1, 1, 1, 1, 0, 1, 0, 0,	// trailing garbage (must be even # of entries)
		};
		int[] revWidths = new int[widths.length];
		for (int i = 0; i < widths.length; i++) revWidths[(widths.length - 1) - i] = widths[i];
		UPCABarCodeDecoder decoder = new UPCABarCodeDecoder();
		for (int pass = 0; pass < 8; pass++) {
			String[]barCodes = decoder.decode(widths, true, null);
			for (int i = 0; i < barCodes.length; i++) {
				System.out.println(barCodes[i]);
			}
			barCodes = decoder.decode(revWidths, true, null);
			for (int i = 0; i < barCodes.length; i++) {
				System.out.println(barCodes[i]);
			}
			for (int i = 0; i < widths.length; i++) {
				widths[i] *= 2;
				revWidths[i] *= 2;
			}
		}
	}
}
