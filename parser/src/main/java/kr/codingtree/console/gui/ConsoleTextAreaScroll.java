package kr.codingtree.console.gui;

import javax.swing.*;

public class ConsoleTextAreaScroll extends JScrollPane {

    private static final int LOCATION[] = {0, 1},
                                SIZE[] = {698, 409};

    public ConsoleTextAreaScroll() {
        super();
        setBorder(null);
        setBounds(LOCATION[0], LOCATION[1], SIZE[0], SIZE[1]);
    }
}
