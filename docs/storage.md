# Storing 


## Storing docs (POSTing JSON)

Docs are stored, the 

1. JSON is converted to Map/List of standard 'JavaJson' types. (ideally numeric precision is retained by use of BigDecimal)
2. Schema checker confirms that:
    * the structure matches (i.e. no unexpected keys)
    * the 'JavaJson' type can be coerced into GraphQL type (see below)
3. Map/List structure converted back to JSON for storage as an event    

