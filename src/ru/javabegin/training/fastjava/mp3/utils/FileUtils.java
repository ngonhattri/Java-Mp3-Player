package ru.javabegin.training.fastjava.mp3.utils;

import java.io.*;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

// утилита для работы с файлами
public class FileUtils {

    // получить имя файла без расширения
    public static String getFileNameWithoutExtension(String fileName) {
        File file = new File(fileName);
        int index = file.getName().lastIndexOf('.');
        if (index > 0 && index <= file.getName().length() - 2) {
            return file.getName().substring(0, index);
        }
        return "noname";
    }

    // получить расширение файла
    public static String getFileExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    // удалить текущий файл-фильтр и установить новый переданный    
    public static void addFileFilter(JFileChooser jfc, FileFilter ff) {
        jfc.removeChoosableFileFilter(jfc.getFileFilter());
        jfc.setFileFilter(ff);
        jfc.setSelectedFile(new File(""));// удалить последнее имя открываемого/сохраняемого файла
    }

    // сохранить объект
    public static void serialize(Object obj, String fileName) throws Exception {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(fileName);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.flush();
        } finally {
            oos.close();
            fos.close();
        }
    }

    // открыть объект
    public static Object deserialize(String fileName) throws Exception {
        FileInputStream fis = null;
        ObjectInputStream oin = null;
        Object ts = null;
        try {
            fis = new FileInputStream(fileName);
            oin = new ObjectInputStream(fis);
           ts = (Object) oin.readObject();
        } finally {
            fis.close();
            oin.close();
        }
        
        return ts;
    }
}
