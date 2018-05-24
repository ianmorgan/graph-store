# Type Mapping rules 

There are in effect 4 types systems in operation:

## The types supported by GraphQL

This is a restricted list, although it does allow the addition of custom types

## The types supported by JSON 
 
This is just String,Number,Array,Map,Boolean and null

## A natural subset of Java types for use with JSON (JavaJson)

Keeping to basic types such as String, Boolean makes it easy to inter-operate with most Java 
libraries and frameworks. In this service we refer to them as 'Java'

## A full set of Java types 

This needs rules to "coerce" into the more limited types above. As the set of Java types is in effect 
infinite some mechanism to include custom rules is needed
  
