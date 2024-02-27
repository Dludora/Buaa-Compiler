package front.Error;

import java.util.ArrayList;
import java.util.Collections;

public class ErrorRecorder {
    public static ArrayList<Error> errors = new ArrayList<>();

    public static void putError(ErrorType errorType, int lineNum) {
        errors.add(new Error(errorType, lineNum));
    }

    public static void PrintErrors() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(errors);
        for (Error error: errors) {
            sb.append(error).append("\n");
        }

        System.out.println(sb);
    }
}
