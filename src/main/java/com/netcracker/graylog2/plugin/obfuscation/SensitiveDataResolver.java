package com.netcracker.graylog2.plugin.obfuscation;

import com.netcracker.graylog2.plugin.obfuscation.search.Finder;
import com.netcracker.graylog2.plugin.obfuscation.search.SensitiveData;

import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * Class provides algorithm by resolving conflicts in sensitive search results.
 */
@Singleton
public class SensitiveDataResolver {

    public static final Comparator<SensitiveData> COMPARATOR = new SensitiveDataComparator();

    public List<SensitiveData> resolveConflicts(ObfuscationRequest request, List<SensitiveData> sensitiveDataList) {
        sensitiveDataList.sort(COMPARATOR);

        SensitiveData previous = null;

        ListIterator<SensitiveData> iterator = sensitiveDataList.listIterator();
        while (iterator.hasNext()) {
            SensitiveData sensitiveData = iterator.next();
            if (previous == null) {
                previous = sensitiveData;
            } else {
                if (previous.getEnd() > sensitiveData.getStart()) {
                    if (previous.getEnd() >= sensitiveData.getEnd()) {
                        iterator.remove();
                    } else {
                        Finder finder = sensitiveData.getFinder();
                        int currentImportance = finder.getImportance();

                        if (previous.getFinder().getImportance() < currentImportance) {
                            removePrevious(iterator);
                            previous = iterator.next();
                        } else if (previous.getFinder().getImportance() > currentImportance) {
                            iterator.remove();
                        } else if (previous.getFinder().getImportance() == currentImportance) {
                            SensitiveData resolvedSensitiveData = new SensitiveData(
                                    previous.getStart(),
                                    sensitiveData.getEnd(),
                                    request.getSourceText().substring(previous.getStart(), sensitiveData.getEnd()),
                                    new ConflictFinder(previous.getFinder(), finder)
                            );

                            removeCurrentAndPrevious(iterator);
                            iterator.add(resolvedSensitiveData);
                            previous = resolvedSensitiveData;
                        }
                    }
                } else {
                    previous = sensitiveData;
                }
            }
        }

        return sensitiveDataList;
    }

    private void removePrevious(ListIterator<SensitiveData> iterator) {
        iterator.previous();
        iterator.previous();
        iterator.remove();
    }

    private void removeCurrentAndPrevious(ListIterator<SensitiveData> iterator) {
        iterator.remove();
        iterator.previous();
        iterator.remove();
    }

    private static final class SensitiveDataComparator implements Comparator<SensitiveData> {

        @Override
        public int compare(SensitiveData left, SensitiveData right) {
            if (left.getStart() < right.getStart()) {
                return -1;
            } else if (left.getStart() > right.getStart()) {
                return 1;
            } else {
                return Integer.compare(right.getEnd(), left.getEnd());
            }
        }
    }

    private static final class ConflictFinder implements Finder {

        private final Finder left;

        private final Finder right;

        public ConflictFinder(Finder left, Finder right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getName() {
            return "{" + left + "#" + right + "}";
        }

        @Override
        public String getFullName() {
            return "{" + left.getFullName() + "|" + right.getFullName() + "}";
        }

        @Override
        public int getImportance() {
            return Math.max(left.getImportance(), right.getImportance());
        }

        @Override
        public String getSearchType() {
            return "Conflict Resolver";
        }
    }
}

