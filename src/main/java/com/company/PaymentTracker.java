package com.company;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentTracker extends TimerTask {
    //Коллекция для хранения платежей
    private final Map<String, BigDecimal> payment = new HashMap<>();
    //Коллекция для хранения курсов варют
    private final Map<String, BigDecimal> rate = new HashMap<>();

    //Функция сохранения данных в коллекцию платежей
    private void setPayment(Map.Entry<String,BigDecimal> entry){
        if (payment.containsKey(entry.getKey())) {
            payment.put(entry.getKey(), payment.get(entry.getKey()).add(entry.getValue()));
        } else {
            payment.put(entry.getKey(), entry.getValue());
        }
    }

    //Функция сохранения данных в колекцию курсов валют
    private void setRate(Map.Entry<String,BigDecimal> entry){
        rate.put(entry.getKey(), entry.getValue());
    }

    //Функция преобразует данные из строки к типу Map.Entry<String,BigDecimal> для сохранения их в коллекции
    /*private*/ Map.Entry<String,BigDecimal> createDataFromString(String input){
        Map.Entry<String,BigDecimal> entry = null;
        Matcher matcher = Pattern.compile("([a-zA-Z]{3}) ((\\-)?([0-9][0-9]*\\.?[0-9]+))").matcher(input);
        if(matcher.find()){
            entry = new AbstractMap.SimpleEntry<String, BigDecimal>(matcher.group(1), new BigDecimal(matcher.group(2)));
        }
        return entry;
    }

    //Функция вывода платежей на экран
    private void printAllPayments(){
        for(Map.Entry<String,BigDecimal> entry  : payment.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                if (rate.containsKey(entry.getKey()) && rate.get(entry.getKey()).compareTo(BigDecimal.ZERO) != 0) {
                    System.out.println(entry.getKey() + " " + entry.getValue() + " (USD " + entry.getValue().divide(rate.get(entry.getKey()), 2, BigDecimal.ROUND_UP) + ")");
                } else
                    System.out.println(entry.getKey() + " " + entry.getValue());
            }
        }
    }

    //Функция чтения данных из файла и преобразование их в List<String>
    /*private*/ List<String> readFile(String filePath){
        List<String> lines = null;
        File file = new File(filePath);
        if (file.isFile()) {
            try {
                lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lines;
    }

    //Функция чтения данных с консоли
    private void readDataFromConsole(){
        Map.Entry<String,BigDecimal> entry = null;
        BufferedReader getPayment = new BufferedReader(new InputStreamReader(System.in));
        try {
            String str = getPayment.readLine();
            while (!str.equals("quit")) {
                entry = createDataFromString(str);
                if(entry == null)
                    System.out.println("Invalid format. Please enter payment or quit.");
                else
                    setPayment(entry);
                str = getPayment.readLine();
            }
        } catch (IOException ex) {
            System.out.println("Reading error");
        }
    }

    //Прцесс вывода данных на экран с определнной периодичностью
    @Override
    public void run() {
        printAllPayments();
    }

    public static void main(String[] args) {
        PaymentTracker timerTask = new PaymentTracker();
        Timer timer = new Timer(true);
        List<String> lStr = null;
        Map.Entry<String,BigDecimal> entry = null;

        //Парсим файл с платижами, результат записываем в payment
        if(args.length > 0){
            lStr = timerTask.readFile(args[0]);
            if (lStr == null)
                System.out.println("File Payments in the specified path (" + args[0] + ") does not exist!");
            else {
                for (String line : lStr) {
                    entry = timerTask.createDataFromString(line);
                    if (entry != null)
                        timerTask.setPayment(entry);
                }
            }
        }
        //Парсим файл с курсами валют, результат записываем в rate
        if(args.length > 1){
            lStr = timerTask.readFile(args[1]);
            if (lStr == null)
                System.out.println("File Rates in the specified path (" + args[1] + ") does not exist!");
            else {
                for (String line : lStr) {
                    entry = timerTask.createDataFromString(line);
                    if (entry != null)
                        timerTask.setRate(entry);
                }
            }
        }

        timer.scheduleAtFixedRate(timerTask, 0, 60000);

        timerTask.readDataFromConsole();

        timer.cancel();
        System.out.println("The End!");
    }

}
