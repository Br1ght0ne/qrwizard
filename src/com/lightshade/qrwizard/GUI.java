package com.lightshade.qrwizard;

//БІБЛІОТЕКИ, НЕОБХІДНІ ДЛЯ РОБОТИ ПРОГРАМИ

//інтерфейс користувача на Swing
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.zxing.NotFoundException;
import com.sun.applet2.preloader.CancelException;
import org.apache.commons.io.FilenameUtils;

public class GUI {

	private JFrame mainFrame;
	private JLabel titleLabel;
	private JPanel controlPanel;
	private JPanel buttonPanel;
	public JTextArea textArea;
	private JButton encodeButton;
	private JButton decodeButton;
	private JButton quitButton;
	public GUI(){
		prepareGUI();
	}
	
	public static void main(String[] args) {
		new GUI();
	}
	
	private void prepareGUI(){
		mainFrame = new JFrame("QRWizard 0.3.0, (c) 2016 Alex Filonenko");
		mainFrame.setSize(800,600);
		mainFrame.setLayout(new GridLayout(3,0));
		mainFrame.addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent windowEvent){
		        System.exit(0);
	         }        
	    });
		titleLabel = new JLabel("QRWizard 0.3.0",JLabel.CENTER );
		float newSize = 50;
		titleLabel.setFont(titleLabel.getFont().deriveFont(newSize));
		textArea = new JTextArea(2,20);
		controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		buttonPanel = new JPanel();
	    buttonPanel.setLayout(new FlowLayout());
	    encodeButton = new JButton("Закодувати");
	    encodeButton.addActionListener(new EncodeActionListener());
	    decodeButton = new JButton("Розкодувати");
	    decodeButton.addActionListener(new DecodeActionListener());
	    quitButton = new JButton("Вихід");
	    quitButton.addActionListener(new QuitActionListener());
	    buttonPanel.add(encodeButton);
	    buttonPanel.add(decodeButton);
	    buttonPanel.add(quitButton);
	    controlPanel.add(buttonPanel, BorderLayout.CENTER);
		mainFrame.add(titleLabel);
		mainFrame.add(textArea);
	    mainFrame.add(controlPanel);
		centerWindow(mainFrame);
		mainFrame.setVisible(true);
	}
	
	private static void centerWindow(Window frame){
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}
	
	class QuitActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
	      }
	}
	
	class EncodeActionListener implements ActionListener {
        private String getFileName() throws CancelException {
            String name = JOptionPane.showInputDialog(mainFrame, "Введіть ім'я для файла (бажано коротке): ");
            if (name == null) {
                throw new CancelException("Користувач відмінив введення імені файла");
            } else {
                return name;
            }
        }
		public void actionPerformed(ActionEvent e) {
            String optionFileName;
            try {
                optionFileName = getFileName();
            } catch (CancelException ce) {
                ce.printStackTrace();
                return;
            }
			String filePath = QrWizard.encode(textArea.getText(), optionFileName);
			JOptionPane.showMessageDialog(null, "Код успішно створений та знаходиться у файлі " + filePath + ".",
					"Результат:", JOptionPane.INFORMATION_MESSAGE);
	      }
	}
	
	class DecodeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileopen = new JFileChooser(".");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "jpg");
			fileopen.setFileFilter(filter);
			int ret = fileopen.showDialog(null, "Відкрити файл");
			if (ret == JFileChooser.APPROVE_OPTION) {
			    File file = fileopen.getSelectedFile();
			    try {
                    String path = file.getPath();
                    System.err.println("PATH: " + path);
                    String ext = FilenameUtils.getExtension(path);
                    System.err.println("EXT: " + ext);
                    if ( !ext.equals("png") ) {
                        throw new FileExtensionException("Неправильне розширення файла");
                    }
			    	String result = QrWizard.decode(file);
			    	JOptionPane.showMessageDialog(null, result,
	                        "Результат:", JOptionPane.PLAIN_MESSAGE);
			    } catch (FileNotFoundException fnfe) {
			    	fnfe.printStackTrace();
			    } catch (IOException ioe) {
			    	ioe.printStackTrace();
			    } catch (NotFoundException nfe) {
			    	nfe.printStackTrace();
			    } catch (FileExtensionException fee) {
                    JOptionPane.showMessageDialog(mainFrame,
                            fee.getMessage(),
                            "Помилка!",
                            JOptionPane.ERROR_MESSAGE);
                }
			}
	      }
	}

	class FileExtensionException extends Exception {
        public FileExtensionException() {}
        public FileExtensionException(String message) {
            super(message);
        }
    }
}
