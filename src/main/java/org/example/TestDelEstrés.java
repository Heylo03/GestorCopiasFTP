package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestDelEstrés {

    private static final int NUM_INSTANCIAS = 16; // Número de instancias

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_INSTANCIAS);

        for (int i = 0; i < NUM_INSTANCIAS; i++) {
            executor.submit(() -> {
                Sincronizador.sincronizacion();
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
