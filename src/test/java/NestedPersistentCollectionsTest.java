import org.junit.jupiter.api.Test;
import persistence.structure.array.PersistentArray;
import persistence.structure.map.PersistentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedPersistentCollectionsTest {
    @Test
    public void testGetIn() {
        var outerArray = new PersistentArray<PersistentArray<PersistentArray<Integer>>>();
        var innerArray = new PersistentArray<PersistentArray<Integer>>();
        var innerInnerArray = new PersistentArray<Integer>();

        outerArray = outerArray.add(innerArray.add(innerInnerArray.add(42)));

        var res = outerArray.getIn(0, 0, 0);

        assertEquals(42, res);
    }

    @Test
    public void testSetIn() {
        var outerArray = new PersistentArray<PersistentArray<PersistentArray<Integer>>>();
        var innerArray = new PersistentArray<PersistentArray<Integer>>();
        var innerInnerArray = new PersistentArray<Integer>();

        outerArray = outerArray.add(innerArray.add(innerInnerArray.add(42)));

        var res = outerArray.setIn(21, 0, 0, 0);

        assertEquals(21, res.getIn(0, 0, 0));
    }

    @Test
    public void testSetInMap() {
        var outerArray = new PersistentArray<PersistentArray<PersistentMap<Integer, Integer>>>();
        var innerArray = new PersistentArray<PersistentMap<Integer, Integer>>();
        var innerInnerArray = new PersistentMap<Integer, Integer>();

        outerArray = outerArray.add(innerArray.add(innerInnerArray.add(42, 42)));

        var res = outerArray.setIn(24, 0, 0, 42);

        assertEquals(24, res.getIn(0, 0, 42));
    }
}
