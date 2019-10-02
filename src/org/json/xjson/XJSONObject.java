package org.json.xjson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

enum State {
    AC1, AC3, SHORT_STRING, OPEN_BRACKET, CLOSE_BRACKET, END, DOT
}
public class XJSONObject {
    private boolean createOnFly;
    private Object jsonObject;

    public XJSONObject () {
        jsonObject = new JSONObject();
        createOnFly = false;
    }

    public XJSONObject (String json) {
        jsonObject = new JSONObject(json);
        createOnFly = false;
    }

    public XJSONObject (JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        createOnFly = false;
    }

    public XJSONObject (JSONArray jsonArray) {
        this.jsonObject = jsonArray;
        createOnFly = false;
    }

    public void setCreateOnFly(boolean createOnFly) {
        this.createOnFly = createOnFly;
    }

    public void initialize(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void initialize(JSONArray jsonArray) {
        this.jsonObject = jsonArray;
    }

    public Object get (String query) throws XJSONException {
        Object tempObj = jsonObject;

        int index = 0;
        State state = State.AC1;
        String buffer = "";

        while (index < query.length()) {
            char ch = query.charAt(index);
            switch (state) {
                case AC1:
                    buffer = "";
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else {
                        throw new XJSONException("Variable names cannot start with a number [at character: " + index + "]");
                    }
                    break;
                case SHORT_STRING:
                    if (ch == '.') {
                        state = State.DOT;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                    } else if (index + 1 == query.length()) {
                        state = State.END;
                    }

                    if (state == State.OPEN_BRACKET || state == State.DOT) {
                        // BUILD OBJ
                        if (((JSONObject) tempObj).has(buffer)) {
                            tempObj = ((JSONObject) tempObj).get(buffer);
                        } else {
                            throw new XJSONException("Object: " + buffer + " doesn't exist! [at character: " + (index - buffer.length() + 1) + "]");
                        }

                        buffer = "";
                    } else if (state == State.END) {
                        buffer += ch;

                        if (tempObj instanceof JSONArray) {
                            throw new XJSONException("Querying an Object, but found Array [at character: " + (index - buffer.length() + 1) + "]");
                        }

                        if (((JSONObject) tempObj).has(buffer)) {
                            tempObj = ((JSONObject) tempObj).get(buffer);
                        } else {
                            throw new XJSONException("Object: " + buffer + " doesn't exist! [at character: " + (index - buffer.length() + 1) + "]");
                        }

                        buffer = "";
                    } else {
                        buffer += ch;
                    }
                    index++;
                    break;
                case OPEN_BRACKET:
                    if (ch == ']') {
                        state = State.CLOSE_BRACKET;
                    } else if (ch == '\\') {
                        if (query.charAt(index+1) == ']') {
                            buffer += ']';
                            index++;
                        } else {
                            buffer += ch;
                        }
                        index++;
                    } else {
                        buffer += ch;
                        index++;
                    }
                    break;
                case CLOSE_BRACKET:
                    try {
                        int num = Integer.parseInt(buffer);

                        if (num < ((JSONArray) tempObj).length()) {
                            tempObj = ((JSONArray) tempObj).get(num);
                        } else {
                            throw new XJSONException("Array Index out of bound [at character: " + index + "]");
                        }

                    } catch (NumberFormatException e) {
                        if (((JSONObject) tempObj).has(buffer)) {
                            tempObj = ((JSONObject) tempObj).get(buffer);
                        } else {
                            throw new XJSONException("Object: " + buffer + " doesn't exist! [at character: " + (index - buffer.length() + 1) + "]");
                        }
                    }
                    buffer = "";
                    state = State.AC3;
                    index++;
                    break;
                case AC3:
                    if (ch == '.') {
                        state = State.DOT;
                        index++;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else if (index+1 == query.length()) {
                        state = State.END;
                    } else {
                        throw new XJSONException("Parsing Error [at character: " + index + "]");
                    }
                    break;
                case DOT:
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else {
                        throw new XJSONException("Parsing Error: Variable names cannot start with a number [at character: " + index + "]");
                    }
                    break;
            }
        }

        return tempObj;
    }

    private static class XObject {
        private String objectName;
        private Object object;

        private XObject (String objectName, Object object) {
            this.objectName = objectName;
            this.object = object;
        }
    }

    public void put (String query, Object obj) throws XJSONException {
        String key = null;
        Object tempObj = jsonObject;

        int index = 0;
        State state = State.AC1;
        String buffer = "";
        ArrayList<XObject> objects = new ArrayList<>();
        objects.add(new XObject("_this", tempObj));

        while (index < query.length()) {
            char ch = query.charAt(index);
            switch (state) {
                case AC1:
                    buffer = "";
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else {
                        throw new XJSONException("Variable names cannot start with a number [at character: " + index + "]");
                    }
                    break;
                case SHORT_STRING:
                    if (ch == '.') {
                        state = State.DOT;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                    } else if (index + 1 == query.length()) {
                        state = State.END;
                    }

                    if (state == State.OPEN_BRACKET || state == State.DOT) {
                        if (((JSONObject) tempObj).has(buffer)) {
                            tempObj = ((JSONObject) tempObj).get(buffer);
                        } else if (!createOnFly) {
                            throw new XJSONException("Object: " + buffer + " doesn't exist! [at character: " + (index - buffer.length() + 1) + "]");
                        } else {
                            JSONObject childObj = new JSONObject();
                            ((JSONObject) tempObj).put(buffer, childObj);
                            tempObj = childObj;
                        }
                        objects.add(new XObject(buffer, tempObj));

                        buffer = "";
                    } else if (state == State.END) {
                        buffer += ch;
                        key = buffer;

                        buffer = "";
                    } else {
                        buffer += ch;
                    }
                    index++;
                    break;
                case OPEN_BRACKET:
                    if (ch == ']') {
                        state = State.CLOSE_BRACKET;
                    } else if (ch == '\\') {
                        if (query.charAt(index+1) == ']') {
                            buffer += ']';
                            index++;
                        } else {
                            buffer += ch;
                        }
                        index++;
                    } else {
                        buffer += ch;
                        index++;
                    }
                    break;
                case CLOSE_BRACKET:
                    try {
                        int num = Integer.parseInt(buffer);
                        if (tempObj instanceof JSONArray) {
                            if (num < ((JSONArray) tempObj).length()) {
                                tempObj = ((JSONArray) tempObj).get(num);
                            } else {
                                throw new XJSONException("Array Index out of bound [at character: " + (index-1) + "]");
                            }
                        } else if (!createOnFly) {
                            throw new XJSONException("Object: " + buffer + " doesn't exist! [at character: " + (index - buffer.length() + 1) + "]");
                        } else {
                            if (num != 0) {
                                throw new XJSONException("Array Index out of bound [at character: " + (index-1) + "]");
                            }

                            JSONArray child = new JSONArray();
                            JSONObject youngerChild = new JSONObject();
                            child.put(0, youngerChild);

                            if (objects.size() > 1) {
                                XObject oneDownObject = objects.get(objects.size() - 1);
                                XObject twoDownObject = objects.get(objects.size() - 2);

                                if (twoDownObject.object instanceof JSONObject) {
                                    ((JSONObject) twoDownObject.object).put(oneDownObject.objectName, child);
                                } else {
                                    int oneDownObjectInteger = Integer.parseInt(oneDownObject.objectName);
                                    ((JSONArray) twoDownObject.object).put(oneDownObjectInteger, child);
                                }

                                objects.remove(objects.size() - 1);
                                objects.add(new XObject(buffer, child));
                                objects.add(new XObject(buffer, youngerChild));
                            } else {
                                objects.remove(objects.size() - 1);

                                objects.add(new XObject("_this", child));
                                objects.add(new XObject(buffer, youngerChild));
                                jsonObject = child;
                            }

                            tempObj = youngerChild;
                        }

                    } catch (NumberFormatException e) {

                        if (((JSONObject) tempObj).has(buffer)) {
                            tempObj = ((JSONObject) tempObj).get(buffer);
                        } else if (!createOnFly) {
                            throw new XJSONException("Object: " + buffer + " doesn't exist! [at character: " + (index - buffer.length() + 1) + "]");
                        } else {
                            JSONObject childObj = new JSONObject();
                            ((JSONObject) tempObj).put(buffer, childObj);
                            tempObj = childObj;
                        }

                        objects.add(new XObject(buffer, tempObj));
                    }

                    if (index + 1 == query.length()) {
                        XObject twoDownObject = objects.get(objects.size() - 2);
                        tempObj = twoDownObject.object;

                        key = buffer;
                        index++;
                        break;
                    }

                    buffer = "";
                    state = State.AC3;
                    index++;
                    break;
                case AC3:
                    if (ch == '.') {
                        state = State.DOT;
                        index++;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else {
                        throw new XJSONException("Parsing Error [at character: " + index + "]");
                    }
                    break;
                case DOT:
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else {
                        throw new XJSONException("Parsing Error: Variable names cannot start with a number [at character: " + index + "]");
                    }
                    break;
            }
        }

        if (key == null) {
            throw new XJSONException("Unexpected Error");
        }

        if (tempObj instanceof JSONArray) {
            int intKey = Integer.parseInt(key);
            ((JSONArray)tempObj).put(intKey, obj);
        } else if (tempObj instanceof JSONObject) {
            ((JSONObject)tempObj).put(key, obj);
        }
    }

    public String toString () {
        return jsonObject.toString();
    }
}
