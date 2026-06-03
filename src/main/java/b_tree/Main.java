package b_tree;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        System.out.println(repeat("=", 60));
        System.out.println("B-ДЕРЕВО: ТЕСТИРОВАНИЕ ПРОИЗВОДИТЕЛЬНОСТИ");
        System.out.println(repeat("=", 60));

        // Создаём B-дерево с минимальной степенью t=3
        BTree bTree = new BTree(3);

        // Генерируем 10000 случайных чисел
        System.out.println("\n1. Генерация массива из 10000 случайных чисел...");
        int[] data = ThreadLocalRandom.current().ints(10000, 0, 100000).toArray();
        System.out.println("   Готово.");

        // ========== 3. ВСТАВКА ВСЕХ ЭЛЕМЕНТОВ ==========
        System.out.println("\n2. Вставка всех элементов в B-дерево...");
        List<BTree.Result> insertResults = new ArrayList<>();
        long insertTotalTime = 0;

        for (int i = 0; i < data.length; i++) {
            BTree.Result res = bTree.insert(data[i]);
            insertResults.add(res);
            insertTotalTime += res.getTimeNanos();

            if ((i + 1) % 1000 == 0) {
                System.out.printf("   Вставлено %d элементов...\n", i + 1);
            }
        }

        // ========== 4. ПОИСК 100 СЛУЧАЙНЫХ ЭЛЕМЕНТОВ ==========
        System.out.println("\n3. Поиск 100 случайных элементов...");
        List<Integer> searchElements = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            searchElements.add(data[rand.nextInt(data.length)]);
        }

        List<BTree.Result> searchResults = new ArrayList<>();
        long searchTotalTime = 0;
        for (int el : searchElements) {
            BTree.Result res = bTree.search(el);
            searchResults.add(res);
            searchTotalTime += res.getTimeNanos();
        }

        // ========== 5. УДАЛЕНИЕ 1000 СЛУЧАЙНЫХ ЭЛЕМЕНТОВ ==========
        System.out.println("\n4. Удаление 1000 случайных элементов...");
        List<Integer> deleteElements = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            deleteElements.add(data[rand.nextInt(data.length)]);
        }

        List<BTree.Result> deleteResults = new ArrayList<>();
        long deleteTotalTime = 0;
        for (int el : deleteElements) {
            BTree.Result res = bTree.delete(el);
            deleteResults.add(res);
            deleteTotalTime += res.getTimeNanos();
        }

        // ========== 6. ВЫЧИСЛЕНИЕ СРЕДНИХ ЗНАЧЕНИЙ ==========
        System.out.println("\n" + repeat("=", 60));
        System.out.println("РЕЗУЛЬТАТЫ ИЗМЕРЕНИЙ");
        System.out.println(repeat("=", 60));

        double avgInsertOps = 0;
        for (BTree.Result res : insertResults) {
            avgInsertOps += res.getOps();
        }
        avgInsertOps /= insertResults.size();

        double avgInsertTime = 0;
        for (BTree.Result res : insertResults) {
            avgInsertTime += res.getTimeNanos();
        }
        avgInsertTime /= insertResults.size();

        double avgSearchOps = 0;
        for (BTree.Result res : searchResults) {
            avgSearchOps += res.getOps();
        }
        avgSearchOps /= searchResults.size();

        double avgSearchTime = 0;
        for (BTree.Result res : searchResults) {
            avgSearchTime += res.getTimeNanos();
        }
        avgSearchTime /= searchResults.size();

        double avgDeleteOps = 0;
        for (BTree.Result res : deleteResults) {
            avgDeleteOps += res.getOps();
        }
        avgDeleteOps /= deleteResults.size();

        double avgDeleteTime = 0;
        for (BTree.Result res : deleteResults) {
            avgDeleteTime += res.getTimeNanos();
        }
        avgDeleteTime /= deleteResults.size();

        // Вывод в читаемом формате
        System.out.printf("\n%-15s | %18s | %21s\n",
                "Операция", "Среднее время (нс)", "Среднее число операций");
        System.out.println(repeat("-", 60));
        System.out.printf("%-15s | %18.2f | %21.2f\n",
                "Вставка", avgInsertTime, avgInsertOps);
        System.out.printf("%-15s | %18.2f | %21.2f\n",
                "Поиск", avgSearchTime, avgSearchOps);
        System.out.printf("%-15s | %18.2f | %21.2f\n",
                "Удаление", avgDeleteTime, avgDeleteOps);

        // Теоретическая оценка
        System.out.println("\n" + repeat("=", 61));
        System.out.println("ТЕОРЕТИЧЕСКАЯ ОЦЕНКА");
        System.out.println(repeat("=", 60));
        double height = Math.log(10000) / Math.log(3);
        System.out.printf("Высота дерева при t=3 и n=10000: ~%.1f уровней\n", height);
        System.out.println("Поиск: O(log₃ n) ≈ " + (int)Math.ceil(height) + " операций");
        System.out.println("Вставка/удаление: O(t·log₃ n) ≈ " + (int)(3 * Math.ceil(height)) + " операций");

        System.out.println("\nТестирование завершено!");
    }

    // Вспомогательный метод для повторения строки
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}