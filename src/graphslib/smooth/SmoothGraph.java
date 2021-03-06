package graphslib.smooth;

import graphslib.utils.DataUnits;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

@SuppressWarnings("serial")
public class SmoothGraph extends JComponent {

	/**
	 * Time between ticks in ms
	 */
	public static final long TICKS = 50L;

	/**
	 * Maximum values saved in memory
	 */
	public static final int MAXIMUM_VALUES = 1000;

	/**
	 * Colors to use
	 */
	private ISmoothColors colors;

	/**
	 * Where valued are saved to be drawn
	 */
	private final List<ValuePair> valuePairs = new ArrayList<ValuePair>();

	/**
	 * Current position to draw net at
	 */
	private int position = 9;

	/**
	 * Max is 100 by default, 0 minimum
	 */
	private long maximum = 100;

	/**
	 * Is this component still active
	 */
	private boolean running = true;

	/**
	 * Show used
	 */
	private boolean showUsed = true;

	/**
	 * Show maximum
	 */
	private boolean showMaximum = true;

	/**
	 * Grids to show
	 */
	private int grids = 5;
	
	/**
	 * Popup menu
	 */
	private JPopupMenu popup;

	public SmoothGraph() {
		this(true);
	}

	public SmoothGraph(boolean repaintThread) {
		this.colors = new SmoothColors();
		valuePairs.add(new ValuePair(0, 0));

		popup = new JPopupMenu();

		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		JCheckBoxMenuItem toggleIncoming = new JCheckBoxMenuItem("Show Maximum", true);
		toggleIncoming.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDrawMaximum(((JCheckBoxMenuItem)e.getSource()).isSelected());
			}
		});
		
		JCheckBoxMenuItem toggleOutgoing = new JCheckBoxMenuItem("Show Used", true);
		toggleOutgoing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDrawUsed(((JCheckBoxMenuItem)e.getSource()).isSelected());
			}
		});
		
		popup.add(toggleIncoming);
		popup.add(toggleOutgoing);

		if (repaintThread) {
			new RepaintThread().start();
		}
	}

	public SmoothGraph(ISmoothColors colors) {
		this();
		this.colors = colors;
	}

	public SmoothGraph(ISmoothColors colors, boolean repaintThread) {
		this(repaintThread);
		this.colors = colors;
	}

	@Override
	public void paintComponent(Graphics g) {
		while (valuePairs.size() > this.getWidth() - 50) {
			if (valuePairs.size() > 0) {
				valuePairs.remove(0);
			} else {
				break;
			}
		}

		long max = 0;

		for (ValuePair vp : valuePairs) {
			if (vp.getUsed() > max && drawUsed()) {
				max = vp.getUsed();
			}

			if (vp.getAvailable() > max && drawMaximum()) {
				max = vp.getAvailable();
			}
		}

		if (max < 10) {
			max = 10;
		}
		
		max += max / 5;		

		setMaximum(max);

		// draw inner color
		g.setColor(colors.getInnerFillColor());
		g.fillRect(1, 1, this.getWidth() - 1, this.getHeight() - 1);

		// draw grid
		g.setColor(colors.getGridColor());
		for (int i = 0; i < this.getHeight(); i += this.getHeight() / grids) {
			g.drawLine(55, i, 60, i);
		}
		
		g.drawLine(60, 0, 60, this.getHeight());

		// draw data size strings
		int lineNumber = 0;
		for (int i = 0; i < this.getHeight(); i += this.getHeight() / grids) {
			long part = max / grids;
			long what = max - (part * (lineNumber++));
			g.drawString(DataUnits.getAsString(what) + "", 5, i + 15);
		}

		// decrease pos
		position -= 3;

		// reset pos
		if (position == 0) {
			position = 9;
		}

		// draw main curve
		int index = valuePairs.size() - 1;
		ValuePair value = new ValuePair(0, 0);
		// set line thickness

		((Graphics2D) g).setStroke(new BasicStroke(2));
		//((Graphics2D) g).setComposite(AlphaComposite.SrcOver.derive(0.5F));

		int drawValueUp = 0;
		int drawValueDown = 0;
						
		for (int i = this.getWidth(); i > 60; i--) {
			if (index > 0) {
				index--;
		
				value = valuePairs.get(index);
				
				int height = this.getHeight();


				drawValueUp = (int) (((float) value.getUsed() / (float) maximum) * height);
				drawValueDown = (int) (((float) value.getAvailable() / (float) maximum) * height);

				boolean drawDownFirst = drawValueDown > drawValueUp;		
				
				if (drawDownFirst && drawMaximum() || !drawDownFirst && drawMaximum()) {
					g.setColor(drawDownFirst ? colors.getFreeColor() : colors.getUsedColor());
					g.fillRect(i, height - (drawDownFirst ? drawValueDown : drawValueUp), 2, height);
				}

				if (!drawDownFirst && drawMaximum() || drawDownFirst && drawUsed()) {
					g.setColor(!drawDownFirst ? colors.getFreeColor() : colors.getUsedColor());
					g.fillRect(i, height - (!drawDownFirst ? drawValueDown : drawValueUp), 2, height);
				}

				i -= 1;
			} else {
				break;
			}
		}

		// draw text
		// g.setColor(colors.getCurveColor());
		// g.drawString(text, 17, this.getHeight() - 10);

		// draw background rectangles
		g.setColor(colors.getBorderColor());
		g.drawRect(0, 0, this.getWidth(), this.getHeight());

		g.dispose();

	}

	public void addValues(long i, long l) {
		valuePairs.add(new ValuePair(i, l));
	}

	public ValuePair getLastValue() {
		if (valuePairs.size() > 0) {
			return valuePairs.get(valuePairs.size() - 1);
		} else {
			return null;
		}
	}

	public void setMaximum(long maximum) {
		this.maximum = maximum;
	}

	public long getMaximum() {
		return this.maximum;
	}

	public void dispose() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public ISmoothColors getColors() {
		return colors;
	}

	public void setColors(ISmoothColors colors) {
		this.colors = colors;
	}

	public boolean drawUsed() {
		return showUsed;
	}

	public void setDrawUsed(boolean showUp) {
		this.showUsed = showUp;
	}

	public boolean drawMaximum() {
		return showMaximum;
	}

	public void setDrawMaximum(boolean showDown) {
		this.showMaximum = showDown;
	}

	public int getGrids() {
		return grids;
	}

	public void setGrids(int grids) {
		this.grids = grids;
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	public void setPopupMenu(JPopupMenu popup) {
		this.popup = popup;
	}

	class RepaintThread extends Thread {

		public RepaintThread() {
			super("Repaint thread");
		}

		/**
		 * Repaints each tick
		 */
		@Override
		public void run() {
			while (running) {
				try {
					SmoothGraph.this.repaint();
					Thread.sleep(TICKS);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}