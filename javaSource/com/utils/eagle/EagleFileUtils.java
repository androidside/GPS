/**
 * 
 */

package com.utils.eagle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;

//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * <P>
 * This code was developed by NASA, Goddard Space Flight Center, Code 665.
 * 
 * @version $Date: 2010/05/26 16:18:34 $
 * @author smaher
 */
public abstract class EagleFileUtils
{

    public static String readCRLFLine(InputStream inStream) throws IOException
    {
        StringBuffer strbuf = new StringBuffer();
        int c;
        byte readBuf[] = new byte[1];

        boolean done = false;
        while (done == false)
        {
            c = inStream.read(readBuf, 0, 1);
            //            System.out.println("C = "+c+" and readBuf = "+readBuf[0]);
            if (c > 0)
            {
                if (readBuf[0] != '\r' && readBuf[0] != '\n')
                {
                    strbuf.append((char) readBuf[0]);
                }

                if (readBuf[0] == '\n')
                {
                    done = true;
                }
            }
            else if (c < 0)
            {
                throw new IOException("Stream closed");
            }

        }
        return strbuf.toString();
    }

}
