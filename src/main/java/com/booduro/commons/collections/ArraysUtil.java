/*
 * Copyright 2018 BOODURO inc.
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
 *
 */

package com.booduro.commons.collections;

/**
 * @author saifeddine.benchahed
 */
public class ArraysUtil {
    public static int[] concat(int[]... arrays) {
        int concatLength = 0;
        for (int[] array: arrays) {
            concatLength += array.length;
        }
        int[] concatenation = new int[concatLength];
        int addedUntilNow = 0;
        for (int[] array: arrays) {
            for (int i = 0; i < array.length; i++) {
                concatenation[addedUntilNow + i] = array[i];
            }
            addedUntilNow += array.length;
        }
        return concatenation;
    }

    public static <H> H[] concat(H[]... arrays) {
        int concatLength = 0;
        for (Object[] array: arrays) {
            concatLength += array.length;
        }
        Object[] concatenation = new Object[concatLength];
        int addedUntilNow = 0;
        for (Object[] array: arrays) {
            for (int i = 0; i < array.length; i++) {
                concatenation[addedUntilNow + i] = array[i];
            }
            addedUntilNow += array.length;
        }
        return (H[]) concatenation;
    }

    public static Object[] asPostfix(Object[] array, int maxLength) {
        if (array.length < maxLength) {
            Object[] newArray = new Object[maxLength];
            for (int i = 0; i < array.length; i++) {
                newArray[i + maxLength - array.length] = array[i];
            }
            return newArray;
        }
        return array;
    }

    public static Object[] asPrefix(Object[] array, int maxLength) {
        if (array.length < maxLength) {
            Object[] newArray = new Object[maxLength];
            for (int i = 0; i < array.length; i++) {
                newArray[i] = array[i];
            }
            return newArray;
        }
        return array;
    }
}
