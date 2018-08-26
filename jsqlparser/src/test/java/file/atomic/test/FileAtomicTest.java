package file.atomic.test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FileAtomicTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Path path = Paths.get("target.file");
                    try {
                        Files.createFile(path);
                        successCount.incrementAndGet();
                        System.out.println("success:" + successCount.get());
                    } catch (IOException e) {
                        failCount.incrementAndGet();
                        System.out.println("fail:" + failCount.get());
                    }
                }
            });
        }
    }

    public void createFileAtomicallyTest() {

    }

}
