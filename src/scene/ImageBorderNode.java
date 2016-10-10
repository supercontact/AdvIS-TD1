package scene;

import java.awt.Image;
import java.awt.Rectangle;

// This node takes a 9-sliced image to draw a rectangle border.
public class ImageBorderNode extends Node {
	
	private static final long serialVersionUID = 1L;
	
	// Need modification to make it serializable
	public transient Image borderImage;
	
	public int sliceX1, sliceX2;
	public int sliceY1, sliceY2;
	public int width, height;
	public int borderWidthLeft, borderWidthRight, borderWidthTop, borderWidthBottom;
	
	public ImageBorderNode() {}
	public ImageBorderNode(Image borderImage, int width, int height, int borderWidth) {
		this.borderImage = borderImage;
		sliceX1 = borderImage.getWidth(null) / 3;
		sliceX2 = sliceX1 * 2;
		sliceY1 = borderImage.getHeight(null) / 3;
		sliceY2 = sliceY1 * 2;
		this.width = width;
		this.height = height;
		borderWidthLeft = borderWidth;
		borderWidthRight = borderWidth;
		borderWidthTop = borderWidth;
		borderWidthBottom = borderWidth;
	}

	@Override
	public Rectangle getContentBounds() {
		return new Rectangle(0, 0, width, height);
	}
	
	@Override
	public void paintNode(Context context) {
		int[] fx = new int[] {0, sliceX1, sliceX2, borderImage.getWidth(null)};
		int[] fy = new int[] {0, sliceY1, sliceY2, borderImage.getHeight(null)};
		int[] ix = new int[] {0, borderWidthLeft, width - borderWidthRight, width};
		int[] iy = new int[] {0, borderWidthTop, height - borderWidthBottom, height};
		
		context.graphics.drawImage(borderImage, ix[0], iy[0], ix[1], iy[1], fx[0], fy[0], fx[1], fy[1], null);
		context.graphics.drawImage(borderImage, ix[1], iy[0], ix[2], iy[1], fx[1], fy[0], fx[2], fy[1], null);
		context.graphics.drawImage(borderImage, ix[2], iy[0], ix[3], iy[1], fx[2], fy[0], fx[3], fy[1], null);
		context.graphics.drawImage(borderImage, ix[2], iy[1], ix[3], iy[2], fx[2], fy[1], fx[3], fy[2], null);
		context.graphics.drawImage(borderImage, ix[2], iy[2], ix[3], iy[3], fx[2], fy[2], fx[3], fy[3], null);
		context.graphics.drawImage(borderImage, ix[1], iy[2], ix[2], iy[3], fx[1], fy[2], fx[2], fy[3], null);
		context.graphics.drawImage(borderImage, ix[0], iy[2], ix[1], iy[3], fx[0], fy[2], fx[1], fy[3], null);
		context.graphics.drawImage(borderImage, ix[0], iy[1], ix[1], iy[2], fx[0], fy[1], fx[1], fy[2], null);
	}
	
}
