package scene;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class EllipseNode extends ShapeNode {
	
	public Point center;
	public int width, height;
	
	public EllipseNode(Point p1, Point p2) {
		center = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
		width = Math.abs(p1.x - p2.x);
		height = Math.abs(p1.y - p2.y);
	}
	public EllipseNode(Point center, int width, int height) {
		this.center = center;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public Shape getShape() {
		return new Ellipse2D.Double(center.x - width / 2, center.y - height / 2, width, height);
	}
	
}
