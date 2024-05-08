/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.objectholders;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Either<L, R> {

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(value, null, true);
    }

    public static <L, R> Either<L, R> left(L value, Class<R> rightClass) {
        return new Either<>(value, null, true);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(null, value, false);
    }

    public static <L, R> Either<L, R> right(Class<L> leftClass, R value) {
        return new Either<>(null, value, false);
    }

    private final L left;
    private final R right;
    private final boolean isLeft;

    private Either(L left, R right, boolean isLeft) {
        this.left = left;
        this.right = right;
        this.isLeft = isLeft;
    }

    public Object get() {
        return isLeft ? left : right;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isRight() {
        return !isLeft;
    }

    public L getLeft() {
        return isLeft ? left : null;
    }

    public R getRight() {
        return isLeft ? null : right;
    }

    public <T> T computeIf(Function<L, T> leftFunction, Function<R, T> rightFunction) {
        return isLeft ? leftFunction.apply(left) : rightFunction.apply(right);
    }

    public void consumeIf(Consumer<L> leftConsumer, Consumer<R> rightConsumer) {
        if (isLeft) {
            leftConsumer.accept(left);
        } else {
            rightConsumer.accept(right);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Either<?, ?> either = (Either<?, ?>) o;
        return isLeft == either.isLeft && Objects.equals(left, either.left) && Objects.equals(right, either.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, isLeft);
    }

}
