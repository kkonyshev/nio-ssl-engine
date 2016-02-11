# Description

Netty-based engine example for client-server data transferring which supports:

  - Plaint sockets
  - SSL connection with client auth

# Examples

Project provide examples for:

  - File transferring
  - Map transferring
  - POJO transferring

### External dependencies

```xml
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-common</artifactId>
            <version>4.1.0.CR1</version>
        </dependency>
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-transport</artifactId>
            <version>4.1.0.CR1</version>
        </dependency>
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-handler</artifactId>
            <version>4.1.0.CR1</version>
        </dependency>
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-codec</artifactId>
            <version>4.1.0.CR1</version>
        </dependency>
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-buffer</artifactId>
            <version>4.1.0.CR1</version>
        </dependency>
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-resolver</artifactId>
            <version>4.1.0.CR1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.5</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
```

### Installation

Onec you have need maven installed locally, just type in project root:

```sh
$ mvn clean package
```

### Version

1.0-SNAPSHOT

### Author

Konstantin Konyshev <<konyshev.konstantin@gmail.com>>

### License

MIT
