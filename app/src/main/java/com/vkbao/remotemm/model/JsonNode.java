package com.vkbao.remotemm.model;

import java.util.ArrayList;
import java.util.List;

public class JsonNode {
    public enum Type { OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL }

    public String key;
    public Object value;
    public Type type;
    public boolean expanded = false;
    public int level = 0;
    public JsonNode parent;
    public Integer indexInArray = null;

    public List<JsonNode> children = new ArrayList<>();
}
