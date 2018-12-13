package multithreading;

public class MainExecutionManager {
    public static void main(String[] args) throws InterruptedException {
        long start1 = System.nanoTime();
        for (int i = 0; i < 400; i++) {
            for (int j = 0; j < 10_000; j++) {
                Math.tan(j);
            }
        }
        System.out.println("delta1" + (System.nanoTime() - start1)/1_000_000);
        ExecutionManager executionManager = new ExecutionManagerImpl();
        long start = System.nanoTime();
        Runnable[] futures = new Runnable[400];
        for (int i = 0; i < 400; i++) {
            futures[i] = (() -> {
                for (int j = 0; j < 10_000; j++) {
                    Math.tan(j) ;
                }
            });
        }
        Runnable callback = () -> {
                System.out.println("END");
        };
        Context context = executionManager.execute(callback, futures);
        for (int i = 0; i < 100; i++) {
            System.out.println(context.getCompletedTaskCount());
            System.out.println(context.isFinished());
        }
        long finish = System.nanoTime();
        Thread.sleep(1000);
        System.out.println("delta2" + (finish - start)/1_000_000);
    }
}
