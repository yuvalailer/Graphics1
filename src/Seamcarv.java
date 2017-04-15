import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.IOException;

public class Seamcarv {
	// input:
	// <input image filename> <output # columns> <output # rows> <energy type>
	// <output image filename>
	//
	public static void main(String[] args) throws IOException {
		// parse arguments:
		File INfile = new File(args[1]);
		int newColumns = Integer.parseInt(args[2]);
		int newRows = Integer.parseInt(args[3]);
		int type = Integer.parseInt(args[4]);
		File OUTfile = new File(args[5]);
		SeamMap[] seammap = null; // TODO

		// create image:
		BufferedImage INimg = ImageIO.read(INfile);
		BufferedImage OUTimg = ImageIO.read(OUTfile); // TODO - change to right
														// function.

		// get image dimensions:
		int oldColumns = INimg.getWidth();
		int oldRows = INimg.getHeight();

		// get energy matrix:
		float[][] energyMatrix = energyCal(INimg, type);

		// Calculate map:
		calculateSeamMap(energyMatrix, seammap);

		// remove seams:
		int size = oldColumns - newColumns; // TODO - vertical vs horizontal ?
		if (size > 0) {
			cutSeams(INimg, seammap, size, OUTimg);
		} else {
			addSeams(INimg, seammap, Math.abs(size), OUTimg);
		}
		// write back the new Image:

		// ImageIO.write(OUTimg, formatName, output);

	} // end of main

	private static int[][] getEnergy(BufferedImage iNimg, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void calculateSeamMap(float[][] energyMatrix, SeamMap[] seammap) {
		// calculate seams dynamically:
		for (int i = 0; i < energyMatrix.length; i++) { // Iterate over the
														// map's bottom row.
			seammap[i] = dynamicSeam(energyMatrix, i); // Calculate the seams in
														// a dynamic form
		}
		// sort by energy:
		Arrays.sort(seammap); // sort map by energy
	}

	private static SeamMap dynamicSeam(float[][] energyMatrix, int i) {
		SeamMap ans = null;
		int totalColumnEnergy = 0;
		int[] sortPath = null;

		for (int j = (energyMatrix[0].length - 1); j >= 0; j--) {
			totalColumnEnergy += energyMatrix[i][j];
		}

		ans.value = totalColumnEnergy; // Weight of the entire seam.
		ans.index = i; // TODO - change to top index, not bottom.. // index of
						// the top row seam index.
		ans.way = sortPath; // TODO - to be 0,1,-1 // the path of the shortest
							// seam
		return ans;
	}

	private static void cutSeams(BufferedImage iNimg, SeamMap[] seammap, int size, BufferedImage oUTimg) {
		int width = iNimg.getWidth();
		int hight = iNimg.getHeight();
		int[][] matrix = new int[iNimg.getWidth() - size][iNimg.getHeight()]; // TODO
																				// -
																				// is
																				// this
																				// the
																				// right
																				// dimm
																				// order..
		boolean edit = true;
		for (int i = 0; i < hight; i++)
			for (int j = 0; j < width; j++) { // TODO - dimensions
				for (int j2 = 0; j2 < size; j2++) {
					if (j == seammap[j2].index) {
						edit = false;
					}
				}
				if (edit) {
					matrix[i][j] = iNimg.getRGB(i, j);
				} else {
					continue;
				}
			}

		// oUTimg =

	}

	private static void addSeams(BufferedImage inImg, SeamMap[] seammap, int size, BufferedImage outImg) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0;
		boolean edit = false;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width + size; j++) {
				for (int k = 0; k < size; j++) {
					if (j == seammap[k].index) {
						edit = true;
						break;
					}
				}
				outImg.setRGB(i, j, inImg.getRGB(i, j-diff));
				if (edit == true) {
					j++;
					diff++;
					outImg.setRGB(i, j, inImg.getRGB(i, j-diff));
					edit = false;
				}
			}
		}
	}

	//
	public static float[][] energyCal(BufferedImage img, int type) {
		float[][] energyArr = null;
		int height = img.getHeight();
		int width = img.getWidth();
		int neighbors = 8;
		if (type != 2) {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if ((i == 0 && j == 0) || (i == height - 1 && j == width - 1) || (i == 0 && j == width - 1)
							|| (i == height - 1 && j == 0)) {
						neighbors = 3;
					} else if (i == 0 || i == height - 1 || j == 0 || j == width - 1) {
						neighbors = 5;
					} else
						neighbors = 8;
					energyArr[i][j] = (RGBdiff(img, i, j) / neighbors);
					if (type == 1) {
						energyArr[i][j] += entropy(img, i, j);
					}
				}
			}
		}
		return energyArr;
	}

	public static float RGBdiff(BufferedImage img, int i, int j) {
		int RGB = img.getRGB(i, j);
		int r = (RGB >> 16) & 0xff;
		int g = (RGB >> 8) & 0xff;
		int b = (RGB) & 0xff;
		int r2 = 0, g2 = 0, b2 = 0, RGB2 = 0;
		float sum = 0;

		for (int k = i - 1; k < i + 2; k++) {
			if (k == -1)
				k++;
			if (k > (img.getHeight() - 1))
				continue;

			for (int l = j - 1; l < j + 2; j++) {
				if (l == -1)
					l++;
				if (l > (img.getWidth() - 1))
					continue;

				RGB2 = img.getRGB(k, l);
				r2 = (RGB2 >> 16) & 0xff;
				g2 = (RGB2 >> 8) & 0xff;
				b2 = (RGB2) & 0xff;
				sum += ((Math.abs(r - r2) + Math.abs(g - g2) + Math.abs(b - b2)) / 3);
			}
		}
		return sum;
	}

	public static float entropy(BufferedImage img, int i, int j) {
		float sum = 0, p = 0, h = 0;
		int RGB2, r2 = 0, g2 = 0, b2 = 0, grey2 = 0;
		int RGB = img.getRGB(i, j);
		int r = (RGB >> 16) & 0xff;
		int g = (RGB >> 8) & 0xff;
		int b = (RGB) & 0xff;
		int grey = (r + g + b) / 3;
		for (int k = i - 4; k < i + 5; k++) {
			if (k < 0)
				k = 0;
			if (k > (img.getHeight() - 1))
				break;
			for (int l = j - 4; l < j + 5; j++) {
				if (l < 0)
					l = 0;
				if (l > (img.getWidth() - 1))
					break;
				RGB2 = img.getRGB(k, l);
				r2 = (RGB2 >> 16) & 0xff;
				g2 = (RGB2 >> 8) & 0xff;
				b2 = (RGB2) & 0xff;
				grey2 = (r2 + g2 + b2) / 3;
				sum += grey;

			}
		}
		for (int k = i - 4; k < i + 5; k++) {
			if (k < 0)
				k = 0;
			if (k > (img.getHeight() - 1))
				break;
			for (int l = j - 4; l < j + 5; j++) {
				if (l < 0)
					l = 0;
				if (l > (img.getWidth() - 1))
					break;
				RGB2 = img.getRGB(k, l);
				r2 = (RGB2 >> 16) & 0xff;
				g2 = (RGB2 >> 8) & 0xff;
				b2 = (RGB2) & 0xff;
				grey2 = (r2 + g2 + b2) / 3;
				p = grey2 / sum;
				h += p * Math.log(p);
			}
		}
		h = -h;
		return h;
	}

}
