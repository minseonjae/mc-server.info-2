package kr.codingtree.console.gui;

import javax.swing.*;
import java.awt.*;

public class CommandLabel extends JLabel {
    private static final int[] LOCATION = {5, 414},
                                SIZE = {10, 26};
    private static final Font FONT = new Font("나눔고딕", Font.PLAIN, 13);
    private static final Color FOREGROUND = new Color(230, 230, 230);

    public CommandLabel() {
        super(">");
        setBounds(LOCATION[0], LOCATION[1], SIZE[0], SIZE[1]);
        setFont(FONT);
        setForeground(FOREGROUND);
    }
}
