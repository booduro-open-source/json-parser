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

package com.booduro.parser.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author saifeddine.benchahed
 */
public class JsonStreamParserTest {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Book {

        @JsonPath("$.owner.*.name")
        public String owner;

        @JsonPath("$.store[*].book.author")
        public String author;

        @JsonPath("$.store[*].book.otherprice")
        public Integer otherprice;
    }

    @Test
    public void parseTest() throws FileNotFoundException {
        List<Book> books = JsonStreamParser.parse(
                new FileInputStream(getClass().getClassLoader().getResource("oneBookStore.json").getFile()),
                Book.class);

        HashSet<Book> expected = new HashSet<>(Arrays.asList(
                new Book("jifa", "hamadi2", 232),
                new Book("khalifa", "hamadi2", 232),
                new Book("jifa", "hamadi1", null),
                new Book("khalifa", "hamadi1", null)));
        assertEquals(expected, new HashSet<>(books));
    }

}
