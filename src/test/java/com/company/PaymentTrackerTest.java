package com.company;


import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PaymentTrackerTest {

    @Test
    void createDataFromString() {
        PaymentTracker paymentTracker = new PaymentTracker();
        String test = "USD 157";
        Map.Entry<String, BigDecimal> expected = null;
        expected = new AbstractMap.SimpleEntry<String, BigDecimal>("USD", new BigDecimal(157));
        Map.Entry<String, BigDecimal> actual = paymentTracker.createDataFromString(test);

        assertEquals(expected,actual);
    }

    @Test
    void readFile() {
        PaymentTracker paymentTracker = new PaymentTracker();
        String path = "e:\\MyProjectJava\\PaymentTracker\\testFile.txt";
        List<String> actual = paymentTracker.readFile(path);
        List<String> expected = new ArrayList<>();
        expected.add("RUB 100");
        expected.add("USD 200");
        expected.add("EUR 300");

        assertEquals(expected,actual);
    }

    @Test
    void setPayment() {
        PaymentTracker paymentTracker = new PaymentTracker();
        Map.Entry<String, BigDecimal> expected = null;

        expected = new AbstractMap.SimpleEntry<String, BigDecimal>("USD", new BigDecimal(100));
        paymentTracker.setPayment(expected);
        assertEquals(new BigDecimal(100), paymentTracker.getQty("USD"));

        expected = new AbstractMap.SimpleEntry<String, BigDecimal>("USD", new BigDecimal(-50));
        paymentTracker.setPayment(expected);
        assertEquals(new BigDecimal(50), paymentTracker.getQty("USD"));

        expected = new AbstractMap.SimpleEntry<String, BigDecimal>("USD", new BigDecimal(250));
        paymentTracker.setPayment(expected);
        assertEquals(new BigDecimal(300), paymentTracker.getQty("USD"));
    }
}