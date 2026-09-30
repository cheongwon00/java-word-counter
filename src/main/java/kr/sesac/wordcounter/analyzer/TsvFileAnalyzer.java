package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.WordUtils;
import kr.sesac.wordcounter.WordCount;
import kr.sesac.wordcounter.exception.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TsvFileAnalyzer implements FileAnalyzer {

    @Override
    public WordCount process(Path path) {
        WordCount wordCount = new WordCount();
        var format = CSVFormat.RFC4180.builder()
                .setDelimiter('\t')//tsv는 tap으로 구분
                .setQuote(null)// "" 특별하게 취급x
                .setHeader()//첫 record를 column 이름으로 읽음
                .setSkipHeaderRecord(true)//헤더를 데이터로 처리x
                .get();

        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            CSVParser parser = format.parse(reader);
            if(parser.getHeaderMap().isEmpty()){
                throw new CsvTsvException(path+": TSV에 헤더가 비어있습니다");
            }
            int headerCount = parser.getHeaderMap().size();
            Map<String,String> trimmedHeaders = new HashMap<>();
            for(String header : parser.getHeaderNames()){
                trimmedHeaders.put(header.trim(),header);
            }
            for (String target : WordUtils.TSV_TARGETS){
                if(!trimmedHeaders.containsKey(target)){
                    throw new CsvTsvException(path+": target header가 존재하지 않습니다.");
                }
            }

            for(CSVRecord record : parser){
                if(record.size() != headerCount){//header와 record의 column수가 다름
                    throw new CsvTsvException(path+": header와 record의 헤더 수가 다릅니다");
                }
                for (String column : WordUtils.TSV_TARGETS){
                    String originalHeader = trimmedHeaders.get(column);
                    String temp = record.get(originalHeader);
                    String[] words = temp.split(WordUtils.DELIMETER);
                    WordUtils.toWordStream(words)
                            .forEach(wordCount::addWord);
                }
            }//for문 끝
        }catch (UncheckedIOException e){
            throw new FileReadException(path + ": 파일 읽기 오류 / " + e.getMessage());
        } catch (IOException e){
            throw new FileReadException(path + ": 파일 읽기 오류 / " + e.getMessage());
        }
        return wordCount;
    }
}
