# Command Line Options 

The following are (will be!) supported 

### ----help (-h)

Prints a help message

### ----schmea [schmea] (-S) 

Will automatically load one of the predefined schemas. By default the starwars 
schema is loaded.

### ----seed [dataset] (-s)

Will automatically seed with the schema and data in the dataset. By default the 
test data for the starwars schema is loaded

### ----noseed (-N)

Don't run any seeding 


### ----eventstore (-E)

Starts looking for a real event store, expected at (http://event-store:7001).

