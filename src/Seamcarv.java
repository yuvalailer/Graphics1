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
		System.out.println("old photo size - "+oldColumns+"X"+oldRows);
		System.out.println("new photo size - "+newColumns+"X"+newRows);

		// create outImge:
		BufferedImage OUTimg = new BufferedImage(newColumns, newRows, INimg.getType());
		//BufferedImage OUTimg = new BufferedImage(oldColumns, oldRows, INimg.getType());
		// get energy matrix:
		float[][] energyMatrix = energyCal(INimg, type);

		// Calculate map:
		calculateSeamMap(energyMatrix, seammap);

		// remove seams:

		int size = oldColumns - newColumns; // TODO - vertical vs horizontal ?
		cutSeams(INimg, seammap, size, OUTimg);
		System.out.println("size of new image is: " + OUTimg.getHeight() + " " + OUTimg.getWidth());

		// write back the new Image:
		ImageIO.write(OUTimg, "jpg", OUTfile); // TODO - "???"

		// print all done
		System.out.println("all done. please enter your diractory to view your new photo.. ");

	} // end of main

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

	private static SeamMap dynamicSeam2(float[][] energyMatrix, int i) {
		// SeamMap ans = null;
		int totalColumnEnergy = 0;
		int[] sortPath = null;

		for (int j = (energyMatrix[0].length - 1); j >= 0; j--) {
			totalColumnEnergy += energyMatrix[i][j];
		}
		SeamMap ans = new SeamMap(totalColumnEnergy, i, sortPath);
		return ans;
	}


	private static SeamMap dynamicSeam(float[][] energyMatrix, int i) {
		int totalColumnEnergy = 0;
		int[] sortPath = new int[energyMatrix[0].length];
		int minIndex = 0;
		int iterIndex = 0;
		int iterMin = 0;

		for (int j = 0; j < energyMatrix.length; j++) {																	//iterate over the last row
			if(energyMatrix[j][energyMatrix[0].length - 1] < energyMatrix[minIndex][energyMatrix[0].length - 1]) {		//choose the minimal 
				minIndex = j;																							// replace minimal index
			}
		}																												// minimal chosen

		sortPath[energyMatrix[0].length - 1] = minIndex;		// minimal index as last index.
		totalColumnEnergy += (int) energyMatrix[minIndex][energyMatrix[0].length - 1]; // add weight
		iterIndex = minIndex;	// set iteration index as the minimal index yet. 

		for (int j = (energyMatrix[0].length - 2); j >= 0; j--) {		// iterate over the y axis
			if((iterIndex > 0) && (iterIndex < energyMatrix.length -1) ) { // not at the edges.
				iterMin = iterIndex - 1;
				for (int simp = -1; simp < 2; simp++) {
					if((energyMatrix[iterMin][j]) > (energyMatrix[iterIndex + simp][j])) { // choose minimal neighbor
						iterMin = iterIndex + simp;	// change minimal 
					}
				}
			} else {
				if(iterIndex == 0) { // right edge. 
					for (int simp = 0; simp < 2; simp++) {
						if((energyMatrix[iterMin][j]) > (energyMatrix[iterIndex + simp][j])) { // choose minimal neighbor
							iterMin = iterIndex + simp;	// change minimal 
						}
					}
				} else { // left edge
					for (int simp = -1; simp < 1; simp++) {
						if((energyMatrix[iterMin][j]) > (energyMatrix[iterIndex + simp][j])) { // choose minimal neighbor
							iterMin = iterIndex + simp;	// change minimal 
						}
					}
				}

			}
			// minimal Index chosen (in iterMin) 
			sortPath[j] = iterMin; // add to path
			totalColumnEnergy += energyMatrix[iterMin][j]; // sum weight  
			iterIndex = iterMin;	// Replace the iteration pointer.
		} 	// finished all Y axis.

		// fill the energy to be infinity (to avoid repetition)

		for (int j = 0; j < sortPath.length; j++) {
			energyMatrix[sortPath[j]][j] = Float.MAX_VALUE; // fill with max float.
		} 

		SeamMap ans = new SeamMap(totalColumnEnergy, sortPath[0], sortPath);
		return ans;
	}
	private static void cutSeamscolor(BufferedImage iNimg, SeamMap[] seammap, int size, BufferedImage oUTimg) {
		int width = iNimg.getWidth();
		int height = iNimg.getHeight();
		boolean edit = true;
		int T = 0;
		
		for (int i = 0; i < height; i++) { // how many rows
			for (int j = 0; j < width; j++) { // how many columns   // dims on purpose
				for (int k = 0; k < size; k++) {
					
					if(j == seammap[k].way[i]){ // if [][X][][] on the i level is on the 'to be deleted' list. 
						edit = false;
						break;
					}
				
				} // end of k
				
				// do changes:
				if (edit) {					
					oUTimg.setRGB(j,i, iNimg.getRGB(j, i));
					 
				} else {
					oUTimg.setRGB(j,i, 100);
				}
				edit = true;
			}
			T = 0; // zero T.
		}
	}


	private static void cutSeams(BufferedImage iNimg, SeamMap[] seammap, int size, BufferedImage oUTimg) {
		int width = iNimg.getWidth();
		int height = iNimg.getHeight();
		boolean edit = true;
		int T = 0;
		
		for (int i = 0; i < height; i++) { // how many rows
			for (int j = 0; j < width; j++) { // how many columns   // dims on purpose
				for (int k = 0; k < size; k++) {
					
					if(j == seammap[k].way[i]){ // if [][X][][] on the i level is on the 'to be deleted' list. 
						edit = false;
						break;
					}
				
				} // end of k
				
				// do changes:
				if (edit) {					
					oUTimg.setRGB(T,i, iNimg.getRGB(j, i));
					T++; // over 400 
					if(T == oUTimg.getWidth()){
						break;
					}
				}
				else {
					//T++;
				}
				edit = true;
			}
			T = 0; // zero T.
		}
	}


	private static void cutSeams2(BufferedImage iNimg, SeamMap[] seammap, int size, BufferedImage oUTimg) {
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
				if (i == seammap[k].index) {
					edit = true;
					break;
				}
			}
			for (int j = 0; j < height; j++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				if (edit == true) {
					i++;
					diff++;
					outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				}
			}
			edit = false;
		}
	}

	private static void addSeamsAlt(BufferedImage inImg, SeamMap[] seammap, int size, BufferedImage outImg) {
		int height = inImg.getHeight();
		int width = inImg.getWidth();
		int diff = 0, avgRGB = 0;
		boolean edit = false;
		for (int i = 0; i < width + size; i++) {
			for (int k = 0; k < size; k++) {
				if (i == seammap[k].index) {
					edit = true;
					break;
				}
			}
			for (int j = 0; j < height; j++) {
				outImg.setRGB(i, j, inImg.getRGB(i - diff, j));
				if (edit == true) {
					i++;
					diff++;
					if ((i - diff - 1 != 0) && (i - diff + 1 != width)) {
						avgRGB = (inImg.getRGB(i - diff - 1, j) + inImg.getRGB(i - diff, j)
						+ inImg.getRGB(i - diff + 1, j)) / 3;
					}
					outImg.setRGB(i, j, avgRGB);
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
			for (int l = j - 4; l < j + 5; j++) {
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
			for (int l = j - 4; l < j + 5; j++) {
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