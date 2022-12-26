package kr.codingtree.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;

public class MCSocketSRV {

//    private static String ADDRESS = "125.177.248.146";
//    private static int PORT = 5758, TIME_OUT = 1000;


    private static String ADDRESS = "leafserver.kr";
    private static int PORT = 25565, TIME_OUT = 1000;

    @SneakyThrows(Exception.class)
    public static void main(String[] args) {
        String[] serverData = null;
        byte[] rawServerData = null;

        Record[] records = new Lookup("_minecraft._tcp." + ADDRESS, Type.SRV).run();

        if (records != null) {
            SRVRecord srv = (SRVRecord) records[0];
            ADDRESS = srv.getTarget().toString();
            PORT = srv.getPort();

            System.out.println("srv use");
        }


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

        JsonObject json = new Gson().fromJson(new String(rawData), JsonObject.class);
        System.out.println(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json));

        File file = new File("/Users/seonjae/Desktop/programming/icon.png");

        Base64Utils.saveToImage(json.get("favicon").getAsString(), file);
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
