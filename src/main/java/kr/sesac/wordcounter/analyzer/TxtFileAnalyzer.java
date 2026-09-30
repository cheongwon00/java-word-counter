package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.WordUtils;
import kr.sesac.wordcounter.WordCount;
import kr.sesac.wordcounter.exception.FileReadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TxtFileAnalyzer implements FileAnalyzer{

    @Override
    public WordCount process(Path path) {
        WordCount wordCount = new WordCount();

        try (BufferedReader reader =
                     Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(WordUtils.DELIMETER);
                WordUtils.toWordStream(words)
                        .forEach(wordCount::addWord);
            }
        } catch (IOException e) {//읽기 권한이 없을때, 읽는 도중 예외
            throw new FileReadException(path + ": 파일 읽기 오류 / " + e.getMessage());
        }
        return wordCount;
    }
}
