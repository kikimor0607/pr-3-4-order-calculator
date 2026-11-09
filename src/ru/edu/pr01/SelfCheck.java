package ru.edu.pr01;

/** Самопроверка: исходная проверка + собственные граничные проверки. */
public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // --- Исходная проверка (ставка 20 % передаётся явно параметром) ---
        check("исходная: base 2*100 = 200", OrderCalculator.calculateBase(2, 100.0) == 200.0);
        check("исходная: total(2,100,10%,НДС 20%) = 216",
                Math.abs(OrderCalculator.calculateTotal(2, 100.0, 10.0, 20.0) - 216.0) < 0.001);

        // --- Вариант 4: НДС 25 % ---
        check("вариант 4: total(2,100,10%) = 180 * 1.25 = 225",
                Math.abs(OrderCalculator.calculateTotal(2, 100.0, 10.0) - 225.0) < 0.001);
        check("НДС считается после скидки: vat(180, 25) = 45",
                Math.abs(OrderCalculator.calculateVat(180.0, 25.0) - 45.0) < 0.001);
        check("скидка — процент: applyDiscount(1000, 5) = 950",
                Math.abs(OrderCalculator.applyDiscount(1000.0, 5.0) - 950.0) < 0.001);
        check("типовая строка 12;2490.50;5: итог 35489.63",
                OrderCalculator.roundMoney(OrderCalculator.calculateTotal(12, 2490.50, 5)) == 35489.63);

        // --- Количество 1..10000 ---
        check("количество 1 (нижняя граница включена)", OrderCalculator.isValid(1, 100, 0));
        check("количество 0 (исключено)", !OrderCalculator.isValid(0, 100, 0));
        check("количество 10000 (верхняя граница включена)", OrderCalculator.isValid(10_000, 100, 0));
        check("количество 10001 (исключено)", !OrderCalculator.isValid(10_001, 100, 0));

        // --- Цена (0; 5 000 000] ---
        check("цена 0.01 допустима", OrderCalculator.isValid(1, 0.01, 0));
        check("цена 0 недопустима", !OrderCalculator.isValid(1, 0, 0));
        check("цена -20 недопустима", !OrderCalculator.isValid(5, -20, 5));
        check("цена 5 000 000 (включено)", OrderCalculator.isValid(1, 5_000_000, 0));
        check("цена 5 000 000.01 (исключено)", !OrderCalculator.isValid(1, 5_000_000.01, 0));
        check("цена NaN недопустима", !OrderCalculator.isValid(1, Double.NaN, 0));

        // --- Скидка 0..30 ---
        check("скидка 0 (включено)", OrderCalculator.isValid(1, 100, 0));
        check("скидка 30 (включено)", OrderCalculator.isValid(1, 100, 30));
        check("скидка 30.01 (исключено)", !OrderCalculator.isValid(1, 100, 30.01));
        check("скидка 35 (исключено)", !OrderCalculator.isValid(3, 999.99, 35));
        check("скидка -1 (исключено)", !OrderCalculator.isValid(1, 100, -1));

        // --- Разбор строк: пустые и неверный формат ---
        error("пустая строка", "", "пустая строка");
        error("пропущена цена", "5;;10", "цена");
        error("количество не число", "abc;100;5", "количество");
        error("дробное количество", "2.5;100;5", "количество");
        error("мало значений", "5;100", "получено 2");
        check("цена с запятой 999,99 понимается",
                !OrderCalculator.processLine("3;999,99;10").startsWith("ОШИБКА"));
        error("строка с ошибкой не считается", "0;1000;10", "Количество 0");

        System.out.println();
        System.out.println("Итого: " + passed + " PASS, " + failed + " FAIL");
        System.out.println(failed == 0 ? "PASS" : "FAIL  complete TODO methods");
    }

    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("PASS  " + name); }
        else    { failed++; System.out.println("FAIL  " + name); }
    }

    private static void error(String name, String line, String expectedPart) {
        String result = OrderCalculator.processLine(line);
        check(name + " -> " + result, result.startsWith("ОШИБКА") && result.contains(expectedPart));
    }
}
