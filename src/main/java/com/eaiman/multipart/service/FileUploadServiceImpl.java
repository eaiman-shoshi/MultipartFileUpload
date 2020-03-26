package com.eaiman.multipart.service;

import com.eaiman.multipart.utils.MultipartFileUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Override
    public Flux<String> getLines(Flux<FilePart> filePartFlux) {

        return filePartFlux.flatMap(filePart ->
                filePart.content().map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return new String(bytes, StandardCharsets.UTF_8);
                }))
                .flatMapIterable(this::processAndGetLinesAsList);
    }

    private List<String> processAndGetLinesAsList(String lines) {

        Supplier<Stream<String>> streamSupplier = () -> Arrays.stream(lines.split("\\r?\\n"));

        if (streamSupplier.get().allMatch(line -> line.matches(MultipartFileUploadUtils.REGEX_RULES))) {
            return streamSupplier.get().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
