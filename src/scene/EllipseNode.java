package scene;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

// This node displays an ellipse.
public class EllipseNode extends PrimitiveNode {
	
	private static final long serialVersionUID = 1L;
	
	public EllipseNode(Point p1, Point p2) {
		super(p1, p2);
	}
	public EllipseNode(Point center, int width, int height) {
		p1 = new Point(center.x - width / 2, center.y - height / 2);
		p2 = new Point(center.x + width / 2, center.y + height / 2);
	}
	
	@Override
	public Shape getShape() {
		int x = Math.min(p1.x, p2.x);
		int y = Math.min(p1.y, p2.y);
		int w = Math.abs(p1.x - p2.x);
		int h = Math.abs(p1.y - p2.y);
		return new Ellipse2D.Double(x, y, w, h);
	}
	
}
