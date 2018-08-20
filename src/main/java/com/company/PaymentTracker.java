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

    private void setPayment(Map.Entry<String,BigDecimal> entry){
        if (payment.containsKey(entry.getKey())) {
            payment.put(entry.getKey(), payment.get(entry.getKey()).add(entry.getValue()));
        } else {
            payment.put(entry.getKey(), entry.getValue());
        }
    }

    private void setRate(Map.Entry<String,BigDecimal> entry){
        rate.put(entry.getKey(), entry.getValue());
    }

    private Map.Entry<String,BigDecimal> createDataFromString(String input){
        Map.Entry<String,BigDecimal> entry = null;
        Matcher matcher = Pattern.compile("([a-zA-Z]{3}) ((\\-)?([0-9][0-9]*\\.?[0-9]+))").matcher(input);
        if(matcher.find()){
            entry = new AbstractMap.SimpleEntry<String, BigDecimal>(matcher.group(1), new BigDecimal(matcher.group(2)));
        }
        return entry;
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

    private List<String> readFile(String filePath){
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

    @Override
    public void run() {
        printAllPayments();
    }

    public static void main(String[] args) {
        PaymentTracker timerTask = new PaymentTracker();
        Timer timer = new Timer(true);
        List<String> lStr = null;
        Map.Entry<String,BigDecimal> entry = null;

        for(int i = 0; i < args.length; i++){
            lStr = timerTask.readFile(args[i]);
            if (lStr == null) {
                if(args[i].contains("payments")) {
                    System.out.println("File Payments in the specified path (" + args[i] + ") does not exist!");
                }
                if(args[i].contains("rates")) {
                    System.out.println("File Rates in the specified path (" + args[i] + ") does not exist!");
                }
            } else {
                for (String line : lStr) {
                    entry = timerTask.createDataFromString(line);
                    if (entry != null) {
                        if (args[i].contains("payments")) {
                            timerTask.setPayment(entry);
                        }
                        if (args[i].contains("rates")) {
                            timerTask.setRate(entry);
                        }
                    }
                }
            }
        }

        timer.scheduleAtFixedRate(timerTask, 0, 60000);

        timerTask.readDataFromConsole();

        timer.cancel();
        System.out.println("The End!");
    }

}
