package com.sunrisedental;

final class TestConsole {
    private TestConsole() {
    }

    static void report(String scenario, Object expected, Object actual) {
        boolean passed = java.util.Objects.equals(expected, actual);
        System.out.printf("TEST: %-55s | Expected: %-12s | Actual: %-12s | Result: %s%n",
                scenario, expected, actual, passed ? "PASS" : "FAIL");
    }
}