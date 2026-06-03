package b_tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BTree {
    private Node root;          // Корень дерева
    private final int t;        // Минимальная степень (параметр дерева)

    public BTree(int t) {
        this.t = t;
        root = new Node(true);  // Начинаем с пустого листового узла
    }

    // ======================== ВНУТРЕННИЙ КЛАСС УЗЛА ========================

    class Node {
        List<Integer> keys;        // Отсортированный список ключей
        List<Node> children;       // Список дочерних узлов (на 1 больше чем keys)
        boolean leaf;              // true - лист, false - внутренний узел

        Node(boolean leaf) {
            this.keys = new ArrayList<>();
            this.children = new ArrayList<>();
            this.leaf = leaf;
        }

        /* Находит позицию ключа в узле с помощью бинарного поиска
        key - искомый ключ
        return - индекс ключа, если найден, иначе индекс для вставки (где ключ мог бы находиться) */
        int findKey(int key) {
            int idx = Collections.binarySearch(keys, key);
            // binarySearch возвращает:
            // - положительный индекс, если ключ найден
            // - отрицательное значение индекса, где он мог бы быть, - 1 (insertionIndex - 1), если не найден
            return idx >= 0 ? idx : -idx - 1;
        }
    }

    // ======================== КЛАСС ДЛЯ РЕЗУЛЬТАТОВ ========================

    public static class Result {
        private final long timeNanos;   // время выполнения в наносекундах
        private final int ops;          // количество операций сравнения

        public Result(long timeNanos, int ops) {
            this.timeNanos = timeNanos;
            this.ops = ops;
        }

        public long getTimeNanos() {
            return timeNanos;
        }
        public int getOps() {
            return ops;
        }

        @Override
        public String toString() {
            return "Result{timeNanos=" + timeNanos + ", ops=" + ops + "}";
        }
    }

    // ======================== ОПЕРАЦИЯ ПОИСКА ========================

    /* Алгоритм:
    1. Начинаем с корня
    2. В текущем узле ищем позицию ключа
    3. Если нашли - возвращаем успех
    4. Если это лист - ключа нет
    5. Иначе переходим к соответствующему дочернему узлу и повторяем */
    public Result search(int key) {
        long startTime = System.nanoTime();   // Засекаем время начала
        int ops = 0;                          // Счётчик операций
        Node curr = root;                     // Начинаем с корня

        while (true) {
            ++ops;                            // Учитываем операцию поиска в узле
            int idx = curr.findKey(key);      // Бинарный поиск внутри узла

            // Если нашли ключ в текущем узле
            if (idx < curr.keys.size() && curr.keys.get(idx) == key) {
                long time = System.nanoTime() - startTime;
                return new Result(time, ops);
            }

            // Если дошли до листа и не нашли - ключа нет
            if (curr.leaf) break;

            // Иначе спускаемся к нужному дочернему узлу
            curr = curr.children.get(idx);
        }

        // Ключ не найден
        long time = System.nanoTime() - startTime;
        return new Result(time, ops);
    }

    // ======================== ОПЕРАЦИЯ ВСТАВКИ ========================

    /* Алгоритм:
    1. Если корень полон (2t-1 ключей), создаём новый корень
    2. Расщепляем старый корень
    3. Рекурсивно вставляем ключ в неполный узел */
    public Result insert(int key) {
        long startTime = System.nanoTime();
        int ops = 0;

        // Случай 1: Корень заполнен (имеет 2t-1 ключей)
        // Нужно увеличить высоту дерева
        if (root.keys.size() == 2 * t - 1) {
            Node newRoot = new Node(false);   // Новый корень (не лист)
            newRoot.children.add(root);        // Старый корень становится левым ребёнком
            splitChild(newRoot, 0);            // Расщепляем старого ребёнка
            root = newRoot;                    // Обновляем корень
            ++ops;
        }

        // Вставляем ключ в неполный узел
        Result res = insertNonFull(root, key, ops);
        long time = System.nanoTime() - startTime;
        return new Result(time, res.getOps());
    }

    /* Рекурсивная вставка ключа в неполный узел
    Узел гарантированно не заполнен (имеет < 2t-1 ключей)

    node - текущий узел
    key - вставляемый ключ
    ops - текущее количество операций
    return - Result с обновлённым счётчиком операций */
    private Result insertNonFull(Node node, int key, int ops) {
        int idx = node.findKey(key);   // Находим позицию для вставки

        // Случай 1: Узел - лист
        // Просто вставляем ключ в нужную позицию
        if (node.leaf) {
            node.keys.add(idx, key);   // Вставка в отсортированный список
            ++ops;
            return new Result(0, ops);
        }
        // Случай 2: Внутренний узел
        else {
            ++ops;

            // Если ребёнок, в которого будем вставлять, заполнен
            if (node.children.get(idx).keys.size() == 2 * t - 1) {
                splitChild(node, idx);   // Расщепляем его
                ++ops;
                // После расщепления определяем, в какого ребёнка вставлять
                if (key > node.keys.get(idx)) ++idx;
            }

            // Рекурсивно вставляем в подходящего ребёнка
            return insertNonFull(node.children.get(idx), key, ops);
        }
    }

    /* Расщепление заполненного дочернего узла

    Принцип:
    - Берём дочерний узел с 2t-1 ключами
    - Поднимаем средний ключ (индекс t-1) в родителя
    - Разделяем оставшиеся ключи на два узла по t-1 ключей

    Например, для t=3 (узел с 5 ключами):
    [1,2,3,4,5] → поднимаем 3 → левый: [1,2], правый: [4,5]

    parent - родительский узел
    idx - индекс расщепляемого ребёнка в списке детей родителя */
    private void splitChild(Node parent, int idx) {
        Node child = parent.children.get(idx);      // Расщепляемый узел
        Node newChild = new Node(child.leaf);       // Новый правый узел

        // 1. Поднимаем средний ключ в родителя
        parent.keys.add(idx, child.keys.get(t - 1));
        parent.children.add(idx + 1, newChild);

        // 2. Перемещаем правую половину ключей в новый узел
        // Ключи от t до 2t-1 (всего t-1 ключей)
        for (int i = t; i < 2 * t - 1; ++i) {
            newChild.keys.add(child.keys.get(i));
        }

        // 3. Удаляем перемещённые ключи из старого узла
        // Оставляем только ключи от 0 до t-2
        for (int i = 0; i < t; ++i) {
            child.keys.remove(t - 1);
        }

        // 4. Если узел не лист, перемещаем соответствующих детей
        if (!child.leaf) {
            // Дети от t до 2t-1 переходят к новому узлу
            for (int i = t; i < 2 * t; i++) {
                newChild.children.add(child.children.get(i));
            }
            // Удаляем перемещённых детей из старого узла
            for (int i = t; i < 2 * t; i++) {
                child.children.remove(t);
            }
        }
    }

    // ======================== ОПЕРАЦИЯ УДАЛЕНИЯ ========================

    /* Удаление ключа из B-дерева
    key - удаляемый ключ
    return - Result с временем и количеством операций */
    public Result delete(int key) {
        long startTime = System.nanoTime();
        int[] ops = new int[]{0};  // Используем массив для передачи по ссылке

        // Рекурсивное удаление
        Result res = deleteRec(root, key, ops);

        // Если корень опустел и у него есть дети, новый корень - первый ребёнок
        if (root.keys.isEmpty() && !root.leaf) {
            root = root.children.get(0);
        }

        long time = System.nanoTime() - startTime;
        return new Result(time, res.getOps());
    }

    /* Рекурсивное удаление ключа
    node - текущий узел
    key - удаляемый ключ
    ops - счётчик операций (массив для изменяемости)
    return - Result с обновлённым счётчиком */
    private Result deleteRec(Node node, int key, int[] ops) {
        ++ops[0];  // Учитываем текущую операцию
        int idx = node.findKey(key);  // Ищем позицию ключа

        // ========== СЛУЧАЙ 1: Ключ найден в текущем узле ==========
        if (idx < node.keys.size() && node.keys.get(idx) == key) {

            // Подслучай 1a: Удаление из листового узла
            // Самый простой случай - просто удаляем ключ
            if (node.leaf) {
                node.keys.remove(idx);
                return new Result(0, ops[0]);
            }
            // Подслучай 1b: Удаление из внутреннего узла
            else {
                Node left = node.children.get(idx);   // Левый ребёнок
                Node right = node.children.get(idx + 1); // Правый ребёнок

                // Вариант 1: Левый ребёнок имеет достаточно ключей (≥ t)
                // Берём предшественника (самый правый ключ в левом поддереве)
                if (left.keys.size() >= t) {
                    int pred = getPredecessor(left);       // Находим предшественника
                    node.keys.set(idx, pred);              // Заменяем удаляемый ключ
                    return deleteRec(left, pred, ops);     // Рекурсивно удаляем предшественника
                }
                // Вариант 2: Правый ребёнок имеет достаточно ключей (≥ t)
                // Берём последователя (самый левый ключ в правом поддереве)
                else if (right.keys.size() >= t) {
                    int succ = getSuccessor(right);        // Находим последователя
                    node.keys.set(idx, succ);              // Заменяем удаляемый ключ
                    return deleteRec(right, succ, ops);    // Рекурсивно удаляем последователя
                }
                // Вариант 3: Оба ребёнка имеют минимальное количество ключей (t-1)
                // Сливаем их и удаляем ключ из объединённого узла
                else {
                    merge(node, idx);                      // Слияние левого и правого
                    return deleteRec(left, key, ops);      // Продолжаем удаление
                }
            }
        }
        // ========== СЛУЧАЙ 2: Ключ НЕ найден в текущем узле ==========
        else {
            // Если дошли до листа - ключа нет в дереве
            if (node.leaf) {
                return new Result(0, ops[0]);
            }

            // Проверяем, достаточно ли ключей у ребёнка, к которому спускаемся
            if (node.children.get(idx).keys.size() < t) {
                ops[0] += fixChild(node, idx);  // Балансируем бедного ребёнка
                idx = node.findKey(key);
                if (idx >= node.children.size()) {
                    idx = node.children.size() - 1;
                }

            }

            // Рекурсивно удаляем из подходящего ребёнка
            return deleteRec(node.children.get(idx), key, ops);
        }
    }

    /* Находит предшественника (максимальный ключ в поддереве)
    Используется при удалении из внутреннего узла

    Алгоритм: идём всё время вправо-вниз до листа, берём самый правый ключ
    node - узел, с которого начинаем поиск
    return - значение ключа-предшественника */
    private int getPredecessor(Node node) {
        while (!node.leaf) {
            node = node.children.get(node.children.size() - 1);
        }
        return node.keys.get(node.keys.size() - 1);
    }

    /* Находит последователя (минимальный ключ в поддереве)
    Используется при удалении из внутреннего узла

    Алгоритм: идём всё время влево-вниз до листа, берём самый левый ключ
    node - узел, с которого начинаем поиск
    return - значение ключа-последователя */
    private int getSuccessor(Node node) {
        while (!node.leaf) {
            node = node.children.get(0);
        }
        return node.keys.get(0);
    }

    /* Слияние двух соседних узлов
    Вызывается, когда оба узла имеют минимальное количество ключей (t-1)

    Процесс:
    1. Берём ключ из родителя между этими узлами
    2. Добавляем его в левый узел
    3. Добавляем все ключи из правого узла в левый
    4. Добавляем всех детей из правого узла в левый (если не листья)
    5. Удаляем ключ из родителя и правый узел

    parent - родительский узел
    idx - индекс левого сливаемого узла (правый имеет индекс idx+1) */
    private void merge(Node parent, int idx) {
        Node left = parent.children.get(idx);   // Левый узел
        Node right = parent.children.get(idx + 1); // Правый узел

        // 1. Добавляем ключ из родителя в левый узел
        left.keys.add(parent.keys.get(idx));

        // 2. Добавляем все ключи из правого узла в левый
        left.keys.addAll(right.keys);

        // 3. Если не листья, добавляем детей
        if (!left.leaf) {
            left.children.addAll(right.children);
        }

        // 4. Удаляем использованный ключ из родителя и правый узел
        parent.keys.remove(idx);
        parent.children.remove(idx + 1);
    }

    /* Исправляет ситуацию, когда у ребёнка недостаточно ключей (< t-1)
    Пытается заимствовать ключ у соседа или выполняет слияние

    parent - родительский узел
    idx - индекс "бедного" ребёнка
    return - количество выполненных операций */
    private int fixChild(Node parent, int idx) {
        int ops = 0;
        Node child = parent.children.get(idx);

        // Вариант 1: Заимствуем у ЛЕВОГО соседа
        if (idx > 0 && parent.children.get(idx - 1).keys.size() >= t) {
            Node leftSib = parent.children.get(idx - 1);

            child.keys.add(0, parent.keys.get(idx - 1));
            parent.keys.set(idx - 1, leftSib.keys.remove(leftSib.keys.size() - 1));

            if (!child.leaf) {
                child.children.add(0, leftSib.children.remove(leftSib.children.size() - 1));
            }
            ++ops;
        }
        // Вариант 2: Заимствуем у ПРАВОГО соседа
        else if (idx < parent.children.size() - 1 &&
                parent.children.get(idx + 1).keys.size() >= t) {
            Node rightSib = parent.children.get(idx + 1);

            child.keys.add(parent.keys.get(idx));
            parent.keys.set(idx, rightSib.keys.remove(0));

            if (!child.leaf) {
                child.children.add(rightSib.children.remove(0));
            }
            ++ops;
        }
        // Вариант 3: Не у кого заимствовать - выполняем слияние
        else {
            if (idx > 0) {
                merge(parent, idx - 1);
            } else {
                merge(parent, idx);
            }
            ++ops;
        }
        return ops;
    }
}