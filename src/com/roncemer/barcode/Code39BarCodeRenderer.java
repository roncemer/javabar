// Code39BarCodeRenderer.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/**
  * This class renders Code 3 of 9 bar codes to a <code>Graphics</code> surface.
  */
public class Code39BarCodeRenderer
	implements BarCodeRenderer, Code39Constants {

	/**
      * Render a Code 3 of 9 bar code to a <code>Graphics</code> surface.
      * @param g The <code>Graphics</code> surface on which to render.
      * @param barCode The bar code.  It must contain only characters in the code 3 of 9 character
      * set.  All other characters will not be rendered.
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
		int textHeight) {

		// Convert lowercase characters to uppercase and remove any invalid characters.
		StringBuffer sb = new StringBuffer();
		int len = barCode.length();
		for (int i = 0; i < len; i++) {
			char c = Character.toUpperCase(barCode.charAt(i));
			if (codeChars.indexOf(c) >= 0) {
				sb.append(c);
			}
		} barCode = sb.toString();
		len = barCode.length();

		// Render the bar code.
		int charWidth = narrowBarWidth * 13;
		int x = 0;
		render(g, startEndCode, narrowBarWidth, barHeight, x);
		x += charWidth;
		int firstCharX = x;
		for (int i = 0; i < len; i++) {
			render(
				g,
			   codePatterns[codeChars.indexOf(barCode.charAt(i))],
			   narrowBarWidth,
			   barHeight,
			   x);
			x += charWidth;
		}
		render(g, startEndCode, narrowBarWidth, barHeight, x);
		x += charWidth;

		// Render the text.
		if (textHeight > 0) {
			Font origFont = g.getFont();
			Font font = new Font("Monospaced", Font.PLAIN, textHeight);
			FontMetrics fm = g.getFontMetrics(font);
			AffineTransform trans = new AffineTransform();
			trans.scale(
				(double) (len * charWidth) /
				(double) (fm.stringWidth(barCode)),
				(double) textHeight / (double) fm.getHeight());
			font = font.deriveFont(trans);
			g.setFont(font);
			g.drawString(barCode, firstCharX, barHeight + textHeight);
			g.setFont(origFont);
		}
	}

	private static void render(
		Graphics g,
	   int[]pattern,
	   int narrowBarWidth,
	   int barHeight,
	   int x) {

		for (int i = 0; i < pattern.length; i++) {
			int w = pattern[i] * narrowBarWidth;
			if ((i & 0x01) == 0) {
				g.fillRect(x, 0, w, barHeight);
			}
			x += w;
		}
	}

	public static void main(String[]args) {
		final String barCode = "THIS IS A DEMO OF CODE 3 OF 9 BARCODES";
		final BarCodeRenderer barCodeRenderer =
			new Code39BarCodeRenderer();
		JFrame frame = new JFrame("Code 3 of 9 Bar Code Renderer");
		Container cont = frame.getContentPane();
		cont.setLayout(new BorderLayout());
		JPanel panel = new JPanel() {
			public void paint(Graphics g) {
				Dimension size = getSize();
				g.setColor(getBackground());
				g.fillRect(0, 0, size.width, size.height);
				g.setColor(getForeground());
				g.translate(10, 10);
				barCodeRenderer.render(g, barCode, 1, 80, 10);
			}
		};
		panel.setBackground(Color.WHITE);
		panel.setForeground(Color.BLACK);
		cont.add(panel);
		frame.addWindowListener(new WindowAdapter () {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		});
		Dimension size = new Dimension(640, 480);
		frame.setSize(size);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(
			(screenSize.width - size.width) / 2,
			(screenSize.height - size.height) / 2);
		frame.setVisible(true);
	}
}
