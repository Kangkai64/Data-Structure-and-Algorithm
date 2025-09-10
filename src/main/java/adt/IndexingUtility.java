package adt;

public final class IndexingUtility {

    private IndexingUtility() {}

    public static <K, E> void addToIndexGroup(ArrayBucketList<K, ArrayBucketList<String, E>> index,
                                              K groupKey,
                                              String entityId,
                                              E entity) {
        if (index == null || groupKey == null || entityId == null || entity == null) {
            return;
        }
        ArrayBucketList<String, E> group = index.getValue(groupKey);
        if (group == null) {
            group = ArrayBucketListFactory.createForStringIds(16);
        }
        group.add(entityId, entity);
        index.add(groupKey, group);
    }

    public static <K, E> void removeFromIndexGroup(ArrayBucketList<K, ArrayBucketList<String, E>> index,
                                                   K groupKey,
                                                   String entityId) {
        if (index == null || groupKey == null || entityId == null) {
            return;
        }
        ArrayBucketList<String, E> group = index.getValue(groupKey);
        if (group != null) {
            group.remove(entityId);
            index.add(groupKey, group);
        }
    }
}
