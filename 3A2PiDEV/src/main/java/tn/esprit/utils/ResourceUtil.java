package tn.esprit.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ResourceUtil {

    private ResourceUtil() {}

    public static String readResourceAsString(String classpathPath) {
        InputStream is = ResourceUtil.class.getClassLoader().getResourceAsStream(classpathPath);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + classpathPath);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + classpathPath, e);
        }
    }
}