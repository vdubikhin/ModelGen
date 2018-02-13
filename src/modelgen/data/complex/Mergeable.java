package modelgen.data.complex;

import java.util.ArrayList;
import java.util.List;


public interface Mergeable<T extends Mergeable<T>> {
    boolean canMergeWith(T itemToMerge);

    boolean mergeWith(T itemToMerge);

    static <T extends Mergeable<T>> void mergeEntries(List<T> entriesToMerge) 
                                         throws NullPointerException, ArrayIndexOutOfBoundsException {
        List<T> originalEntries = new ArrayList<>(entriesToMerge);
        List<T> mergedEntries = new ArrayList<>();
        boolean merged = true;
        while (merged) {
            merged = false;
            for (T entryToMerge: originalEntries) {
                boolean mergePossible = false;
                for (T mergedEntry: mergedEntries) {
                    if (mergedEntry.canMergeWith(entryToMerge)) {
                        mergedEntry.mergeWith(entryToMerge);
                        mergePossible = true;
                        merged = true;
                        break;
                    }
                }
                
                if (!mergePossible)
                    mergedEntries.add(entryToMerge);
            }
            originalEntries = mergedEntries;
            mergedEntries = new ArrayList<>();
        }

        entriesToMerge.clear();
        entriesToMerge.addAll(originalEntries);
    }
}
