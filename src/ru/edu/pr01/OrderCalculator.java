package ru.edu.pr01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Расчёт стоимости заказа: база -> скидка -> НДС -> итог.
 * Все пороги и ставка НДС собраны в константах в начале класса.
 */
public class OrderCalculator {
    /** Минимальное количество единиц (включительно). */
    public static final int MIN_QUANTITY = 1;
    /** Максимальное количество единиц (включительно). */
    public static final int MAX_QUANTITY = 10_000;
    /** Максимальная цена единицы, руб. (включительно). Цена должна быть > 0. */
    public static final double MAX_UNIT_PRICE = 5_000_000.0;
    /** Скидка в процентах, диапазон 0..30 включительно. */
    public static final double MIN_DISCOUNT = 0.0;
    public static final double MAX_DISCOUNT = 30.0;
    /** Вариант 4: ставка НДС 25 %. */
    public static final double VAT_RATE = 25.0;

    /** true, если все три значения в допустимых диапазонах. */
    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        return validationError(quantity, unitPrice, discountPercent) == null;
    }

    /** Текст ошибки для первого неверного значения или null, если всё корректно. */
    public static String validationError(int quantity, double unitPrice, double discountPercent) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            return "Количество " + quantity + " вне диапазона " + MIN_QUANTITY + ".." + MAX_QUANTITY;
        }
        if (!(unitPrice > 0) || unitPrice > MAX_UNIT_PRICE) {      // !(x > 0) ловит и NaN
            return "Цена " + unitPrice + " должна быть больше 0 и не больше " + MAX_UNIT_PRICE;
        }
        if (!(discountPercent >= MIN_DISCOUNT) || discountPercent > MAX_DISCOUNT) {
            return "Скидка " + discountPercent + "% вне диапазона " + MIN_DISCOUNT + ".." + MAX_DISCOUNT;
        }
        return null;
    }

    /** Базовая сумма = цена × количество. */
    public static double calculateBase(int quantity, double unitPrice) {
        return unitPrice * quantity;
    }

    /** Сумма после скидки: скидка — это ПРОЦЕНТ, а не абсолютная сумма. */
    public static double applyDiscount(double base, double discountPercent) {
        return base * (100.0 - discountPercent) / 100.0;
    }

    /** НДС считается от суммы ПОСЛЕ скидки. */
    public static double calculateVat(double discounted, double vatPercent) {
        return discounted * vatPercent / 100.0;
    }

    /** Итог по ставке НДС из параметра (используется SelfCheck и main). */
    public static double calculateTotal(int quantity, double unitPrice, double discountPercent, double vatPercent) {
        double base = calculateBase(quantity, unitPrice);
        double discounted = applyDiscount(base, discountPercent);
        return discounted + calculateVat(discounted, vatPercent);
    }

    /** Итог по ставке НДС варианта (VAT_RATE). */
    public static double calculateTotal(int quantity, double unitPrice, double discountPercent) {
        return calculateTotal(quantity, unitPrice, discountPercent, VAT_RATE);
    }

    /** Округление до копеек (только для вывода и сравнения). */
    public static double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Полный расчёт одной строки "quantity;unitPrice;discountPercent".
     * Возвращает текст отчёта или текст ошибки (расчёт при ошибке не выполняется).
     */
    public static String processLine(String line) {
        if (line == null || line.isBlank()) {
            return "ОШИБКА: пустая строка";
        }
        String[] p = line.split(";", -1);
        if (p.length != 3) {
            return "ОШИБКА: ожидалось 3 значения, получено " + p.length;
        }
        int quantity;
        double price;
        double discount;
        try {
            quantity = Integer.parseInt(p[0].trim());
        } catch (NumberFormatException e) {
            return "ОШИБКА: количество \"" + p[0].trim() + "\" не целое число";
        }
        try {
            price = Double.parseDouble(p[1].trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return "ОШИБКА: цена \"" + p[1].trim() + "\" не число";
        }
        try {
            discount = Double.parseDouble(p[2].trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return "ОШИБКА: скидка \"" + p[2].trim() + "\" не число";
        }
        String error = validationError(quantity, price, discount);
        if (error != null) {
            return "ОШИБКА: " + error;
        }
        double base = calculateBase(quantity, price);
        double discounted = applyDiscount(base, discount);
        double vat = calculateVat(discounted, VAT_RATE);
        return String.format("база %.2f руб.; после скидки %.2f руб.; НДС %.0f%% %.2f руб.; итого %.2f руб.",
                base, discounted, VAT_RATE, vat, discounted + vat);
    }

    /**
     * Без аргументов — ввод с клавиатуры через Scanner (строка quantity;unitPrice;discountPercent).
     * С аргументом — путь к CSV (первая строка — заголовок).
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            try (Scanner sc = new Scanner(Paths.get(args[0]), StandardCharsets.UTF_8)) {
                if (sc.hasNextLine()) sc.nextLine();          // заголовок
                int n = 1;
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    System.out.println(n++ + ") [" + line + "] -> " + processLine(line));
                }
            }
        } else {
            Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
            System.out.print("Введите количество;цену;скидку%: ");
            if (sc.hasNextLine()) {
                System.out.println(processLine(sc.nextLine()));
            }
        }
    }
}
