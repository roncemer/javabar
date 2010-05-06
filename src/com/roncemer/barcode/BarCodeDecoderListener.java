// BarCodeDecoderListener.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

/**
  * Listener interface for <code>BarCodeDecoder</code> class.
  *
  * @author Ronald B. Cemer
  */
public interface BarCodeDecoderListener {
	/**
      * This method gets called each time a bar code is found in the image.
      * @param barCode The decoded bar code.
      * @param decoder The <code>BarCodeDecoder</code> which found the bar
      * code.  Based on the class name of the <code>decoder</code>, you can
      * determine which type of bar code it is.
      * @return <code>true</code> to continue decoding; <code>false</code> to
      * stop.
      */
	public boolean barCodeFound(String barCode, BarCodeDecoder decoder);
}
