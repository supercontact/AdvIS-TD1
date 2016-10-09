package model;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import scene.Node;

public class AnnotatedPhoto implements Serializable {
	
	private static final long serialVersionUID = 5L;

	public File imageURL;
	public ArrayList<String> tags;
	public Node annotation;
	
	private ArrayList<Node> nodeList;
	
	transient public Image image;
	transient public Image thumbnail;
	transient public boolean imageLoaded = false;
	
	transient private int index;
	
	public AnnotatedPhoto(File url) {
		imageURL = url;
		tags = new ArrayList<>();
		annotation = new Node();
		nodeList = new ArrayList<Node>();
	}
	
	public boolean loadPhoto() {
		try {
			image = ImageIO.read(imageURL);		
			
			imageLoaded = true;
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void generateThumbnail(int size) {
		if (image == null) return;
		
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		int l = Math.min(w, h);
		BufferedImage squareImage = ((BufferedImage)image).getSubimage((w - l) / 2, (h - l) / 2, l, l);
		thumbnail = squareImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
	}
	
	public int getIndex() {
		return index;
	}
	protected void setIndex(int index) {
		this.index = index;
	}
	
	public void registerNode(Node node) {
		nodeList.add(node);
	}
	
	public void undo() {
		if (nodeList.size() > 0) {
			Node lastNode = nodeList.remove(nodeList.size() - 1);
			while (lastNode.getChildCount() > 0) {
				lastNode.getParent().addChild(lastNode.getChild(0));
			}
			lastNode.setParent(null);
		}
	}
	
	public void clear() {
		annotation.removeAllChild();
		nodeList.clear();
	}
}
