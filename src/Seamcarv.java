import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Seamcarv {
	
	public static void main(String[] args) throws IOException {
		// parse arguments:
		File INfile = (new File(args[0]));          // name of input file 
		int newColumns = Integer.parseInt(args[1]); // the new number of columns
		int newRows = Integer.parseInt(args[2]);	// the new number of rows
		int type = Integer.parseInt(args[3]);		// type of energy function. 0 = regular, 1 = entropy, 2 = forwarding
		File OUTfile = new File(args[4]);			// the output file 
		boolean fd = OUTfile.createNewFile();		// check
		if(fd == false) {
			System.err.println("failed to create the output file");
		}

		// create image:
		BufferedImage INimg = ImageIO.read(INfile);

		// get image dimensions:
		int oldColumns = INimg.getWidth();
		int oldRows = INimg.getHeight();
		SeamMap seammap = new SeamMap();
		SeamMap[] seamMat = new SeamMap[Math.abs(newColumns - oldColumns)];

		float[][] energyMatrix = new float[oldColumns][oldRows];

		System.out.println("Procedure Initiated");								// prints for aid user understanding.
		System.out.println("Old photo size: " + oldColumns + "X" + oldRows);
		System.out.println("New photo size: " + newColumns + "X" + newRows);

		// get energy matrix:
		if (type != 2) {
			energyMatrix = energyCal(INimg, type);
		} else {
			energyMatrix = energyCalForward(INimg, type);
		}
		// Calculate map:

		// create outImge:
		BufferedImage tmpIMG = new BufferedImage(oldColumns, oldRows, INimg.getType());
		BufferedImage colIMG = new BufferedImage(newColumns, oldRows, INimg.getType());
		BufferedImage OUTimg = new BufferedImage(newColumns, newRows, INimg.getType());
		BufferedImage transIMG = new BufferedImage(oldRows, newColumns, INimg.getType());

		int colSize = oldColumns - newColumns;
		int rowSize = oldRows - newRows;
		tmpIMG = INimg;

		if (colSize < 0) {
			seamMat = iterDynamic(energyMatrix, Math.abs(colSize));
			colIMG = addSeams(INimg, seamMat, Math.abs(colSize));
		} else {
			seammap = dynamicSeam(energyMatrix); // if colsize > 0
			for (int i = 0; i < Math.abs(colSize); i++) {
				// remove seams:
				tmpIMG = cutSeams(tmpIMG, seammap, 1);
				if (type != 2) {
					energyMatrix = energyCal(tmpIMG, type);
				} else {
					energyMatrix = energyCalForward(tmpIMG, type);
				}
				seammap = dynamicSeam(energyMatrix);
			}
			colIMG = tmpIMG;
		}
		
		if (rowSize != 0) {
			transIMG = transposeIMG(colIMG);
			if (type != 2) {
				energyMatrix = energyCal(transIMG, type);
			} else {
				energyMatrix = energyCalForward(transIMG, type);
			}
			if (rowSize < 0) {
				seamMat = iterDynamic(energyMatrix, Math.abs(rowSize));
				transIMG = addSeams(transIMG, seamMat, Math.abs(rowSize));
			} else {
				for (int i = 0; i < Math.abs(rowSize); i++) {
					// Calculate map:
					seammap = dynamicSeam(energyMatrix);
					transIMG = cutSeams(transIMG, seammap, 1);

					if (type != 2) {
						energyMatrix = energyCal(transIMG, type);
					} else {
						energyMatrix = energyCalForward(transIMG, type);
					}
				}
			}
			OUTimg = transposeIMG(transIMG);
		} else {
			OUTimg = colIMG;
		}

		// write back the new Image:
		ImageIO.write(OUTimg, "jpg", OUTfile); // TODO - "???"

		// print all done
		System.out.println("All done. Please enter your directory to view your new photo..");

	} // end of main

	
	
	////////////////////////////////////////////////////////////////////////////////////
	//////////////////// AUX Functions /////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	

	
	private static BufferedImage addSeams(BufferedImage inImg, SeamMap seammap[], int size) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0;
		BufferedImage outImg = new BufferedImage(width + size, height, inImg.getType());
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width + size; i++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				for (int k = 0; k < seammap.length; k++) {
					if ((i - diff) == seammap[k].way[j]) {
						i++;
						diff++;
						outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
					}
				}
			}
			diff = 0;
		}
		return outImg;
	}

	private static BufferedImage addSeamsAlt(BufferedImage inImg, SeamMap[] seammap, int size) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0, avgRGB = 0, r0 = 0, r1 = 0, r2 = 0, g0 = 0, g1 = 0, g2 = 0, b0 = 0, b1 = 0, b2 = 0;
		int RGB0 = 0, RGB1 = 0, RGB2 = 0, rAVG = 0, gAVG = 0, bAVG = 0;
		BufferedImage outImg = new BufferedImage(width + size, height, inImg.getType());
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width + size; i++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				for (int k = 0; k < seammap.length; k++) {
					if ((i - diff) == seammap[k].way[j]) {
						i++;
						diff++;
						if ((i - diff - 1 > -1) && (i - diff + 1 < width)) {
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
			}
			diff = 0;
		}
		return outImg;
	}
	
	
	private static SeamMap[] iterDynamic(float[][] energyMatrix, int size) {	// find the needed number of the best seams. for adding. 
		SeamMap[] seammap = new SeamMap[size];									// array of best seams 
		SeamMap itSeam = new SeamMap();											// current best seam 

		for (int i = 0; i < seammap.length; i++) {
			itSeam = dynamicSeam(energyMatrix);									// find best seam 
			for (int j = 0; j < energyMatrix[0].length; j++) {					//
				energyMatrix[itSeam.way[j]][j] = 255;							// color it with worst energy possible
			}
			seammap[i] = itSeam;												//add current best seam to its place in the array 
		}
		return seammap;															// return array	
	}

	private static SeamMap dynamicSeam(float[][] energyMatrix) {				// calculate the best seam in picture by dynamic programming
		int totalColumnEnergy = 0;												// seam's accumulative weight
		int[] sortPath = new int[energyMatrix[0].length];						// to be the shortest path 
		int minIndex = 0;														// current minimal index
		int iterIndex = 0;														// the whole iteration's minimal index candidate
		int iterMin = 0;														// the whole iteration's minimal index
		float[][] memoMatrix = new float[energyMatrix.length][energyMatrix[0].length];	// weight memoization matrix 
		// Preparation
		for (int i = 0; i < memoMatrix.length; i++) {						
			memoMatrix[i][0] = energyMatrix[i][0]; 										// fill first line with the energy parameters
		}
		for (int j = 0; j < memoMatrix[0].length - 1; j++) {// Y
			for (int i = 0; i < memoMatrix.length; i++) { // X
				memoMatrix[i][j + 1] = memoMatrix[i][j] + energyMatrix[i][j + 1]; 		// [][A][] fill B's new weight with A's
																						// [][B][] if filling improves its weight

				if (i > 0) { 																		// [ ][A][ ]
					if (memoMatrix[i - 1][j + 1] > energyMatrix[i - 1][j + 1] + memoMatrix[i][j]) { // [B][ ][ ]
						memoMatrix[i - 1][j + 1] = energyMatrix[i - 1][j + 1] + memoMatrix[i][j];
					}
					if (memoMatrix[i][j + 1] > energyMatrix[i][j + 1] + memoMatrix[i - 1][j]) { // [A][X][ ]
						memoMatrix[i][j + 1] = energyMatrix[i][j + 1] + memoMatrix[i - 1][j]; 	// [ ][B][ ]
					} 
				}
			}
		}

		// selection
		for (int j = 0; j < memoMatrix.length; j++) { 																// iterate over the last row
			if (((memoMatrix[j][memoMatrix[0].length - 1]) < (memoMatrix[minIndex][memoMatrix[0].length - 1]))) { 	// choose the minimal
				minIndex = j; 																						// replace minimal index
			}
		} // minimal chosen

		sortPath[memoMatrix[0].length - 1] = minIndex; 													// minimal index as last index.
		totalColumnEnergy += (int) memoMatrix[minIndex][memoMatrix[0].length - 1]; 						// add weight
		iterIndex = minIndex; 																			// set iteration index as the minimal index yet.

		for (int j = (memoMatrix[0].length - 2); j >= 0; j--) { 										// iterate over the y axis
			if ((iterIndex > 0) && (iterIndex < memoMatrix.length - 1)) { 								// not at the edges 
				iterMin = iterIndex - 1;
				for (int simp = -1; simp < 2; simp++) {
					if (((memoMatrix[iterMin][j]) > (memoMatrix[iterIndex + simp][j]))) { 				// choose minimal neighbor
						iterMin = iterIndex + simp; 													// change minimal
					}
				}
			} else {
				if (iterIndex == 0) { 																	// right edge.
					iterMin = 0;																		// zero minimal 
					for (int simp = 0; simp < 2; simp++) {												// compare
						if (((memoMatrix[iterMin][j]) > (memoMatrix[iterIndex + simp][j]))) {			// choose minimal neighbor
							iterMin = iterIndex + simp;										 			// change minimal
						}
					}
				} else { 																				// left edge
					iterMin = memoMatrix.length - 2;													// zero minimal
					for (int simp = -1; simp < 1; simp++) {												// compare
						if (((memoMatrix[iterMin][j]) > (memoMatrix[iterIndex + simp][j]))) { 			// choose minimal neighbor
							iterMin = iterIndex + simp; 												// change minimal
						}
					}
				}

			}
			// minimal Index chosen (in iterMin)
			sortPath[j] = iterMin; 																		// add to path
			totalColumnEnergy += memoMatrix[iterMin][j]; 												// sum weight
			iterIndex = iterMin; 																		// Replace the iteration pointer.
		} 																								// finished all Y axis.

		int value = totalColumnEnergy;																	// set value to total seam energy
		SeamMap ans = new SeamMap(value, sortPath[0], sortPath);										// crate the seam 
		return ans;																						// return it 
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


	private static BufferedImage cutSeams(BufferedImage iNimg, SeamMap seammap, int size) {	// cut the given seams 
		int width = iNimg.getWidth();														// images width 
		int height = iNimg.getHeight();														// images height 
		int T = 0;
		BufferedImage OUTimg = new BufferedImage(width - 1, height, iNimg.getType());		// Create output 
		// System.out.println(width);
		for (int i = 0; i < height; i++) { 													// how many columns // dims on
			for (int j = 0; j < width; j++) { 												// how many rows
				if (j != seammap.way[i]) { 													// if [][X][][] on the i level is on
																							// the 'to be deleted' list.
					// do changes:
					OUTimg.setRGB(j - T, i, iNimg.getRGB(j, i)); 							// copy if needed 
				} else {
					T++;																	// hold your position 
				}
			} // J
			T = 0;
		} // i
		return OUTimg;
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
		float sum = (Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2)) / 3;
		return sum;
	}

	private static float CU(BufferedImage image, int i, int j) {
		return (getDifference(image, i + 1, j, i - 1, j));
	}

	private static float CR(BufferedImage image, int i, int j) {
		return (getDifference(image, i + 1, j, i - 1, j)) + (getDifference(image, i, j - 1, i + 1, j));
	}

	private static float CL(BufferedImage image, int i, int j) {
		return (getDifference(image, i + 1, j, i - 1, j)) + (getDifference(image, i, j - 1, i + 1, j));
	}

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
	
	public static float[][] energyCalForward(BufferedImage img, int type) { // the forward energy function 
		float[][] mat = new float[img.getWidth()][img.getHeight()];			// answer matrix 
		float min = 0;
		for (int i = 1; i < img.getWidth() - 1; i++) {
			mat[i][0] = CU(img, i, 0);										// fill first line with the B type energy from paper 
		}
		for (int i = 0; i < img.getHeight(); i++) {							// fill edges columns with max energy to avoid choosing them 
			mat[0][i] = 255;
			mat[img.getWidth() - 1][i] = 255;
		}

		for (int j = 1; j < img.getHeight() - 1; j++) {
			for (int i = 1; i < img.getWidth() - 1; i++) {

				min = mat[i - 1][j] + CU(img, i, j);						// fill cell with the B type energy from paper
				if (mat[i - 1][j - 1] + CL(img, i, j) < min) {				// Compare to A type energy from paper
					min = mat[i - 1][j - 1] + CL(img, i, j);				// replace if needed
				}
				if (mat[i - 1][j + 1] + CR(img, i, j) < min) {				// Compare to A type energy from paper
					min = mat[i - 1][j + 1] + CR(img, i, j);				// replace if needed
				}
				mat[i][j] = min;											// set matrix to found minimal 
			}

		}
		return mat;															// return answer matrix 
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


}