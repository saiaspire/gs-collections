/*
 * Copyright 2011 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.utility.internal;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.set.sorted.ImmutableSortedSet;
import com.gs.collections.api.set.sorted.MutableSortedSet;
import com.gs.collections.api.set.sorted.SortedSetIterable;
import com.gs.collections.impl.block.factory.Comparators;
import com.gs.collections.impl.set.sorted.mutable.TreeSortedSet;
import com.gs.collections.impl.utility.Iterate;

/**
 * SortedSet algebra operations.
 */
public final class SortedSetIterables
{
    private SortedSetIterables()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static <T> MutableSortedSet<MutableSortedSet<T>> powerSet(SortedSet<T> set)
    {
        Comparator<? super T> comparator = set.comparator();
        MutableSortedSet<T> innerTree = TreeSortedSet.newSet(comparator);
        TreeSortedSet<MutableSortedSet<T>> sortedSetIterables = TreeSortedSet.newSet(Comparators.<T>powerSet());
        MutableSortedSet<MutableSortedSet<T>> seed = sortedSetIterables.with(innerTree);

        return Iterate.injectInto(seed, set, new Function2<MutableSortedSet<MutableSortedSet<T>>, T, MutableSortedSet<MutableSortedSet<T>>>()
        {
            public MutableSortedSet<MutableSortedSet<T>> value(MutableSortedSet<MutableSortedSet<T>> accumulator, final T element)
            {
                return accumulator.union(accumulator.collect(new Function<MutableSortedSet<T>, MutableSortedSet<T>>()
                {
                    public MutableSortedSet<T> valueOf(MutableSortedSet<T> set)
                    {
                        MutableSortedSet<T> newSet = set.clone();
                        newSet.add(element);
                        return newSet;
                    }
                }).toSet());
            }
        });
    }

    /**
     * Returns an Immutable version of powerset where the inner sets are also immutable.
     */
    public static <T> ImmutableSortedSet<ImmutableSortedSet<T>> immutablePowerSet(SortedSet<T> set)
    {
        return powerSet(set).collect(new Function<MutableSortedSet<T>, ImmutableSortedSet<T>>()
        {
            public ImmutableSortedSet<T> valueOf(MutableSortedSet<T> set)
            {
                return set.toImmutable();
            }
        }, TreeSortedSet.<ImmutableSortedSet<T>>newSet(Comparators.<T>powerSet())).toImmutable();
    }

    public static <T> int compare(SortedSetIterable<T> setA, SortedSetIterable<T> setB)
    {
        Iterator<T> itrA = setA.iterator();
        Iterator<T> itrB = setB.iterator();
        if (setA.comparator() != null)
        {
            Comparator<? super T> comparator = setA.comparator();
            while (itrA.hasNext())
            {
                if (itrB.hasNext())
                {
                    int val = comparator.compare(itrA.next(), itrB.next());
                    if (val != 0)
                    {
                        return val;
                    }
                }
                else
                {
                    return 1;
                }
            }
            return itrB.hasNext() ? -1 : 0;
        }

        while (itrA.hasNext())
        {
            if (itrB.hasNext())
            {
                int val = ((Comparable<T>) itrA.next()).compareTo(itrB.next());
                if (val != 0)
                {
                    return val;
                }
            }
            else
            {
                return 1;
            }
        }
        return itrB.hasNext() ? -1 : 0;
    }
}
