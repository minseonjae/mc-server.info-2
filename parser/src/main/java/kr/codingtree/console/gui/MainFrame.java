package kr.codingtree.console.gui;

import kr.codingtree.console.command.ConsoleCommand;
import kr.codingtree.console.gui.listener.CommandFieldKeyListener;
import kr.codingtree.console.gui.listener.ComponentFocusListener;
import kr.codingtree.console.gui.listener.ConsoleCloseListener;
import kr.codingtree.console.gui.thread.CommandLabelBlinkThread;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {

    private static final int[] SIZE = {700, 472};
    private static final Color BACKGROUND = Color.BLACK;

    @Getter
    private ConsoleCommand command;

    @Getter
    private ConsoleTextAreaScroll textAreaScroll;
    @Getter
    private ConsoleTextArea textArea;
    @Getter
    private InputCommandField commandField;
    @Getter
    private CommandLabel commandLabel;
    @Getter
    private CommandLabelBlinkThread commandLabelBlinkThread;

    public MainFrame(String title, ConsoleCommand command) {
        super(title);
        this.command = command;

        setSize(SIZE[0], SIZE[1]);
        getContentPane().setBackground(BACKGROUND);

        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        ComponentFocusListener focusListener = new ComponentFocusListener(this);

        addFocusListener(focusListener);
        addWindowListener(new ConsoleCloseListener(this));

        add(textAreaScroll = new ConsoleTextAreaScroll());
        textAreaScroll.setViewportView(textArea = new ConsoleTextArea());
        textArea.addFocusListener(focusListener);

        add(commandField = new InputCommandField());
        commandField.requestFocus(true);
        commandField.setFocusable(true);
        commandField.requestFocus();
        commandField.addKeyListener(new CommandFieldKeyListener(this));

        add(commandLabel = new CommandLabel());
        commandLabel.addFocusListener(focusListener);
    }

    public void newBlinkThread() {
        commandLabelBlinkThread = new CommandLabelBlinkThread(this);
    }

    public void log(Object message) {
        if (message.equals("\n")) {
            textArea.append(message.toString());
        } else {
            textArea.append(" [");
            textArea.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            textArea.append("] ");
            textArea.append(message.toString());
        }
        textArea.setCaretPosition(textArea.getDocument().getLength());

        String msg = textArea.getText();

        if (msg.split("\n").length > 5000) {
            textArea.setText(msg.substring(msg.indexOf("\n") + 1));
        }
    }
}
