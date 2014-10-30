package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVReader
import org.apache.tika.metadata.HttpHeaders
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaMetadataKeys
import org.apache.tika.mime.MediaType
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.xml.sax.helpers.DefaultHandler

import java.util.jar.JarFile
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileService {

    def serviceMethod() {}

    def detectFormat(file){
        AutoDetectParser parser = new AutoDetectParser()
        parser.setParsers(new HashMap<MediaType, Parser>())

        Metadata metadata = new Metadata()
        metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, file.getName())

        InputStream stream = new FileInputStream(file)
        parser.parse(stream, new DefaultHandler(), metadata, new ParseContext())
        stream.close()

        metadata.get(HttpHeaders.CONTENT_TYPE)
    }

    def extractZip(File file){
        try {
            def basename = file.getName().substring(0, file.getName().lastIndexOf("."))
            def todir = file.getParentFile()
            log.info("Extracting " + file + " to " + todir)
            def jar = new JarFile(file)
            def enu = jar.entries()
            if(enu.hasMoreElements()){
                def entry = enu.nextElement()
                def entryPath = entry.getName()
                def istream = jar.getInputStream(entry)
                def outputFile = new File(todir, entryPath)
                def ostream = new FileOutputStream(outputFile)
                copyStream(istream, ostream)
                ostream.close()
                istream.close()
                [file:outputFile, success:true]
            } else {
                [file:null, success:false, message: "Empty zip file"]
            }
        } catch (Exception e){
            log.error(e.getMessage(),e)
            [file:null, success:false, message: "Problem extracting ZIP"]
        }
    }

    def zipFile(File file){
        // input file
        FileInputStream input = new FileInputStream(file);

        // out put file
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file.getAbsolutePath()+ ".zip") )

        // name the file inside the zip  file
        output.putNextEntry(new ZipEntry(file.getName()))

        // buffer size
        def b = new byte[1024]
        def count = -1

        while ((count = input.read(b)) > 0) {
            output.write(b, 0, count);
        }

        output.close()
        input.close()
    }

    def extractGZip(file){
        try {
            def basename = file.getName().substring(0, file.getName().lastIndexOf("."))
            def todir = file.getParentFile()
            def istream = new GZIPInputStream(new FileInputStream(file))
            println("Extracting " + file + " to " + todir)
            def outputFile = new File(todir, basename)
            def ostream = new FileOutputStream(outputFile)
            copyStream(istream, ostream)
            ostream.close()
            istream.close()
            [file:outputFile, success:true]
        } catch (Exception e){
            log.error(e.getMessage(),e)
            [file:null, success:false, message: "Problem extracting GZIP"]
        }
    }

    def copyStream(istream, ostream) {
        def bytes = new byte[1024]
        def len = -1
        while ((len = istream.read(bytes, 0, 1024)) != -1){
          ostream.write(bytes, 0, len)
        }
    }

    def getCSVReaderForText(String raw) {
        def separator = getSeparator(raw)
        def csvReader = new CSVReader(new StringReader(raw), separator.charAt(0))
        csvReader
    }

    def isCSVFile(file){
        //attempt to read a line....
        try {
            def rdr = new BufferedReader(new FileReader(file))
            def firstLine = rdr.readLine()
            def separator = getSeparator(firstLine)
            [success:true, separator:separator]
        } catch(Exception e){
            [success:false]
        }
    }

    def getSeparator(String raw) {
        int tabs = raw.count("\t")
        int commas = raw.count(",")
        if(tabs > commas)
            return '\t'
        else if(commas)
            return ','
        else
            null
    }

    def getSeparatorName(String raw) {
        int tabs = raw.count("\t")
        int commas = raw.count(",")
        if(tabs > commas)
            return "TAB"
        else
            return "COMMA"
    }
}
