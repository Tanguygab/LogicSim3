package io.github.tanguygab.logicsim3;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LSFrame extends JFrame implements ActionListener, CircuitChangedListener {

	private static final long serialVersionUID = -5281157929385660575L;

	io.github.tanguygab.logicsim3.LogicSimFile lsFile;

	JMenuBar mnuBar;
	JToolBar btnBar;

	DefaultListModel<Object> partListModel = new DefaultListModel<Object>();
	JList<Object> lstParts = new JList<Object>(partListModel);
	JComboBox<String> cbNumInputs = null;
	io.github.tanguygab.logicsim3.LSPanel lspanel = new io.github.tanguygab.logicsim3.LSPanel();

	JLabel sbText = new JLabel();
	JLabel sbCoordinates = new JLabel();

	int popupGateIdx;
	JPopupMenu popup;
	JMenuItem menuItem_remove;
	JMenuItem menuItem_properties;

	public LSFrame(String title) {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		lsFile = new io.github.tanguygab.logicsim3.LogicSimFile(defaultCircuitFileName());
		lsFile.circuit.setRepaintListener(lspanel);
		lspanel.setChangeListener(this);

		try {
			createUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ask if we should close
	 */
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (showDiscardDialog(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.EXIT)) == false)
				return;
			System.exit(0);
		}
	}

	private String defaultModuleFileName() {
		String fn = io.github.tanguygab.logicsim3.App.getModulePath();
		fn += io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.UNNAMED);
		fn += "." + io.github.tanguygab.logicsim3.App.MODULE_FILE_SUFFIX;
		return fn;
	}

	private String defaultCircuitFileName() {
		String fn = io.github.tanguygab.logicsim3.App.getCircuitPath();
		fn += io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.UNNAMED);
		fn += "." + io.github.tanguygab.logicsim3.App.CIRCUIT_FILE_SUFFIX;
		return fn;
	}

	/** Component initialization */
	private void createUI() {
		setTitle("LogicSim");

		String mode = io.github.tanguygab.logicsim3.LSProperties.getInstance().getProperty(io.github.tanguygab.logicsim3.LSProperties.MODE, io.github.tanguygab.logicsim3.LSProperties.MODE_NORMAL);

		mnuBar = new JMenuBar();

		JMenu mnu = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.FILE));

		JMenuItem m = createMenuItem(io.github.tanguygab.logicsim3.Lang.NEW, KeyEvent.VK_N, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionNew(e);
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.OPEN, KeyEvent.VK_O, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOpen(e);
			}
		});
		mnu.add(m);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.SAVE, KeyEvent.VK_S, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSave(e, false);
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.SAVEAS, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSave(e, true);
			}
		});
		mnu.add(m);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.MODULECREATE, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionCreateModule(e);
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.PROPERTIES, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (io.github.tanguygab.logicsim3.FileInfoDialog.showFileInfo(io.github.tanguygab.logicsim3.LSFrame.this, lsFile)) {
					setAppTitle();
				}
			}
		});
		mnu.add(m);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.EXPORT, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportImage();
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.PRINT, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.doPrint();
			}
		});
		mnu.add(m);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.EXIT, KeyEvent.VK_X, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (showDiscardDialog(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.EXIT)) == false)
					return;
				System.exit(0);
			}
		});
		mnu.add(m);

		mnuBar.add(mnu);

		// ------------------------------------------------------------------
		// EDIT
		mnu = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.EDIT));

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.SELECTALL, KeyEvent.VK_A, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.circuit.selectAll();
				lspanel.repaint();
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.SELECT, 0, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.LSPanel.ACTION_SELECT);
				lspanel.requestFocusInWindow();
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.SELECTNONE, 0, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.circuit.deselectAll();
				lspanel.repaint();
			}
		});
		mnu.add(m);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.WIRENEW, KeyEvent.VK_W, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getButtonWidget(io.github.tanguygab.logicsim3.Lang.WIRENEW).doClick();
			}
		});
		m.setEnabled(io.github.tanguygab.logicsim3.LSProperties.MODE_EXPERT.equals(mode));
		mnu.add(m);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.INPUTHIGH, 0, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.Pin.HIGH);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTHIGH_HELP));
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.INPUTLOW, 0, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.Pin.LOW);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTLOW_HELP));
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.INPUTINV, 0, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.Pin.INVERTED);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTINV_HELP));
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.INPUTNORM, 0, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.Pin.NORMAL);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTNORM_HELP));
			}
		});
		mnu.add(m);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.ROTATE, 0, false);
		m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, false));
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.rotateSelected();
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.MIRROR, KeyEvent.VK_M, false);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.mirrorSelected();
			}
		});
		mnu.add(m);

		mnuBar.add(mnu);
		// ------------------------------------------------------------------
		// SETTINGS
		mnu = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SETTINGS));

		boolean sel = io.github.tanguygab.logicsim3.LSProperties.getInstance().getPropertyBoolean(io.github.tanguygab.logicsim3.LSProperties.PAINTGRID, true);
		final JCheckBoxMenuItem mSettingsPaintGrid = new JCheckBoxMenuItem(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.PAINTGRID));
		mSettingsPaintGrid.setSelected(sel);
		mSettingsPaintGrid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				io.github.tanguygab.logicsim3.LSProperties.getInstance().setPropertyBoolean(io.github.tanguygab.logicsim3.LSProperties.PAINTGRID, mSettingsPaintGrid.isSelected());
				lspanel.repaint();
			}
		});
		mnu.add(mSettingsPaintGrid);

		boolean autowire = io.github.tanguygab.logicsim3.LSProperties.getInstance().getPropertyBoolean(io.github.tanguygab.logicsim3.LSProperties.AUTOWIRE, true);
		final JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.AUTOWIRE));
		cbMenuItem.setSelected(autowire);
		cbMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem bmi = (JCheckBoxMenuItem) e.getSource();
				io.github.tanguygab.logicsim3.LSProperties.getInstance().setPropertyBoolean(io.github.tanguygab.logicsim3.LSProperties.AUTOWIRE, bmi.isSelected());
				lspanel.repaint();
			}
		});
		mnu.add(cbMenuItem);

		m = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.GATEDESIGN));
		String gatedesign = io.github.tanguygab.logicsim3.LSProperties.getInstance().getProperty(io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN,
				io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN_IEC);

		JRadioButtonMenuItem mGatedesignIEC = new JRadioButtonMenuItem();
		mGatedesignIEC.setText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.GATEDESIGN_IEC));
		mGatedesignIEC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionGateDesign(e);
			}
		});
		mGatedesignIEC.setSelected(io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN_IEC.equals(gatedesign));
		m.add(mGatedesignIEC);

		JRadioButtonMenuItem mGatedesignANSI = new JRadioButtonMenuItem();
		mGatedesignANSI.setText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.GATEDESIGN_ANSI));
		mGatedesignANSI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionGateDesign(e);
			}
		});
		mGatedesignANSI.setSelected(io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN_ANSI.equals(gatedesign));
		m.add(mGatedesignANSI);

		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(mGatedesignIEC);
		btnGroup.add(mGatedesignANSI);

		mnu.add(m);

		JMenu mnuMode = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.MODE));
		btnGroup = new ButtonGroup();

		JRadioButtonMenuItem mnuItem = new JRadioButtonMenuItem(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.NORMAL));
		mnuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionMode(e);
			}
		});
		mnuItem.setSelected(io.github.tanguygab.logicsim3.LSProperties.MODE_NORMAL.equals(mode));
		btnGroup.add(mnuItem);
		mnuMode.add(mnuItem);

		mnuItem = new JRadioButtonMenuItem(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.EXPERT));
		mnuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionMode(e);
			}
		});
		mnuItem.setSelected(io.github.tanguygab.logicsim3.LSProperties.MODE_EXPERT.equals(mode));
		btnGroup.add(mnuItem);
		mnuMode.add(mnuItem);

		mnu.add(mnuMode);

		// ---------------------------------------------------------------

		m = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.COLORMODE));
		btnGroup = new ButtonGroup();
		String cMode = io.github.tanguygab.logicsim3.LSProperties.getInstance().getProperty(io.github.tanguygab.logicsim3.LSProperties.COLORMODE, io.github.tanguygab.logicsim3.LSProperties.COLORMODE_ON);

		JRadioButtonMenuItem mCmOn = new JRadioButtonMenuItem();
		mCmOn.setText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.COLORMODE_ON));
		mCmOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionColorMode(e);
			}
		});
		mCmOn.setSelected(io.github.tanguygab.logicsim3.LSProperties.COLORMODE_ON.equals(cMode));
		m.add(mCmOn);

		JRadioButtonMenuItem mCmOff = new JRadioButtonMenuItem();
		mCmOff.setText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.COLORMODE_OFF));
		mCmOff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionColorMode(e);
			}
		});
		mCmOff.setSelected(io.github.tanguygab.logicsim3.LSProperties.COLORMODE_OFF.equals(cMode));
		m.add(mCmOff);

		btnGroup.add(mCmOn);
		btnGroup.add(mCmOff);

		mnu.add(m);

		JMenu mnuLang = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.LANGUAGE));
		String currentLanguage = io.github.tanguygab.logicsim3.LSProperties.getInstance().getProperty(io.github.tanguygab.logicsim3.LSProperties.LANGUAGE, "de");
		createLanguageMenu(mnuLang, currentLanguage);
		mnu.add(mnuLang);

		mnu.addSeparator();

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.GATESETTINGS, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.gateSettings();
			}
		});
		mnu.add(m);

		mnuBar.add(mnu);

		// ------------------------------------------------------------------
		// HELP
		mnu = new JMenu(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.HELP));

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.HELP, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new HTMLHelp();
			}
		});
		mnu.add(m);

		m = createMenuItem(io.github.tanguygab.logicsim3.Lang.ABOUT, 0, true);
		m.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new LSFrame_AboutBox(io.github.tanguygab.logicsim3.LSFrame.this);
			}
		});
		mnu.add(m);

		mnuBar.add(mnu);

		setJMenuBar(mnuBar);

		// ------------------------------------------------------------------
		// compose GUI

		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BorderLayout());
		statusBar.add(sbText, BorderLayout.WEST);
		statusBar.add(sbCoordinates, BorderLayout.EAST);
		statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setStatusText(" ");
		// sbText.setPreferredSize(new Dimension(700, 15));
		sbCoordinates.setText(" ");
		// sbCoordinates.setPreferredSize(new Dimension(200, 20));
		add(statusBar, BorderLayout.SOUTH);

		lspanel.setPreferredSize(new Dimension(1000, 600));
		lspanel.setBackground(Color.white);
		lspanel.setDoubleBuffered(true);

		lstParts.addMouseListener(new PopupListener());
		lstParts.setCellRenderer(new GateListRenderer());
		lstParts.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				actionLstGatesSelected(e);
			}
		});

		String[] gateInputNums = new String[4];
		for (int i = 0; i < 4; i++) {
			gateInputNums[i] = (i + 2) + " " + io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTS);
		}
		cbNumInputs = new JComboBox<String>(gateInputNums);

		JPanel pnlGateList = new JPanel();
		pnlGateList.setLayout(new BorderLayout());
		pnlGateList.setPreferredSize(new Dimension(120, 200));
		pnlGateList.setMinimumSize(new Dimension(100, 200));
		pnlGateList.add(new JScrollPane(lstParts), BorderLayout.CENTER);
		pnlGateList.add(cbNumInputs, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(170);
		splitPane.add(pnlGateList, JSplitPane.LEFT);
		splitPane.add(lspanel, JSplitPane.RIGHT);

		getContentPane().add(splitPane, BorderLayout.CENTER);

		btnBar = new JToolBar();

		io.github.tanguygab.logicsim3.LSButton btnLS = new io.github.tanguygab.logicsim3.LSButton("new", io.github.tanguygab.logicsim3.Lang.NEW);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionNew(e);
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("open", io.github.tanguygab.logicsim3.Lang.OPEN);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOpen(e);
			}
		});
		btnBar.add(btnLS);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("save", io.github.tanguygab.logicsim3.Lang.SAVE);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSave(e, false);
			}
		});
		btnBar.add(btnLS);

		btnBar.add(getMenuGap());

		io.github.tanguygab.logicsim3.LSToggleButton btnToggle = new io.github.tanguygab.logicsim3.LSToggleButton("play", io.github.tanguygab.logicsim3.Lang.SIMULATE);
		btnToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSimulate(e);
			}
		});
		btnBar.add(btnToggle, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("reset", io.github.tanguygab.logicsim3.Lang.RESET);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.circuit.reset();
				lspanel.repaint();
			}
		});
		btnBar.add(btnLS, null);

		btnBar.add(getMenuGap());

		btnLS = new io.github.tanguygab.logicsim3.LSButton("zoomout", io.github.tanguygab.logicsim3.Lang.ZOOMOUT);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.zoomOut();
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("zoomall", io.github.tanguygab.logicsim3.Lang.ZOOMALL);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.zoomAll();
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("zoomin", io.github.tanguygab.logicsim3.Lang.ZOOMIN);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.zoomIn();
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnToggle = new io.github.tanguygab.logicsim3.LSToggleButton("select", io.github.tanguygab.logicsim3.Lang.SELECT);
		btnToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.LSPanel.ACTION_SELECT);
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnToggle, null);

		// btnLS = new LSButton("select", Lang.SELECT);
		// btnLS.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// lspanel.setAction(LSPanel.ACTION_SELECT);
		// lspanel.requestFocusInWindow();
		// }
		// });
		// btnBar.add(btnLS, null);

		btnBar.add(getMenuGap());

		btnLS = new io.github.tanguygab.logicsim3.LSButton("rotate", io.github.tanguygab.logicsim3.Lang.ROTATE);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.rotateSelected();
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("mirror", io.github.tanguygab.logicsim3.Lang.MIRROR);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.mirrorSelected();
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS);

		btnBar.add(getMenuGap());

		btnLS = new io.github.tanguygab.logicsim3.LSButton("inputnorm", io.github.tanguygab.logicsim3.Lang.INPUTNORM);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.Pin.NORMAL);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTNORM_HELP));
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("inputinv", io.github.tanguygab.logicsim3.Lang.INPUTINV);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.Pin.INVERTED);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTINV_HELP));
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("inputhigh", io.github.tanguygab.logicsim3.Lang.INPUTHIGH);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.Pin.HIGH);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTHIGH_HELP));
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("inputlow", io.github.tanguygab.logicsim3.Lang.INPUTLOW);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(Pin.LOW);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.INPUTLOW_HELP));
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnBar.add(getMenuGap());

		btnLS = new io.github.tanguygab.logicsim3.LSButton("newwire", io.github.tanguygab.logicsim3.Lang.WIRENEW);
		btnLS.setEnabled(getMenuWidget(io.github.tanguygab.logicsim3.Lang.WIRENEW).isEnabled());
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.LSPanel.ACTION_ADDWIRE);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.WIRENEW_HELP));
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new io.github.tanguygab.logicsim3.LSButton("addpoint", io.github.tanguygab.logicsim3.Lang.ADDPOINT);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.LSPanel.ACTION_ADDPOINT);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.ADDPOINT_HELP));
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		btnLS = new LSButton("delpoint", io.github.tanguygab.logicsim3.Lang.REMOVEPOINT);
		btnLS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lspanel.setAction(io.github.tanguygab.logicsim3.LSPanel.ACTION_DELPOINT);
				setStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.REMOVEPOINT_HELP));
				lspanel.requestFocusInWindow();
			}
		});
		btnBar.add(btnLS, null);

		add(btnBar, BorderLayout.NORTH);

		// ------------------------------------------------------------------
		// Create the popup menu.
		popup = new JPopupMenu();
		menuItem_remove = new JMenuItem(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.REMOVEGATE));
		menuItem_remove.addActionListener(this);
		popup.add(menuItem_remove);
		menuItem_properties = new JMenuItem(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.PROPERTIES));
		menuItem_properties.addActionListener(this);
		popup.add(menuItem_properties);
		// Add listener to components that can bring up popup menus.
		lspanel.addMouseListener(new PopupListener());

		fillGateList();
		setAppTitle();

		lspanel.requestFocusInWindow();
	}

	private JMenuItem createMenuItem(io.github.tanguygab.logicsim3.Lang lang, int key, boolean isDialog) {
		JMenuItem m = new JMenuItem(io.github.tanguygab.logicsim3.I18N.tr(lang) + (isDialog ? "..." : ""));
		if (key != 0)
			m.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK, false));
		m.setName(lang.toString());
		return m;
	}

	private void setStatusText(String string) {
		sbText.setText("  " + string);
	}

	private Component getMenuGap() {
		int is = io.github.tanguygab.logicsim3.LSProperties.getInstance().getPropertyInteger("iconsize", 48);
		return Box.createHorizontalStrut(is / 2);
	}

	/**
	 * handles popup menus
	 */
	public void actionPerformed(ActionEvent e) { // popup menu
		JMenuItem source = (JMenuItem) (e.getSource());
		if (source == menuItem_remove) {
			lspanel.circuit.removeGateIdx(popupGateIdx);
			lspanel.repaint();
		} else if (source == menuItem_properties) {
			if (popupGateIdx >= 0) {
				io.github.tanguygab.logicsim3.Gate g = (io.github.tanguygab.logicsim3.Gate) lspanel.circuit.parts.get(popupGateIdx);
				g.showPropertiesUI(this);
				lspanel.repaint();
			}
		}
	}

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (e.getSource() == lspanel) {
					for (int i = 0; i < lspanel.circuit.parts.size(); i++) {
						io.github.tanguygab.logicsim3.CircuitPart part = lspanel.circuit.parts.get(i);
						if (part instanceof io.github.tanguygab.logicsim3.Gate) {
							io.github.tanguygab.logicsim3.Gate g = (io.github.tanguygab.logicsim3.Gate) part;
							if (g.insideFrame(e.getX(), e.getY())) {
								popupGateIdx = i;
								menuItem_properties.setEnabled(g.hasPropertiesUI());
								popup.show(e.getComponent(), e.getX(), e.getY());
								break;
							}
						}
					}
				}
			}
		}
	}

	void actionSimulate(ActionEvent e) {
		io.github.tanguygab.logicsim3.LSToggleButton btn = (io.github.tanguygab.logicsim3.LSToggleButton) e.getSource();

		if (btn.isSelected()) {
			if (!io.github.tanguygab.logicsim3.Simulation.getInstance().isRunning()) {
				lspanel.circuit.deselectAll();
				repaint();
				io.github.tanguygab.logicsim3.Simulation.getInstance().start();
				changedStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SIMULATION_STARTED));
			}
		} else {
			if (io.github.tanguygab.logicsim3.Simulation.getInstance().isRunning()) {
				io.github.tanguygab.logicsim3.Simulation.getInstance().stop();
				changedStatusText(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SIMULATION_STOPPED));
			}
		}
	}

	boolean showDiscardDialog(String title) {
		if (lsFile.changed) {
			int result = io.github.tanguygab.logicsim3.Dialogs.confirmDiscardDialog(this);
			return (result == JOptionPane.YES_OPTION);
		}
		return true;
	}

	/**
	 * handles initial steps to create a new circuit file
	 * 
	 * @param e
	 */
	void actionNew(ActionEvent e) {
		if (showDiscardDialog(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.NEW)) == false)
			return;
		lsFile = new io.github.tanguygab.logicsim3.LogicSimFile(defaultCircuitFileName());
		lsFile.circuit.setRepaintListener(lspanel);
		setAppTitle();
		lspanel.clear();
	}

	/**
	 * handles opening of files
	 * 
	 * @param e
	 */
	void actionOpen(ActionEvent e) {
		if (io.github.tanguygab.logicsim3.Simulation.getInstance().isRunning())
			io.github.tanguygab.logicsim3.Simulation.getInstance().stop();

		if (showDiscardDialog(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.OPEN)) == false)
			return;

		File file = new File(lsFile.fileName);
		JFileChooser chooser = new JFileChooser(file.getParent());
		chooser.setFileFilter(setupFilter());
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			lsFile.fileName = chooser.getSelectedFile().getAbsolutePath();
		} else
			return;

		try {
			lsFile = XMLLoader.loadXmlFile(lsFile.fileName);
		} catch (RuntimeException x) {
			System.err.println(x);
			x.printStackTrace(System.err);
			io.github.tanguygab.logicsim3.Dialogs.messageDialog(this, io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.READERROR) + " " + x.getMessage());
		}
		if (lsFile.getErrorString() != null) {
			io.github.tanguygab.logicsim3.Dialogs.messageDialog(this, lsFile.getErrorString());
		}
		setAppTitle();
		lspanel.clear();
		lspanel.circuit = lsFile.circuit;
		lspanel.circuit.setRepaintListener(lspanel);
		lspanel.circuit.reset();
	}

	/**
	 * setup a file filter for displaying files who have the correct ending
	 * 
	 * @return
	 */
	private FileFilter setupFilter() {
		io.github.tanguygab.logicsim3.LogicSimFileFilter filter = new io.github.tanguygab.logicsim3.LogicSimFileFilter();
		filter.addExtension(io.github.tanguygab.logicsim3.App.CIRCUIT_FILE_SUFFIX);
		filter.addExtension(io.github.tanguygab.logicsim3.App.MODULE_FILE_SUFFIX);
		filter.setDescription(
				"LogicSim Files (" + "." + io.github.tanguygab.logicsim3.App.CIRCUIT_FILE_SUFFIX + ", " + "." + io.github.tanguygab.logicsim3.App.MODULE_FILE_SUFFIX + ")");
		return filter;
	}

	/**
	 * set window title
	 */
	private void setAppTitle() {
		String name = lsFile.extractFileName();
		name = "LogicSim - " + name;
		if (lsFile.changed)
			name += "*";
		this.setTitle(name);
	}

	/**
	 * handles saving of circuit file
	 * 
	 * @param e
	 */
	void actionSave(ActionEvent e, boolean saveAs) {
		String fileName = lsFile.fileName;
		boolean unnamed = false;
		if (lsFile.extractFileName().equals(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.UNNAMED))) {
			unnamed = true;
		}
		boolean showDialog = fileName == null || fileName.length() == 0;
		showDialog = showDialog || unnamed;
		showDialog = showDialog || saveAs;

		if (showDialog)
			if (showSaveDialog() == false)
				return;
		lsFile.circuit = lspanel.circuit;
		try {
			XMLCreator.createXML(lsFile);
		} catch (RuntimeException err) {
			System.err.println(err);
			err.printStackTrace(System.err);
			io.github.tanguygab.logicsim3.Dialogs.messageDialog(this, io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SAVEERROR) + " " + err.getMessage());
		}

		setAppTitle();
		setStatusText(String.format(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SAVED), lsFile.fileName));
		lsFile.changed = false;
		fillGateList();
	}

	/**
	 * helper method to show the save dialog
	 * 
	 * @return
	 */
	public boolean showSaveDialog() {
		File file = new File(lsFile.fileName);
		String parentDirName = file.getParent();

		JFileChooser chooser = new JFileChooser(parentDirName);
		chooser.setDialogTitle(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SAVECIRCUIT));

		String s = "LogicSim Files (" + "." + io.github.tanguygab.logicsim3.App.CIRCUIT_FILE_SUFFIX + ", " + "." + io.github.tanguygab.logicsim3.App.MODULE_FILE_SUFFIX + ")";
		FileNameExtensionFilter filter = new FileNameExtensionFilter(s, io.github.tanguygab.logicsim3.App.CIRCUIT_FILE_SUFFIX,
				io.github.tanguygab.logicsim3.App.MODULE_FILE_SUFFIX);
		chooser.setFileFilter(filter);

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lsFile.fileName = chooser.getSelectedFile().getAbsolutePath();
			// check fileName
			int lastSeparator = lsFile.fileName.lastIndexOf(File.separatorChar);
			int lastDot = lsFile.fileName.lastIndexOf(".");
			if (lastDot < lastSeparator) {
				// ending is missing
				if (lsFile.circuit.isModule())
					lsFile.fileName += "." + io.github.tanguygab.logicsim3.App.MODULE_FILE_SUFFIX;
				else
					lsFile.fileName += "." + io.github.tanguygab.logicsim3.App.CIRCUIT_FILE_SUFFIX;
			}
			return true;
		} else
			return false;
	}

	/**
	 * handles initial steps to create a new module
	 * 
	 * @param e
	 */
	void actionCreateModule(ActionEvent e) {
		if (lsFile.circuit.isModule()) {
			io.github.tanguygab.logicsim3.Dialogs.messageDialog(this, io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.ALREADYMODULE));
			return;
		}

		if (!lsFile.circuit.isEmpty()) {
			String filename = lsFile.extractFileName();
			String fn = io.github.tanguygab.logicsim3.App.getModulePath();
			fn += filename;
			fn += "." + io.github.tanguygab.logicsim3.App.MODULE_FILE_SUFFIX;
			lsFile.fileName = fn;
			lsFile.changed = true;
		} else {
			lsFile = new LogicSimFile(defaultModuleFileName());
			lsFile.circuit.setRepaintListener(lspanel);
		}

		if (!FileInfoDialog.showFileInfo(this, lsFile))
			return;

		setAppTitle();

		io.github.tanguygab.logicsim3.Gate g = new MODIN();
		g.moveTo(150, 100);
		g.loadProperties();
		lsFile.circuit.addGate(g);
		g = new MODOUT();
		g.moveTo(650, 100);
		g.loadProperties();
		lsFile.circuit.addGate(g);
		lspanel.circuit = lsFile.circuit;
		lspanel.circuit.setRepaintListener(lspanel);
		lspanel.repaint();
	}

	/**
	 * save image in file system
	 */
	void exportImage() {
		String filename = "logicsim.png";
		JFileChooser chooser = new JFileChooser();
		io.github.tanguygab.logicsim3.LogicSimFileFilter filter = new LogicSimFileFilter();
		filter.addExtension(".png");
		filter.setDescription("Portable Network Graphics");
		chooser.setFileFilter(filter);

		chooser.setDialogTitle(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SAVECIRCUIT));
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			filename = chooser.getSelectedFile().getAbsolutePath();
			if (!filename.endsWith(".png")) {
				filename += ".png";
			}
		} else {
			return;
		}

		BufferedImage image = (BufferedImage) this.createImage(this.lspanel.getWidth(), this.lspanel.getHeight());
		Graphics g = image.getGraphics();
		lspanel.circuit.deselectAll();
		lspanel.paint(g);
		try {
			ImageIO.write(image, "png", new File(filename));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * fill gate list
	 */
	void fillGateList() {
		partListModel.clear();
		for (Category cat : App.cats) {
			if ("hidden".equals(cat.title))
				continue;
			if (cat.getGates().size() == 0)
				continue;
			partListModel.addElement(cat.title);
			for (io.github.tanguygab.logicsim3.Gate g : cat.getGates()) {
				partListModel.addElement(g);
			}
		}
	}

	/**
	 * handles gates list
	 * 
	 * @param e
	 */
	void actionLstGatesSelected(ListSelectionEvent e) {
		if (Simulation.getInstance().isRunning())
			return;
		
		int sel = lstParts.getSelectedIndex();
		if (sel < 0)
			return;
		int numInputs = Integer.parseInt(cbNumInputs.getSelectedItem().toString().substring(0, 1));

		Object o = lstParts.getSelectedValue();
		if (!(o instanceof io.github.tanguygab.logicsim3.Gate))
			return;

		io.github.tanguygab.logicsim3.Gate gate = (io.github.tanguygab.logicsim3.Gate) o;
		// gate is normal gate or module
		gate = GateLoaderHelper.create((Gate) o);
		if (gate instanceof io.github.tanguygab.logicsim3.Module) {
			io.github.tanguygab.logicsim3.Module m = (Module) gate;
			lspanel.setAction(m);
			if (m.lsFile.getDescription() != null)
				setStatusText(m.lsFile.getDescription());
			else
				setStatusText(m.type);
			lspanel.requestFocusInWindow();
		} else {
			// gate is normal Gate-Object
			if (gate.supportsVariableInputs())
				gate.createDynamicInputs(numInputs);
			lspanel.setAction(gate);

			if (gate.type.contains("test"))
				setStatusText(gate.type);
			else if (io.github.tanguygab.logicsim3.I18N.hasString(gate.type, "description")) {
				setStatusText(io.github.tanguygab.logicsim3.I18N.getString(gate.type, "description"));
			} else {
				setStatusText(io.github.tanguygab.logicsim3.I18N.getString(gate.type, "title"));
			}
			lspanel.requestFocusInWindow();
		}
		lstParts.clearSelection();
	}

	/**
	 * handles gate design (IEC/ISO)
	 * 
	 * @param e
	 */
	void actionGateDesign(ActionEvent e) {
		String gatedesign = null;
		JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
		if (src.getText().equals(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.GATEDESIGN_IEC))) {
			if (src.isSelected())
				gatedesign = io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN_IEC;
			else
				gatedesign = io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN_ANSI;
		} else {
			if (src.isSelected())
				gatedesign = io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN_ANSI;
			else
				gatedesign = io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN_IEC;
		}
		io.github.tanguygab.logicsim3.LSProperties.getInstance().setProperty(io.github.tanguygab.logicsim3.LSProperties.GATEDESIGN, gatedesign);
		this.lspanel.repaint();
	}

	/**
	 * handles color mode (on-redblack / off - blackwhite for printing)
	 * 
	 * @param e
	 */
	private void actionColorMode(ActionEvent e) {
		String mode = null;
		JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
		if (src.getText().equals(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.COLORMODE_ON))) {
			if (src.isSelected())
				mode = io.github.tanguygab.logicsim3.LSProperties.COLORMODE_ON;
			else
				mode = io.github.tanguygab.logicsim3.LSProperties.COLORMODE_OFF;
		} else {
			// the expert item is clicked
			if (src.isSelected()) {
				mode = io.github.tanguygab.logicsim3.LSProperties.COLORMODE_OFF;
			} else {
				mode = io.github.tanguygab.logicsim3.LSProperties.COLORMODE_ON;
			}
		}
		io.github.tanguygab.logicsim3.LSProperties.getInstance().setProperty(io.github.tanguygab.logicsim3.LSProperties.COLORMODE, mode);

		Wire.setColorMode();

		this.lspanel.repaint();
	}

	/**
	 * handles mode (normal/expert)
	 * 
	 * @param e
	 */
	void actionMode(ActionEvent e) {
		String mode = null;
		JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
		if (src.getText().equals(io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.NORMAL))) {
			if (src.isSelected())
				mode = io.github.tanguygab.logicsim3.LSProperties.MODE_NORMAL;
			else
				mode = io.github.tanguygab.logicsim3.LSProperties.MODE_EXPERT;
		} else {
			// the expert item is clicked
			if (src.isSelected()) {
				mode = io.github.tanguygab.logicsim3.LSProperties.MODE_EXPERT;
			} else {
				mode = io.github.tanguygab.logicsim3.LSProperties.MODE_NORMAL;
			}
		}
		io.github.tanguygab.logicsim3.LSProperties.getInstance().setProperty(io.github.tanguygab.logicsim3.LSProperties.MODE, mode);

		// activate widgets
		getMenuWidget(io.github.tanguygab.logicsim3.Lang.WIRENEW).setEnabled(io.github.tanguygab.logicsim3.LSProperties.MODE_EXPERT.equals(mode));
		getButtonWidget(io.github.tanguygab.logicsim3.Lang.WIRENEW).setEnabled(io.github.tanguygab.logicsim3.LSProperties.MODE_EXPERT.equals(mode));

		this.lspanel.repaint();
	}

	/**
	 * helper method to get a certain menu component
	 * 
	 * so we don't have to set every item as member variable
	 * 
	 * @param lang
	 * @return
	 */
	private AbstractButton getMenuWidget(io.github.tanguygab.logicsim3.Lang lang) {
		for (int i = 0; i < mnuBar.getMenuCount(); i++) {
			JMenu mnu = mnuBar.getMenu(i);
			for (Component c : mnu.getMenuComponents()) {
				if (lang.toString().equals(c.getName()))
					return (AbstractButton) c;
			}
		}
		return null;
	}

	/**
	 * helper method to get a certain button component
	 * 
	 * so we don't have to set every button as member variable
	 * 
	 * @param lang
	 * @return
	 */
	private AbstractButton getButtonWidget(io.github.tanguygab.logicsim3.Lang lang) {
		for (Component c : btnBar.getComponents()) {
			if (lang.toString().equals(c.getName()))
				return (AbstractButton) c;
		}
		return null;
	}

	/**
	 * add all languages from file system to languages menu
	 * 
	 * @param menu
	 * @param currentLanguage
	 */
	void createLanguageMenu(JMenu menu, String currentLanguage) {
		List<String> langs = io.github.tanguygab.logicsim3.I18N.getLanguages();
		ButtonGroup btnGroup = new ButtonGroup();
		for (String lang : langs) {
			JMenuItem item = new JRadioButtonMenuItem(lang);
			if (lang.equals(currentLanguage))
				item.setSelected(true);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					io.github.tanguygab.logicsim3.LSProperties.getInstance().setProperty(LSProperties.LANGUAGE,
							((JMenuItem) e.getSource()).getText());
					Dialogs.messageDialog(io.github.tanguygab.logicsim3.LSFrame.this, io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.LSRESTART));
				}
			});
			btnGroup.add(item);
			menu.add(item);
		}
	}

	@Override
	public void changedCircuit() {
		if (lsFile != null) {
			lsFile.changed = true;
		}
		setAppTitle();
	}

	@Override
	public void changedStatusText(String text) {
		// this is a hack - maybe it is ok...
		if (io.github.tanguygab.logicsim3.LSPanel.NOTHING.equals(text)) {
			for (Component c : btnBar.getComponents()) {
				if (c instanceof io.github.tanguygab.logicsim3.LSToggleButton) {
					io.github.tanguygab.logicsim3.LSToggleButton b = (io.github.tanguygab.logicsim3.LSToggleButton) c;
					if (!b.id.equals(io.github.tanguygab.logicsim3.I18N.langToStr(io.github.tanguygab.logicsim3.Lang.SIMULATE)))
						b.setSelected(false);
				}
			}
			return;
		}
		if (LSPanel.MSG_ABORTED.equals(text)) {
			for (Component c : btnBar.getComponents()) {
				if (c instanceof io.github.tanguygab.logicsim3.LSToggleButton) {
					io.github.tanguygab.logicsim3.LSToggleButton b = (LSToggleButton) c;
					b.setSelected(false);
				}
			}
			setStatusText(I18N.tr(Lang.ABORTED));
			repaint();
		} else
			setStatusText(text);
	}

	@Override
	public void changedZoomPos(double zoom, Point pos) {
		sbCoordinates.setText(
				"X: " + pos.x / 10 * 10 + ", Y: " + pos.y / 10 * 10 + "   Zoom: " + Math.round(zoom * 100) + "%");
	}

	@Override
	public void setAction(int action) {
	}

	@Override
	public void needsRepaint(CircuitPart circuitPart) {
	}

}
