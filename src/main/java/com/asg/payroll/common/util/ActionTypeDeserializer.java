//package com.asg.payroll.common.util;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class ActionTypeDeserializer extends JsonDeserializer<ActionType> {
//
//    @Override
//    public ActionType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//        String value = p.getValueAsString();
//        if (value == null) {
//            return null;
//        }
//
//        return switch (value.toUpperCase()) {
//            case "NOCHANGE" -> ActionType.NOCHANGE;
//            case "ISCREATED" -> ActionType.ISCREATED;
//            case "ISDELETED" -> ActionType.ISDELETED;
//            case "ISUPDATED" -> ActionType.ISUPDATED;
//            default -> throw new IllegalArgumentException("Invalid ActionType: " + value +
//                ". Valid values are: NOCHANGE, ISCREATED, ISDELETED, ISUPDATED");
//        };
//    }
//}