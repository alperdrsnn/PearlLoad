package dev.grinn34.pearlload.pearl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParkedPearlTest {

    @Test
    void mapRoundTripKeepsFlightState() {
        ParkedPearl original = new ParkedPearl(
                "xuid:1",
                "world",
                -12.5,
                64.0,
                8.25,
                0.4,
                -0.03,
                1.2,
                90f,
                -15f,
                40
        );
        ParkedPearl restored = ParkedPearl.fromMap(original.toMap());
        assertNotNull(restored);
        assertEquals(original, restored);
    }

    @Test
    void missingOwnerIsRejected() {
        assertEquals(null, ParkedPearl.fromMap(Map.of("level", "world", "x", 1)));
    }
}
