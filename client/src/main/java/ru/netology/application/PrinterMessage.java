package ru.netology.application;

public class PrinterMessage implements Printable {
    @Override
    public void print(String msg) {
        System.out.println(msg);
    }
}
