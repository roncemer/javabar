// ImageBarCodeScannerDemo.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import com.roncemer.util.*;

/**
  * AWT application to demonstrate the ability to decode bar codes within a
  * scanned image.<p>
  * Usage: java com.roncemer.barcode.ImageBarCodeScannerDemo &lt;image_filename>
  * <br>
  * where &lt;image_filename> is the filename of an image which contains one or
  * more bar codes.
  * @author Ronald B. Cemer
  */
public class ImageBarCodeScannerDemo
	extends Frame {

	private Image image = null;
	private ImageBarCodeScanner scanner = new ImageBarCodeScanner();
	private MyImageCanvas imageCanvas = null;

	class MyImageCanvas extends Canvas {
		private Image image;
		public MyImageCanvas() {
			super();
		}

		public void setImage(Image image) {
			this.image = image;
			repaint();
		}

		public void paint(Graphics g) {
			if (image != null) g.drawImage(image, 0, 0, null);
		}
	}

	public ImageBarCodeScannerDemo() {
		super("Bar code extraction from a scanned image");
		setSize(800, 600);
		ScrollPane scrollPane = new ScrollPane();
		imageCanvas = new MyImageCanvas();
		scrollPane.add(imageCanvas);
		add(scrollPane);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				((Frame) (e.getSource())).hide(); System.exit(0);
			}
		});
		show();
	}

	public void process(String imageFilename) {
		String imageFileUrlString = "file://" + new File(imageFilename).getAbsolutePath();
		try {
			ImageProducer imageProducer =
				(ImageProducer) (new URL(imageFileUrlString).getContent());
			image = createImage(imageProducer);
		} catch(IOException e) {
			e.printStackTrace();
		}
		if (image == null) {
			System.err.println("Cannot find image file at " + imageFileUrlString);
			return;
		}
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 0);
		try { mt.waitForAll(); } catch(InterruptedException e) {}
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		int npix = w * h;
		int[] pixels = new int[npix];
		PixelGrabber grabber = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
		try { grabber.grabPixels(); } catch(InterruptedException e) { e.printStackTrace(); }
		for (int i = 0; i < npix; i++) {
			pixels[i] = ImageUtils.rgbToGrayscale(pixels[i]);
		}
		String[] foundBarCodes = scanner.decodeBarCodesFromImage(pixels, w, h, false, null);
		if (foundBarCodes.length == 0) {
			System.out.println
				("*** No bar codes were found in image " + imageFilename + " ***");
		} else {
			System.out.println
				("The following bar codes were found in image " + imageFilename + ": ");
			for (int i = 0; i < foundBarCodes.length; i++) {
				System.out.println("    [" + foundBarCodes[i] + "]");
			}
		}

// Set to true to show sobel enhanced bar code images.
		if (false) {
			int thresh = 96;
			int intens = 64;
			int[] newPixels = new int[npix];
			ImageUtils.sobelEnhance
				(pixels, newPixels, w, h, thresh, intens);
			for (int i = 0; i < npix; i++) {
				int pix = newPixels[i];
				newPixels[i] = pix | (pix << 8) | (pix << 16) | 0xff000000;
			}
			image = imageCanvas.createImage(new MemoryImageSource(w, h, newPixels, 0, w));
		}

		imageCanvas.setSize(w, h);
		imageCanvas.setImage(image);
	}

	public static void main(String[]args) {
		if (args.length < 1) {
			System.err.println("Please specify one or more image filenames.");
			System.exit(1);
		}
		ImageBarCodeScannerDemo demo = new ImageBarCodeScannerDemo();
		for (int i = 0; i < args.length; i++) {
			demo.process(args[i]);
		}
		System.out.println("done.");
	}
}
