package xyz.demo.erupt.example;

import xyz.erupt.auth.model.EruptMenu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author liyuepeng
 * @date 2020-03-06
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try (//创建一个ObjectOutputStream输出流
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("object.txt"))) {
            //将对象序列化到文件s
            EruptMenu eruptMenu = new EruptMenu();
            oos.writeObject(eruptMenu);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
