package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.WordCount;

public class AnalyzeResult {
    private final WordCount wordCount;
    private final AnalyzeStatus status;

    public AnalyzeResult(WordCount wordCount,AnalyzeStatus status){
        this.wordCount = wordCount;
        this.status = status;
    }

    public WordCount getWordCount() {
        return wordCount;
    }

    public AnalyzeStatus getStatus() {
        return status;
    }
}
