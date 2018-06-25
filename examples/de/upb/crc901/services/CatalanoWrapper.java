/**
 * CatalanoWrapper.java
 * Copyright (C) 2017 Paderborn University, Germany
 * 
 * @author: Felix Mohr (mail@felixmohr.de)
 */

/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.upb.crc901.services;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Concurrent.Filters.Grayscale;

import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.interfaces.Instance;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.commons.lang3.reflect.MethodUtils;

public class CatalanoWrapper {

	public static Instance applyFilter(final Instance inst, final Object filter) {

		/* check whether filter has applyInPlace method */
		Class<?> clazz = filter.getClass();
		try {
			Method m = MethodUtils.getAccessibleMethod(clazz, "applyInPlace", FastBitmap.class);
			if (m == null)
				m = MethodUtils.getAccessibleMethod(clazz, "ApplyInPlace", FastBitmap.class);

			FastBitmap image = instance2FastBitmap(inst);
			m.invoke(filter, image);
			return fastBitmap2GrayScaledInstance(image);
		} catch (Exception e) {
			e.printStackTrace();
			return inst;
		}
	}

	public static FastBitmap instance2FastBitmap(final Instance inst) {
		int width = inst.get(0).intValue();
		int type = inst.get(1).intValue();

		if (type == 0)
			return new FastBitmap(getGrayscaleImageFromVector(inst, width));
		else if (type == 1)
			return new FastBitmap(getRGBImageFromVector(inst, width));
		throw new UnsupportedOperationException();
	}

	private static int[][][] getRGBImageFromVector(Instance inst, int width) {
		int height = (int) ((inst.getNumberOfColumns() - 2) * 1f / width / 3);
		int[][][] image = new int[(int) height][width][3];
		int row = 0;
		int column = 0;
		int color = 0;
		Iterator<Double> it = inst.iterator();
		it.next();
		it.next();
		while (it.hasNext()) {
			int val = it.next().intValue();
			image[row][column][color++] = val;

			/* switch to next column/row if color/col has reached maximum respectively */
			if (color == 3) {
				color = 0;
				column++;
			}
			if (column == width) {
				column = 0;
				row++;
			}
		}
		return image;
	}

	private static int[][] getGrayscaleImageFromVector(Instance inst, int width) {
		int[][] image = new int[(inst.getNumberOfColumns() - 2) / width][width];
		int row = 0;
		int col = 0;
		Iterator<Double> it = inst.iterator();
		it.next();
		it.next();
		while (it.hasNext()) {
			int val = it.next().intValue();
			image[row][col++] = val;

			/* switch to next row if col has reached width */
			if (col == width) {
				col = 0;
				row++;
			}
		}
		return image;
	}

	public static Instance fastBitmap2Instance(final FastBitmap fb) {
		Instance instance = new SimpleInstanceImpl();
		int[][][] image = fb.toMatrixRGBAsInt();
		instance.add(new Double(fb.getWidth()));
		instance.add(1.0); // code for rgb images
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++) {
				for (int k = 0; k < image[i][j].length; k++) {
					instance.add(new Double(image[i][j][k]));
				}
			}
		}
		return instance;
	}

	public static Instance fastBitmap2GrayScaledInstance(final FastBitmap fb) {

		Instance instance = new SimpleInstanceImpl();
		if (!fb.isGrayscale()) {
			new Grayscale().applyInPlace(fb);
		}
		int[][] image = fb.toMatrixGrayAsInt();
		instance.add(new Double(fb.getWidth()));
		instance.add(0.0); // code for grayscale
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++) {
				instance.add(new Double(image[i][j]));
			}
		}
		return instance;
	}
}
