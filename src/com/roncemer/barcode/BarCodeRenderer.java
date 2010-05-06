// BarCodeRenderer.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

import java.awt.*;

/**
  * Common interface for bar code renderers which render bar codes to a
  * <code>Graphics</code> surface.
  */
public interface BarCodeRenderer {
	/**
      * Render a bar code to a <code>Graphics</code> surface.
      * @param g The <code>Graphics</code> surface on which to render.
      * @param barCode The bar code.  It must contain only characters in the character set of
      * the bar code format that the implementing renderer is rendering.  All other characters
      * will not be rendered.
      * @param narrowBarWidth The width of a narrow bar.
      * @param barHeight The height of the bars.
      * @param textHeight The height of the text to be renderered under the bar code.  If this is
      * zero, no text will be renderered.  If this is greater than zero, the bar code text is
      * rendered under the bar code.
      */
	public void render(
		Graphics g,
		String barCode,
		int narrowBarWidth,
		int barHeight,
		int textHeight);
}
