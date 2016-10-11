package component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import custom.ResourceManager;
import model.AnnotatedPhoto;

// This component is shown in the tag management menu, representing a tag that you can select/rename/delete.
public class TagOption extends JPanel implements MouseListener, KeyListener, ActionListener, FocusListener {
	
	private static final long serialVersionUID = 1L;

	public String name;
	
	public JLabel tagName;
	public JTextField tagNameEditing;
	public JButton deleteButton;
	public JCheckBox checkBox;
	
	public TagOption(String name) {
		this.name = name;
		setOpaque(false);
		Setup();
	}
	
	private void Setup() {
		setPreferredSize(new Dimension(300, 40));
		setMaximumSize(new Dimension(300, 40));
		setMinimumSize(new Dimension(300, 40));
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setLayout(null);
		
		checkBox = new JCheckBox("");
		tagName = new JLabel(name);
		tagNameEditing = new JTextField(name);
		deleteButton = new JButton("Remove Tag");
		add(checkBox);
		add(tagName);
		add(tagNameEditing);
		add(deleteButton);
		
		checkBox.setVisible(false);
		checkBox.setBounds(new Rectangle(5, 5, 30, 30));
		checkBox.setMargin(new Insets(0, 0, 0, 0));
		checkBox.addActionListener(this);
		tagName.setBounds(new Rectangle(40, 5, 200, 30));
		tagName.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		tagName.addMouseListener(this);
		tagNameEditing.setBounds(new Rectangle(40, 5, 200, 30));
		tagNameEditing.setVisible(false);
		tagNameEditing.addFocusListener(this);
		tagNameEditing.addKeyListener(this);
		deleteButton.setUI(new ColorImageButtonUI(ResourceManager.trashIcon, 
				new Color(192, 192, 192), 
				new Color(255, 80, 55), 
				new Color(255, 25, 0), 
				new Color(0, 0, 0), 
				new Color(192, 192, 192)
		));
		deleteButton.setBounds(new Rectangle(250, 7, 25, 25));
		deleteButton.setBorder(null);
		deleteButton.setMargin(new Insets(0, 0, 0, 0));
		deleteButton.addActionListener(
				event -> PhotoApplication.app.removeTag(name)
		);
		
		repaint();
	}
	
	public void setRelatedPhotos(Set<AnnotatedPhoto> photos) {
		checkBox.setVisible(!photos.isEmpty());
		
		if (photos.isEmpty()) return;
		
		int taggedPhotoCount = 0;
		for (AnnotatedPhoto photo : photos) {
			if (photo.tags.contains(name)) {
				taggedPhotoCount++;
			}
		}
		if (taggedPhotoCount == photos.size()) {
			checkBox.setSelected(true);
		} else if (taggedPhotoCount == 0) {
			checkBox.setSelected(false);
		} else {
			// Should have a third "partial selection" state. To be implemented
			checkBox.setSelected(false);
		}
	}
	
	public void startEditing() {
		tagName.setVisible(false);
		tagNameEditing.setVisible(true);
		tagNameEditing.selectAll();
		tagNameEditing.requestFocus();
	}
	
	public void endEditing() {
		String oldName = tagName.getText();
		name = tagNameEditing.getText();
		tagName.setText(name);
		tagName.setVisible(true);
		tagNameEditing.setVisible(false);
		PhotoApplication.app.renameTag(oldName, name);
	}

	
	// MouseListener: Mouse events
	@Override
	public void mouseClicked(MouseEvent e) {
		startEditing();
	}
	@Override
	public void mousePressed(MouseEvent e) {} // Do nothing
	@Override
	public void mouseReleased(MouseEvent e) {} // Do nothing
	@Override
	public void mouseEntered(MouseEvent e) {} // Do nothing
	@Override
	public void mouseExited(MouseEvent e) {} // Do nothing

	
	// KeyListener: Key events
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			endEditing();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {} // Do nothing
	@Override
	public void keyTyped(KeyEvent e) {} // Do nothing


	// FocusListener: Focus events
	@Override
	public void focusLost(FocusEvent e) {
		endEditing();
	}
	@Override
	public void focusGained(FocusEvent e) {} // Do nothing
	
	
	// ActionListener: Check box event
	@Override
	public void actionPerformed(ActionEvent e) {
		if (checkBox.isSelected()) {
			PhotoApplication.app.model.addTagToSelectedPhotos(name);
		} else {
			PhotoApplication.app.model.removeTagFromSelectedPhotos(name);
		}
	}
}
