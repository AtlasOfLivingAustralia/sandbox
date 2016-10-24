package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVReader
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.ReaderInputStream
import org.apache.tika.metadata.HttpHeaders
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaMetadataKeys
import org.apache.tika.mime.MediaType
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.xml.sax.helpers.DefaultHandler

import java.nio.file.Paths
import java.util.jar.JarFile
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileService {

    public static final String TAB_SEPARATOR = 'TAB'
    public static final String COMMA_SEPARATOR = 'COMMA'
    def grailsApplication

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
        File outputFile = new File("${file.absolutePath}.zip")

        zipStreamToFile(outputFile, file.name, input)
    }

    def zipStreamToFile(File outputZipFile, String inputFileName, InputStream input) {
        // out put file
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outputZipFile) )

        // name the file inside the zip  file
        output.putNextEntry(new ZipEntry(inputFileName))

        input.withStream {
            output.withStream {
                output << input
            }
        }
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

    def getFileForFileId(String fileId) {
        new File(new File(grailsApplication.config.uploadFilePath, fileId), "${fileId}.csv")
    }

    def getCSVReaderForFile(File file) {
        new CSVReader(file.newReader('UTF-8'), getSeparator(file).charAt(0))
    }

    def getCSVReaderForText(String raw) {
        def separator = getSeparator(raw)
        def csvReader = new CSVReader(new StringReader(raw), separator.charAt(0))
        csvReader
    }

    def getSeparator(File file) {
        int tabs = 0, commas = 0;
        file.withReader('UTF-8') { fr ->
            fr.eachLine { line ->
                tabs += line.count('\t')
                commas += line.count(',')
            }
        }
        return tabs > commas ? '\t' : commas > 0 ? ',': null
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

    def separatorName(String separator) {
        separator == '\t' ? TAB_SEPARATOR : COMMA_SEPARATOR
    }

    def getSeparatorName(String raw) {
        separatorName(getSeparator(raw))
    }

    def getSeparatorName(File file) {
        separatorName(getSeparator(file))
    }

    def saveArchiveCopy(String uid, Reader input, String headers, boolean firstLineIsData, String separatorName) {
        final archiveDir = new File(grailsApplication.config.uploadFilePath, 'archive')
        final archiveFile = new File(archiveDir, "${uid}.csv.zip")
        FileUtils.forceMkdir(archiveDir)

        if (separatorName == TAB_SEPARATOR) {
            headers = headers.replaceAll(',','\t')
        }

        headers += System.lineSeparator()

        if (!firstLineIsData) {
            try {
                def firstLine = input.readLine()
                log.debug("Throwing away $firstLine")
            } catch (e) {
                IOUtils.closeQuietly(input)
                throw e
            }
        }

        MultiReader combinedReader = new MultiReader(new StringReader(headers), input)
        zipStreamToFile(archiveFile, "${uid}.csv", new ReaderInputStream(combinedReader, 'UTF-8'))
    }

    File getArchiveCopy(String uid) {
        Paths.get(grailsApplication.config.uploadFilePath, 'archive', "${uid}.csv.zip").toFile()
    }
}
