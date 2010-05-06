// BarCodeDecoder.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

/**
  * Abstract bar code decoding class.
  * Subclasses of this class perform barcode decoding using relative widths
  * of alternating bars and spaces, starting with the width of the first
  * suspected bar.
  *
  * @author Ronald B. Cemer
  */
public abstract class BarCodeDecoder {
	/**
      * Subclasses can set this to <code>true</code> to indicate that the
      * listener aborted the scan after finding a bar code.
      */
	protected boolean abortedByListener = false;

	/**
      * Decode a barcode given relative widths of alternating bars and spaces,
      * starting with the width of the first suspected bar.
      * @param widths The relative widths of alternating bars and spaces,
      * starting with the width of the first suspected bar.
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
		boolean includeCheckDigits,
		BarCodeDecoderListener listener) {

		return decode(widths, widths.length, includeCheckDigits, listener);
	}
	/**
      * Decode a barcode given relative widths of alternating bars and spaces,
      * starting with the width of the first suspected bar.
      * @param widths The relative widths of alternating bars and spaces,
      * starting with the width of the first suspected bar.
      * @param numBarsAndSpaces The total number of bars and spaces.  Must be
      * less than or equal to <code>widths.length</code>.  This allows
      * applications to pre-allocate a buffer of a fixed size, and to load
      * widths into the front of the buffer, thus making the buffer re-usable.
      * @param includeCheckDigits <code>true</code> to return check digits;
      * <code>false</code> to strip them off.
      * @param listener The <code>BarCodeDecoderListener</code> to be notified
      * each time a bar code is decoded, or <code>null</code> if none.
      * @return A <code>String</code> array containing all decoded bar codes.
      * This array will be of zero length if no bar codes were successfully
      * decoded.
      */
	public abstract String[] decode(
		int[]widths,
		int numBarsAndSpaces,
		boolean includeCheckDigits,
		BarCodeDecoderListener listener
	);

	/**
      * @return <code>true</code> if the listener aborted the last scan, or
      * <code>false</code> if not.
      */
	public boolean getAbortedByListener() {
		return abortedByListener;
	}
}
