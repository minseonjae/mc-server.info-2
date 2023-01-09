package kr.codingtree.console.command;

import kr.codingtree.console.Console;

public interface CommandExecutor {
    boolean onCommand(Console console, String[] cmds);
}
