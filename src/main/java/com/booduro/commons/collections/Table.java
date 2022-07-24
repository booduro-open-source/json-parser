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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author saifeddine.benchahed
 */
public class Table {
    public final Object[] headers;
    public final List<Object[]> rows;

    private Table(Object[] headers, List<Object[]> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public Table fullJoin(Table that,
                          int thisHeaderIdx,
                          Function thisColumnTransformation,
                          int thatHeaderIdx,
                          Function thatColumnTransformation) {
        Object[] headers = ArraysUtil.concat(this.headers, that.headers);
        List<Object[]> rows = new ArrayList<>();

        Map<Object, Integer> thisJoinValToIndex = new HashMap<>();
        for (int i = 0; i < this.rows.size(); i++) {
            thisJoinValToIndex.put(thisColumnTransformation.apply(this.rows.get(i)[thisHeaderIdx]), i);
        }
        for (int i = 0; i < that.rows.size(); i++) {
            Integer idx = thisJoinValToIndex.remove(thatColumnTransformation.apply(that.rows.get(i)[thatHeaderIdx]));
            if (idx != null) {
                rows.add(ArraysUtil.concat(this.rows.get(idx), that.rows.get(i)));
            } else {
                rows.add(ArraysUtil.asPostfix(that.rows.get(i), headers.length));
            }
        }
        for (Integer thisNonJoinedIdx: thisJoinValToIndex.values()) {
            rows.add(ArraysUtil.asPrefix(this.rows.get(thisNonJoinedIdx), headers.length));
        }
        return Table.table(headers, rows);
    }

    public void writeToCsvOutputStream(OutputStream csvOutputStream) {
        try {
            //header
            byte[] csvHeader = Arrays.stream(headers).map(o -> o.toString()).collect(Collectors.joining(","))
                    .getBytes(Charset.forName("UTF-8"));
            csvOutputStream.write(csvHeader);
            //rows
            rows.stream().forEach(row -> {
                try {
                    csvOutputStream.write("\n".getBytes(Charset.forName("UTF-8")));
                    byte[] rowBytes = Arrays.stream(row).map(o -> o.toString()).collect(Collectors.joining(",")).getBytes(Charset.forName("UTF-8"));

                    csvOutputStream.write(rowBytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            csvOutputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                csvOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Table table(Object[] headers, List<Object[]> rows) {
        return new Table(headers, rows);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        public Object[] headers;
        public List<Object[]> rows = new ArrayList<>();

        public Builder withHeaders(Object... headers) {
            this.headers = headers;
            return this;
        }


        public Builder withRow(Object... row) {
            this.rows.add(row);
            return this;
        }

        public Table build() {
            return table(headers, rows);
        }
    }

    /**
     * [[a,b,c], [d]] = [aXb, aXd, cXd]
     *
     * @param tableLists
     * @return
     */
    public static List<Table> product(List<List<Table>> tableLists) {
        List<Table> product = tableLists.get(0);
        int i = 1;
        while (i < tableLists.size()) {
            product = productOfTwo(product, tableLists.get(i));
            i++;
        }
        return product;
    }

    private static List<Table> productOfTwo(List<Table> tableList1, List<Table> tableList2) {
        if (tableList1 != null && tableList2 != null) {
            List<Table> product = new ArrayList<>();
            for (Table table1: tableList1) {
                for (Table table2: tableList2) {
                    Object[] allHeaders = ArraysUtil.concat(table1.headers, table2.headers);
                    List<Object[]> allRows = new ArrayList();
                    for (Object[] tuple1: table1.rows) {
                        for (Object[] tuple2: table2.rows) {
                            allRows.add(ArraysUtil.concat(tuple1, tuple2));
                        }
                    }
                    product.add(new Table(allHeaders, allRows));
                }
            }
            return product;
        } else if (tableList1 == null) {
            return tableList2;
        } else {
            return tableList1;
        }
    }

    public static Table mergeAll(List<List<Table>> allTables) {
        Set allHeaders = new HashSet<>();
        for (List<Table> subList: allTables) {
            for (Table table: subList) {
                for (Object header: table.headers) {
                    allHeaders.add(header);
                }
            }
        }
        Map<Object, Integer> headerToIdx = new HashMap<>();
        Object[] mergeTableHeaders = allHeaders.toArray();
        List<Object[]> mergeTableRows = new ArrayList<>();
        for (int i = 0; i < mergeTableHeaders.length; i++) {
            headerToIdx.put(mergeTableHeaders[i], i);
        }
        for (List<Table> subList: allTables) {
            for (Table table: subList) {
                Object[] header = table.headers;
                for (Object[] row: table.rows) {
                    Object[] mergeTableRow = new Object[mergeTableHeaders.length];
                    for (int i = 0; i < header.length; i++) {
                        mergeTableRow[headerToIdx.get(header[i])] = row[i];
                    }
                    mergeTableRows.add(mergeTableRow);
                }
            }
        }
        return Table.table(mergeTableHeaders, mergeTableRows);
    }
}
