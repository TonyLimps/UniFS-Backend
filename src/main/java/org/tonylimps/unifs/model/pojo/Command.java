package org.tonylimps.unifs.model.pojo;

import lombok.Data;

@Data
public class Command {
    public static final class Type {
        public static final String AUTH = "auth";
        public static final String OFFER = "offer";
        public static final String ANSWER = "answer";
        public static final String CANDIDATE = "candidate";
    }
    String version;
    String type;
    String ticket;
    Long from;
    Long to;
    Object payload;
}
