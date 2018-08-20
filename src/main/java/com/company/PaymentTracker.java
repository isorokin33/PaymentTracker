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

    private void setPayment(String[] data){
        BigDecimal qty = new BigDecimal(data[1]);
        if (payment.containsKey(data[0])) {
            payment.put(data[0], payment.get(data[0]).add(qty));
        } else {
            payment.put(data[0], qty);
        }
    }

    private void setRate(String[] data){
        BigDecimal qty = new BigDecimal(data[1]);
        rate.put(data[0], qty);
    }

    private String[] createDataFromString(String input){
        String[] data = new String[2];
        Matcher matcher = Pattern.compile("([a-zA-Z]{3}) ((\\-)?([0-9][0-9]*\\.?[0-9]+))").matcher(input);
        if(matcher.find()){
            data[0] = matcher.group(1);
            data[1] = matcher.group(2);
        }
        return data;
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
        String[] data;
        BufferedReader getPayment = new BufferedReader(new InputStreamReader(System.in));
        try {
            String str = getPayment.readLine();
            while (!str.equals("quit")) {
                data = createDataFromString(str);
                if(data[0] == null && data[1] == null)
                    System.out.println("Invalid format. Please enter payment or quit.");
                else
                    setPayment(data);
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
        String[] dStr ;

        //Rates
        lStr = timerTask.readFile("e:\\MyProjectJava\\PaymentTracker\\rate.txt");
        if(lStr != null) {
            for (String line : lStr) {
                dStr = timerTask.createDataFromString(line);
                if (dStr[0] != null && dStr[1] != null) {
                    timerTask.setRate(dStr);
                }
            }
        }

        //Payments
        if (args.length > 0) {
            lStr = timerTask.readFile(args[0]);
            if(lStr == null) {
                System.out.println("File in the specified path (" + args[0] + ") does not exist!");
            } else {
                for (String line : lStr) {
                    dStr = timerTask.createDataFromString(line);
                    if (dStr[0] != null && dStr[1] != null) {
                        timerTask.setPayment(dStr);
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
