package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVWriter
import org.apache.commons.io.FileUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.web.multipart.MultipartFile

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

//        def dataResourceUid = params.dataResourceUid
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
            String extractedString = tikaService.parseFile(newFile)
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

    def http = HttpClients.createDefault()

    def reloadUidCaches() {
//        def http = new HttpClient()
        //reference the UID caches
        def get = new HttpGet(grailsApplication.config.sandboxHubsWebapp + "/occurrences/refreshUidCache")
        http.execute(get)
    }

    String downloadUrl(String uid) {
        grailsApplication.config.biocacheServiceUrl + "/occurrences/index/download?reasonTypeId=" + grailsApplication.config.downloadReasonId + "&q=data_resource_uid:" + uid + "&" + grailsApplication.config.biocacheServiceDownloadParams
    }

    String spatialPortalUrl(String uid) {
        grailsApplication.config.spatialPortalUrl + "?q=data_resource_uid:" + uid + grailsApplication.config.spatialPortalUrlOptions
    }

    String biocacheUrl(String uid) {
        grailsApplication.config.sandboxHubsWebapp + "/occurrences/search?q=data_resource_uid:" + uid
    }
}
