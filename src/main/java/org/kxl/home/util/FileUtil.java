package org.kxl.home.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static List<File> recurSionDir(File root, List<File> result) {
        if (result == null) result = new ArrayList<>(1024);
        File[] childFiles = root.listFiles();
        for (File f : childFiles) {
            if (f.isDirectory()) {
                recurSionDir(f, result);
            } else {
                result.add(f);
            }
        }
        return result;
    }

    public static void writeFile(String file, List<String> lines, boolean clear) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        if (clear) {
            raf.setLength(0);
        }
        for (String line : lines) {
            raf.writeBytes(line);
        }
        raf.close();
    }

    public static List<String> readFile(String file){
        BufferedReader br = null;
        List<String> contents = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while( (line = br.readLine())!=null ){
                contents.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            }catch (Exception e){}
        }
        return contents;
    }

}
