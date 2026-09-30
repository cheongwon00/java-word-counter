package kr.sesac.wordcounter;

import kr.sesac.wordcounter.analyzer.*;
import kr.sesac.wordcounter.exception.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class Analyze {
    private String inputPath;//입력 경로
    private int tryCount;//시도 횟수
    private int successCount;//성공 파일 수
    private int failCount;//실패 파일 수
    private int skipCount;//지원하지 않는 확장자 파일 수
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
            switch (WordUtils.PARELLEL_SETTING){
                case GENERAL -> analyzeDirectory(p);
                case PARELLEL -> parallelAnalyzeDirectory(p);
            }
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
        int totalFileNum = 0;
        for (Path path : paths){//각 디렉토리마다 처리
            if(Files.isDirectory(path)) continue;//하위 폴더는 분석하지 않음(건너뜀 포함x)
            totalFileNum++;
            AnalyzeResult result = commonAnalyze(path);
            switch (result.getStatus()){
                case SUCCESS -> {
                    tryCount++;
                    wordCount.addAllWord(result.getWordCount());
                    successCount++;
                }
                case FAIL -> {
                    tryCount++;
                    failCount++;
                }
                case SKIP -> skipCount++;
            }
        }//for문 끝
        if (totalFileNum == skipCount){
            throw new UnsupportedFileException(p+": 디렉토리에 지원하는 확장자를 가진 파일이 없습니다.");
        }
        if(successCount == 0){
            allFail=true;
        }
    }

    private void parallelAnalyzeDirectory(Path p) {

        List<Path> paths;
        // 디렉토리 밑의 파일 경로들 가져오기
        try (Stream<Path> stream = Files.list(p)) {
            paths = stream
                    .filter(Files::isRegularFile)
                    .toList();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        ExecutorService executor =
                Executors.newFixedThreadPool(WordUtils.THREAD_POOL);
        List<Future<AnalyzeResult>> futures =
                new ArrayList<>();
        // 파일 분석 작업 제출
        for (Path path : paths) {
            Future<AnalyzeResult> future =
                    executor.submit(() -> commonAnalyze(path));
            futures.add(future);
        }
        try {
            // 분석 결과 받기
            for (Future<AnalyzeResult> future : futures) {
                try {
                    AnalyzeResult result = future.get();
                    switch (result.getStatus()) {
                        case SUCCESS -> {
                            tryCount++;
                            successCount++;
                            wordCount.addAllWord(result.getWordCount());
                        }
                        case FAIL -> {
                            tryCount++;
                            failCount++;
                        }
                        case SKIP -> {
                            skipCount++;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("파일 분석이 중단되었습니다.", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("파일 분석 중 예외가 발생했습니다.", e);
                }
            }
        } finally {
            executor.shutdown();
        }
        // 모든 파일이 SKIP
        if (paths.size() == skipCount) {
            throw new UnsupportedFileException(p + ": 디렉토리에 지원하는 확장자를 가진 파일이 없습니다.");
        }
        // 분석을 시도했지만 모두 실패
        if (successCount == 0) {
            allFail = true;
        }
    }

    private void analyzeFile(Path p){
        AnalyzeResult result = commonAnalyze(p);
        switch (result.getStatus()){
            case SUCCESS -> {
                tryCount++;
                wordCount = result.getWordCount();
                successCount++;
            }
            case SKIP ->{
                skipCount++;
                throw new UnsupportedFileException(p+": 지원하는 확장자는 .txt, .csv, .tsv, html입니다.");
            }
            case FAIL -> {
                tryCount++;
                failCount++;
            }
        }
        if(successCount == 0){
            allFail=true;
        }
    }

    private AnalyzeResult commonAnalyze(Path p){
        String fileName = p.getFileName().toString();
        int index = fileName.lastIndexOf(".");
        if(index == -1){//확장자가 없는 경우
            return new AnalyzeResult(new WordCount(),AnalyzeStatus.SKIP);
        }
        FileAnalyzer fileAnalyzer;
        String extension = fileName.substring(index+1).toLowerCase();
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
            case "html","htm" ->{
                fileAnalyzer = new HtmlFileAnalyzer();
            }
            default ->{
                return new AnalyzeResult(new WordCount(),AnalyzeStatus.SKIP);
            }
        }
        try {
            WordCount wc = fileAnalyzer.process(p);
            return new AnalyzeResult(wc,AnalyzeStatus.SUCCESS);
        }catch (FileReadException | CsvTsvException | InvalidHtmlException e){
            System.out.println(e.getMessage());
            return new AnalyzeResult(new WordCount(),AnalyzeStatus.FAIL);
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

    public boolean isAllFail() {
        return allFail;
    }

    public WordCount getWordCount() {
        return wordCount;
    }
}
