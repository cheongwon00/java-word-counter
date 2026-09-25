package kr.sesac.wordcounter;

import java.util.List;

public class StringPatterns {
    public static final String DELIMETER = "[^a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+";//단어로 포함되지 않는 문자
    public static final String NUMBER = "^[0-9]+$";//숫자로만 하나 이상 이루어져 있는지

    public static final List<String> CSV_TARGETS = List.of("text");
    public static final List<String> TSV_TARGETS = List.of("document");
}
