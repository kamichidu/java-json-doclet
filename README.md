JSON Doclet
====================================================================================================
This is the doclet implementation for javadoc command.

Requires:

1. Java Development Toolkit 1.7 or higher
1. Apache Maven

How to Use
----------------------------------------------------------------------------------------------------
$ is a shell prompt:
```sh:
$ git clone https://github.com/kamichidu/java-json-doclet json-doclet
$ cd json-doclet
$ mvn package
$ $JAVA_HOME/bin/javadoc \
    -classpath $JAVA_HOME/lib/tools.jar \
    -docletpath ./target/json-doclet-0.0.0-jar-with-dependencies.jar \
    -doclet jp.michikusa.chitose.doclet.JsonDoclet \
    -ofile path/to/output/json \
    ...
```

This doclet provides some arguments below:

1. -ofile {filename}

    This is required argument.
    You can specify output filename.
