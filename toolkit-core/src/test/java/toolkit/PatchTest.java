package toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;


import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import toolkit.utils.Patch;

@ExtendWith(MockitoExtension.class)
public class PatchTest {

    @Mock
    private IService<String> mockService;

    private Patch<String> patch;

    @BeforeEach
    public void setUp() {
        patch = new Patch<>();
    }

    @Test
    public void testApplyPatchWithEmptyLists() {
        patch.applyPatch(mockService);
        verifyNoInteractions(mockService);
    }

    @Test
    public void testApplyPatchWithInsertList() {
        List<String> insertItems = Arrays.asList("a", "b");
        patch.setInsertList(insertItems);

        patch.applyPatch(mockService);

        verify(mockService).saveBatch(insertItems);
        verify(mockService, never()).removeByIds(anyList());
        verify(mockService, never()).updateBatchById(anyList());
    }

    @Test
    public void testApplyPatchWithDeleteList() {
        List<String> deleteItems = Arrays.asList("c", "d");
        patch.setDeleteList(deleteItems);

        patch.applyPatch(mockService);

        verify(mockService).removeByIds(deleteItems);
        verify(mockService, never()).saveBatch(anyList());
        verify(mockService, never()).updateBatchById(anyList());
    }

    @Test
    public void testApplyPatchWithUpdateList() {
        List<String> updateItems = Arrays.asList("e", "f");
        patch.setUpdateList(updateItems);

        patch.applyPatch(mockService);

        verify(mockService).updateBatchById(updateItems);
        verify(mockService, never()).saveBatch(anyList());
        verify(mockService, never()).removeByIds(anyList());
    }

    @Test
    public void testApplyPatchWithAllLists() {
        List<String> insertItems = Arrays.asList("a", "b");
        List<String> deleteItems = Arrays.asList("c", "d");
        List<String> updateItems = Arrays.asList("e", "f");

        patch.setInsertList(insertItems);
        patch.setDeleteList(deleteItems);
        patch.setUpdateList(updateItems);

        patch.applyPatch(mockService);

        verify(mockService).saveBatch(insertItems);
        verify(mockService).removeByIds(deleteItems);
        verify(mockService).updateBatchById(updateItems);
    }

    @Test
    public void testToPatch() {
        Patch<String> stringPatch = new Patch<>();
        stringPatch.setInsertList(Arrays.asList("1", "2"));
        stringPatch.setDeleteList(Arrays.asList("3", "4"));
        stringPatch.setUpdateList(Arrays.asList("5", "6"));

        Patch<Integer> intPatch = stringPatch.toPatch(Integer.class);

        assertNotNull(intPatch);
        assertEquals(2, intPatch.getInsertList().size());
        assertEquals(2, intPatch.getDeleteList().size());
        assertEquals(2, intPatch.getUpdateList().size());
    }
}