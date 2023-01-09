package kr.codingtree.console.stream;

import kr.codingtree.console.gui.MainFrame;

import java.io.PrintStream;

public class ConsolePrintStream extends PrintStream {
    public ConsolePrintStream(MainFrame frame) {
        super(new ConsoleOutputStream() {
            public void write(String text) {
                frame.log(text);
            }
        });
    }
}
