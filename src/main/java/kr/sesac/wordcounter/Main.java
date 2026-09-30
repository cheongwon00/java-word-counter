package kr.sesac.wordcounter;

import kr.sesac.wordcounter.exception.DefaultValueException;
import kr.sesac.wordcounter.exception.UnsupportedFileException;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Map<String,Long> wordCount = new LinkedHashMap<>();
        StringBuilder summary = new StringBuilder();
        boolean complete = false;

        //처음 선택 옵션
        StringBuilder sb = new StringBuilder();
        sb
                .append("문서 단어 분석기\n")
                .append("1. 새 분석 시작\n")
                .append("2. 상위 N개 단어 보기\n")
                .append("3. 특정 단어 횟수 찾기\n")
                .append("4. 전체 결과 저장\n")
                .append("5. 최근 분석 요약 보기\n")
                .append("0. 종료");
        System.out.println(sb);

        int commandNumber;
        boolean end = false;
        while(!end){
            commandNumber = readCommandNumber(scanner,"선택 > ");
            switch (commandNumber){
                case 1 ->{
                    while(true){
                        try {
                            Analyze analyze = newAnalyze(scanner);
                            summary = new StringBuilder();
                            summary
                                    .append("\n분석 완료\n")
                                    .append("입력: " + analyze.getInputPath() + "\n")
                                    .append("파일: 시도 " + analyze.getTryCount() + "개 / 성공 " + analyze.getSuccessCount() + "개 / 실패 " +analyze.getFailCount() + "개 / 지원하지 않아 건너뜀 " + analyze.getSkipCount() + "개\n")
                                    .append("전체 단어: " + analyze.getWordCount().getTotalWord() + "개 / 서로 다른 단어: " + analyze.getWordCount().getDifferentWord() + "개\n")
                                    .append("처리 시간: " + String.format("%.1fms",analyze.getElapsedTime()) + "\n");
                            System.out.println(summary);
                            wordCount = analyze.getWordCount().sortWordCounter();
                            complete = true;
                            if (analyze.isAllFail())
                                complete = false;
                            break;
                        }catch (UnsupportedFileException e){
                            System.out.println(e.getMessage());
                        }
                    }
                }
                case 2 ->{
                    if(!complete){
                        System.out.println("이전에 분석한 파일이 없습니다.");
                        continue;
                    }
                    topNWords(scanner,wordCount);
                }
                case 3 ->{
                    if(!complete){
                        System.out.println("이전에 분석한 파일이 없습니다.");
                        continue;
                    }
                    findWord(scanner,wordCount);
                }
                case 4 ->{
                    if(!complete){
                        System.out.println("이전에 분석한 파일이 없습니다.");
                        continue;
                    }
                    saveResult(wordCount);
                }
                case 5 ->{
                    if(summary.isEmpty()){
                        System.out.println("이전 분석 결과가 없습니다.");
                        continue;
                    }
                    System.out.println(summary);
                }
                case 0 ->{
                    System.out.println("프로그램을 종료합니다.");
                    end=true;
                }
            }
        }

    }

    private static String readText(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readCommandNumber(Scanner scanner, String prompt){
        int num = -1;
        while (true){
            String text = readText(scanner,prompt);
            try {
                num = Integer.parseInt(text);
            }catch (NumberFormatException e){
                System.out.println("메뉴 번호에 해당하는 값을 입력하시오");
                continue;
            }
            if((num<0) || (num>5)){
                System.out.println("메뉴 번호에 해당하는 값을 입력하시오");
            }else break;
        }
        return num;
    }

    private static Analyze newAnalyze(Scanner scanner){
        Analyze analyze = new Analyze();
        while (true){
            String path = readText(scanner,"파일 또는 폴더 경로 > ");
            try {
                analyze.analyze(path);
                break;
            }catch (FileNotFoundException e){
                System.out.println(e.getMessage());
            }
        }
        return analyze;
    }

    private static void topNWords(Scanner scanner, Map<String,Long> wordCount){
        int topN = 10;
        while(true){
            String input = readText(scanner,"몇 개를 볼까요? (기본 10) > ");
            try {
                if(input.isEmpty()){
                    throw new DefaultValueException("");
                }
                topN = Integer.parseInt(input);
                if(topN>=1) break;
                System.out.println("1 이상의 정수를 입력하세요.");
            }catch (NumberFormatException e){
                System.out.println("1 이상의 정수를 입력하세요.");
            }catch (DefaultValueException e){
                topN = 10;
                break;
            }
        }
        int min = Math.min(topN,wordCount.size());
        int i=1;
        for (String word : wordCount.keySet()){
            if(i>min) break;
            System.out.println(i + ". " + word + " : " + wordCount.get(word) + "회");
            i++;
        }
    }

    private static void findWord(Scanner scanner,Map<String,Long> wordCount){
        while(true) {
            String input = readText(scanner, "찾을 단어 > ");
            String[] words = input.split(WordUtils.DELIMETER);
            List<String> list = WordUtils.toWordStream(words)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (list.size() != 1) {
                System.out.println("단어 하나를 입력하세요.");
                continue;
            }
            String word = list.getFirst();
            System.out.println(word + " : " + wordCount.getOrDefault(word,0L) + "회");
            break;
        }
    }

    private static void saveResult(Map<String,Long> wordCount){
        Path outPath = Path.of("out/counts.tsv");
        try {
            Files.createDirectories(outPath.getParent());
        } catch (IOException e) {
            System.out.println("out/counts.tsv 파일 생성에 실패했습니다.");
        }
        try (BufferedWriter writer =
                     Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)){
            writer.write("word\tcount\n");
            for (String word : wordCount.keySet()){
                writer.write(word + "\t" + wordCount.get(word) + "\n");
            }
            System.out.println("전체 결과 " + wordCount.size() + "개 단어를 " + outPath.toString() + "에 저장했습니다.");
        }catch (IOException e){
            System.out.println("out/counts.tsv 파일 쓰기에 실패했습니다");
        }
    }
}
