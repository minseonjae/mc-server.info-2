package kr.codingtree.test;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MCSocketB {

    /*

        1.8b ~ 1.3

     */
    @SneakyThrows(Exception.class)
    public static void main(String[] args) {

        String[] serverData = null;
        byte[] rawServerData = null;

        @Cleanup Socket socket = new Socket();
        long startTime = System.currentTimeMillis();

        socket.connect(new InetSocketAddress("125.177.248.146", 5758), 1000);

        @Cleanup DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        @Cleanup DataInputStream dis = new DataInputStream(socket.getInputStream());

        dos.writeBytes("\u00FE");

        if (dis.readUnsignedByte() == 0xFE) {

            int dataLen = dis.readUnsignedShort();
            rawServerData = new byte[dataLen * 2];
            dis.readFully(rawServerData, 0, dataLen * 2);

            serverData = new String(rawServerData, StandardCharsets.UTF_16).split("\u00A7");

            System.out.println(serverData);

            /*

            0 mote
            1 current players
            2 max players

             */
        } else {
            System.out.println("not connect");
        }
    }

}
