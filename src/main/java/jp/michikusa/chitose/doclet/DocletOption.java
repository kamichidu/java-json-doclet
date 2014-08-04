package jp.michikusa.chitose.doclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DocletOption {
    public static int getOptionLength( String option) {
        switch ( option) {
            case "-ofile":
                return 2;
            case "-append":
                return 1;
            case "-pretty":
                return 1;
        }
        return 0;
    }

    public DocletOption( String[][] options) {
        for ( final String[] pair : options) {
            switch ( pair[0]) {
                case "-ofile":
                    this.outputFilename = new File( pair[1]);
                    break;
                case "-append":
                    this.appendMode = true;
                    break;
                case "-pretty":
                    this.pretty = true;
                    break;
            }
        }

        if ( this.outputFilename == null) {
            throw new IllegalArgumentException( "missing `-ofile' argument.");
        }
    }

    public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream( this.outputFilename, this.appendMode);
    }

    public boolean isPretty() {
        return this.pretty;
    }

    private File outputFilename;

    private boolean appendMode;

    private boolean pretty;
}
