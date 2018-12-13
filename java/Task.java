package multithreading;

import java.util.concurrent.Callable;

public class Task<T> {
    private final Callable<? extends T> callable;
    private volatile T result = null;
    private volatile MyException exception = null;

    public Task(Callable<? extends T> callable) {
        this.callable = callable;
    }
    public T get() {
        T firstCheck = check();
        if (firstCheck == null) {
            synchronized (this) {
                T secondCheck = check();
                if (secondCheck == null) {
                    try {
                        result = callable.call();
                        return result;
                    } catch (Exception e) {
                        exception = new MyException("Exception in callabale.call()");
                        throw exception;
                    }
                }
                return secondCheck;
            }
        }
        return firstCheck;
    }

    private T check() {
        if (result != null) {
            return result;
        }
        else if (exception != null) {
            throw exception;
        }
        return null;
    }
}
class MyException extends RuntimeException{
    MyException (String message) {
        super(message);
    }

}