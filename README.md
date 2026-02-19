# nonulls

[![Maven Central](https://img.shields.io/maven-central/v/net.vanfleteren.nonulls/nonulls-parent.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=net.vanfleteren.nonulls)

A collection of libraries to support a null-avoidance coding style in Java.

NoNulls consists of the following modules:

## Validator

A recursive validator that checks objects for `null` values, letting you know where they are located.

### Usage

```java
List<String> nullPaths = NullValidator.findNullPaths(myObject);
if (!nullPaths.isEmpty()) {
    System.out.println("Nulls found: " + nullPaths);
}

// Or throw an exception if nulls are found
MyObject validated = NullValidator.assertNoNulls(myObject);
```

It supports POJOs, Records, Collections, Maps, Arrays, and Optional.

Available from Maven Central as

```xml
<dependency>
    <groupId>net.vanfleteren.nonulls</groupId>
    <artifactId>validator</artifactId>
    <version><!-- latest-version --></version>
</dependency>
```


## Jackson Modules

Available for both Jackson 2 and 3. These modules help handle nulls during deserialization, allowing you to avoid having nulls in your deserialized objects.
To allow a null in json to be deserialized, you have to declare it as `Optional<T>` instead of `T` in your model.

```xml
<dependency>
    <groupId>net.vanfleteren.nonulls</groupId>
    <!-- or jackson2 -->
    <artifactId>jackson3</artifactId>
    <version><!-- latest-version --></version>
</dependency>
```

### Features

- **Treat null collections as empty**: Automatically converts `null` in JSON to empty lists, sets, or maps.
- **Filter nulls in collections/maps**: Automatically removes `null` elements from lists/sets and `null` values from maps.
- **Empty-aware Optional**: Treats empty strings in JSON as `Optional.empty()`.
- **Fail on nulls**: Can be configured to throw an exception if any `null` values are encountered during deserialization.
- **Result type**: Returns a `Result<T>` instead of throwing an exception when deserializing.
- **Customizable**: Allows you to register additional type handlers. This is how the Vavr support is implemented.

### Usage

```java
// jackson 2 (3 also supported)
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new NoNullsModule());

// Or with specific configuration
NoNullsModule module = NoNullsModule.builder()
    .failOnNulls(true)
    .treatNullCollectionsAsEmpty(true)
    .build();
mapper.registerModule(module);
```

### Result Type

The library provides a `Result<T>` type that can be used to safely handle deserialization outcomes:

```java
Result<MyData> result = mapper.readValue(json, new TypeReference<Result<MyData>>() {});

if (result instanceof Result.Success<MyData> success) {
    process(success.value());
} else if (result instanceof Result.NullsFound<MyData> failure) {
    System.out.println("Nulls found at: " + failure.exception().getNullPaths());
}
```


## [Vavr](https://github.com/vavr-io/vavr) Validator

A Vavr-friendly wrapper for the validator that returns Vavr types instead of throwing exceptions.
It also knows about types like Either and Option when checking graphs for nulls.

```xml
<dependency>
    <groupId>net.vanfleteren.nonulls</groupId>
    <artifactId>validator-vavr</artifactId>
    <version><!-- latest-version --></version>
</dependency>
```

```java
Try<MyObject> result = NullValidator.assertNoNulls(myObject);
Either<List<String>, MyObject> either = NullValidator.assertNoNullsEither(myObject);
Validation<List<String>, MyObject> validation = NullValidator.validate(myObject);
```