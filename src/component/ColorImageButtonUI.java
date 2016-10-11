package component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

import util.ImageProcessing;

// This class is a simple ButtonUI that shows an image tinted by different colors on different states.
public class ColorImageButtonUI extends BasicButtonUI {
	
	public Image image;
	
	private Color normalColor = new Color(255, 255, 255);
	private Color hoverColor = new Color(224, 224, 224);
	private Color pressedColor = new Color(160, 160, 160);
	private Color selectedColor = new Color(192, 192, 192);
	private Color disabledColor = new Color(128, 128, 128);
	
	private BufferedImage originalImage, normalImage, hoverImage, pressedImage, selectedImage, disabledImage;
	
	public ColorImageButtonUI(Image image) {
		this.image = image;
		updateCachedImages();
	}
	public ColorImageButtonUI(Image image, Color normalColor, Color hoverColor, Color pressedColor, Color selectedColor, Color disabledColor) {
		this.image = image;
		this.normalColor = normalColor;
		this.hoverColor = hoverColor;
		this.pressedColor = pressedColor;
		this.selectedColor = selectedColor;
		this.disabledColor = disabledColor;
		updateCachedImages();
	}
	
	public void setNormalColor(Color color) {
		normalColor = color;
		updateCachedImages();
	}
	public void setHoverColor(Color color) {
		hoverColor = color;
		updateCachedImages();
	}
	public void setPressedColor(Color color) {
		pressedColor = color;
		updateCachedImages();
	}
	public void setSelectedColor(Color color) {
		selectedColor = color;
		updateCachedImages();
	}
	public void setDisabledColor(Color color) {
		disabledColor = color;
		updateCachedImages();
	}
	
	private void updateCachedImages() {
		if (originalImage == null) {
			originalImage = ImageProcessing.cloneImage(image);
			normalImage = ImageProcessing.cloneImage(image);
			hoverImage = ImageProcessing.cloneImage(image);
			pressedImage = ImageProcessing.cloneImage(image);
			selectedImage = ImageProcessing.cloneImage(image);
			disabledImage = ImageProcessing.cloneImage(image);
		}
		ImageProcessing.colorImage(normalColor, originalImage, normalImage);
		ImageProcessing.colorImage(hoverColor, originalImage, hoverImage);
		ImageProcessing.colorImage(pressedColor, originalImage, pressedImage);
		ImageProcessing.colorImage(selectedColor, originalImage, selectedImage);
		ImageProcessing.colorImage(disabledColor, originalImage, disabledImage);
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
		JButton button = (JButton)c;
		if (!button.getModel().isEnabled()) {
			g.drawImage(disabledImage, 0, 0, null);
		} else if (button.getModel().isPressed()) {
			g.drawImage(pressedImage, 0, 0, null);
		} else if (button.getModel().isRollover()) {
			g.drawImage(hoverImage, 0, 0, null);
		} else if (button.getModel().isSelected()) {
			g.drawImage(hoverImage, 0, 0, null);
		} else {
			g.drawImage(normalImage, 0, 0, null);
		}
	}
}
