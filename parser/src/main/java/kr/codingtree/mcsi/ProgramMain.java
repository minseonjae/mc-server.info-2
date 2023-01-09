package kr.codingtree.mcsi;

import kr.codingtree.console.Console;

public class ProgramMain {

    private static MCServerInfo mcsi = MCServerInfo.getInstance();

    public static void main(String[] args) {
        mcsi.setConsole(new Console("MCServerInfo"));

        mcsi.getConsole().show();


    }

}
