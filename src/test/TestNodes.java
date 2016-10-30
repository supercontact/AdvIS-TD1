package test;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;

import javax.swing.JFrame;

import component.GraphicalComponent;
import custom.ResourceManager;
import scene.EllipseNode;
import scene.ImageBorderNode;
import scene.ImageNode;
import scene.Node;
import scene.PolygonNode;
import scene.RectangleNode;
import scene.StraightLineNode;
import scene.TextNode;

public class TestNodes extends JFrame {

    private static final long serialVersionUID = 1L;
    
    public GraphicalComponent nodeComponent;
    

    public static void main(String[] args) {
    	new TestNodes();
    }
   
    public TestNodes() {
        super("Test");
        
        ResourceManager.loadResources();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        setPreferredSize(new Dimension(1024, 768));
        setMinimumSize(new Dimension(300, 400));
        setVisible(true);
        
        nodeComponent = new GraphicalComponent();
        Node n1 = new StraightLineNode(new Point(0, 0), new Point(300, 300));
        nodeComponent.graphicalNode = n1;
        Node n2 = new StraightLineNode(new Point(0, 0), new Point(100, 0));
        Node n3 = new StraightLineNode(new Point(0, 0), new Point(100, 0));
        n1.addChild(n2);
        n1.addChild(n3);
        n2.setPosition(new Point(300, 300));
        n3.setPosition(new Point(300, 300));
        n3.setRotation(Math.PI / 3);
        n1.setRotation(0.2);
        n1.fillColor = new Color(255, 0, 0, 128);
        n1.strokeColor = Color.blue;
        n2.strokeColor = Color.green;
        n2.strokeWidth = 4f;
        Node n4 = new PolygonNode(Arrays.asList(new Point[] {
        		new Point(0, 0),
        		new Point(200, 50),
        		new Point(200, -50)
        }));
        n4.setPosition(50, 0);
        n3.addChild(n4);
        Node n5 = new RectangleNode(new Point(-50, -50), new Point(50, 50), 20);
        n5.setPosition(100, 0);
        n2.addChild(n5);
        n2.alpha = 0.6f;
        n5.strokeColor = Color.pink;
        n5.fillColor = Color.cyan;
        EllipseNode n6 = new EllipseNode(new Point(150, 150), 100, 50);
        n6.drawBorder = false;
        n1.addChild(n6);
        Node n7 = new ImageNode(ResourceManager.textSizeIcon);
        n5.addChild(n7);
        n7.setPosition(-25, -50);
        n7.setScale(1, 2);
        n1.clip = new Ellipse2D.Double(80,80,340,340);
        n7.clip = new Rectangle(0, 20, 100, 15);
        TextNode n8 = new TextNode("Hello! This is the deblisoookoko jogogjgoioiiboiboiu sip!\nlook\nat\n  you!");
        n8.textSize = 20;
        n8.setParent(n6);
        n8.textColor = new Color(90, 0, 90);
        n8.setPosition(new Point(150, 120));
        n8.setRotation(-0.3);
        n8.width = 200;
        n8.showMarker = true;
        n8.markerPosition = 30;
        ImageBorderNode n9 = new ImageBorderNode(ResourceManager.frameImage, 300, 300, 20);
        n1.getChildren().add(0, n9);
        n9.setPosition(new Point(90, 90));
        
        //nodeComponent.showBounds();
        add(nodeComponent, BorderLayout.CENTER);
       
        pack();
    }
}