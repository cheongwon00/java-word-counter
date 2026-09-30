package kr.sesac.wordcounter.analyzer;

import kr.sesac.wordcounter.WordCount;

import java.nio.file.Path;

public interface FileAnalyzer {
    WordCount process(Path path);
}
