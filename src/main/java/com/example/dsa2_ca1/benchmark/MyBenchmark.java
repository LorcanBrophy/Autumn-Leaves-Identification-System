package com.example.dsa2_ca1.benchmark;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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


@Measurement(iterations = 3, time = 2)
@Warmup(iterations = 3, time = 2)
@Fork(value = 1)

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)

public class MyBenchmark {

    private Controller controller;

    private BufferedImage testImage;
    private int[] binaryGrid;
    private UnionFind unionFind;

    private MyList<int[]> data;

    @Setup(Level.Invocation)
    public void setup() {
        controller = new Controller();

        // load image as buffered image
        try (InputStream inputStream = getClass().getResourceAsStream("/com/example/dsa2_ca1/149.png")) {
            assert inputStream != null;
            testImage = controller.resize(ImageIO.read(inputStream), 512, 512);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        controller.selectedColour = Color.color(0.886, 0.741, 0.8, 1.00);

        // build binaryGrid
        binaryGrid = controller.buildBinaryGrid(testImage);

        // build unionFind
        unionFind = controller.buildUnionFind(binaryGrid, 512, 512);

        // build data set for sorting algo
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
