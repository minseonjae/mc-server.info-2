package kr.codingtree.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;

public class MCSocket {

    private static final String ADDRESS = "125.177.248.146";
    private static final int PORT = 5758, TIME_OUT = 1000;

    @SneakyThrows(Exception.class)
    public static void main(String[] args) {
        String[] serverData = null;
        byte[] rawServerData = null;

        @Cleanup Socket socket = new Socket();
        long startTime = System.currentTimeMillis();
        socket.connect(new InetSocketAddress(ADDRESS, PORT), TIME_OUT);
        long ping = System.currentTimeMillis() - startTime;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream payLoad = new DataOutputStream(baos), dos = new DataOutputStream(socket.getOutputStream());
        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        payLoad.writeByte(0x00);
        writeVarInt(payLoad, 0x00);
        writeVarInt(payLoad, ADDRESS.length());

        payLoad.writeBytes(ADDRESS);
        payLoad.writeShort(PORT);
        writeVarInt(payLoad, 0x01);
        writeVarInt(dos, baos.size());

        dos.write(baos.toByteArray());
        dos.writeByte(0x01);
        dos.writeByte(0x00);

        int totalLength = readVarInt(dis), packetId = readVarInt(dis), jsonLength = readVarInt(dis);
        byte[] rawData = new byte[jsonLength];

        dis.readFully(rawData);

        LinkedHashMap<String, Object> map = new Gson().fromJson(new String(rawData), LinkedHashMap.class);
        System.out.println(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(map));
    }

    @SneakyThrows(Exception.class)
    public static void writeVarInt(DataOutputStream dos, int intData) {
        while (true) {
            if ((intData & 0xFFFFFF80) == 0) {
                dos.writeByte(intData);
                return;
            }
            dos.writeByte(intData & 0x7F | 0x80);
            intData >>>= 7;
        }
    }
    @SneakyThrows(Exception.class)
    public static int readVarInt(DataInputStream dis) {
        int i = 0, j = 0;

        while (true) {
            int varInt = dis.readByte();
            i |= (varInt & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if((varInt & 0x80) != 128) {
                break;
            }
        }
        return i;
    }
}
