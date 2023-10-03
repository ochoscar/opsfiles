# Project opsfiles

Project with a set of utils operations with files.

## Build project

Run next command to build project

```shell
mvn package
```

## Run Copy

You can provide two parameters, first one is copy option and second one the folder where is located a bounch of another.
The algorithm go thought all internal folder and move all files to the specified folder.
The algorithm only search one level depth.

```shell
cd target
java -jar opsfiles-1.0.jar copy <abosolute_path>
```

## Delete repeated files

It's common to have repeated files in our system.
This utility find all repeated files in path and ask user for delete every one.
The algorithm only search one level depth.

```shell
cd target
java -jar opsfiles-1.0.jar delete <abosolute_path>
```

## Delete incomplete files

It's common to have .part files in our system.
This utility find all and delete them
The algorithm only search one level depth.

```shell
cd target
java -jar opsfiles-1.0.jar delete_incomplete <abosolute_path>
```
