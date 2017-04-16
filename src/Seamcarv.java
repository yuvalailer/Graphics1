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
		File INfile = (new File(args[0]));
		int newColumns = Integer.parseInt(args[1]);
		int newRows = Integer.parseInt(args[2]);
		int type = Integer.parseInt(args[3]);
		File OUTfile = new File(args[4]);
		boolean fd = OUTfile.createNewFile();
		// TODO err

		// create image:
		BufferedImage INimg = ImageIO.read(INfile);

		// get image dimensions:
		int oldColumns = INimg.getWidth();
		int oldRows = INimg.getHeight();
		SeamMap[] seammap = new SeamMap[oldColumns];

		System.out.println("procedure initiated");
		System.out.println("old photo size - " + oldColumns + "X" + oldRows);
		System.out.println("new photo size - " + newColumns + "X" + newRows);

		// get energy matrix:
		float[][] energyMatrix = energyCal(INimg, type);

		// Calculate map:
		calculateSeamMap(energyMatrix, seammap);

		// create outImge:
		BufferedImage colIMG = new BufferedImage(newColumns, oldRows, INimg.getType());
		
		int colSize = oldColumns - newColumns;
		int rowSize = oldRows - newRows;
		
		// remove seams:
		if (colSize >= 0) {
			cutSeams(INimg, seammap, colSize, colIMG);
		} else if (colSize < 0) {
			addSeams(INimg, seammap, Math.abs(colSize), colIMG);
		}
		
		BufferedImage OUTimg = new BufferedImage(newColumns, newRows, INimg.getType());
		if (rowSize != 0) {
			
			BufferedImage transIMG = new BufferedImage(oldRows, newColumns, INimg.getType());
			BufferedImage transOUT = new BufferedImage(newRows, newColumns, INimg.getType());
			transIMG = transposeIMG(colIMG);
			
			energyMatrix = energyCal(transIMG, type);
			// Calculate map:
			calculateSeamMap(energyMatrix, seammap);
			
			if (rowSize > 0) {
				cutSeams(transIMG, seammap, rowSize, transOUT);
			} else if (rowSize < 0) {
				addSeams(transIMG, seammap, Math.abs(rowSize), transOUT);
			}
			OUTimg = transposeIMG(transOUT);
		}
		else {
			OUTimg = colIMG;
		}

		// write back the new Image:
		ImageIO.write(OUTimg, "jpg", OUTfile); // TODO - "???"

		// print all done
		System.out.println("all done. please enter your directory to view your new photo.. ");

	} // end of main

	
	////////////////////////////////////////////////////////////////////////////////////
	
	
	private static float[][] transposeMAT(float[][] energyMatrix) {
		float[][] outMAT = new float[energyMatrix[0].length][energyMatrix.length];
		for (int i = 0; i < energyMatrix.length; i++) {
			for (int j = 0; j < energyMatrix[0].length; j++) {
				outMAT[j][i] = energyMatrix[i][j];
			}
		}
		return outMAT;
	}

	private static BufferedImage transposeIMG(BufferedImage iNimg) {
		int height = iNimg.getHeight();
		int width = iNimg.getWidth();
		BufferedImage OUTimg = new BufferedImage(height, width, iNimg.getType());
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				OUTimg.setRGB(j, i, iNimg.getRGB(i, j));
			}
		}
		return OUTimg;
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
		// SeamMap ans = null;
		int totalColumnEnergy = 0;
		int[] sortPath = null;

		for (int j = (energyMatrix[0].length - 1); j >= 0; j--) {
			totalColumnEnergy += energyMatrix[i][j];
		}
		SeamMap ans = new SeamMap(totalColumnEnergy, i, sortPath);
		return ans;
	}

	private static void cutSeams2(BufferedImage iNimg, SeamMap[] seammap, int size, BufferedImage oUTimg) {
		int width = iNimg.getWidth();
		int hight = iNimg.getHeight();
		// int[][] matrix = new int[iNimg.getWidth() - size][iNimg.getHeight()];
		// // TODO - is this the right dimm order..
		boolean edit = true;
		int M;
		for (int i = 0; i < width; i++) {
			M = 0;
			for (int j = 0; j < hight; j++) { // TODO - dimensions
				for (int j2 = 0; j2 < size; j2++) {
					if (j == seammap[j2].index) {
						edit = false;
					}
				}
				if (edit) {
					oUTimg.setRGB(i, M, iNimg.getRGB(i, j));
					M++;
				} else {
					continue;
				}
			}
		}
	}

	private static void cutSeams(BufferedImage iNimg, SeamMap[] seammap, int size, BufferedImage oUTimg) {
		int width = iNimg.getWidth();
		int height = iNimg.getHeight();
		// int[][] matrix = new int[iNimg.getWidth() - size][iNimg.getHeight()];
		// // TODO - is this the right dimm order..
		boolean edit = false;
		int diff = 0;
		for (int i = 0; i < width; i++) {
			for (int k = 0; k < size; k++) {
				if (i == seammap[k].index) {
					edit = true;
				}
			}
			if (!edit) {
				for (int j = 0; j < height; j++) { // TODO - dimensions
					oUTimg.setRGB(i - diff, j, iNimg.getRGB(i, j));
				}
			} else {
				diff++;
				edit = false;
			}
		}
	}

	private static void addSeams(BufferedImage inImg, SeamMap[] seammap, int size, BufferedImage outImg) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0;
		boolean edit = false;
		for (int i = 0; i < width + size; i++) {
			for (int k = 0; k < size; k++) {
				if ((i - diff) == seammap[k].index) {
					edit = true;
					break;
				}
			}

			for (int j = 0; j < height; j++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
			}
			if (edit == true) {
				i++;
				diff++;
				for (int j = 0; j < height; j++) {
					outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				}
			}
			edit = false;
		}
	}

	private static void addSeamsAlt(BufferedImage inImg, SeamMap[] seammap, int size, BufferedImage outImg) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0, avgRGB = 0, r0 = 0, r1 = 0, r2 = 0, g0 = 0, g1 = 0, g2 = 0, b0 = 0, b1 = 0, b2 = 0;
		int RGB0 = 0, RGB1 = 0, RGB2 = 0, rAVG = 0, gAVG = 0, bAVG = 0;
		boolean edit = false;
		for (int i = 0; i < width + size; i++) {
			for (int k = 0; k < size; k++) {
				if ((i - diff) == seammap[k].index) {
					edit = true;
					break;
				}
			}
			for (int j = 0; j < height; j++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
			}
			if (edit == true) {
				i++;
				diff++;
				if ((i - diff - 1 != 0) && (i - diff + 1 != width)) {
					for (int j = 0; j < height; j++) {
						RGB0 = inImg.getRGB(i - diff - 1, j);
						RGB1 = inImg.getRGB(i - diff, j);
						RGB2 = inImg.getRGB(i - diff + 1, j);
						r0 = (RGB0 >> 16) & 0xff;
						g0 = (RGB0 >> 8) & 0xff;
						b0 = (RGB0) & 0xff;
						r1 = (RGB1 >> 16) & 0xff;
						g1 = (RGB1 >> 8) & 0xff;
						b1 = (RGB1) & 0xff;
						r2 = (RGB2 >> 16) & 0xff;
						g2 = (RGB2 >> 8) & 0xff;
						b2 = (RGB2) & 0xff;
						rAVG = (r0 + r1 + r2) / 3;
						gAVG = (g0 + g1 + g2) / 3;
						bAVG = (b0 + b1 + b2) / 3;
						avgRGB = ((rAVG & 0x0ff) << 16) | ((gAVG & 0x0ff) << 8) | (bAVG & 0x0ff);
						outImg.setRGB(i, j, avgRGB);
					}
				}
			}
			edit = false;
		}
	}

	//
	public static float[][] energyCal(BufferedImage img, int type) {

		int height = img.getHeight();
		int width = img.getWidth();
		float[][] energyArr = new float[width][height];
		int neighbors = 8;
		if (type != 2) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if ((i == 0 && j == 0) || (i == width - 1 && j == height - 1) || (i == 0 && j == height - 1)
							|| (i == width - 1 && j == 0)) {
						neighbors = 3;
					} else if (i == 0 || i == width - 1 || j == 0 || j == height - 1) {
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
			if (k > (img.getWidth() - 1))
				continue;

			for (int l = j - 1; l < j + 2; l++) {
				if (l == -1)
					l++;
				if (l > (img.getHeight() - 1))
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
			if (k > (img.getWidth() - 1))
				break;
			for (int l = j - 4; l < j + 5; l++) {
				if (l < 0)
					l = 0;
				if (l > (img.getHeight() - 1))
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
			if (k > (img.getWidth() - 1))
				break;
			for (int l = j - 4; l < j + 5; l++) {
				if (l < 0)
					l = 0;
				if (l > (img.getHeight() - 1))
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