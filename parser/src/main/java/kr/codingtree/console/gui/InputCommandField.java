package kr.codingtree.console.gui;

import javax.swing.*;
import java.awt.*;

public class InputCommandField extends JTextField {

    private static final int[] LOCATION = {20, 415},
                                SIZE = {670, 26};
    private static final Font FONT = new Font("나눔고딕", Font.PLAIN, 12);
    private static final Color FOREGROUND = new Color(230, 230, 230),
                                BACKGROUND = Color.BLACK;

    public InputCommandField() {
        super();
        setBorder(null);
        setBounds(LOCATION[0], LOCATION[1], SIZE[0], SIZE[1]);
        setFont(FONT);
        setForeground(FOREGROUND);
        setBackground(BACKGROUND);
    }
}
