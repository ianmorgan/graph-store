# Updates and Validations 

The Graph Store application follows simple document style updates whereby there is no distinction 
between the more traditional create and update. This has certain advantages in more distributed systems,
one being that there is no longer a need to explicitly track the point of creation so that a create 
can be issued in place of an update (generally simple enough in a standalone application, but not 
always so easy in distributed system with asynchronous comms). 

The obvious downside is that its now possible to get partially complete documents. The simple answer is 
to treat these a read problem, they will simply be skipped in the final GraphQL result
 (but logged as warning). 

Any updates made via the API will ALWAYS validate against the types in the GraphQL schema, so for example 
if there is a Date type, 'dateOfBirth' must be in a valid date format. Mandatory checks can be controlled 
by the additional 'validationMode' param, which has the following options

## Create 

in Create mode, all the mandatory fields must be supplied. It is conceptually the same as 
the SQL INSERT statement

## Update

If Update mode, any existing state is first read back and then the update applied, after which 
a check is made to see if all fields now pass the mandatory fields check. It is conceptually the same as 
the SQL UPDATE statement. 

This option has a small performance hit (the current state must be retrieved before the write). 
It also potentially sensitive to out of order updates, for example in normal operation updates A always arrives
before update B, but this might not be guaranteed (a service may be down, a queue might fail over 
and resend old messages) leading to a situation whereby updates will get rejected due to a transient 
 inconsistent state. 

## Skip 

Simply skip all mandatory validation. This is the default if nothing is supplied.  