package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.WordUtils;
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

public class HtmlFileAnalyzer implements FileAnalyzer{

    @Override
    public WordCount process(Path path) {
        WordCount wordCount = new WordCount();

        try {
            Document document = Jsoup.parse(path.toFile(), StandardCharsets.UTF_8.name());
            Elements matches = document.select(WordUtils.HTML_TARGET);
            if(matches.size() != 1){//content가 1개가 아닌경우
                throw new InvalidHtmlException(path+": 본문 요소는 정확히 하나여야 합니다.");
            }

            Element content = matches.first();
            content.select("script, style, nav, header, footer").remove();
            String line = content.text();
            String[] words = line.split(WordUtils.DELIMETER);
            WordUtils.toWordStream(words)
                    .forEach(wordCount::addWord);
        } catch (IOException e) {
            throw new FileReadException(path + ": 파일 읽기 오류 / " + e.getMessage());
        }
        return wordCount;
    }
}
