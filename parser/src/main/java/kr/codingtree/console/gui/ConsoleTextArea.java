package kr.codingtree.console.gui;

import javax.swing.*;
import java.awt.*;

public class ConsoleTextArea extends JTextArea {

    private static final Font FONT = new Font("나눔고딕", Font.PLAIN, 13);
    private static final Color FOREGROUND = new Color(230, 230, 230),
                                BACKGROUND = Color.BLACK;

    public ConsoleTextArea() {
        super();
        setEditable(false);
        setFont(FONT);
        setForeground(FOREGROUND);
        setBackground(BACKGROUND);
    }
}
