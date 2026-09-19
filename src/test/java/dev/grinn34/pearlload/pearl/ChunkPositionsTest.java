package dev.grinn34.pearlload.pearl;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkPositionsTest {

    @Test
    void chunkOfUsesFloorForNegativeBlocks() {
        assertEquals(0, ChunkPositions.chunkOf(0));
        assertEquals(0, ChunkPositions.chunkOf(15.9));
        assertEquals(1, ChunkPositions.chunkOf(16));
        assertEquals(-1, ChunkPositions.chunkOf(-0.1));
        assertEquals(-1, ChunkPositions.chunkOf(-16));
        assertEquals(-2, ChunkPositions.chunkOf(-16.1));
    }

    @Test
    void radiusZeroIsOnlyThePearlChunk() {
        Set<Long> keys = ChunkPositions.tickets(3, -2, 0, 3, -2);
        assertEquals(Set.of(ChunkPositions.key(3, -2)), keys);
    }

    @Test
    void radiusOneIsThreeByThree() {
        Set<Long> keys = ChunkPositions.tickets(0, 0, 1, 0, 0);
        assertEquals(9, keys.size());
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                assertTrue(keys.contains(ChunkPositions.key(x, z)));
            }
        }
    }

    @Test
    void lookaheadOutsideRadiusIsAdded() {
        Set<Long> keys = ChunkPositions.tickets(0, 0, 0, 2, 0);
        assertEquals(Set.of(ChunkPositions.key(0, 0), ChunkPositions.key(2, 0)), keys);
    }

    @Test
    void flightTicketsIncludeDistantLandingChunk() {
        Set<Long> keys = ChunkPositions.flightTickets(0.5, 0.5, 2.0, 0.0, 32, 0.01, 0);
        assertTrue(keys.contains(ChunkPositions.key(0, 0)));
        assertTrue(keys.size() > 1);
    }

    @Test
    void lookaheadInsideRadiusIsNotDuplicated() {
        Set<Long> seen = new HashSet<>();
        ChunkPositions.visit(0, 0, 1, 1, 0, (x, z) -> {
            assertTrue(seen.add(ChunkPositions.key(x, z)), x + "," + z);
        });
        assertEquals(9, seen.size());
    }
}
