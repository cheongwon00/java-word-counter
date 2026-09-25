package kr.sesac.wordcounter.analyzer;

import java.nio.file.Path;

public interface FileAnalyzer {
    AnalyzerDTO process(Path path);
}
