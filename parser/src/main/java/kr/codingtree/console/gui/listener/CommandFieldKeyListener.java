package kr.codingtree.console.gui.listener;

import kr.codingtree.console.gui.MainFrame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CommandFieldKeyListener implements KeyListener {

    private MainFrame frame;

    public CommandFieldKeyListener(MainFrame frame) {
        this.frame = frame;
    }

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !frame.getCommandField().getText().isEmpty() && frame.getCommandField().getText().length() > 0) {
            String cmd = frame.getCommandField().getText();
            if (!frame.getCommand().execute(cmd)) {
                System.out.println("해당 명령어가 존재하지 않습니다! \"" + cmd + "\"");
            }
            frame.getCommandField().setText(null);
        }
    }
}
