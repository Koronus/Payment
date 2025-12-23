package com.example.Payment.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static javax.crypto.Cipher.SECRET_KEY;

// ВАЖНО: Аннотация @Converter должна быть!
//@Converter(autoApply = true) // autoApply = true автоматически применяет ко всем String полям
// ИЛИ
@Component
@Converter
public class SecureCardNumberConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private final String encryptionKey;

    // Конструктор с внедрением значения из properties
    public SecureCardNumberConverter(
            @Value("${card.encryption.key:1234567890123456}")
            String encryptionKey) {

        //Отладка вывода данных о карте в консоль
//        System.out.println("=== ИНИЦИАЛИЗАЦИЯ КОНВЕРТЕРА ===");
//        System.out.println("Ключ из properties: '" + encryptionKey + "'");
//        System.out.println("Длина ключа в символах: " + encryptionKey.length());
//        System.out.println("Длина ключа в байтах: " + encryptionKey.getBytes(StandardCharsets.UTF_8).length);

        // Проверяем длину ключа
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Неверная длина ключа AES: " + keyBytes.length + " байт. " +
                            "Должно быть 16, 24 или 32 байта. " +
                            "Ваш ключ: '" + encryptionKey + "' (" + encryptionKey.length() + " символов)"
            );
        }

        this.encryptionKey = encryptionKey;
//        System.out.println("Конвертер инициализирован с ключом длиной " + keyBytes.length + " байт");
//        System.out.println("=================================");
    }

    @Override
    public String convertToDatabaseColumn(String cardNumber) {
        //System.out.println("=== ШИФРОВАНИЕ (сохранение в БД) ===");

        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }

        // Если это уже зашифрованные данные (base64), возвращаем как есть
        if (isBase64Encrypted(cardNumber)) {
            //System.out.println("Данные уже зашифрованы, пропускаем");
            return cardNumber;
        }

        try {
            // Очищаем от пробелов
            String cleanNumber = cardNumber.replaceAll("\\s+", "");
            System.out.println("Очищенный номер: " + maskCardNumber(cleanNumber));

            // Генерируем IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Создаем ключ
            SecretKey secretKey = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );

            // Инициализируем шифрование
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            // Шифруем
            byte[] encryptedCardNumber = cipher.doFinal(
                    cleanNumber.getBytes(StandardCharsets.UTF_8)
            );

            // Комбинируем IV + зашифрованные данные
            byte[] combined = new byte[iv.length + encryptedCardNumber.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedCardNumber, 0, combined, iv.length, encryptedCardNumber.length);

            String result = Base64.getEncoder().encodeToString(combined);
            System.out.println("Зашифровано успешно. Результат: " +
                    result.substring(0, Math.min(20, result.length())) + "...");

            return result;

        } catch (Exception e) {
            //System.err.println("ОШИБКА ШИФРОВАНИЯ: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка при шифровании номера карты", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedCardNumber) {
        //System.out.println("=== ДЕШИФРОВАНИЕ (чтение из БД) ===");

        if (encryptedCardNumber == null || encryptedCardNumber.trim().isEmpty()) {
            return null;
        }

        // Вариант 1: Это обычный номер карты (не зашифрованный)
        if (isPlainCardNumber(encryptedCardNumber)) {
//            System.out.println("Это обычный номер карты (не зашифрован): " +
//                    maskCardNumber(encryptedCardNumber));
            return encryptedCardNumber;
        }

        // Вариант 2: Это base64 зашифрованные данные
        if (isBase64Encrypted(encryptedCardNumber)) {
            //System.out.println("Пытаемся расшифровать base64 данные длиной: " + encryptedCardNumber.length());

            try {
                byte[] combined = Base64.getDecoder().decode(encryptedCardNumber);

                if (combined.length < GCM_IV_LENGTH) {
                    System.out.println("Данные слишком короткие");
                    return "****";
                }

                byte[] iv = new byte[GCM_IV_LENGTH];
                System.arraycopy(combined, 0, iv, 0, iv.length);

                byte[] encryptedData = new byte[combined.length - GCM_IV_LENGTH];
                System.arraycopy(combined, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

                SecretKey secretKey = new SecretKeySpec(
                        encryptionKey.getBytes(StandardCharsets.UTF_8),
                        "AES"
                );

                Cipher cipher = Cipher.getInstance(ALGORITHM);
                GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

                byte[] decryptedCardNumber = cipher.doFinal(encryptedData);
                String result = new String(decryptedCardNumber, StandardCharsets.UTF_8);

         //       System.out.println("Успешно расшифровано: " + maskCardNumber(result));
                return result;

            } catch (Exception e) {
                System.err.println("ОШИБКА ДЕШИФРОВАНИЯ: " + e.getClass().getName() + ": " + e.getMessage());

                // Если не удалось расшифровать, возможно это старые данные
                return "**** (не удалось расшифровать)";
            }
        }

        // Вариант 3: Неизвестный формат
       // System.out.println("Неизвестный формат данных: " +
         //       encryptedCardNumber.substring(0, Math.min(30, encryptedCardNumber.length())) + "...");
        return "****";
    }

    private boolean isPlainCardNumber(String data) {
        if (data == null) return false;

        // Убираем пробелы и проверяем на цифры
        String clean = data.replaceAll("\\s+", "");
        return clean.matches("\\d{13,19}");
    }

    private boolean isBase64Encrypted(String data) {
        if (data == null) return false;

        // Проверяем на base64 формат
        return data.matches("^[A-Za-z0-9+/]+={0,2}$") &&
                data.length() >= 20 && // Зашифрованные данные обычно длинные
                data.length() % 4 == 0; // Base64 обычно кратен 4
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "***";
        }
        String clean = cardNumber.replaceAll("\\s+", "");
        if (clean.length() >= 16) {
            return clean.substring(0, 4) + " **** **** " + clean.substring(clean.length() - 4);
        }
        return clean.substring(0, 4) + " **** **" + clean.substring(clean.length() - 2);
    }
}

