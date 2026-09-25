package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.StringPatterns;
import kr.sesac.wordcounter.WordCount;
import kr.sesac.wordcounter.exception.FileReadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class TxtFileAnalyzer implements FileAnalyzer{
    private WordCount wordCount = new WordCount();
    private long totalWordCount  = 0L;
    private long differentWordCount = 0L;

    @Override
    public AnalyzerDTO process(Path path) {
        try (BufferedReader reader =
                     Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(StringPatterns.DELIMETER);
                Arrays.stream(words)
                        .filter(str -> !str.isEmpty())//""제거
                        .filter(str -> !str.matches(StringPatterns.NUMBER))//숫자로만 이루어진 단어 제거
                        .map(String::toLowerCase)
                        .forEach(wordCount::addWord);
            }
        } catch (IOException e) {//읽기 권한이 없을때, 읽는 도중 예외
            throw new FileReadException("파일을 읽는 도중 예외");
        }
        totalWordCount = wordCount.getTotalWord();
        differentWordCount = (long) wordCount.getWordCount().size();
        return new AnalyzerDTO(wordCount,totalWordCount,differentWordCount);
    }
}
