package com.example.dsa2_ca1.benchmark;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.example.dsa2_ca1.model.MyArrayList;
import com.example.dsa2_ca1.model.MyList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.paint.Color;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import com.example.dsa2_ca1.model.UnionFind;
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
    private MyList<int[]> data;
    private int[] binaryGrid;
    private UnionFind unionFind;

    @Setup(Level.Iteration)
    public void setup() {
        controller = new Controller();
        new JFXPanel();

        testImage = new Image(Objects.requireNonNull(getClass().getResource("/com/example/dsa2_ca1/autumn.png")).toExternalForm(), 512, 512, false, false);
        controller.selectedColour = Color.color(0.886, 0.741, 0.8, 1.00);

        binaryGrid = controller.buildBinaryGrid(testImage);
        unionFind = controller.buildUnionFind(binaryGrid, 512, 512);

        data = new MyArrayList<>(10000);
        for (int i = 0; i < 10000; i++) {
            data.add(new int[]{i, (int)(Math.random() * 10000)});
        }
    }

    // BM for main algo

    @Benchmark
    public void buildBinaryGridBM() {
        controller.buildBinaryGrid(testImage);
    }

    @Benchmark
    public void buildUnionFindBM() {
        controller.buildUnionFind(binaryGrid, 512, 512);
    }

    @Benchmark
    public void buildValidClustersBM() {
        controller.buildValidClusters(binaryGrid, unionFind);
    }

    // BM for sorting nodes

    @Benchmark
    public void insertionSortBM() {
        controller.insertionSort(data);
    }

    public static void main(String[] args) throws RunnerException, IOException {
        Main.main(args);
    }
}
