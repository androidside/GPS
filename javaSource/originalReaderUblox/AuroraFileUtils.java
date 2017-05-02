/**
 * 
 */

package originalReaderUblox;

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
public abstract class AuroraFileUtils
{

//    public static File newestFile(File dir)
//    {
//        File ret = null;
//        File[] files = dir.listFiles();
//        if (files.length > 0)
//        {
//            ret = files[0];
//            for (int i = 1; i < files.length; i++)
//            {
//                if (ret.lastModified() < files[i].lastModified())
//                {
//                    ret = files[i];
//                }
//            }
//        }
//        return ret;
//    }
//
//    public static Collection<File> matchingFiles(File dir, String wildcardFilter)
//    {
//        return FileUtils.listFiles(dir, new WildcardFileFilter(wildcardFilter), null);
//    }
//
//    public static Collection<File> matchingFilesAndDirectories(File dir, String wildcardFilter)
//    {
//        return FileUtils.listFilesAndDirs(
//                dir,
//                new WildcardFileFilter(wildcardFilter),
//                new WildcardFileFilter(wildcardFilter));
//    }

    /**
     * Uses the fact mkdir on Linux is atomic.  Returns whether this call
     * created the lock or not
     * @param directoryName
     * @return
     * @throws IOException
     */
//    public static boolean createLinuxLockDirectory(File directoryName) throws IOException
//    {
//        ExecHelper exec = ExecHelper.exec(new String[] {"mkdir", directoryName.getAbsolutePath()}, null, 0);
//        return (exec.getStatus() == 0);
//    }

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

//    public static void diskUsage()
//    {
//        NumberFormat nf = NumberFormat.getNumberInstance();
//        for (Path root : FileSystems.getDefault().getRootDirectories())
//        {
//
//            System.out.print(root + ": ");
//            try
//            {
//                FileStore store = Files.getFileStore(root);
//                System.out.println(
//                        "available=" + nf.format(store.getUsableSpace()) + ", total="
//                                + nf.format(store.getTotalSpace()));
//            }
//            catch (IOException e)
//            {
//                System.out.println("error querying space: " + e.toString());
//            }
//        }
//    }

}
