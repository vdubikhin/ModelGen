package modelgen.data.complex;

import java.util.ArrayList;
import java.util.List;

import modelgen.shared.Logger;


public interface Mergeable<T extends Mergeable<T>> {
    boolean canMergeWith(T itemToMerge);

    boolean mergeWith(T itemToMerge);

    final static String ERROR_PREFIX = "Mergeable error.";
    static <T extends Mergeable<T>> void mergeEntries(List<T> entriesToMerge) {
        try {
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
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out if index exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
    }
}
