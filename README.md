### An easier way to access/edit json elements in Java

Following command:
```java
xobj.put("query.match.field1", "somedata");
```
changes JSONObject to:
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

Constructors:
```java
XJSONObect()
XJSONObect(String jsonStr)
XJSONObect(JSONObject object)
XJSONObect(JSONArray object)
```

Methods to access/manipulate JSON:
```java
// To get an Object form json
Object get(String query)

// To put an Object into json
void put(String query, Object obj)
```

The objects must be typecast to their original form while retrieving them.
```java
String str = (String) xobj.get("query.match.username");
```

Querying examples
```

```