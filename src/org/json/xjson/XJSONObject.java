package org.json.xjson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.SendingContext.RunTime;

enum State {
    AC1, AC2, AC3, SHORT_STRING, OPEN_BRACKET, CLOSE_BRACKET, END, DOT, ERROR, BUILD_OBJ
}
public class XJSONObject {
    private boolean createOnFly;
    JSONObject jsonObject;

    XJSONObject () {
        jsonObject = new JSONObject();
        createOnFly = false;
    }

    XJSONObject (String json) {
        jsonObject = new JSONObject(json);
        createOnFly = false;
    }

    XJSONObject (JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        createOnFly = false;
    }

    public void setCreateOnFly(boolean createOnFly) {
        this.createOnFly = createOnFly;
    }

    Object get (String query) throws RuntimeException {
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
                        System.out.println("CANNOT START WITH NUMBER");
                        state = State.ERROR;
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
                        System.out.println("BUILDING OBJECT: " + buffer);
                        tempObj = ((JSONObject) tempObj).get(buffer);
                        buffer = "";
                    } else if (state == State.END) {
                        buffer += ch;
                        tempObj = ((JSONObject) tempObj).get(buffer);
                        buffer = "";
                    } else {
                        buffer += ch;
                    }
                    index++;
                    break;
                case OPEN_BRACKET:
                    if (ch == ']') {
                        state = State.CLOSE_BRACKET;
                    } else {
                        buffer += ch;
                        index++;
                    }
                    break;
                case CLOSE_BRACKET:
                    System.out.println("Received buffer: " + buffer);
                    try {
                        int num = Integer.parseInt(buffer);
                        tempObj = ((JSONArray) tempObj).get(num);
                    } catch (NumberFormatException e) {
                        tempObj = ((JSONObject) tempObj).get(buffer);
                    }
                    buffer = "";
                    state = State.AC3;
                    index++;
                    break;
                case AC3:
                    if (ch == '.') {
                        state = State.DOT;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else if (index+1 == query.length()) {
                        state = State.END;
                    } else {
                        System.out.println("FROM AC3");
                        state = State.ERROR;
                    }
                    break;
                case DOT:
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else {
                        System.out.println("FROM DOT");
                        state = State.ERROR;
                    }
                    break;
            }
            if (state == State.ERROR) {
                System.out.println("WRONG QUERY");
                throw new RuntimeException();
            }
        }

        return tempObj;
    }

    void put (String query, Object obj) {
        String key = null;
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
                        System.out.println("CANNOT START WITH NUMBER");
                        state = State.ERROR;
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
                            JSONObject childObj = new JSONObject();
                            ((JSONObject) tempObj).put(buffer, childObj);
                            tempObj = childObj;
                        }

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
                    } else {
                        buffer += ch;
                        index++;
                    }
                    break;
                case CLOSE_BRACKET:
                    if (index + 1 == query.length()) {
                        key = buffer;
                        index++;
                        break;
                    }
                    try {
                        int num = Integer.parseInt(buffer);
                        tempObj = ((JSONArray) tempObj).get(num);
                    } catch (NumberFormatException e) {
                        tempObj = ((JSONObject) tempObj).get(buffer);
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
                        System.out.println("FROM AC3");
                        state = State.ERROR;
                    }
                    break;
                case DOT:
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else {
                        System.out.println("FROM DOT");
                        state = State.ERROR;
                    }
                    break;
            }
            if (state == State.ERROR) {
                System.out.println("WRONG QUERY");
                throw new RuntimeException();
            }
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

    public static void main (String args[]) {
        XJSONObject xobj = new XJSONObject();
        xobj.put("users.ahmed.username[0]", "ahmednr123");

        System.out.println("users.ahmed.username: " + xobj.toString());
    }
}
