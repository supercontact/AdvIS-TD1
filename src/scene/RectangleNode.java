package scene;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

// This node displays a rectangle.
public class RectangleNode extends PrimitiveNode {
	
	private static final long serialVersionUID = 1L;
	
	public int roundCornerRadius = 0;
	
	public RectangleNode(Point p1, Point p2) {
		super(p1, p2);
	}
	public RectangleNode(Point p1, Point p2, int roundCornerRadius) {
		super(p1, p2);
		this.roundCornerRadius = roundCornerRadius;
	}
	
	@Override
	Shape getShape() {
		int x = Math.min(p1.x, p2.x);
		int y = Math.min(p1.y, p2.y);
		int w = Math.abs(p1.x - p2.x);
		int h = Math.abs(p1.y - p2.y);
		if (roundCornerRadius == 0) {
			return new Rectangle(x, y, w, h);
		} else {
			return new RoundRectangle2D.Double(x, y, w, h, roundCornerRadius, roundCornerRadius);
		}
	}
	
}
