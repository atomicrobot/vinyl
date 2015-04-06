# Vinyl

[![Build Status](https://travis-ci.org/madebyatomicrobot/vinyl.svg?branch=master)](https://travis-ci.org/madebyatomicrobot/vinyl)

Vinyl makes it simple to work with [Cursors][cursor-javadoc] and 
[ContentValues][contentvalues-javadoc] in your application's domain language.  Vinyl is the best 
kind of record.

Motivation
==========

Assume there is this cursor describing employees:

| name | age | manager |
|------|-----|---------|
| John | 30  | 0       |
| Jane | 30  | 1       |

Accessing the fields for the cursor for the would normally look similar to this (undesirable) code:

```java
cursor.moveToFirst();
while (!cursor.isAfterLast()) {
    String name = cursor.getString(cursor.getColumnIndex("name"));
    int age = cursor.getInt(cursor.getColumnIndex("age"));
    boolean manager = cursor.getInt(cursor.getColumnIndex("manager")) != 0;
    cursor.moveToNext();
}
```

Usage
=====

Start by creating an interface, annotated with `@Record`, with no argument methods named the 
same as fields in the cursor. `@Record` triggers a code generator that will cause a new class 
named `EmployeeRecord` to be generated.

```java
@Record
public interface Employee {
    String name();

    int age();

    boolean manager();
} 
```

## Accessing Fields

Accessing the fields for the cursor can now be written as:
```java
Employee employee = EmployeeRecord.wrapCursor(cursor);
cursor.moveToFirst();
while (!cursor.isAfterLast()) {
    String name = employee.name();
    int age = employee.age();
    boolean manager = employee.manager();
    cursor.moveToNext();
}
```

Take note that the `Employee` returned from `wrapCursor` reflects the current state of the cursor. 
If you need an instance of `Employee` that will not change with the cursor you can call 
`buildFromCursor` instead.

```java
cursor.moveToFirst();
while (!cursor.isAfterLast()) {
    Employee employee = EmployeeRecord.buildFromCursor(cursor);
    cursor.moveToNext();
}
```

## Custom Projections

The fields of a cursor will not always match up to how we want to access them in code. Assume the 
cursor above contains a field named `hire_dt` represented by the `INTEGER` type that described the 
hire date for an employee. 

We can specify that a Java method should map to a differently named underlying cursor field with 
the `@Projection` annotation.

```java
@Record
public interface Employee {
    String name();

    int age();

    boolean manager();

    @Projection("hire_dt") 
    long hireDate();
}
```

If the projection you need to you will vary at runtime (ex: [getting the name of an Android 
contact][contacts-training-doc]), you can specify a class that will perform the dynamic 
projection evaluation.

```java
@Record
public interface ContactProjection {
    @Projection(conditionalProjection = DisplayNameProjection.class)
    String displayName();

    class DisplayNameProjection implements ConditionalProjection {
        @Override
        public String projection() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? 
                Contacts.DISPLAY_NAME_PRIMARY : 
                Contacts.DISPLAY_NAME;
        }
    }
}
```

## Converters

Going one step further, we really don't want a `long` for the hire date but instead would rather 
have a `java.util.Date`.

```java
@Record
public interface Employee {
    String name();

    int age();

    boolean manager();

    @Converter(fieldClass = Long.class, converter = LongToDateConverter.class)
    @Projection("hire_dt") 
    Date hireDate();
}
```

Where `LongToDateConverter` has this implementation:

```java
public class LongToDateConverter {
    public Date convertFrom(Long value) {
        return (value == null) ? null : new Date(value);
    }

    public Long convertTo(Date value) {
        return (value == null) ? null : value.getTime();
    }
}
```

Now we can access hire dates like this:

```java
Employee employee = EmployeeRecord.wrapCursor(cursor);
Date hireDate = employee.hireDate();
```

## Supported Types

Without specifying a converter, the following types are supported:

- `boolean`
- `short`
- `int`
- `long`
- `float`
- `double`
- `byte[]`
- `String`
- `Boolean`
- `Short`
- `Integer`
- `Long`
- `Float`
- `Double`

A few things to note about these types:

- Cursors do not expose boolean types. Under the covers the generated code will evaluate to boolean 
with a test like this: `cursor.getInt(cursor.getColumnIndex("...")) != 0`.  If this default behavior 
does not match how your cursor exposes booleans (ex: String `true` and `false`) you will need to use 
a converter.
- Non-primitive types will perform `cursor.isNull(...)` on the field and will either return null or 
the value of the field.

## @NotNull

If you know that a field will never be null, you can annotate the field with the `@NotNull` 
annotation from the [Android Support Annotations library][support-annotations].

```
@Record
public interface Employee {
    @NotNull
    String name();

    int age();

    boolean manager();

    @Converter(cursorClass = Long.class, converter = LongToDateConverter.class)
    @Projection("hire_dt") 
    Date hireDate();
}
```

## ContentValues

Accessing values via cursors only represent one half of the solution. 
`ContentValues` are the typical compliment to `Cursors`. 
Creating content values that map back to the same fields looks like this:

```
ContentValues cv = EmployeeRecord.contentValuesBuilder()
        .name("Coder McCoder")
        .age(42)
        .manager(false)
        .hireDate(today)
        .build();
```

## Philosophy

Vinyl's purpose is to make it easier to operate on [Cursors][cursor-javadoc] and
[ContentValues][contentvalues-javadoc]. While these types are frequently associated with databases,
SQL, or [ContentProviders][contentprovider-javadoc], they are typically used at a higher layer of 
the application stack. As such, no direct attempt will be made at this time to simplify those other
components.

Including in your project
=========================

```groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    // Or latest versions
    classpath 'com.android.tools.build:gradle:1.1.2'
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'android-apt'

dependencies {
  apt 'com.madebyatomicrobot:vinyl-compiler:{latest-version}'
  compile 'com.madebyatomicrobot:vinyl-annotations:{latest-version}'
}
```

| Artifact | Latest Version |
|------|---------|
| vinyl-compiler | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.madebyatomicrobot/vinyl-compiler/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.madebyatomicrobot/vinyl-compiler/) |
| vinyl-annotations | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.madebyatomicrobot/vinyl-annotations/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.madebyatomicrobot/vinyl-annotations/) |


Snapshots of the development version are available in [Sonatype’s `snapshots` repository][snap].

Alternatives
============

- [Cupboard](https://bitbucket.org/qbusict/cupboard/)


Related Projects
================

- [Schematic](https://github.com/SimonVT/schematic) is a library to generate 
[ContentProviders][contentprovider-javadoc].

License
=======

    Copyright 2015 Atomic Robot LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    
[cursor-javadoc]: http://developer.android.com/reference/android/database/Cursor.html
[contentvalues-javadoc]: http://developer.android.com/reference/android/content/ContentValues.html
[contentprovider-javadoc]: http://developer.android.com/reference/android/content/ContentProvider.html
[contacts-training-doc]: http://developer.android.com/training/contacts-provider/retrieve-names.html
[support-annotations]: http://tools.android.com/tech-docs/support-annotations
[snap]: https://oss.sonatype.org/content/repositories/snapshots/
