package com.eaiman.multipart.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    // this is for multiple file upload
    @Override
    public Flux<String> getLines(Flux<FilePart> filePartFlux) {

        return filePartFlux.flatMap(this::getLines);
    }

    // this is for single file upload
    @Override
    public Flux<String> getLines(Mono<FilePart> filePartMono) {

        return filePartMono
                .flatMap(this::saveFile)
//                .flatMapMany(this::getLines);
                .flatMapMany(this::getLinesForExcel);
    }

    private Mono<String> saveFile(FilePart filePart) {
        System.out.println("handling file upload - " + filePart.filename());

        // if a file with the same name already exists in a repository, delete and recreate it
        final String filename = filePart.filename();
        File file = new File(filename);
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            return Mono.error(e); // if creating a new file fails return an error
        }

        var filePath = file.getAbsolutePath();

        try {
            // create an async file channel to store the file on disk
            final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE);

            final CloseCondition closeCondition = new CloseCondition();

            // pointer to the end of file offset
            AtomicInteger fileWriteOffset = new AtomicInteger(0);
            // error signal
            AtomicBoolean errorFlag = new AtomicBoolean(false);

            System.out.println("subscribing to file parts");
            // FilePart.content produces a flux of data buffers, each need to be written to the file
            return filePart.content().doOnEach(dataBufferSignal -> {
                if (dataBufferSignal.hasValue() && !errorFlag.get()) {
                    // read data from the incoming data buffer into a file array
                    DataBuffer dataBuffer = dataBufferSignal.get();
                    int count = dataBuffer.readableByteCount();
                    byte[] bytes = new byte[count];
                    dataBuffer.read(bytes);

                    // create a file channel compatible byte buffer
                    final ByteBuffer byteBuffer = ByteBuffer.allocate(count);
                    byteBuffer.put(bytes);
                    byteBuffer.flip();

                    // get the current write offset and increment by the buffer size
                    final int filePartOffset = fileWriteOffset.getAndAdd(count);
                    System.out.println("processing file part at offset - " + filePartOffset);
                    // write the buffer to disk
                    closeCondition.onTaskSubmitted();
                    fileChannel.write(byteBuffer, filePartOffset, null, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            // file part successfuly written to disk, clean up
                            System.out.println("done saving file part - " + filePartOffset);
                            byteBuffer.clear();

                            if (closeCondition.onTaskCompleted())
                                try {
                                    System.out.println("closing after last part");
                                    fileChannel.close();
                                } catch (IOException ignored) {
                                    ignored.printStackTrace();
                                }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            // there as an error while writing to disk, set an error flag
                            errorFlag.set(true);
                            System.out.println("error saving file part - " + filePartOffset);
                        }
                    });
                }
            }).doOnComplete(() -> {
                // all done, close the file channel
                System.out.println("done processing file parts");
                if (closeCondition.canCloseOnComplete())
                    try {
                        System.out.println("closing after complete");
                        fileChannel.close();
                    } catch (IOException ignored) {
                    }

            }).doOnError(t -> {
                // ooops there was an error
                System.out.println("error processing file parts");
                try {
                    fileChannel.close();
                } catch (IOException ignored) {
                }
                // take last, map to a status string
            })
                    .last()
                    .map(dataBuffer -> {
                        System.out.println(filePart.filename() + " " + (errorFlag.get() ? "error" : "uploaded"));
                        return filePath;
                    });
        } catch (IOException e) {
            // unable to open the file channel, return an error
            System.out.println("error opening the file channel");
            return Mono.error(e);
        }
    }

    @Override
    public Flux<String> getLines(FilePart filePart) {

        return filePart.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .collectList()
                .map(list -> String.join("", list))
                .map(string -> {
                    var text = new StringBuilder("");
                    try (var inp = new ByteArrayInputStream(string.getBytes())) {
                        System.out.println(string);
                        System.out.println("----------------------------------------");
                        System.out.println(inp.available());
                        var d = OPCPackage.open(inp);
                        var wb = new XSSFWorkbook(d);

                        List<String> sheetNames = List.of("Feuil1");
                        List<XSSFSheet> sheets = new ArrayList<>();
                        List<XSSFSheet> finalSheets = sheets;
                        sheetNames.forEach(nm -> finalSheets.add(wb.getSheet(nm)));
                        sheets = sheets.stream().filter(Objects::nonNull).collect(Collectors.toList());

                        System.out.println(sheets.size());
                        System.out.println(sheets.get(0).getPhysicalNumberOfRows());

                        sheets.forEach(sheet -> {
                            var extractor = new XSSFExcelExtractor(sheet.getWorkbook());
                            extractor.setFormulasNotResults(true);
                            extractor.setIncludeSheetNames(false);
                            text.append(extractor.getText());
                        });


                        inp.close();
                        wb.close();
                    } catch (IOException | InvalidFormatException e) {
                        e.printStackTrace();
                    }

                    return text.toString();
                })
                .map(this::processAndGetLinesAsList)
                .flatMapIterable(Function.identity());
    }

    // this is for single file upload
