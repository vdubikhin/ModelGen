package modelgen.shared;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Logger {

    private Logger() {}

    static public void debugPrintln(String string, int printLevel) {
        if (printLevel > 0)
            System.out.println(string);
    }

    static public void errorLogger(String logString) {
        System.out.println(logString);
    }

    static public void errorLoggerTrace(String logString, Throwable e) {
        System.out.println(logString);
        
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        
        System.out.println(exceptionAsString);
    };
    
    
}
