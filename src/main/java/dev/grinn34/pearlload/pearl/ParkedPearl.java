package dev.grinn34.pearlload.pearl;

import java.util.LinkedHashMap;
import java.util.Map;

public record ParkedPearl(
        String owner,
        String levelFolder,
        double x,
        double y,
        double z,
        double motionX,
        double motionY,
        double motionZ,
        float yaw,
        float pitch,
        int age
) {

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("owner", owner);
        map.put("level", levelFolder);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("mx", motionX);
        map.put("my", motionY);
        map.put("mz", motionZ);
        map.put("yaw", yaw);
        map.put("pitch", pitch);
        map.put("age", age);
        return map;
    }

    public static ParkedPearl fromMap(Map<?, ?> map) {
        Object owner = map.get("owner");
        Object level = map.get("level");
        if (owner == null || level == null) {
            return null;
        }
        return new ParkedPearl(
                String.valueOf(owner),
                String.valueOf(level),
                number(map.get("x")),
                number(map.get("y")),
                number(map.get("z")),
                number(map.get("mx")),
                number(map.get("my")),
                number(map.get("mz")),
                (float) number(map.get("yaw")),
                (float) number(map.get("pitch")),
                (int) number(map.get("age"))
        );
    }

    private static double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0;
        }
        return Double.parseDouble(String.valueOf(value));
    }
}
