/*
 * Copyright 2017 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.commons.function;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import pw.phylame.commons.util.Validate;
import pw.phylame.commons.value.Optional;
import pw.phylame.commons.value.Pair;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Helper for functional operations.
 */
public final class Functionals {
    public static final int ZIP_ALL = 0;
    public static final int ZIP_LEFT = 1;
    public static final int ZIP_RIGHT = 2;

    public static <E> void foreach(@NonNull Iterator<E> i, @NonNull Consumer<? super E> consumer) {
        while (i.hasNext()) {
            consumer.consume(i.next());
        }
    }

    public static <E> boolean any(@NonNull Iterator<E> i, @NonNull Prediction<? super E> prediction) {
        while (i.hasNext()) {
            if (prediction.test(i.next())) {
                return true;
            }
        }
        return false;
    }

    public static <E> boolean all(@NonNull Iterator<E> i, @NonNull Prediction<? super E> prediction) {
        while (i.hasNext()) {
            if (!prediction.test(i.next())) {
                return false;
            }
        }
        return true;
    }

    public static <E, R> Iterator<R> map(@NonNull Iterator<E> i, @NonNull Function<? super E, ? extends R> transform) {
        return new MappingIterator<>(i, transform);
    }

    public static <E> Iterator<E> filter(@NonNull Iterator<E> i, @NonNull Prediction<? super E> prediction) {
        return new FilterIterator<>(i, prediction);
    }

    public static <E> E reduce(@NonNull Iterator<E> i, @NonNull BiFunction<? super E, ? super E, ? extends E> fun) {
        if (!i.hasNext()) {
            throw new IllegalStateException("empty iterator must with initial value");
        }
        return reduce(i, i.next(), fun);
    }

    public static <E> E reduce(@NonNull Iterator<E> i, E initial, @NonNull BiFunction<? super E, ? super E, ? extends E> fun) {
        if (!i.hasNext()) {
            return initial;
        }
        E value = initial;
        while (i.hasNext()) {
            value = fun.apply(value, i.next());
        }
        return value;
    }

    public static <A, B> Iterator<Pair<A, B>> zip(@NonNull Iterator<A> i1, @NonNull Iterator<B> i2) {
        return zip(i1, i2, ZIP_ALL);
    }

    public static <A, B> Iterator<Pair<A, B>> zip(@NonNull Iterator<A> i1, @NonNull Iterator<B> i2, int mode) {
        Validate.require(mode == ZIP_ALL || mode == ZIP_LEFT || mode == ZIP_RIGHT,
                "mode must be one of ZIP_ALL, ZIP_LEFT, ZIP_RIGHT");
        return new ZipIterator<>(i1, i2, mode);
    }

    public static <E> Pair<Iterator<E>, Iterator<E>> partition(@NonNull Iterator<E> i,
                                                               @NonNull Prediction<? super E> prediction) {
        val list1 = new LinkedList<E>();
        val list2 = new LinkedList<E>();
        while (i.hasNext()) {
            E obj = i.next();
            if (prediction.test(obj)) {
                list1.add(obj);
            } else {
                list2.add(obj);
            }
        }
        return new Pair<>(list1.iterator(), list2.iterator());
    }

    public static <E> Optional<E> find(@NonNull Iterator<E> i, @NonNull Prediction<? super E> prediction) {
        while (i.hasNext()) {
            E obj = i.next();
            if (prediction.test(obj)) {
                return Optional.of(obj);
            }
        }
        return Optional.empty();
    }

    @SafeVarargs
    public static <E> Iterator<E> flatten(Iterator<? extends E>... iterators) {
        if (iterators.length == 0) {
            return Collections.emptyIterator();
        }
        return new FlattenIterator<>(iterators);
    }

    @SafeVarargs
    public static <E, R> Iterator<R> flatMap(@NonNull Function<? super E, R> transform, Iterator<? extends E>... iterators) {
        if (iterators.length == 0) {
            return Collections.emptyIterator();
        }
        return map(flatten(iterators), transform);
    }

    @RequiredArgsConstructor
    private static class FilterIterator<I> implements Iterator<I> {
        private final Iterator<I> iterator;
        private final Prediction<? super I> filter;
        private I next = null;
        private boolean present = false;

        @Override
        public boolean hasNext() {
            if (next == null) {
                present = false;
                while (iterator.hasNext()) {
                    next = iterator.next();
                    if (filter.test(next)) {
                        present = true;
                        break;
                    }
                }
            }
            return present;
        }

        @Override
        public I next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            val answer = next;
            next = null;
            return answer;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    @RequiredArgsConstructor
    private static class MappingIterator<E, R> implements Iterator<R> {
        private final Iterator<E> iterator;
        private final Function<? super E, ? extends R> transformer;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public R next() {
            return transformer.apply(iterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @RequiredArgsConstructor
    private static class ZipIterator<A, B> implements Iterator<Pair<A, B>> {
        private final Iterator<? extends A> i1;
        private final Iterator<? extends B> i2;
        private final int mode;

        private boolean done = false;
        private boolean hasNext = false;
        private A a = null;
        private B b = null;

        @Override
        public boolean hasNext() {
            if (!hasNext && !done) {
                a = null;
                b = null;
                boolean hasA = false;
                if (i1.hasNext()) {
                    a = i1.next();
                    hasA = true;
                }
                boolean hasB = false;
                if (i2.hasNext()) {
                    b = i2.next();
                    hasB = true;
                }
                hasNext = true;
                if (!hasA) { // i1 prediction
                    if (!hasB || mode == ZIP_LEFT) {
                        done = true;
                        hasNext = false;
                    }
                } else if (!hasB) { // i2 prediction
                    if (!hasA || mode == ZIP_RIGHT) {
                        done = true;
                        hasNext = false;
                    }
                }
            }
            return hasNext;
        }

        @Override
        public Pair<A, B> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            hasNext = false;
            return new Pair<>(a, b);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @RequiredArgsConstructor
    private static class FlattenIterator<E> implements Iterator<E> {
        private final Iterator<? extends E>[] iterators;
        private int index = 0;
        private Iterator<? extends E> iterator;

        @Override
        public boolean hasNext() {
            if (index == iterators.length) { // no more iterator
                return false;
            }
            do {
                iterator = iterators[index];
                if (iterator != null && iterator.hasNext()) {
                    return true;
                }
            } while (++index != iterators.length);
            return false;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
