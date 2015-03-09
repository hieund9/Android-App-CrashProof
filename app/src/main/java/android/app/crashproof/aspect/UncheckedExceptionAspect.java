package android.app.crashproof.aspect;

import android.util.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.io.PrintWriter;
import java.io.StringWriter;

@Aspect
public final class UncheckedExceptionAspect {
    private static final String TAG = UncheckedExceptionAspect.class.getName();

    private final class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable e) {
            // Here we provide a sample logic for showing the exception in Console.
            // You should probably customize it.
            StringWriter stringWriter = new StringWriter();
            PrintWriter  printWriter  = new PrintWriter(stringWriter);

            e.printStackTrace(printWriter);

            Log.e(TAG, "This exception has crashed the app: " + e.toString() + "\n" + stringWriter.toString());

            printWriter.close();
        }
    }

    // You should customize your Application package name here
    @Pointcut("execution(* android.app.crashproof.MyApplication.onCreate(..))")
    void applicationOnCreate() {
        // Defines a pointcut to intercept Application.onCreate()
    }

    @Before("applicationOnCreate()")
    public void advise(JoinPoint joinPoint) {
        // Intercepts Application.onCreate() to set the default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    }

    // You should customize your package name here
    @Pointcut("execution(* android.app.crashproof..*.*(..))")
    void methodExecution() {
        // Defines a pointcut to intercept all methods under com.mpayme package and sub-packages.
    }

    // You should customize your package name here
    @Pointcut("execution(android.app.crashproof..*.new(..))")
    void constructorExecution() {
        // Defines a pointcut to intercept all constructors under com.mpayme package and sub-packages.
    }

    // You should customize the package name of this aspect class here
    @Pointcut("within(android.app.crashproof.aspect..*)")
    void inAspectPackage() {
        // Defines a pointcut that represents this package.
    }

    @Around("(methodExecution() || constructorExecution()) && !inAspectPackage()")
    public Object advise(final ProceedingJoinPoint joinPoint) throws Throwable {
        // Intercepts all methods and constructors under com.project pacakge and sub-packages but not within this package.
        try {
            return joinPoint.proceed();
        } catch (final NullPointerException e) {
            log(joinPoint, e);
        // You may also catch other unchecked exceptions here if your code does not rely on these exceptions
        }/* catch (ArithmeticException e) {
            log(joinPoint, e);
        } catch (ArrayStoreException e) {
            log(joinPoint, e);
        } catch (BufferOverflowException e) {
            log(joinPoint, e);
        } catch (BufferUnderflowException e) {
            log(joinPoint, e);
        } catch (ClassCastException e) {
            log(joinPoint, e);
        } catch (ConcurrentModificationException e) {
            log(joinPoint, e);
        } catch (DOMException e) {
            log(joinPoint, e);
        } catch (EmptyStackException e) {
            log(joinPoint, e);
        } catch (IllegalArgumentException e) {
            log(joinPoint, e);
        } catch (IllegalMonitorStateException e) {
            log(joinPoint, e);
        } catch (IllegalStateException e) {
            log(joinPoint, e);
        } catch (IndexOutOfBoundsException e) {
            log(joinPoint, e);
        } catch (MissingResourceException e) {
            log(joinPoint, e);
        } catch (NegativeArraySizeException e) {
            log(joinPoint, e);
        } catch (NoSuchElementException e) {
            log(joinPoint, e);
        } catch (ProviderException e) {
            log(joinPoint, e);
        } catch (SecurityException e) {
            log(joinPoint, e);
        } catch (UndeclaredThrowableException e) {
            log(joinPoint, e);
        } catch (UnsupportedOperationException e) {
            log(joinPoint, e);
        }*/

        return null;
    }

    private static void log(final ProceedingJoinPoint joinPoint, final Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter  printWriter  = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);

        Log.e(joinPoint.getTarget().getClass().getName(), "This exception has been swallowed: " + e.toString() + "\n" + stringWriter.toString());

        printWriter.close();
    }
}
