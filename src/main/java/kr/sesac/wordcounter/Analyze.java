package kr.sesac.wordcounter;

import kr.sesac.wordcounter.analyzer.*;
import kr.sesac.wordcounter.exception.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Analyze {
    private String inputPath;//입력 경로
    private int tryCount;//시도 횟수
    private int successCount;//성공 파일 수
    private int failCount;//실패 파일 수
    private int skipCount;//지원하지 않는 확장자 파일 수
    private AnalyzerDTO analyzerDTO = new AnalyzerDTO();
    private WordCount wordCount = new WordCount();
    private boolean allFail = false;
    private double elapsedTime;

    public void analyze(String path) throws FileNotFoundException {
        Path p = Path.of(path);

        if(!Files.exists(p))
            throw new FileNotFoundException("경로를 찾을 수 없습니다: " + path);

        inputPath = path;
        if(Files.isDirectory(p)) {//입력 경로가 디렉토리인 경우
            long startTime = System.nanoTime();
            analyzeDirectory(p);
            long endTime = System.nanoTime();
            elapsedTime = (endTime - startTime) / 1_000_000.0;
        }
        else{//입력 경로가 디렉토리가 아닌 경우
            long startTime = System.nanoTime();
            analyzeFile(p);
            long endTime = System.nanoTime();
            elapsedTime = (endTime - startTime) / 1_000_000.0;
        }
    }

    private void analyzeDirectory(Path p){
        //디렉토리 밑에 파일들 경로들 저장
        List<Path> paths;
        try (Stream<Path> stream = Files.list(p)) {
            paths = stream.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int totalFileNum = paths.size();

        for (Path path : paths){//각 디렉토리마다 처리
            tryCount++;
            String fileName = path.getFileName().toString();
            int index = fileName.lastIndexOf(".");
            if(index == -1){//확장자가 없는 경우
                skipCount++;
                continue;
            }
            //확장자(소문자로)
            String extension = fileName.substring(index+1).toLowerCase();
            FileAnalyzer fileAnalyzer;
            switch (extension){//확장자에 따른 analyzer 주입
                case "txt" ->{
                    fileAnalyzer = new TxtFileAnalyzer();
                }
                case "csv" ->{
                    fileAnalyzer = new CsvFileAnalyzer();
                }
                case "tsv" ->{
                    fileAnalyzer = new TsvFileAnalyzer();
                }
                case "html" ->{
                    fileAnalyzer = new HtmlFileAnalyzer();
                }
                default ->{
                    skipCount++;
                    continue;
                }
            }//switch 끝
            try {
                AnalyzerDTO temp = fileAnalyzer.process(path);
                wordCount.addAllWord(temp.getWordCount());
                successCount++;
            }catch (FileReadException e){
                failCount++;
            }catch (DifferentColumnException e){//csv 헤더, 레코드 칼럼수 다름
                failCount++;
            } catch (EmptyCsvTsvException e){//빈 csv 파일
                failCount++;
            } catch (MissingTargetHeaderException e){//target 헤더가 존재x
                failCount++;
            } catch (InvalidHtmlException e){//html content가 1개가 아님
                failCount++;
            }
        }//for문 끝
        if (totalFileNum == skipCount){
            throw new UnsupportedFileException("디렉토리에 지원하는 확장자를 가진 파일이 없습니다.");
        }
        if(totalFileNum == failCount){
            allFail=true;
        }
        analyzerDTO.setWordCount(wordCount);
        analyzerDTO.setTotalWordCount(wordCount.getTotalWord());
        analyzerDTO.setDifferentWordCount(wordCount.getWordCount().size());
    }//analyzeDirectory 메서드 끝

    private void analyzeFile(Path p){
        tryCount++;
        String fileName = p.getFileName().toString();
        int index = fileName.lastIndexOf(".");
        if(index == -1){//확장자가 없는 경우
            throw new UnsupportedFileException("지원하는 확장자는 .txt, .csv, .tsv, html입니다.");
        }
        String extension = fileName.substring(index+1).toLowerCase();
        FileAnalyzer fileAnalyzer;
        switch (extension){
            case "txt" ->{
                fileAnalyzer = new TxtFileAnalyzer();
            }
            case "csv" ->{
                fileAnalyzer = new CsvFileAnalyzer();
            }
            case "tsv" ->{
                fileAnalyzer = new TsvFileAnalyzer();
            }
            case "html" ->{
                fileAnalyzer = new HtmlFileAnalyzer();
            }
            default ->{
                throw new UnsupportedFileException("지원하는 확장자는 .txt, .csv, .tsv, html입니다.");
            }
        }//switch 끝
        try {
            analyzerDTO = fileAnalyzer.process(p);
            wordCount = analyzerDTO.getWordCount();
            successCount++;
        }catch (FileReadException e){
            allFail=true;
            failCount++;
        }catch (DifferentColumnException e){//csv 헤더, 레코드 칼럼수 다름
            allFail=true;
            failCount++;
        } catch (EmptyCsvTsvException e){//빈 csv 파일
            allFail=true;
            failCount++;
        } catch (MissingTargetHeaderException e){//target 헤더가 존재x
            allFail=true;
            failCount++;
        } catch (InvalidHtmlException e){//html content가 1개가 아님
            allFail=true;
            failCount++;
        }
    }

    public String getInputPath() {
        return inputPath;
    }

    public int getTryCount() {
        return tryCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public AnalyzerDTO getAnalyzerDTO() {
        return analyzerDTO;
    }

    public boolean isAllFail() {
        return allFail;
    }
}
