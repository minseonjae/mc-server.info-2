package kr.codingtree.console.command;

import kr.codingtree.console.Console;
import lombok.Getter;

import java.util.HashSet;

public class ConsoleCommand {

    public ConsoleCommand(Console console) {
        this.console = console;
    }

    private Console console;

    @Getter
    private HashSet<CommandExecutor> executors = new HashSet<>();

    public void register(CommandExecutor executor) {
        executors.add(executor);
    }

    public boolean execute(String command) {
        String[] commands = command.trim().toLowerCase().split(" ");

        for (CommandExecutor executor : executors) {
            if (executor.onCommand(console, commands)) {
                return true;
            }
        }

        return false;
    }
}
