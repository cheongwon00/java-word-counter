package kr.sesac.wordcounter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WordCount {
    private Map<String,Long> wordCount = new HashMap<>();
    private long totalWord = 0L;

    public void addWord(String word){
        wordCount.put(word,wordCount.getOrDefault(word,0L)+1);
        totalWord++;
    }

    public void addAllWord(WordCount wc){
        Map<String, Long> other = wc.getWordCount();
        other.entrySet().stream()
                .forEach(entry ->
                        wordCount.put(entry.getKey(), wordCount.getOrDefault(entry.getKey(),0L) + entry.getValue()));
        totalWord+=wc.getTotalWord();
    }

    public Map<String,Long> sortWordCounter(){
        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String,Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,
                        (a,b)->a, LinkedHashMap::new));
    }

    public Map<String,Long> getWordCount(){
        return new HashMap<>(wordCount);
    }

    public long getTotalWord() {
        return totalWord;
    }
    public long getDifferentWord(){
        return wordCount.size();
    }
}