//    @Override
//    public Flux<String> getLines(FilePart filePart) {
//        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
//        return filePart.content()
//                .map(dataBuffer -> {
//                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
//                    dataBuffer.read(bytes);
//                    DataBufferUtils.release(dataBuffer);
//
//                    return new String(bytes, StandardCharsets.UTF_8);
//                })
//                .map(this::processAndGetLinesAsList)
//                .flatMapIterable(Function.identity());
//    }

    // this is for both single and multiple file upload under `files` param key
    @Override
    public Flux<String> getLinesFromMap(Mono<MultiValueMap<String, Part>> filePartMap) {
        return filePartMap.flatMapIterable(map ->
                map.keySet().stream()
                        .filter(key -> key.equals("files"))
                        .flatMap(key -> map.get(key).stream().filter(part -> part instanceof FilePart))
                        .collect(Collectors.toList()))
                .flatMap(part -> getLines((FilePart) part));
    }

    public Flux<String> getLinesForExcel(String filePath) {
        var text = new StringBuilder("");
        try {
            var inp = new FileInputStream(filePath);
            var wb = new XSSFWorkbook(inp);

            for (Iterator<org.apache.poi.ss.usermodel.Sheet> it = wb.sheetIterator(); it.hasNext(); ) {
                var extractor = new XSSFExcelExtractor((XSSFWorkbook) it.next().getWorkbook());
                extractor.setFormulasNotResults(true);
                extractor.setIncludeSheetNames(false);
                text.append(extractor.getText());
            }
            inp.close();
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return Mono.just(text.toString())
                .map(this::processAndGetLinesAsList)
                .flatMapIterable(Function.identity());
    }

    private List<String> processAndGetLinesAsList(String string) {
        Supplier<Stream<String>> streamSupplier = string::lines;

        return streamSupplier.get().filter(s -> !s.isBlank()).collect(Collectors.toList());
    }

    class CloseCondition {
        Logger LOGGER = LoggerFactory.getLogger(CloseCondition.class);

        AtomicInteger tasksSubmitted = new AtomicInteger(0);
        AtomicInteger tasksCompleted = new AtomicInteger(0);
        AtomicBoolean allTaskssubmitted = new AtomicBoolean(false);

        /**
         * notify all tasks have been subitted, determine of the file channel can be closed
         *
         * @return true if the asynchronous file stream can be closed
         */
        public boolean canCloseOnComplete() {
            allTaskssubmitted.set(true);
            return tasksCompleted.get() == tasksSubmitted.get();
        }

        /**
         * notify a task has been submitted
         */
        public void onTaskSubmitted() {
            tasksSubmitted.incrementAndGet();
        }

        /**
         * notify a task has been completed
         *
         * @return true if the asynchronous file stream can be closed
         */
        public boolean onTaskCompleted() {
            boolean allSubmittedClosed = tasksSubmitted.get() == tasksCompleted.incrementAndGet();
            return allSubmittedClosed && allTaskssubmitted.get();
        }
    }

}
