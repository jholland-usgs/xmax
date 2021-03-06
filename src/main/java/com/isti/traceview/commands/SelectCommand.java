package com.isti.traceview.commands;

import com.isti.traceview.AbstractUndoableCommand;
import com.isti.traceview.gui.GraphPanel;
import org.apache.log4j.Logger;

/**
 * This command selects chosen channels, i.e hides all unchosen channels on graph panel
 * 
 * @author Max Kokoulin
 */
public class SelectCommand extends AbstractUndoableCommand {
	private static final Logger logger = Logger.getLogger(SelectCommand.class);
	private GraphPanel graphPanel = null;

	/**
	 * @param gp
	 *            target graph panel
	 */
	public SelectCommand(GraphPanel gp) {
		this.graphPanel = gp;
	}

	public void run() {
		try {
			super.run();
			graphPanel.select(false);
		} catch (Exception e) {
			logger.error("SelectCommand error: ", e);
		}
	}

	public void undo() {
		try {
			super.undo();
			graphPanel.select(true);
		} catch (Exception e) {
			// do nothing
			logger.error("Exception:", e);	
		}
	}

	public boolean canUndo() {
		return true;
	}
}
