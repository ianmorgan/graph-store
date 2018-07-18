# (GraphQL) Fetchers

The process of wiring up resolvers (referred to a "fetchers" in this design) to return the data required 
by the graphQL query is described below. These build on the [DAO](daos) layer, which is also driven by the 
the GraphQL schema but only provides a more traditional REST style access. 

## Overview 

The entry point is GraphQLFactory, which is passed the actual GraphQL schema a collection of the DAOS. It 
examines the queryies and build fetchers according to the following rules 

### Document by ID 

A query like the that below is the most simple, simply lookup a document by its ID

```yaml
droid(id: ID!): Droid
``` 

This is implemented by a DocDataFetcher.


### Interface by ID 

A query like the that below simply tries to lookup for each document in the 
interface until a result is returned.

```yaml
character(id: ID!): Character
```

This is implemented by an InterfaceDataFetcher.

### A collection of documents 

A query like that below is slightly more complicated. It needs to find all documents Human where the 
name field matches.

```yaml
humans(name : String!) : [Human]
```
This is implemented by a DocListDataFetcher.  A production quality implementation will need some type of 
index, but the current implementation simple runs through the entire collection in memory. 
