package com.github.freeacs.web.app.util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;



/**
 * A Swing dialog that helps converting normal html to XML encoded data.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class HelpTextConverter extends JFrame implements ActionListener,WindowListener,ClipboardOwner{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1L;
	
	/** The input. */
	private JTextArea input;
	
	/** The button. */
	private JButton button;
	
	/**
	 * Instantiates a new help text converter.
	 */
	public HelpTextConverter(){
		setTitle("Helpt text converter");
		Container con = this.getContentPane();
		setLayout(new BorderLayout());
		con.add((input=new JTextArea()),BorderLayout.CENTER);
		input.setText("<p>Input text here that needs to be escaped for use in XML documents</p>");
		con.add((button=new JButton("Convert text and place in clipboard")),BorderLayout.SOUTH);
		button.addActionListener(this);
		setSize(800,600);
		setVisible(true);
		addWindowListener(this);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent arg0) {
		this.dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		input.setText(EscapeChars.forXML(input.getText()));
		StringSelection data = new StringSelection(input.getText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(data, data);
		JOptionPane.showMessageDialog(this, "The text is now in the clipboard");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
		
	}
}
