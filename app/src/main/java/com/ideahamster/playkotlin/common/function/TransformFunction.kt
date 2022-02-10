package com.ideahamster.playkotlin.common.function
/**
 * Represents a function which process given input argument to return the specified return type.
 *
 * @param <T> the type of the input argument of the function
 * @param <R> the type of the return type of the function
 */
fun interface TransformFunction<T, R> {

    /**
     * Applies this function to the given input argument.
     *
     * @param value is a input argument
     * @return the function result in the specified return type
     */
    fun apply(value: T): R
}