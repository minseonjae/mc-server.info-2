package kr.codingtree.test;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

@UtilityClass
public class Base64Utils {

    public static byte[] getBytes(String base64) {
        base64 = base64.replace("data:image/jpeg;base64,", "").replace("data:image/png;base64,", "");
        return Base64.getDecoder().decode(base64);
    }

    @SneakyThrows
    public static void saveToImage(String base64, File imagePath) {
        byte[] bytes = getBytes(base64);

        @Cleanup BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imagePath));
        bos.write(bytes, 0, bytes.length);
    }

}