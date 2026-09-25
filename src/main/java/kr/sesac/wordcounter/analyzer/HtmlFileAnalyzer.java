package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.StringPatterns;
import kr.sesac.wordcounter.WordCount;
import kr.sesac.wordcounter.exception.FileReadException;
import kr.sesac.wordcounter.exception.InvalidHtmlException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

public class HtmlFileAnalyzer implements FileAnalyzer{
    private WordCount wordCount = new WordCount();
    private long totalWordCount  = 0L;
    private long differentWordCount = 0L;
    @Override
    public AnalyzerDTO process(Path path) {
        try {
            Document document = Jsoup.parse(path.toFile(), StandardCharsets.UTF_8.name());
            Elements matches = document.select("#content");
            if(matches.size() != 1){//content가 1개가 아닌경우
                throw new InvalidHtmlException("본문 요소는 정확히 하나여야 합니다.");
            }

            Element content = matches.first();
            content.select("script, style, nav, header, footer").remove();
            String line = content.text();
            String[] words = line.split(StringPatterns.DELIMETER);
            Arrays.stream(words)
                    .filter(str -> !str.isEmpty())//""제거
                    .filter(str -> !str.matches(StringPatterns.NUMBER))//숫자로만 이루어진 단어 제거
                    .map(String::toLowerCase)
                    .forEach(wordCount::addWord);
        } catch (IOException e) {
            throw new FileReadException("파일을 읽는 도중 예외");
        }
        totalWordCount = wordCount.getTotalWord();
        differentWordCount = (long) wordCount.getWordCount().size();
        return new AnalyzerDTO(wordCount,totalWordCount,differentWordCount);
    }
}
