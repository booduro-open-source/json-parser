This library allows parsing JSON files in streaming mode using a subset of JSONpath expressions.

# Usage #
Given the following JSON file

```json
{
  "owner": {
    "info1": {
      "name": "khalifa"
    },
    "info2": {
      "name": "jifa"
    }
  },
  "store": [
    {
      "ee": {
        "author": "hamade",
        "price": 32
      },
      "book": {
        "author": "hamadi1",
        "price": 34
      }
    },
    {
      "book": {
        "author": "hamadi2",
        "price": 35,
        "otherprice": 232
      }
    }
  ],
  "3de": [
    "3",
    "323",
    3
  ],
  "4de": [
    "3",
    "323",
    3
  ]
}
```

You can specify the fields to extract using the following POJO

```java
public static class Book {
        @JsonPath("$.owner.*.name")
        public String owner;

        @JsonPath("$.store[*].book.author")
        public String author;

        @JsonPath("$.store[*].book.otherprice")
        public int otherprice;
}
```

And run the parser as follows

```java
List<Book> books = JsonStreamParser.parse(jsonInputStream, Book.class);
```



    
