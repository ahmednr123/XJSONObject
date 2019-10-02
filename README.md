### An easier way to access/edit json elements in Java

Following command:
```java
xobj.put("query.match.field1", "somedata");
```
changes contents of a JSONObject to:
```json
{
    "query": {
        "match": {
            "field1": "somedata"
        } 
    }
}
```

The library provides an easier way to access json elements in Java and edit them as well. It is build on top of JSONObject so any Object stored inside the XJSONObject is part of the JSONObject library. 

The library only interprets a query string and accesses the element within the JSONObject.

### Query format

There are two main set of operators used in the query. 

Dot `.` and Square brackets `[]`

Dot `.` can only be used to access json keys that follow the javascript variable name conventions.

```java
//Valid
xobj.get("query._obj");
xobj.get("_jsonObject");

//Invalid
xobj.get("23_var");
xobj.get("json.007_agent");
```

Square brackets `[]` can be used to define two things, a json array if the value within the brackets is a number or a json object if the bracket contains any non numeric character.

And as a json key can literally be any string, so those can be accessed using the square brackets `[]`

```java
//Valid
xobj.get("users[0]");           // 0th element of array "users"
xobj.get("users[]");          // If the key is an empty string
xobj.get("[users][0]");         // "users" is considered as a string
xobj.get("query.sort[0].timestamp")

//Invalid
xobj.get("[10]arfg");
xobj.get("[]]");
```

If incase the key string contains a closing square bracket it can be escaped using two backslashes `\\`.
```java
xobj.get("[\\]]");             // To access the key: "]"
```

## Using the library
#### Constructors:
```java
XJSONObect()
XJSONObect(String jsonStr)
XJSONObect(JSONObject object)
XJSONObect(JSONArray object)
```

#### Methods to access/manipulate JSON:
```java
// To get an Object form json
Object get(String query)

// To put an Object into json
void put(String query, Object obj)
```

The objects must be type cast to their original form while retrieving them.
```java
String str = (String) xobj.get("query.match.username");
```

The `put(..)` method can create objects as it interprets the query if the object is not already created. 

To do this the `createOnFly` option must be set to `true`, using the `setCreateOnFly(boolean)` method. 

By default `createOnFly` is set to `false`
```java
XJSONObject xobj = new XJSONObject();
xobj.setCreateOnFly(true);

xobj.put("users[0].username", "John Doe");
System.out.println(xobj.toString());
```
Output:
```json
{
    "users": [
        {"username" :  "John Doe"}
    ]
}
```

#### Exceptions

The library throws 5 different types of errors, which must be caught by `XJSONException`.

The exceptions are:
```java
// Only if trying to access through '.'
"Interpret Error: Variable names cannot start with a number [at character index: (index)]"

// If incase the object doesn't exist. 
// If the createOnFly is set to true the put() method ignores this 
// and creates an object by itself
"Object: (object name) doesn't exist! [at character index: (index)]"

// When using the get() method to access string key in arrays
"Querying an Object, but found Array [at character index: (index)]"

// If the array index is more than the length of the array
"Array Index out of bound [at character index: (index)]"

// If closing bracket ']' is not followed by opening bracket '[' or a dot '.'
"Interpret Error: Expected '[' or '.' [at character index: (index)]"
```