# REST API  

## Introduction 
All the types in the GraphQL schema are also available as traditional REST style API. So taking the 
basic [Star Wars](https://github.com/ianmorgan/graph-store/blob/master/src/schema/starwars.graphqls) then Droid, Human 
and Character are all available via a REST API. Character is read-only as it is an Interface, whereas Droid and Human 
can be modified. As there is currently no support for mutations via the GraphQL API, the only ways to get data into the 
system is to either use the REST API or write directly to underlying event stream.

By convention internally these resources are referred to as 'docs' as they follow the same basic document metaphor
used by document centric databases such as MongoDB. 

## Retrieving a Doc 

This is classic REST, e.g. 

http://graphstore.app/docs/Droid/2001

or with curl

```bash
curl -H "Accept: application/json" http://graphstore.app/docs/Droid/2001
```

## Updating a Doc 


todo ....


 