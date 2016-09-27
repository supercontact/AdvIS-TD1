import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PhotoApplication extends JFrame{

    private static final long serialVersionUID = 1L;
    
    JLabel status = new JLabel();
    JMenuBar menuBar = new JMenuBar();;
    JMenu fileMenu = new JMenu("File");
    JMenu viewMenu = new JMenu("View");
    JMenuItem importItem = new JMenuItem("Import");
    JMenuItem deleteItem = new JMenuItem("Delete");
    JMenuItem clearItem = new JMenuItem("Clean");
    JMenuItem quitItem = new JMenuItem("Quit");
    JMenuItem photoItem = new JMenuItem("Photo viewer");
    JMenuItem browserItem = new JMenuItem("Browser");
    JMenuItem splitItem = new JMenuItem("Split mode");
    JPanel body = new JPanel();
    JScrollPane scrollPane = new JScrollPane();
    PhotoComponent photoView = new PhotoComponent();
    FadePanel controlPanel = new FadePanel();
    FadePanel controlPanelEditMode = new FadePanel();
    JButton prev = new JButton("Prev");
    JButton next = new JButton("Next");
    JButton setColor = new JButton("Set color");
    JLabel setStrokeWidthLabel = new JLabel("Stroke width:");
    JSlider setStrokeWidth = new JSlider();
    JLabel setTextSizeLabel = new JLabel("Text size:");
    JSlider setTextSize = new JSlider();
    JLabel setFontLabel = new JLabel(" Font:");
    JComboBox<String> setFont = new JComboBox<String>(GlobalSettings.fontStrings);
    JToolBar tool = new JToolBar();
    ArrayList<JToggleButton> categories = new ArrayList<>(); 

    public static void main(String[] args) {
    	SavedSettings.loadSettings();
        new PhotoApplication();
    }
   
    public PhotoApplication() {
        super("Assignment #1");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        setPreferredSize(new Dimension(1024, 768));
        setMinimumSize(new Dimension(300, 400));
        setVisible(true);
       
        setupMenuBar();
        setupToolBar();
        setupMainArea();
       
        pack();
    }
    
    private void setupMenuBar() {
    	setJMenuBar(menuBar);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
       
        fileMenu.add(importItem);
        fileMenu.add(deleteItem);
        fileMenu.add(clearItem);
        fileMenu.add(quitItem);
        
        importItem.addActionListener(
        		event -> importImages()
        );
        deleteItem.addActionListener(
        		event -> {
        			if (photoView.deleteCurrentPhoto()) {
        				showStatusText("Photo removed (The original file is still there).");
        			} else {
        				showStatusText("No photo to remove!");
        			}
        		}
        );
        clearItem.addActionListener(
        		event -> {
        			if (photoView.clearCurrentPhoto()) {
        				showStatusText("All annotations and strokes are removed from the photo.");
        			} else {
        				showStatusText("No photo to clean!");
        			}
        		}
        );
        quitItem.addActionListener(
        		event -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        );
       
       
        viewMenu.add(photoItem);
        viewMenu.add(browserItem);
        viewMenu.add(splitItem);
        
        photoItem.addActionListener(
        		event -> showStatusText("Switched to photo viewer mode.")
        );
        browserItem.addActionListener(
        		event -> showStatusText("Switched to browser mode (Not yet supported).")
        );
        splitItem.addActionListener(
        		event -> showStatusText("Switched to split mode (Not yet supported).")
        );
    }
    
    private void setupToolBar() {
    	add(tool, BorderLayout.NORTH);
    	
    	categories.add(new JToggleButton("Family"));
        categories.add(new JToggleButton("Vacation"));
        categories.add(new JToggleButton("School"));
        
        for (JToggleButton button : categories) {
        	button.setMinimumSize(new Dimension(80, 30));
        	button.setPreferredSize(new Dimension(80, 30));
        	button.setMaximumSize(new Dimension(80, 30));
        	tool.add(button);
        	 
        	button.addItemListener(
        			event -> {
        				if (event.getStateChange() == ItemEvent.SELECTED) {
        					showStatusText("Catogory " + ((JToggleButton)event.getItem()).getText() + " is selected (Not yet supported).");
        				} else {
        					showStatusText("Catogory " + ((JToggleButton)event.getItem()).getText() + " is deselected (Not yet supported).");
        				}
        			}
        	);
        }
    }
    
    private void setupMainArea() {
    	add(body, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        
        body.setLayout(new BorderLayout());
        body.add(scrollPane, BorderLayout.CENTER);
        
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.setViewportView(photoView);
        
        photoView.add(controlPanel);
        photoView.add(controlPanelEditMode);
        photoView.controlPanel = controlPanel;
        photoView.controlPanelEditMode = controlPanelEditMode;
        
        controlPanel.setOpaque(false);
        controlPanel.add(prev);
        controlPanel.add(next);
        
        next.addActionListener(
        		event -> photoView.nextPhoto()
        );
        prev.addActionListener(
        		event -> photoView.prevPhoto()
        );
        
        controlPanelEditMode.setOpaque(false);
        controlPanelEditMode.add(setStrokeWidthLabel);
        controlPanelEditMode.add(setStrokeWidth);
        controlPanelEditMode.add(setTextSizeLabel);
        controlPanelEditMode.add(setTextSize);
        controlPanelEditMode.add(setColor);
        controlPanelEditMode.add(setFontLabel);
        controlPanelEditMode.add(setFont);
        
        setStrokeWidth.setOpaque(false);
        setStrokeWidth.setMinimum(1);
        setStrokeWidth.setMaximum(30);
        setStrokeWidth.setValue(5);
        setStrokeWidth.addChangeListener(
        		event -> photoView.currentStrokeWidth = setStrokeWidth.getValue()
        );
        
        setTextSize.setOpaque(false);
        setTextSize.setMinimum(5);
        setTextSize.setMaximum(100);
        setTextSize.setValue(15);
        setTextSize.addChangeListener(
        		event -> photoView.currentTextSize = setTextSize.getValue()
        );
        
        setColor.addActionListener(
        		event -> photoView.currentColor = JColorChooser.showDialog(
        				photoView,
                        "Choose stroke and text color",
                        photoView.currentColor)
        );
        
        setFont.addActionListener(
        		event -> photoView.currentFontName = (String)setFont.getSelectedItem()
        );
       
        showStatusText("Status");
    }
    
	public void showStatusText(String text) {
		status.setText(text);
	}
	
	public void importImages() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(SavedSettings.settings.defaultFileLocation);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
		
		int state = fileChooser.showOpenDialog(this);
		if (state == JFileChooser.CANCEL_OPTION) {
			showStatusText("Operation canceled!");
			return;
		}
		if (state == JFileChooser.ERROR_OPTION) {
			showStatusText("An error happened!");
			return;
		}
		File[] files = fileChooser.getSelectedFiles();
		photoView.addPhotos(files);
		SavedSettings.settings.defaultFileLocation = files[0].getParentFile();
		SavedSettings.saveSettings();
		showStatusText(files.length + " photo(s) are selected. Showing the first photo.");
	}

}