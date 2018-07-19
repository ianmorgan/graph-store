# (GraphQL) Fetchers

## Overview 
"wiring up" GraphQL queries using the [GraphQL Java](https://github.com/graphql-java/graphql-java) library is 
essential a 2 step process. 

Step 1. For each type, interface and union defined in the schema a TypeResolver is required, which tells the library 
which fields are expected. This process is quite simple, though it feels a little unnecessary as the information is 
readily available in the schema which has just been parsed. Presumably this is a trade off in the internal API design, 
which is flexible enough to drive directly through Java without an actual schema file 
(_or I have just miss understood the API_). 

Step 2. To return actual data, a "Fetcher" is required (so essentially for each query a fetcher must be wired up). 
Fetchers are somewhat more complicated and different patterns are required for types, unions and interfaces, though they all 
ultimately delegate to the [DAO](daos) layer. The GraphQL Java library supports a variety of object mappers in fetchers, 
but these all work using a basic Java Map (the key in the Map must match the name of the field, and the value must be stored 
in a Java class that can be coerced by the [Type Mapping](typeMappings) rules). The underlying data in the Map is simply 
that returned by the [DAO](daos), with some extra pseudo fields (all prefixed #), which are used to pass additional 
data for amongst others pagination and type resolution.  

The entry point in the code is GraphQLFactory, which is passed the actual GraphQL schema and a collection of the DAOS. 
It examines the queries and build fetchers according to the following rules.

### Document by ID 

A query like the that below is the most simple, simply lookup a document by its ID

```yaml
droid(id: ID!): Droid
``` 

This is implemented by a DocDataFetcher.


### Interface by ID 

A query like the that below simply tries a lookup for each document in the 
interface until a result is returned.

```yaml
character(id: ID!): Character
```

This is implemented by an InterfaceDataFetcher.

### A collection of documents 

A query like that below is slightly more complicated. It needs to find all documents of type 'Human' where the 
name field matches.

```yaml
humans(name : String!) : [Human]
```

This is implemented by a DocListDataFetcher.  A production quality implementation will need some type of 
index, but the current implementation simple runs through the entire collection in memory.


### Pseudo fields 

As noted, these are purely for internal processing. 

#### #docType

The docType, which is actually the 'type' name in the GraphQL schema, e.g. 'Human'.