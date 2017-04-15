import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Seamcarv {
	// input: 
	// <input image filename> <output # columns> <output # rows> <energy type>
	//<output image filename>
	//
	public static void main(String[] args) throws IOException {
	// parse arguments: 
	File INfile = new File(args[1]);
	int newColumns = Integer.parseInt(args[2]);
	int newRows = Integer.parseInt(args[3]);
	int type = Integer.parseInt(args[4]);
	File OUTfile = new File(args[5]);
	int[][][] SeamMap = null; // TODO 
	
	//create image: 
	BufferedImage INimg = ImageIO.read(INfile);
	BufferedImage OUTimg = ImageIO.read(OUTfile); // TODO - change to right function. 
	
	// get image dimensions: 
	int oldColumns = INimg.getWidth();
	int oldRows = INimg.getHeight();
	
	// get energy matrix: 
	int[][] energyMatrix = getEnergy(INimg,type);
	
	// Calculate map:
	calculateSeamMap(energyMatrix,SeamMap);
	
	// remove seams: 
	int size = oldColumns - newColumns; // TODO - vertical vs horizontal ? 
	cutSeams(INimg,SeamMap,size,OUTimg);
	
	// write back the new Image: 
	
	//ImageIO.write(OUTimg, formatName, output);
	
	} // end of main 

	private static int[][] getEnergy(BufferedImage iNimg, int type) {
		// TODO Auto-generated method stub
		return null;
	}


	private static void calculateSeamMap(int[][] energyMatrix, int[][][] seamMap) {
		// calculate seams dynamically:
		for (int i = 0; i < energyMatrix.length; i++) { 	// Iterate over the map's bottom row.  
			seamMap[i] = dynamicSeam(energyMatrix,i); 		// Calculate the seams in a dynamic form
		}
		// sort by energy: 
		sortSeamsMap(seamMap);								// sort map by energy 
	}
	

	private static int[][] dynamicSeam(int[][] energyMatrix, int i) {
		int[][] ans = {null,null,null};
		int totalColumnEnergy = 0;
		int[] sortPath = null;
		
		for (int j = (energyMatrix[0].length - 1); j >= 0; j--) {
			totalColumnEnergy += energyMatrix[i][j]; 
		}
		
		ans[0][0] = totalColumnEnergy; 				// Weight of the entire seam. 
		ans[1][0] = i;		//TODO - change to top index, not bottom..	// index of the top row seam index. 
		ans[2] = sortPath; 	// TODO - to be 0,1,-1  // the path of the shortest seam 
		return ans;
	}

	private static void sortSeamsMap(int[][][] seamMap) {
		//TODO- sort
	}

	private static void cutSeams(BufferedImage iNimg, int[][][] seamMap, int size, BufferedImage oUTimg) {
		BufferedImage tmpImg = iNimg;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < seamMap.length; j++) {
				//if(seamMap[j][1] != j){
					
				}
			}
		}
	
}
