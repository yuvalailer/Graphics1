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
		SeamMap seammap = new SeamMap();

		System.out.println("procedure initiated");
		System.out.println("old photo size - " + oldColumns + "X" + oldRows);
		System.out.println("new photo size - " + newColumns + "X" + newRows);

		// get energy matrix:
		float[][] energyMatrix = energyCal(INimg, type);

		// Calculate map:
		seammap = dynamicSeam(energyMatrix);

		// create outImge:
		BufferedImage tmpIMG = new BufferedImage(oldColumns, oldRows, INimg.getType());
		BufferedImage colIMG = new BufferedImage(newColumns, oldRows, INimg.getType());
		BufferedImage OUTimg = new BufferedImage(newColumns, newRows, INimg.getType());
		BufferedImage transIMG = new BufferedImage(oldRows, newColumns, INimg.getType());
		BufferedImage transOUT = new BufferedImage(newRows, newColumns, INimg.getType());

		int colSize = oldColumns - newColumns;
		int rowSize = oldRows - newRows;
		tmpIMG = INimg;
		for (int i = 0; i < Math.abs(colSize); i++) {
			// System.out.println(i);
			// remove seams:
			if (colSize >= 0) {
				tmpIMG = cutSeams(tmpIMG, seammap, 1);
			} else if (colSize < 0) {
				tmpIMG = addSeams(tmpIMG, seammap, 1);
			}
			energyMatrix = energyCal(tmpIMG, type);
			seammap = dynamicSeam(energyMatrix);
			// for (int j = 0; j < seammap.way.length; j++) {
			// System.out.print(seammap.way[j] + " ");
			// }
			// System.out.println("");
		}
		colIMG = tmpIMG;

		if (rowSize != 0) {

			transIMG = transposeIMG(colIMG);
			for (int i = 0; i < Math.abs(rowSize); i++) {
				energyMatrix = energyCal(transIMG, type);
				// Calculate map:
				seammap = dynamicSeam(energyMatrix);

				if (rowSize > 0) {
					transIMG = cutSeams(transIMG, seammap, 1);
				} else if (rowSize < 0) {
					transIMG = addSeams(transIMG, seammap, 1);
				}
			}
			OUTimg = transposeIMG(transIMG);
		} else {
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

	
	
	private static float getDifference(BufferedImage image, int i1, int j1, int i2, int j2) {
		int RGB1 = image.getRGB(i1, j1);
		int r1 = (RGB1 >> 16) & 0xff;
		int g1 = (RGB1 >> 8) & 0xff;
		int b1 = (RGB1) & 0xff;
		int RGB2 = image.getRGB(i2, j2);
		int r2 = (RGB2 >> 16) & 0xff;
		int g2 = (RGB2 >> 8) & 0xff;
		int b2 = (RGB2) & 0xff;
		float sum = (Math.abs(r1-r2) + Math.abs(g1-g2) + Math.abs(b1-b2))/3; //TODO square root
		return sum;
	}

	private static float CU(BufferedImage image, int i, int j) {
		return (getDifference(image, i+1, j, i-1, j));
	}
	
	private static float CR(BufferedImage image, int i, int j) {
		return (getDifference(image, i+1, j, i-1, j)) + (getDifference(image, i, j-1, i+1, j));
	}
	
	private static float CL(BufferedImage image, int i, int j) {
		return (getDifference(image, i+1, j, i-1, j)) + (getDifference(image, i, j-1, i+1, j));
	}

	private static SeamMap dynamicSeam(float[][] energyMatrix) {
		int totalColumnEnergy = 0;
		int[] sortPath = new int[energyMatrix[0].length];
		int minIndex = 0;
		int iterIndex = 0;
		int iterMin = 0;
		for (int j = 0; j < energyMatrix.length; j++) { // iterate over the last
														// row
			if (((energyMatrix[j][energyMatrix[0].length - 1]) < (energyMatrix[minIndex][energyMatrix[0].length
					- 1]))) { // choose the minimal
				minIndex = j; // replace minimal index
			}
		} // minimal chosen

		sortPath[energyMatrix[0].length - 1] = minIndex; // minimal index as
															// last index.
		totalColumnEnergy += (int) energyMatrix[minIndex][energyMatrix[0].length - 1]; // add
																						// weight
		iterIndex = minIndex; // set iteration index as the minimal index yet.

		for (int j = (energyMatrix[0].length - 2); j >= 0; j--) { // iterate
																	// over the
																	// y axis
			if ((iterIndex > 0) && (iterIndex < energyMatrix.length - 1)) { // not
																			// at
																			// the
																			// edges.
				iterMin = iterIndex - 1;
				for (int simp = -1; simp < 2; simp++) {
					if (((energyMatrix[iterMin][j]) > (energyMatrix[iterIndex + simp][j]))) { // choose
																								// minimal
																								// neighbor
						iterMin = iterIndex + simp; // change minimal
					}
				}
			} else {
				if (iterIndex == 0) { // right edge.
					iterMin = 0;
					for (int simp = 0; simp < 2; simp++) {
						if (((energyMatrix[iterMin][j]) > (energyMatrix[iterIndex + simp][j]))) { // choose
																									// minimal
																									// neighbor
							iterMin = iterIndex + simp; // change minimal
						}
					}
				} else { // left edge
					iterMin = energyMatrix.length - 2;
					for (int simp = -1; simp < 1; simp++) {
						if (((energyMatrix[iterMin][j]) > (energyMatrix[iterIndex + simp][j]))) { // choose
																									// minimal
																									// //
																									// neighbor
							iterMin = iterIndex + simp; // change minimal
						}
					}
				}

			}
			// minimal Index chosen (in iterMin)
			sortPath[j] = iterMin; // add to path
			totalColumnEnergy += energyMatrix[iterMin][j]; // sum weight
			iterIndex = iterMin; // Replace the iteration pointer.
		} // finished all Y axis.

		// fill the energy to be infinity (to avoid repetition)

		// for (int j = 0; j < sortPath.length; j++) {
		// energyMatrix[sortPath[j]][j] = -1; // fill with max float.
		// }
		int value = totalColumnEnergy;
		// }
		SeamMap ans = new SeamMap(value, sortPath[0], sortPath);
		return ans;
	}

	private static void cutSeamscolor(BufferedImage iNimg, SeamMap[] seammap, int size, BufferedImage oUTimg) {
		int width = iNimg.getWidth();
		int height = iNimg.getHeight();
		boolean edit = true;
		int T = 0;

		for (int i = 0; i < height; i++) { // how many rows
			for (int j = 0; j < width; j++) { // how many columns // dims on
												// purpose
				for (int k = 0; k < size; k++) {

					if (j == seammap[k].way[i]) { // if [][X][][] on the i level
													// is on the 'to be deleted'
													// list.
						edit = false;
						break;
					}

				} // end of k

				// do changes:
				if (edit) {
					oUTimg.setRGB(j, i, iNimg.getRGB(j, i));

				} else {
					oUTimg.setRGB(j, i, 100);
				}
				edit = true;
			}
		}
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

	/*
	 * private static void calculateSeamMap(float[][] energyMatrix, SeamMap[]
	 * seammap) { // calculate seams dynamically: for (int i = 0; i <
	 * energyMatrix.length; i++) { // Iterate over the // map's bottom row.
	 * seammap[i] = dynamicSeam(energyMatrix, i); // Calculate the seams in // a
	 * dynamic form } // sort by energy: Arrays.sort(seammap); // sort map by
	 * energy }
	 */

	private static BufferedImage cutSeams(BufferedImage iNimg, SeamMap seammap, int size) {
		int width = iNimg.getWidth();
		int height = iNimg.getHeight();
		boolean edit = true;
		int T = 0;
		BufferedImage OUTimg = new BufferedImage(width - 1, height, iNimg.getType());
		// System.out.println(width);
		for (int i = 0; i < height; i++) { // how many columns // dims on
			for (int j = 0; j < width; j++) { // how many rows
				// System.out.println("i: " + i + " j: " + j); // purpose
				if (j != seammap.way[i]) { // if [][X][][] on the i level is on
											// the 'to be deleted' list.
					// do changes:
					OUTimg.setRGB(j - T, i, iNimg.getRGB(j, i));
				} else {
					T++;
				}
			} // J
			T = 0;
		} // i
		return OUTimg;
	}

	private static BufferedImage addSeams(BufferedImage inImg, SeamMap seammap, int size) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0;
		boolean edit = false;
		BufferedImage outImg = new BufferedImage(width + 1, height, inImg.getType());
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width + size; i++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				if ((i - diff) == seammap.way[j]) {
					i++;
					diff++;
					outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				}
			}
			diff = 0;
		}
		return outImg;
	}

	private static BufferedImage addSeamsAlt(BufferedImage inImg, SeamMap seammap, int size) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0, avgRGB = 0, r0 = 0, r1 = 0, r2 = 0, g0 = 0, g1 = 0, g2 = 0, b0 = 0, b1 = 0, b2 = 0;
		int RGB0 = 0, RGB1 = 0, RGB2 = 0, rAVG = 0, gAVG = 0, bAVG = 0;
		boolean edit = false;
		BufferedImage outImg = new BufferedImage(width + 1, height, inImg.getType());
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width + size; i++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				if ((i - diff) == seammap.way[j]) {
					i++;
					diff++;
					if ((i - diff - 1 != 0) && (i - diff + 1 != width)) {
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
			diff = 0;
		}
		return outImg;
	}

	//
	
	public static float[][] energyCal(BufferedImage img, int type) {
		float[][] mat = new float[img.getWidth()][img.getHeight()];
		float min = 0;
		
		for (int i=1; i < img.getWidth()-1; i++) {
			mat[i][0] = CU(img, i, 0);
		}
		for (int i = 0; i < img.getHeight(); i++) {
			mat[0][i] = 1000;
			mat[img.getWidth()-1][i] = 1000;
		}
		
		for (int j = 1; j < img.getHeight()-1; j++) {
			for (int i = 1; i < img.getWidth()-1; i++) {
				min = mat[i-1][j] + CU(img, i, j);
				if (mat[i-1][j-1] + CL(img,i,j) < min) {
					min = mat[i-1][j-1] + CL(img,i,j);
				}
				if (mat[i-1][j+1] + CR(img,i,j) < min) {
					min = mat[i-1][j+1] + CR(img,i,j);
				}
				mat[i][j] = min;
			}
			
		}
		return mat;
	}
	
	public static float[][] energyCalORiginal(BufferedImage img, int type) {

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