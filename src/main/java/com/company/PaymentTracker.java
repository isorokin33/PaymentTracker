package com.company;

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
    private final Map<String, BigDecimal> payment = new HashMap<>();
    private final Map<String, BigDecimal> rate = new HashMap<>();

    private void createPayment(String currency, BigDecimal qty){
        if (payment.containsKey(currency)) {
            payment.put(currency, payment.get(currency).add(qty));
        } else {
            payment.put(currency, qty);
        }
    }

    private void createRate(String currency, BigDecimal qty){
        if (rate.containsKey(currency)) {
            System.out.println("Duplication currency: " + currency);
        } else {
            rate.put(currency, qty);
        }
    }

    private void createDataFromString(String input, boolean isPayment){
        Matcher matcher = Pattern.compile("([a-zA-Z]{3}) ((\\-)?([0-9][0-9]*\\.?[0-9]+))").matcher(input);
        if(matcher.find()){
            String currency = matcher.group(1);
            BigDecimal qty = new BigDecimal (matcher.group(2));
            if(isPayment)
                createPayment(currency,qty);
            else
                createRate(currency,qty);
        } else {
            System.out.println("Invalid format. Please enter transaction or quit.");
        }
    }

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

    public void readFile(String filePath, boolean isPayment){
        File file = new File(filePath);
        if (file.isFile()) {
            try {
                //читаем по строчно данные из файла и записываем его в List
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for(String line: lines){
                    createDataFromString(line, isPayment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //если файла не существует выдаем ошибку
            System.out.println("File in the specified path (" + filePath + ") does not exist!");
        }
    }

    private void readDataFromConsole(){
        BufferedReader getPayment = new BufferedReader(new InputStreamReader(System.in));
        try {
            //читаем первую строку с консоли и записываем в обект str
            String str = getPayment.readLine();
            //чтение с консоли происходит до тех пор пока пользователь не введет "quit"
            while (!str.equals("quit")) {
                createDataFromString(str, true); //Записываем данные в payment
                str = getPayment.readLine(); //читаем следующую строку
            }
        } catch (IOException ex) {
            System.out.println("Reading error");
        }
    }

    @Override
    public void run() {
        printAllPayments();
    }

    public static void main(String[] args) {
        PaymentTracker timerTask = new PaymentTracker();
        Timer timer = new Timer(true);

        if (args.length > 0)
            timerTask.readFile(args[0],true);
        if(args.length > 1)
           timerTask.readFile(args[1],false);

        timer.scheduleAtFixedRate(timerTask, 0, 60000);

        timerTask.readDataFromConsole();

        timer.cancel();
        System.out.println("The End!");
    }

}
