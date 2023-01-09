package kr.codingtree.console.gui.listener;

import kr.codingtree.console.gui.MainFrame;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ComponentFocusListener implements FocusListener {

    private MainFrame frame;

    public ComponentFocusListener(MainFrame frame) {
        this.frame = frame;
    }

    public void focusLost(FocusEvent e) {}
    public void focusGained(FocusEvent e) {
        frame.getCommandField().requestFocus();
    }
}
