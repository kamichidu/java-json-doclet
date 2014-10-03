package jp.michikusa.chitose.doclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DocletOption
{
    public static int getOptionLength(String option)
    {
        if("-ofile".equals(option))
        {
            return 2;
        }
        else if("-append".equals(option))
        {
            return 1;
        }
        else if("-pretty".equals(option))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public DocletOption(String[][] options)
    {
        for(final String[] pair : options)
        {
            if("-ofile".equals(pair[0]))
            {
                this.outputFilename = new File( pair[1]);
            }
            else if("-append".equals(pair[0]))
            {
                this.appendMode = true;
            }
            else if("-pretty".equals(pair[0]))
            {
                this.pretty = true;
            }
        }

        if(this.outputFilename == null)
        {
            throw new IllegalArgumentException("missing `-ofile' argument.");
        }
    }

    public OutputStream openOutputStream()
        throws IOException
    {
        return new FileOutputStream(this.outputFilename, this.appendMode);
    }

    public boolean isPretty()
    {
        return this.pretty;
    }

    private File outputFilename;

    private boolean appendMode;

    private boolean pretty;
}
