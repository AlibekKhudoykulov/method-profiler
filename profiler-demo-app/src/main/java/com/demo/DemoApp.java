package com.demo;

import com.profiler.agent.annotation.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DemoApp {

    private static final Random random = new Random();

    @Profile(name = "user-service")
    public void getUserById(int id) {
        try {
            Thread.sleep(50 + random.nextInt(100));
        } catch (InterruptedException e) {}
    }

    @Profile(trackMemory = true)
    public List<String> generateData(int size) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add("Item-" + i + "-" + random.nextDouble());
        }
        return data;
    }

    @Profile
    public int calculateSum(int n) {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += i;
        }
        return sum;
    }

    @Profile
    public void mayThrowError() {
        if (random.nextInt(10) < 3) {
            throw new RuntimeException("Random error");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Demo app started. PID: " + ProcessHandle.current().pid());
        System.out.println("Profiler dashboard: http://localhost:9999/metrics");
        System.out.println("Running workload...\n");

        DemoApp app = new DemoApp();

        // Run for 60 seconds
        long endTime = System.currentTimeMillis() + 60_000;
        int iteration = 0;

        while (System.currentTimeMillis() < endTime) {
            iteration++;

            app.getUserById(iteration);
            app.calculateSum(10000 + iteration);

            if (iteration % 5 == 0) {
                app.generateData(1000);
            }

            try {
                app.mayThrowError();
            } catch (Exception e) {
                // ignore
            }

            if (iteration % 10 == 0) {
                System.out.println("Completed " + iteration + " iterations");
            }

            Thread.sleep(100);
        }

        System.out.println("\nDemo finished. Total iterations: " + iteration);
        System.out.println("Check metrics at http://localhost:9999/metrics");
        Thread.sleep(10000); // Wait to allow viewing metrics
    }
}