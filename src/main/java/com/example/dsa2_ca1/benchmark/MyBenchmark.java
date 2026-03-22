package com.example.dsa2_ca1.benchmark;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.dsa2_ca1.model.MyArrayList;
import com.example.dsa2_ca1.model.MyList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.paint.Color;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import com.example.dsa2_ca1.controller.Controller;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.swing.*;


@Measurement(iterations = 10, time = 2)
@Warmup(iterations = 5, time = 2)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)

public class MyBenchmark {

    private Controller controller;
    private Image testImage;
    public MyList<int[]> data;

    @Setup(Level.Iteration)
    public void setup() {
        controller = new Controller();

        new JFXPanel();


        testImage = new Image("/com/example/dsa2_ca1/autumn.png", 512, 512, true, false);

        controller.selectedColour = Color.color(0.886, 0.741, 0.8, 1.00);

        data = new MyArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            data.add(new int[]{i, (int)(Math.random() * 10000)});
        }
    }

    @Benchmark
    public void bubbleSortBM() {
        controller.bubbleSort(data);
    }

    @Benchmark
    public void buildBinaryGridBM() {
        controller.buildBinaryGrid(testImage);
    }

    public static void main(String[] args) throws RunnerException, IOException {
        Main.main(args);
    }
}
