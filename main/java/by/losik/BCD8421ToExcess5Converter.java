package by.losik;

import java.util.Scanner;

public class BCD8421ToExcess5Converter {
    public static int convertDigit(int bcd) {
        return (bcd % 10) + 5 > 10 ? ((bcd % 10) + 5) % 10 : (bcd % 10) + 5 ;
    }
    public static int binaryStringToInt(String binary) {
        if (binary == null || binary.trim().isEmpty()) {
            throw new IllegalArgumentException("Двоичная строка не может быть null или пустой");
        }

        binary = binary.trim();
        boolean isNegative = false;

        if (binary.startsWith("-")) {
            isNegative = true;
            binary = binary.substring(1);
        }

        int result = 0;

        for (int i = 0; i < binary.length(); i++) {
            char c = binary.charAt(i);
            if (c != '0' && c != '1') {
                throw new IllegalArgumentException(
                        String.format("Недопустимый символ '%c' в двоичной строке на позиции %d", c, i+1)
                );
            }
            result = (result << 1) | (c - '0');
        }

        return isNegative ? -result : result;
    }
    public static String intToBinaryString(int number) {
        if (number == 0) return "0";

        StringBuilder binary = new StringBuilder();
        boolean isNegative = number < 0;
        int absNumber = Math.abs(number);

        while (absNumber > 0) {
            binary.insert(0, absNumber & 1);
            absNumber >>= 1;
        }

        if (isNegative) {
            binary.insert(0, "-");
        }
        if(binary.length() > 3){
            return binary.substring(binary.length()-4,binary.length());
        }
        else{
            while(binary.length() < 4){
                binary.insert(0, "0");
            }
            return binary.toString();
        }
    }
    public static int binaryToDigit(String binary) {
        if (binary.length() != 4) {
            throw new IllegalArgumentException("Двоичное представление должно быть 4-битным");
        }

        return binaryStringToInt(binary);
    }
    public static String digitToBinary(int digit) {
        return intToBinaryString(digit);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Преобразователь кода Д8421 в код Д8421+5");
        System.out.println("Выберите режим работы:");
        System.out.println("1 - Преобразование одной тетрады");
        System.out.println("2 - Преобразование двоичного представления тетрады");

        int mode = scanner.nextInt();

        try {
            switch (mode) {
                case 1 -> {
                    System.out.print("Введите десятичное число: ");
                    int digit = (int)scanner.nextDouble();
                    if(digit < 0) {
                        throw new RuntimeException("Положительное число");
                    }
                    int convertedDigit = convertDigit(digit);
                    System.out.printf("Результат преобразования: %d -> %d\n", digit, convertedDigit);
                    System.out.printf("Двоичное представление: %s -> %s\n",
                            digitToBinary(digit), digitToBinary(convertedDigit));
                }
                case 2 -> {
                    System.out.print("Введите 4-битное двоичное представление тетрады (например, 0101): ");
                    String binary = scanner.next();
                    int digitFromBinary = binaryToDigit(binary);
                    int convertedFromBinary = convertDigit(digitFromBinary);
                    System.out.printf("Результат преобразования: %s -> %s\n",
                            binary, digitToBinary(convertedFromBinary));
                }
                default -> throw new RuntimeException("Неверный режим работы");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        scanner.close();
    }
}