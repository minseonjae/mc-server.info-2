package kr.codingtree.console.commands;

import kr.codingtree.console.Console;
import kr.codingtree.console.command.CommandExecutor;

public class DebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(Console console, String[] cmds) {
        if (cmds.length == 1 && (cmds[0].equals("debug") || cmds[0].equals("디버그"))) {
            console.setDebugging(!console.isDebugging());
            console.log("디버그 모드가 " + (console.isDebugging() ? "켜" : "꺼") + "졌습니다!");
            return true;
        }
        return false;
    }

}
