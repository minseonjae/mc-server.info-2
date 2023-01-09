package kr.codingtree.console;

import kr.codingtree.console.command.ConsoleCommand;
import kr.codingtree.console.commands.DebugCommand;
import kr.codingtree.console.gui.MainFrame;
import kr.codingtree.console.stream.ConsolePrintStream;
import lombok.Getter;
import lombok.Setter;

public class Console {

    @Setter
    private String title = "console";

    @Getter
    private MainFrame frame;

    @Getter
    private ConsoleCommand command;

    @Getter
    private boolean hide = true;

    @Getter
    @Setter
    private boolean debugging = false;

    public Console(String title) {
        frame = new MainFrame(this.title = title, command = new ConsoleCommand(this));

        command.register(new DebugCommand());

        ConsolePrintStream printStream = new ConsolePrintStream(frame);

        System.setOut(printStream);
        System.setErr(printStream);
    }

    public boolean show() {
        if (hide) {
            frame.setVisible(true);
            frame.newBlinkThread();
            frame.getCommandLabelBlinkThread().start();
            return true;
        }

        return false;
    }
    public boolean hide() {
        if (hide) return false;
        frame.setVisible(false);
        frame.getCommandLabelBlinkThread().setStopped(true);
        return true;
    }

    public void log(Object message) {
        System.out.println(message);
    }
    public void debugLog(Object message) {
        if (debugging) {
            System.out.println("[debug] " + message.toString());
        }
    }
}
