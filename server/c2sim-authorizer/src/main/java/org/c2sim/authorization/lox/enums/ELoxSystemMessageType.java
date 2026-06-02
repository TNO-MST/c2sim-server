package org.c2sim.authorization.lox.enums;

import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.interfaces.TextEnum;
import org.c2sim.authorization.utils.EnumUtil;

import java.util.Set;
import java.util.stream.Collectors;

public enum ELoxSystemMessageType implements TextEnum{

        /** SIMAN */
        SIMAN("siman"),
        /** Siman response */
        SIMAN_RESPONSE("simanResponse"),
        /** Query */
        QUERY("query"),
        /** Unknown message type */
        UNKNOWN("Unknown");

        private final String text;

    ELoxSystemMessageType(String text) {
            this.text = text;
        }

        public static Set<ELoxSystemMessageType> fromTextSet(Set<String> texts) {
            return EnumUtil.fromTextSet(ELoxSystemMessageType.class, texts);
        }

        public String getText() {
            return text;
        }

        public static String toText(Set<ELoxSystemMessageType> msgType) {
            return String.join(
                    C2SimClaims.CLAIM_LIST_SEPARATOR,
                    msgType.stream().map(ELoxSystemMessageType::getText).collect(Collectors.toSet()));
        }
    }
