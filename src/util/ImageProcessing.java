package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageProcessing {
	
	public static BufferedImage cloneImage(Image source) {
		BufferedImage copy = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    Graphics g = copy.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return copy;
	}
	
	public static void colorImage(Color c, BufferedImage oldImg, BufferedImage newImg) {
    	for (int x = 0; x < newImg.getWidth(); x++) {
    		for (int y = 0; y < newImg.getHeight(); y++) {
    			int rgb = oldImg.getRGB(x, y);
    			int b = Integer.remainderUnsigned(rgb, 0x100) ;
    			int g = (rgb >>> 8) % 0x100;
    			int r = (rgb >>> 16) % 0x100;
    			int a = rgb >>> 24;
    			int newRgb = (a << 24) + ((r * c.getRed() / 255) << 16) + ((g * c.getGreen() / 255) << 8) + (b * c.getBlue() / 255);
    			newImg.setRGB(x, y, newRgb);
    		}
    	}
    }
}
