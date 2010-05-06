// UPCABarCodeRenderer.java
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
  * This class renders UPCA bar codes to a <code>Graphics</code> surface.
  */
public class UPCABarCodeRenderer
	implements BarCodeRenderer, UPCAConstants {

	/**
      * Render a UPCA bar code to a <code>Graphics</code> surface.
      * @param g The <code>Graphics</code> surface on which to render.
      * @param barCode The bar code.  It must contain only digits.  All other characters will not
      * be rendered.  If the bar code is shorter than 11 digits, it will be padded on the left
      * with zeroes to bring the length up to 11 digits.  If the bar code is longer than 11 digits,
      * it will be truncated to 11 digits.  The check digit will be automatically calculated, so
      * it is not necessary to include it at the end.  If the check digit is included (for a total
      * of 12 digits), it will be ignored.
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

		if (textHeight < 0) textHeight = 0;
		int textBaseline = barHeight + textHeight;
		int charWidth = 7 * narrowBarWidth;
		int charsWidth = 12 * charWidth;
		int startEndCodeWidth = startEndCode.length * narrowBarWidth;
		int centerDividerWidth = centerDivider.length * narrowBarWidth;
		int totalWidth = startEndCodeWidth + charsWidth + centerDividerWidth + startEndCodeWidth;

		Font origFont = null, smallFont = null, largeFont = null;
		FontMetrics smallFontMetrics = null, largeFontMetrics = null;
		if (textHeight > 0) {
			origFont = g.getFont();
			largeFont = new Font("Monospaced", Font.PLAIN, textHeight);
			largeFontMetrics = g.getFontMetrics(largeFont);
			AffineTransform trans = new AffineTransform();
			trans.scale(
				(double)charsWidth/(double)(largeFontMetrics.stringWidth(barCode.substring(1, 11))),
				(double)textHeight / (double)largeFontMetrics.getHeight());
			 largeFont = largeFont.deriveFont(trans);
			 trans = new AffineTransform();
			 trans.scale(0.75, 0.75);
			 smallFont = largeFont.deriveFont(trans);
			 smallFontMetrics = g.getFontMetrics(smallFont);
		}
		// Toss all but the first 11 numeric digits.
		StringBuffer sb = new StringBuffer();
		int len = barCode.length();
		for (int i = 0; i < len; i++) {
			char c = barCode.charAt(i);
			if ("0123456789".indexOf(c) >= 0) {
				sb.append(c);
				if (sb.length() == 11) {
					break;
				}
			}
		}
		// Add leading zeroes if fewer than 11 digits.
		while (sb.length() < 11) {
			sb.insert(0, '0');
		}
		// Append check digit.
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
		sb.append(Integer.toString(cs));
		barCode = sb.toString();

		// Render the bar code.
		int x = 0;
		if (textHeight > 0) {
			g.setFont(smallFont);
			String s = barCode.substring(0, 1);
			g.drawString(s, x, textBaseline);
			x += smallFontMetrics.stringWidth(s) + (narrowBarWidth * 2);
		}
		boolean black = true;
		black = render(g, startEndCode, narrowBarWidth, barHeight + (textHeight / 2), x, black);
		x += startEndCodeWidth;
		if (textHeight > 0) {
			g.setFont(largeFont);
			g.drawString(barCode.substring(1, 6), x, textBaseline);
		}
		for (int i = 0; i < 12; i++) {
			if (i == 6) {
				black = render
					(g, centerDivider, narrowBarWidth, barHeight + (textHeight / 2), x, black);
				x += centerDividerWidth;
				if (textHeight > 0) {
					g.setFont(largeFont);
					g.drawString(barCode.substring(6, 11), x, textBaseline);
				}
			}
			black = render
				(g, codePatterns[barCode.charAt(i) - '0'], narrowBarWidth, barHeight, x, black);
			x += charWidth;
		}
		black = render(g, startEndCode, narrowBarWidth, barHeight + (textHeight / 2), x, black);
		x += startEndCodeWidth;
		if (textHeight > 0) {
			x += narrowBarWidth * 2;
			g.setFont(smallFont);
			String s = barCode.substring(11, 12);
			g.drawString(s, x, textBaseline);
			x += smallFontMetrics.stringWidth(s);
			g.setFont(origFont);
		}
	}

	private static boolean render(
		Graphics g,
		int[] pattern,
		int narrowBarWidth,
		int barHeight,
		int x,
		boolean black) {

		for (int i = 0; i < pattern.length; i++) {
			int w = pattern[i] * narrowBarWidth;
			if (black) {
				g.fillRect(x, 0, w, barHeight);
			}
			black = !black;
			x += w;
		}
		return black;
	}

	public static void main(String[]args) {
		final String barCode = "12345678901";
		final BarCodeRenderer barCodeRenderer = new UPCABarCodeRenderer();
		JFrame frame = new JFrame("UPCA Bar Code Renderer");
		Container cont = frame.getContentPane();
		cont.setLayout(new BorderLayout());
		JPanel panel = new JPanel() {
			public void paint(Graphics g) {
				Dimension size = getSize();
				g.setColor(getBackground());
				g.fillRect(0, 0, size.width, size.height);
				g.setColor(getForeground());
				g.translate(10, 10);
				barCodeRenderer.render(g, barCode, 1, 80, 12);
			}
		};
		panel.setBackground(Color.  WHITE);
		panel.setForeground(Color.  BLACK);
		cont.add(panel);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		});
		Dimension size = new Dimension(640, 480);
		frame.setSize(size);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(
			(screenSize.width - size.width) / 2,
			(screenSize.height - size.height) / 2
		);
		frame.setVisible(true);
	}
}
