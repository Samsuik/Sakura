package me.samsuik.sakura.utils.collections;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.world.ticks.ScheduledTick;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
@NullMarked
public final class ScheduledTickQueue<T extends ScheduledTick<?>> extends AbstractQueue<T> {
    private static final ScheduledTick<?>[] EMPTY_ARRAY = new ScheduledTick[0];

    private @Nullable T[] elements = (T[]) EMPTY_ARRAY;
    private int head;
    private int tail;
    private int size;
    private int mask;

    private boolean sorted = false;

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean offer(final T element) {
        if (this.size == this.elements.length) this.grow();
        this.maintainOrder(element);

        this.elements[this.tail] = element;
        this.tail = (this.tail + 1) & this.mask;
        this.size++;
        return true;
    }

    @Override
    public @Nullable T poll() {
        if (this.size == 0) return null;
        if (!this.sorted) this.sort();

        final T result = this.elements[this.head];
        this.elements[this.head] = null;
        this.head = (this.head + 1) & this.mask;
        this.size--;
        return result;
    }

    @Override
    public @Nullable T peek() {
        if (this.size == 0) return null;
        if (!this.sorted) this.sort();

        return this.elements[this.head];
    }

    private void grow() {
        final @Nullable T[] elements = this.elements;
        final int newLength = Math.max(8, elements.length << 1);
        final T[] newElements = (T[]) new ScheduledTick[newLength];

        if (this.head < this.tail) {
            System.arraycopy(elements, this.head, newElements, 0, this.size);
        } else {
            final int trailing = elements.length - this.head;
            System.arraycopy(elements, this.head, newElements, 0, trailing);
            System.arraycopy(elements, 0, newElements, trailing, this.tail);
        }

        this.elements = newElements;
        this.head = 0;
        this.tail = this.size;
        this.mask = newLength - 1;
    }

    private void sort() {
        if (this.tail < this.head) {
            // compact before sorting
            System.arraycopy(this.elements, this.head, this.elements, this.tail, this.elements.length - this.head);
            Arrays.fill(this.elements, this.size, this.elements.length, null);
            this.head = 0;
            this.tail = this.size;
        }

        ObjectArrays.quickSort(this.elements, this.head, this.tail, ScheduledTick.DRAIN_ORDER);
        this.sorted = true;
    }

    private void maintainOrder(final T newElement) {
        if (!this.sorted) {
            return;
        }

        final T lastElement = this.elements[(this.tail - 1) & this.mask];
        if (lastElement != null && ScheduledTick.DRAIN_ORDER.compare(lastElement, newElement) > 0) {
            this.sorted = false;
        }
    }

    private void removeAt(final int index) {
        final @Nullable T[] elements = this.elements;
        final int mask = this.elements.length - 1;
        final int tail = this.tail;
        final int head = this.head;

        this.elements[index] = null;
        this.size--;

        if (tail < head) {
            if (index >= head) {
                System.arraycopy(elements, head, elements, head + 1, index - head);
                elements[head] = null;
                this.head = (head + 1) & mask;
            } else if (index < tail) {
                System.arraycopy(elements, index + 1, elements, index, tail - index - 1);
                this.tail = (tail - 1) & mask;
                elements[this.tail] = null;
            }
        } else {
            if (index == head) {
                this.head = (head + 1) & mask;
            } else {
                System.arraycopy(elements, index + 1, elements, index, tail - index - 1);
                this.tail = (tail - 1) & mask;
                elements[this.tail] = null;
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        if (!this.sorted) this.sort();
        return new Itr();
    }

    private final class Itr implements Iterator<T> {
        private int cursor = ScheduledTickQueue.this.head;
        private int count = 0;
        private int lastRet = -1;

        @Override
        public boolean hasNext() {
            return this.count < ScheduledTickQueue.this.size;
        }

        @Override
        public T next() {
            if (!this.hasNext()) throw new NoSuchElementException();

            this.lastRet = cursor;
            final T nextElement = ScheduledTickQueue.this.elements[this.cursor];
            this.cursor = (this.cursor + 1) & ScheduledTickQueue.this.mask;
            this.count++;

            return nextElement;
        }

        @Override
        public void remove() {
            if (this.lastRet == -1) throw new IllegalStateException();
            ScheduledTickQueue.this.removeAt(this.lastRet);

            this.cursor = (this.cursor - 1) & ScheduledTickQueue.this.mask;
            this.count++;
            this.lastRet = -1;
        }
    }
}
