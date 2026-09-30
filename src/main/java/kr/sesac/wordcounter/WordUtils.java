package kr.sesac.wordcounter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class WordUtils {
    public static final String DELIMETER = "[^a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+";//단어로 포함되지 않는 문자
    public static final String NUMBER = "^[0-9]+$";//숫자로만 하나 이상 이루어져 있는지

    public static final List<String> CSV_TARGETS = List.of("text");
    public static final List<String> TSV_TARGETS = List.of("document");
    public static final String HTML_TARGET = "#content";
    public static final SettingParellel PARELLEL_SETTING = SettingParellel.PARELLEL;
    public static final int THREAD_POOL = 8;

    public static Stream<String> toWordStream(String[] words){
        return Arrays.stream(words)
                .filter(str -> !str.isEmpty())//""제거
                .filter(str -> !str.matches(WordUtils.NUMBER))//숫자로만 이루어진 단어 제거
                .map(String::toLowerCase);
    }
}