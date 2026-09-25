package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.StringPatterns;
import kr.sesac.wordcounter.WordCount;
import kr.sesac.wordcounter.exception.DifferentColumnException;
import kr.sesac.wordcounter.exception.EmptyCsvTsvException;
import kr.sesac.wordcounter.exception.FileReadException;
import kr.sesac.wordcounter.exception.MissingTargetHeaderException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CsvFileAnalyzer implements FileAnalyzer{
    private WordCount wordCount = new WordCount();
    private long totalWordCount  = 0L;
    private long differentWordCount = 0L;

    @Override
    public AnalyzerDTO process(Path path) {
        var format = CSVFormat.RFC4180.builder()
                .setHeader()//첫 record를 column 이름으로 읽음
                .setSkipHeaderRecord(true)//헤더를 데이터로 처리x
                .get();

        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            CSVParser parser = format.parse(reader);
            if(parser.getHeaderMap().isEmpty()){
                throw new EmptyCsvTsvException("CSV에 헤더가 비어있습니다");
            }
            int headerCount = parser.getHeaderMap().size();
            Map<String,String> trimmedHeaders = new HashMap<>();
            for(String header : parser.getHeaderNames()){
                trimmedHeaders.put(header.trim(),header);
            }
            for (String target : StringPatterns.CSV_TARGETS){
                if(!trimmedHeaders.containsKey(target)){
                    throw new MissingTargetHeaderException("target header가 존재하지 않습니다.");
                }
            }

            for(CSVRecord record : parser){
                if(record.size() != headerCount){//header와 record의 column수가 다름
                    throw new DifferentColumnException("header와 record의 헤더 수가 다릅니다");
                }
                for (String column : StringPatterns.CSV_TARGETS){
                    String originalHeader = trimmedHeaders.get(column);
                    String temp = record.get(originalHeader);
                    String[] words = temp.split(StringPatterns.DELIMETER);
                    Arrays.stream(words)
                            .filter(str -> !str.isEmpty())//""제거
                            .filter(str -> !str.matches(StringPatterns.NUMBER))//숫자로만 이루어진 단어 제거
                            .map(String::toLowerCase)
                            .forEach(wordCount::addWord);
                }
            }//for문 끝
        }catch (UncheckedIOException e){
            throw new FileReadException("파일을 읽는 도중 예외");
        } catch (IOException e){
            throw new FileReadException("파일을 읽는 도중 예외");
        }
        totalWordCount = wordCount.getTotalWord();
        differentWordCount = (long) wordCount.getWordCount().size();
        return new AnalyzerDTO(wordCount,totalWordCount,differentWordCount);
    }
}
