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

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Hashtable;

public class SRVTest {

    public static void main(String[] args) {
        System.out.println("lib");
        Arrays.asList(lib_SRV_Record("leafserver.kr")).forEach(value -> System.out.println(value));
        System.out.println("java");
        Arrays.asList(java_SRV_Record("leafserver.kr")).forEach(value -> System.out.println(value));
    }

    @SneakyThrows(Exception.class)
    public static Object[] lib_SRV_Record(String address) {
        Record[] records = new Lookup("_minecraft._tcp." + address, Type.SRV).run();

        if (records != null) {
            SRVRecord srv = (SRVRecord) records[0];
            return new Object[] {srv.getTarget().toString(), srv.getPort()};
        }
        return null;
    }

    @SneakyThrows(Exception.class)
    public static Object[] java_SRV_Record(String address) {
        Hashtable<String, String> table = new Hashtable<>();
        table.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

        DirContext ctx = new InitialDirContext(table);
        Attributes attrs = ctx.getAttributes("_minecraft._tcp." + address, new String[] {"SRV"});
        NamingEnumeration<?> e = attrs.getAll();
        while (e.hasMore()) {
            Attribute attr = (Attribute) e.next();
            String srvRecord = attr.get().toString();
            String[] srvRecordData = srvRecord.split(" ");
            return new Object[] {srvRecordData[3], srvRecordData[2]};
        }
        return null;
    }
}
