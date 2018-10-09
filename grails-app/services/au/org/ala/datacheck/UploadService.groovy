package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVWriter
import org.apache.commons.io.FileUtils
import org.apache.http.impl.client.HttpClients
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.web.multipart.MultipartFile

import javax.xml.transform.OutputKeys
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult

class UploadService {
    final static String OCCURRENCE_ID_COLUMN = "occurrenceID"
    final static String CATALOG_NUMBER_COLUMN = "catalogNumber"

    def grailsApplication
    def fileService

    /**
     * This function chooses a key field. It chooses occurrenceID if present, otherwise catalogNumber.
     * @param headers
     * @return
     */
    String getKeyFieldFromHeader(String[] headers){
        String key = headers?.find { it == OCCURRENCE_ID_COLUMN}
        if(!key) {
            key = headers?.find { it == CATALOG_NUMBER_COLUMN}
        }

        key
    }

    static class UploadException extends RuntimeException {
        UploadException(String message) {
            super(message)
        }
    }

    Map<String, ?> uploadFile(String dataResourceUid, MultipartFile f) {
        if (f.empty) {
            throw new UploadException('file cannot be empty')
        }

        if (dataResourceUid) {
            log.info "Loading data resource ${dataResourceUid}"
        }

        def fileId = UUID.randomUUID().toString()
        def uploadDir = new File(grailsApplication.config.uploadFilePath, fileId)
        log.debug "Creating upload directory $uploadDir"
        FileUtils.forceMkdir(uploadDir)

        log.debug "Transferring file to directory..."
        def newFile = new File(uploadDir, f.originalFilename)
        f.transferTo(newFile)

        log.debug "Detecting file formats..."
        def contentType = fileService.detectFormat(newFile)

        log.debug "Content type.... $contentType"
        //if its GZIPPED or ZIPPED extract the file
        if (contentType == "application/zip") {
            //upzip it
            def result = fileService.extractZip(newFile)
            if (result.success) {
                newFile = result.file
                contentType = fileService.detectFormat(result.file)
            } else {
                throw new UploadException(result.message)
            }
        } else if (contentType == "application/x-gzip") {
            def result = fileService.extractGZip(newFile)
            if (result.success) {
                newFile = result.file
                contentType = fileService.detectFormat(result.file)
            } else {
                throw new UploadException(result.message)
            }
        }

        def extractedFile = new File(uploadDir, fileId + '.csv')

        if (contentType.startsWith("text")) {
            //extract and re-write into common CSV format
            log.debug("Is a CSV....")
            FileUtils.copyFile(newFile, extractedFile)
        } else {
            //extract the data
            def csvWriter = new CSVWriter(new FileWriter(extractedFile))
            String extractedString = parseFile(newFile)
            //HTML version of the file
            Document doc = Jsoup.parse(extractedString)
            Elements dataTable = doc.select("tr")

            if (dataTable?.size() > 0) {
                def cellCount = dataTable?.get(0)?.select("td")?.size() ?: 0
                dataTable.each { tr ->
                    def cells = tr.select("td")
                    def fields = new String[cellCount]
                    if (cells) {
                        cells.eachWithIndex { cell, idx ->
                            if (idx < cellCount) {
                                fields[idx] = cell.text().trim()
                            }
                        }
                    }
                    csvWriter.writeNext(fields)
                }
                csvWriter.close()
            }
        }

        //create a zipped version for uploading
        fileService.zipFile(extractedFile)

        return [fileId: fileId, fileName: newFile.name]
    }

    String downloadUrl(String uid) {
        grailsApplication.config.biocacheService.baseURL + "/occurrences/index/download?reasonTypeId=" + grailsApplication.config.downloadReasonId + "&q=data_resource_uid:" + uid + "&" + grailsApplication.config.biocacheServiceDownloadParams
    }

    String downloadOfflineUrl(String uid) {
        grailsApplication.config.biocacheService.baseURL + "/download?downloadType=records&reasonTypeId=${grailsApplication.config.downloadReasonId ?: '10'}&downloadFormat=${grailsApplication.config.downloadFormat ?: 'dwc'}&fileType=${grailsApplication.config.downloadFileType ?: 'csv'}&searchParams=${URLEncoder.encode("?q=data_resource_uid:" + uid, "UTF-8")}&targetUri=${URLEncoder.encode(biocacheUrl(uid), "UTF-8")}"
    }

    String spatialPortalUrl(String uid) {
        grailsApplication.config.spatial.baseURL + "?q=data_resource_uid:" + uid + grailsApplication.config.spatialPortalUrlOptions
    }

    String biocacheUrl(String uid) {
        grailsApplication.config.biocache.baseURL + "/occurrences/search?q=data_resource_uid:" + uid
    }

    /**
     * Parse a file and return the content and metadata that Apache Tika has found
     * through its parsers as an XML string.
     *
     * https://github.com/dewarim/tikaParser/blob/master/grails-app/services/tikaParser/TikaService.groovy
     *
     * @param file the file to parse
     * @param tikaConfig a TikaConfig instance
     * @param metadata a TikaMetadata instance
     * @return an XML string which contains an XHTML document with metadata in the head and
     * content data in the body section.
     */
    String parseFile(File file) {
        TikaConfig tikaConfig = new TikaConfig()
        Metadata metadata = new Metadata()
        SAXTransformerFactory factory = SAXTransformerFactory.newInstance()
        TransformerHandler handler = factory.newTransformerHandler()
        handler.transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        handler.transformer.setOutputProperty(OutputKeys.INDENT, "yes")

        StringWriter sw = new StringWriter()
        handler.result = new StreamResult(sw)

        Parser parser = new AutoDetectParser(tikaConfig)
        ParseContext pc = new ParseContext()
        try {
            parser.parse(new FileInputStream(file), handler, metadata, pc)
            return sw.toString()
        } catch (Exception e) {
            log.error("Failed to parse file ${file.absolutePath}", e)
            throw e
        }
    }
}
