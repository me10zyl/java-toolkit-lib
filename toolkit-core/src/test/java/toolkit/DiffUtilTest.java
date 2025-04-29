package toolkit;


import org.junit.jupiter.api.Test;
import toolkit.utils.DiffUtil;
import toolkit.utils.Patch;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiffUtilTest {

    static class TestObject {
        private int id;
        private String name;

        public TestObject(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }
    }

    @Test
    public void testDiff_InsertOnly() {
        List<TestObject> requestObjs = Arrays.asList(
            new TestObject(1, "new1"),
            new TestObject(2, "new2")
        );
        List<TestObject> dbObjects = Arrays.asList();
        
        Patch<TestObject> patch = DiffUtil.diff(requestObjs, dbObjects, TestObject::getId);
        
        assertEquals(2, patch.getInsertList().size());
        assertEquals(0, patch.getDeleteList().size());
        assertEquals(0, patch.getUpdateList().size());
    }

    @Test
    public void testDiff_DeleteOnly() {
        List<TestObject> requestObjs = Arrays.asList();
        List<TestObject> dbObjects = Arrays.asList(
            new TestObject(1, "old1"),
            new TestObject(2, "old2")
        );
        
        Patch<TestObject> patch = DiffUtil.diff(requestObjs, dbObjects, TestObject::getId);
        
        assertEquals(0, patch.getInsertList().size());
        assertEquals(2, patch.getDeleteList().size());
        assertEquals(0, patch.getUpdateList().size());
    }

    @Test
    public void testDiff_UpdateOnly() {
        List<TestObject> requestObjs = Arrays.asList(
            new TestObject(1, "new1"),
            new TestObject(2, "new2")
        );
        List<TestObject> dbObjects = Arrays.asList(
            new TestObject(1, "old1"),
            new TestObject(2, "old2")
        );
        
        Patch<TestObject> patch = DiffUtil.diff(requestObjs, dbObjects, TestObject::getId);
        
        assertEquals(0, patch.getInsertList().size());
        assertEquals(0, patch.getDeleteList().size());
        assertEquals(2, patch.getUpdateList().size());
    }

    @Test
    public void testDiff_MixedOperations() {
        List<TestObject> requestObjs = Arrays.asList(
            new TestObject(1, "new1"),  // update
            new TestObject(3, "new3")   // insert
        );
        List<TestObject> dbObjects = Arrays.asList(
            new TestObject(1, "old1"),  // update
            new TestObject(2, "old2")   // delete
        );
        
        Patch<TestObject> patch = DiffUtil.diff(requestObjs, dbObjects, TestObject::getId);
        
        assertEquals(1, patch.getInsertList().size());
        assertEquals(1, patch.getDeleteList().size());
        assertEquals(1, patch.getUpdateList().size());
    }

    @Test
    public void testDiff_EmptyLists() {
        List<TestObject> requestObjs = Arrays.asList();
        List<TestObject> dbObjects = Arrays.asList();
        
        Patch<TestObject> patch = DiffUtil.diff(requestObjs, dbObjects, TestObject::getId);
        
        assertEquals(0, patch.getInsertList().size());
        assertEquals(0, patch.getDeleteList().size());
        assertEquals(0, patch.getUpdateList().size());
    }
}
