# Standards 

Common behaviours expected across services: 


## Use of HTTP status codes 

The general style is more RPC over JSON/HTTP than pure REST, thought it doesn't follow the 
[JSON-RPC](http://www.jsonrpc.org/) spec.

Services should restrict status codes to just 200 (Success) and 500(Error/Exception).

 Note *Error/Exception* refers to technical / protocol problems (i.e. a problem making the call, 
 or a problem with the server) and not genuine application errors (for example trying to create an
 account that already exists), which should be returned as part of the data (see below). In programming 
  terms this can be thought of as an API where any method may through exceptions but any meaningful 
  business rule should be coded to as part of the return data. 
  
 Servers may still of course return other status codes before they even reach the application logic, 
 e.g. 404, 401, 503
 
## Use of HTTP verbs 

Only GET, POST and DELETE are used. 

POST is used for any operation that will modify data (apart from DELETE) and non-modifying operations that require 
JSON data (generally either because of the size or complexity of the required parameters).

## Returning data 

### 200 response

All data is returned as a JSON Object. For success responses data is under the 'data' key  (this follows the 
convention for JSON-RPC and GraphQL). 

Some APIs may chose to return partial responses (this is typically for APIs that 
 aggregate across multiple backend services) in which case there may be errors even for a 
 success response (this is part of GraphQL spec).  Use of this feature is an API decision , and it gives clients the 
 flexibility to accept or reject partial responses.
 
### 500 response 

This should return a JSON object with one or more errors under the 'errors' key. Use the conventions documented in the 
[GraphQL](http://facebook.github.io/graphql/October2016/#sec-Response-Format) spec. 

## Client processing 

The following are recommended as default rules:

* if code indicates an authorisation problem process as appropriate (for example redirect to login page)
* if not 500 or 200 fail with any appropriate logging 
* if 500 fail, logging errors collections 
* if 200 with errors fail, logging errors collections (so default is to aggressively reject any partial response)
* process results in 'data'. If data contains errors or problems associated to the application business rules 
process as appropriate 

 
 
 
 

