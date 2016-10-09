package scene;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.List;

public class PolygonNode extends ShapeNode {
	
	private static final long serialVersionUID = 1L;
	
	public List<Point> corners;
	
	public PolygonNode(List<Point> corners) {
		this.corners = corners;
	}
	
	@Override
	Shape getShape() {
		Polygon polygon = new Polygon();
		for (Point p : corners) {
			polygon.addPoint(p.x, p.y);
		}
		return polygon;
	}
	
}
