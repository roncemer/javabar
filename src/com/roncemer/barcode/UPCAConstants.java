// UPCAConstants.java
// Copyright (c) 2002-2010 Ronald B. Cemer
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.

package com.roncemer.barcode;

/**
  * UPC-A and EAN-13 constants.
  */
public interface UPCAConstants {
	public static final int[] startEndCode = { 1, 1, 1 };
	public static final int[] centerDivider = { 1, 1, 1, 1, 1 };
	public static final int[][] codePatterns = {
		{3, 2, 1, 1},			// 0
		{2, 2, 2, 1},			// 1
		{2, 1, 2, 2},			// 2
		{1, 4, 1, 1},			// 3
		{1, 1, 3, 2},			// 4
		{1, 2, 3, 1},			// 5
		{1, 1, 1, 4},			// 6
		{1, 3, 1, 2},			// 7
		{1, 2, 1, 3},			// 8
		{3, 1, 1, 2}			// 9
	};
}
