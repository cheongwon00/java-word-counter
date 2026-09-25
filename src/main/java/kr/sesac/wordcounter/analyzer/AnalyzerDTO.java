package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.WordCount;

public class AnalyzerDTO {
    private WordCount wordCount = new WordCount();
    private long totalWordCount = 0L;
    private long differentWordCount = 0L;

    public AnalyzerDTO() {
    }

    public AnalyzerDTO(WordCount wordCount, long totalWordCount, long differentWordCount){
        this.wordCount = wordCount;
        this.totalWordCount = totalWordCount;
        this.differentWordCount = differentWordCount;
    }

    public WordCount getWordCount() {
        return wordCount;
    }

    public long getTotalWordCount() {
        return totalWordCount;
    }

    public long getDifferentWordCount() {
        return differentWordCount;
    }

    public void setTotalWordCount(long totalWordCount) {
        this.totalWordCount = totalWordCount;
    }

    public void setDifferentWordCount(long differentWordCount) {
        this.differentWordCount = differentWordCount;
    }

    public void setWordCount(WordCount wordCount) {
        this.wordCount = wordCount;
    }
}
