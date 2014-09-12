package be.bow.iterator;

import java.util.*;

public class IterableUtils {

    public static <T extends Object> DataIterable<T> createIterable(final DataIterable<? extends T>... iterables) {
        return createIterable(CombineMethod.SEQUENTIAL, iterables);
    }

    public static <T extends Object> DataIterable<T> createIterable(CombineMethod combineMethod, final DataIterable<? extends T>... iterables) {
        return createIterable(combineMethod, Arrays.asList(iterables));
    }

    public static <T extends Object> DataIterable<T> createIterable(final List<DataIterable<? extends T>> iterables) {
        return createIterable(CombineMethod.SEQUENTIAL, iterables);
    }

    public static <T> DataIterable<T> createIterable(final Collection<T> collection) {
        return new DataIterable<T>() {
            @Override
            public CloseableIterator<T> iterator() {
                return IterableUtils.iterator(collection.iterator());
            }

            @Override
            public long apprSize() {
                return collection.size();
            }
        };
    }

    public static <T extends Object> DataIterable<T> createIterable(final CombineMethod combineMethod, final List<DataIterable<? extends T>> iterables) {
        return new DataIterable<T>() {
            @Override
            public CloseableIterator<T> iterator() {
                List<CloseableIterator<? extends T>> iterators = new ArrayList<>();
                for (DataIterable<? extends T> iterable : iterables) {
                    iterators.add(iterable.iterator());
                }
                if (combineMethod == CombineMethod.SEQUENTIAL) {
                    return new SequentialIteratorOfIterators<>(iterators);
                } else {
                    return new InterleavedIteratorOfIterators<>(iterators);
                }
            }

            @Override
            public long apprSize() {
                long result = 0;
                for (DataIterable<? extends T> iterable : iterables) {
                    result += iterable.apprSize();
                }
                return result;
            }
        };
    }

    public static <T> CloseableIterator<T> iterator(final SimpleIterator<T> simpleIt) {
        return iterator(simpleIt, null);
    }

    public static <T> CloseableIterator<T> iterator(final SimpleIterator<T> simpleIt, final T lastValue) {
        return new CloseableIterator<T>() {

            private T nextValue;

            {
                findNext();
            }

            private void findNext() {
                try {
                    nextValue = simpleIt.next();
                    if (nextValue == null) {
                        if (lastValue == null) {
                            close();
                        }
                    } else {
                        if (lastValue != null && nextValue.equals(lastValue)) {
                            close();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Could not read next value ", e);
                }
            }

            @Override
            public boolean hasNext() {
                if (lastValue == null) {
                    return nextValue != null;
                } else {
                    return !lastValue.equals(nextValue);
                }
            }

            @Override
            public synchronized T next() {
                T result = nextValue;
                findNext();
                return result;
            }

            @Override
            public void remove() {
                throw new RuntimeException("Not implemented");
            }

            @Override
            public void closeInt() {
                synchronized (this) {
                    try {
                        simpleIt.close();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to close iterator", e);
                    }
                }
            }
        };
    }

    public static <T extends Object> CloseableIterator<T> iterator(final Iterator<T> iterator) {
        return new CloseableIterator<T>() {
            @Override
            public void closeInt() {
                //do nothing
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }

    public static <T extends Object> CloseableIterator<T> maxSizeIterator(final long maxIterations, final CloseableIterator<T> iterator) {
        return new CloseableIterator<T>() {

            private long numDone = 0;

            @Override
            protected void closeInt() {
                iterator.close();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext() && numDone < maxIterations;
            }

            @Override
            public T next() {
                numDone++;
                return iterator.next();
            }
        };
    }

    public static <T extends Object> DataIterable<T> maxSizeIterable(final long maxIterations, final DataIterable<T> iterable) {
        if (maxIterations <= 0) {
            throw new RuntimeException("Incorrect number of iterations " + maxIterations);
        }
        return new DataIterable<T>() {
            @Override
            public CloseableIterator<T> iterator() {
                return maxSizeIterator(maxIterations, iterable.iterator());
            }

            @Override
            public long apprSize() {
                return Math.min(maxIterations, iterable.apprSize());
            }
        };
    }

    public static enum CombineMethod {
        SEQUENTIAL, INTERLEAVED
    }

}
