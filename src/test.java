import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class test {

	public static void main(String[] args) {

		// TODO Auto-generated method stub

	}

	public float[][] energyCal(BufferedImage img, int type) {
		float[][] energyArr = null;
		int height = img.getHeight();
		int width = img.getWidth();
		int neighbors = 8;
		if (type != 2) {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if ((i==0 && j==0) || (i==height-1 && j==width-1) || (i==0 && j==width-1) || (i==height-1 && j==0)) {
						neighbors = 3;
					}
					else if (i==0 || i==height-1 || j==0 || j==width-1) {
						neighbors = 5;
					}
					else neighbors = 8;
					energyArr[i][j] = (RGBdiff(img, i, j) / neighbors);
					if (type == 1) {
						energyArr[i][j] += entropy(img, i, j);
					}
				}
			}
		}
		return energyArr;
	}

	public float RGBdiff(BufferedImage img, int i, int j) {
		int RGB = img.getRGB(i, j);
		int r = (RGB >> 16) & 0xff;
		int g = (RGB >> 8) & 0xff;
		int b = (RGB) & 0xff;
		int r2 = 0, g2 = 0, b2 = 0, RGB2 = 0;
		float sum = 0;
		
		for (int k = i - 1; k < i + 2; k++) {
			if (k == -1) k++;
			if (k > (img.getHeight()-1)) continue;
			
			for (int l = j - 1; l < j + 2; j++) {
				if (l == -1) l++;
				if (l > (img.getWidth()-1)) continue;
					
				RGB2 = img.getRGB(k, l);
				r2 = (RGB2 >> 16) & 0xff;
				g2 = (RGB2 >> 8) & 0xff;
				b2 = (RGB2) & 0xff;
				sum += ((Math.abs(r - r2) + Math.abs(g - g2) + Math.abs(b - b2)) / 3);
			}
		}
		return sum;
	}

	public float entropy(BufferedImage img, int i, int j) {
		float sum = 0, p = 0, h = 0;
		int RGB2, r2 = 0, g2 = 0, b2 = 0, grey2 = 0;
		int RGB = img.getRGB(i, j);
		int r = (RGB >> 16) & 0xff;
		int g = (RGB >> 8) & 0xff;
		int b = (RGB) & 0xff;
		int grey = (r + g + b) / 3;
		for (int k = i - 4; k < i + 5; k++) {
			if (k < 0) k = 0;
			if (k > (img.getHeight()-1)) break;
			for (int l = j - 4; l < j + 5; j++) {
				if (l < 0) l = 0;
				if (l > (img.getWidth()-1)) break;
				RGB2 = img.getRGB(k, l);
				r2 = (RGB2 >> 16) & 0xff;
				g2 = (RGB2 >> 8) & 0xff;
				b2 = (RGB2) & 0xff;
				grey2 = (r2 + g2 + b2) / 3;
				sum += grey;

			}
		}
		for (int k = i - 4; k < i + 5; k++) {
			if (k < 0) k = 0;
			if (k > (img.getHeight()-1)) break;
			for (int l = j - 4; l < j + 5; j++) {
				if (l < 0) l = 0;
				if (l > (img.getWidth()-1)) break;
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
