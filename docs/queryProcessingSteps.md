# Query Processing Steps

To process a query, the [Data Access(DAO)](daos) and [(GraphQL)Fetchers](fetchers) are needed. 
 
The query: 

```
{droid(id: "2001"){name,starships{name,model},friends{name,friends(first: 2){name}}}}
``` 

Will be processed as follows: 

A [DocDataFetcher[(https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/graphstore/graphql/DocDataFetcher.kt)] 
for Droid will have been wired automatically by convention based on the 
return type of Droid in the GraphQL schema. When processing the query, GraphQL Java will call the
standard get() method which is passed the GraphQL Java 
'DataFetchingEnvironment' object. This has enough information to understand that this is a simple 
lookup by ID. 

GraphQL will also provide the following map of query args, which is basically the query converted to 
a Map. It has all the information apart from the entry to the query, 'droid(id: "2001")' 

```json
{
  "name": {},
  "starships": {},
  "starships/name": {},
  "starships/model": {},
  "friends": {},
  "friends/name": {},
  "friends/friends": {"first": 2},
  "friends/friends/name": {}
}
```

The initial query for the Droid will just return the raw data. In JSON form (and with the fields not in the query 
excluded) this looks like:

```json
{
    "name" : "R2-D2",
    "starships" : [ "12" ],
    "friends" : [ "1000", "1002", "1003" ]
}
```

At this point we now need to expand out the ID fields by making further calls to the DAOs. There is a difference 
to the original call, which was passed the 'DataFetchingEnvironment' object as now the rules are driven by the map of 
query args instead. The steps in summary are: 

* find the docs we need to expand out by looking for the unique nodes in the args, in this case 'starships' and 'friends'
* for each of these rebuild a simpler set of args that represent the doc in question
* retrieve data and merge into the original map, replacing the IDs with real data
* recurse as necessary, e.g. finding friends of friends .

The [ArgsWalker](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/graphstore/graphql/ArgsWalker.kt)
encapsulates the rules necessary to 'walk' though the args - its really quite similar to walking a directory tree.

To retrieve data for the 'starships', using the ArgsWalker the node is rewritten as 
```json
{
  "name": {},
  "model": {}
}
```

And a call is made to the Starship DAO for each starship ID. After this, the data looks like 

```json
{
    "name" : "R2-D2",
    "starships" : [ {
            "name" : "X-wing",
            "model" : "T-65 X-wing"
          } ],
    "friends" : [ "1000", "1002", "1003" ]
}
```

The call for friends is a little more complex as this query has friends of friends. Using ArgsWalker,
the args are rewritten as 

```json
{
  "name": {},
  "friends": {"first": 2},
  "friends/name": {}
}
```

The first call just returns friends with the ID of their 
friends. So the data now looks like.

```json
{
  "name" : "R2-D2",
      "starships" : [ {
        "name" : "X-wing",
        "model" : "T-65 X-wing"
      } ],
      "friends" : [ {
        "name" : "Luke Skywalker",
        "friends" : "[1002, 1003, 2000, 2001]"
      }, {
        "name" : "Han Solo",
        "friends" : "[1001, 1003, 2001]"
      }, {
        "name" : "Leia Organa",
        "friends" : "[1000, 1002, 2000, 2001]"
      } ]
 }
```

To retrieve the friends of friends futher calls are required. Using the ArgsWalker, the args below are built. Note 
that in this case a root node ("/") has been added as there are parameters to be passed into the query. 

```json
{
  "/": {"first": 2},
  "name": {}
}
```




