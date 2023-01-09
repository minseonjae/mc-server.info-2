package kr.codingtree.test;

import java.io.File;

public class FileNameTest {

    public static void main(String[] args) {
        File file = new File("/Users/seonjae/Pictures/Screenshot/스크린샷 2022-12-28 오전 12.48.03.png");

        System.out.println(file.getAbsolutePath());
        System.out.println(file.getPath());
        System.out.println(file.getParentFile().getPath());
        System.out.println(file.getName());
    }

    public <T> T asdf(T obj) {
        return obj;
    }

}
