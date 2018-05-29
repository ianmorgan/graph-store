# The type conversion chain 

## Storing docs (POSTing JSON)

The following processing stack applies:

1. JSON is converted to Map/List of standard 'JavaJson' types. (ideally numeric precision is retained by use of BigDecimal)
2. Schema checker confirms that:
    * the structure matches (i.e. no unexpected keys)
    * the 'JavaJson' type can be coerced into GraphQL type (see below)
3. Map/List structure converted back to JSON for storage as an event    

## Reading as simple JSON doc (classic REST)

The following processing stack applies:

1. Events for the aggregate are retrieved from the event store
2. For each event
    * JSON converted to Map/List of standard 'JavaJson' types.
    * Reduce builds new state by applying generic 'doc merge' algorithm
3. Map/List structure converted back to JSON for returning as document 

_note that no schema check is applied to simple read_     
    
## Reading using GraphQL query 

The following processing stack applies

1. GraphQL query is parsed 
2. For each 'document' identified in the query (see below)
    * build document state to Map/List structure (steps 1 and 2 of 'Reading as simple JSON doc')
    * convert to GraphQL type (not sure what I mean here)
3. Apply higher level GraphQL concepts 
    * paging etc 
4. Serialize to JSON 

_this needs quite a lot more detail_         