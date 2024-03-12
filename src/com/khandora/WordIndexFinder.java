package com.khandora;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WordIndexFinder {

    public static void main(String[] args) {
        String fileName = "src/com/khandora/WarAndPeace.txt";
        String targetWord = "Пьер";

        List<Integer> indexes = findIndexesOfWordFromText(fileName, targetWord);
        System.out.println("Индексы всех вхождений слова \"" + targetWord + "\" в тексте романа Война и Мир: " + indexes);
    }

    private static List<Integer> findIndexesOfWordFromText(String fileName, String targetWord) {
        List<Integer> indexes = new ArrayList<>();
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            List<WordIndexTask> tasks = new ArrayList<>();
            int lineNumber = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                WordIndexTask task = new WordIndexTask(line, targetWord, lineNumber);
                tasks.add(task);
            }

            indexes = forkJoinPool.invoke(new ForkJoinMergeTask(tasks));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            forkJoinPool.shutdown();
        }

        return indexes;
    }

    private static class WordIndexTask extends RecursiveTask<List<Integer>> {
        private final String line;
        private final String targetWord;
        private final int lineNumber;

        public WordIndexTask(String line, String targetWord, int lineNumber) {
            this.line = line;
            this.targetWord = targetWord;
            this.lineNumber = lineNumber;
        }

        @Override
        protected List<Integer> compute() {
            List<Integer> localIndexes = new ArrayList<>();
            int index = line.indexOf(targetWord);

            while (index != -1) {
                localIndexes.add(lineNumber + index);
                index = line.indexOf(targetWord, index + 1);
            }

            return localIndexes;
        }
    }

    private static class ForkJoinMergeTask extends RecursiveTask<List<Integer>> {
        private final List<WordIndexTask> tasks;

        public ForkJoinMergeTask(List<WordIndexTask> tasks) {
            this.tasks = tasks;
        }

        @Override
        protected List<Integer> compute() {
            List<Integer> mergedIndexes = new ArrayList<>();
            for (WordIndexTask task : tasks) {
                mergedIndexes.addAll(task.fork().join());
            }

            return mergedIndexes;
        }
    }
}

