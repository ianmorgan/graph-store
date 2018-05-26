# Type Mapping rules 


Anything beyond simple mapping to equivalent types requires a way of inferring type information, 
which includes use of an external schema (like GraphQL), examining the Java classes (the way in which 
most Java serialization frameworks operate) or providing custom mapping rules. As this is a GraphQL 
centric service the GraphQL schema system is used to drive all mapping rules, but for this to work smoothly 
it is important that the Kotlin/Java code, the JSON serializers and the GraphQL schema mappers follow 
consistent rules.

There are in effect 4 types systems in operation. The rules and reasons are explained below and 
any common rules are implemented in the 'mapper' package. 
  

## The types supported by GraphQL

This is a restricted list, although it does allow the addition of custom types. Unfortunately this 
doesn't include any support for date / time. This service defines the following additional types by default

* DateTime - A date in (ISO 8601)[https://en.wikipedia.org/wiki/ISO_8601] format (e.g 'xT07:22Z') which 
is also the generally accepted JSON format 
* Time - A time in "HH:mm:ss" format, (e.g. '13:24:33')
* Date - Just a date, e.g '2012-03-19'
* JavaTimestamp - The Java timestamp format, milliseconds since 1970
* Java8Timestamp - The Java 8 timestamp, made of two longs, allowing just about any date / time to be represented


## The types supported by JSON 
 
This is just String,Number,Array,Map,Boolean and null.

There are conversions defined between the GraphQL types and 

## A natural subset of Java types for use with JSON (JavaJson)

Keeping to basic types such as String, Boolean makes it easy to inter-operate with most Java 
libraries and frameworks. In this service these are referred to as 'JavaJson' types

## A full set of Java types 

This needs rules to "coerce" into the more limited types above. As the set of Java types is in effect 
infinite some mechanism to include custom rules is needed
  


