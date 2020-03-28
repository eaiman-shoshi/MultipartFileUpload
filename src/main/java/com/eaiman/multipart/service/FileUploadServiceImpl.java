package com.eaiman.multipart.service;

import com.eaiman.multipart.utils.MultipartFileUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    // this is for multiple file upload
    @Override
    public Flux<String> getLines(Flux<FilePart> filePartFlux) {

        return filePartFlux.flatMap(this::getLines);
    }

    // this is for single file upload
    @Override
    public Flux<String> getLines(Mono<FilePart> filePartMono) {

        return filePartMono.flatMapMany(this::getLines);
    }

    // this is for single file upload
    @Override
    public Flux<String> getLines(FilePart filePart) {
        return filePart.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .flatMapIterable(this::processAndGetLinesAsList);
    }

    // this is for both single and multiple file upload under `files` param key
    @Override
    public Flux<String> getLinesFromMap(Mono<MultiValueMap<String, Part>> filePartMap) {
        return filePartMap.flatMapIterable(map ->
                map.keySet().stream()
                        .filter(key -> key.equals("files"))
                        .flatMap(key -> map.get(key).stream())
                        .filter(part -> part instanceof FilePart)
                        .collect(Collectors.toList()))
                .flatMap(part -> getLines((FilePart) part));
    }

    private List<String> processAndGetLinesAsList(String lines) {

        Supplier<Stream<String>> streamSupplier = () -> Arrays.stream(lines.split("\\r?\\n"));

        if (streamSupplier.get().allMatch(line -> line.matches(MultipartFileUploadUtils.REGEX_RULES))) {
            return streamSupplier.get().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
