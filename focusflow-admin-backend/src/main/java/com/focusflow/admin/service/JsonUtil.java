package com.focusflow.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static final ObjectMapper om = new ObjectMapper();
    public static String toJson(Object o){
        try { return om.writeValueAsString(o); }
        catch (Exception e){ return "[]"; }
    }
}
