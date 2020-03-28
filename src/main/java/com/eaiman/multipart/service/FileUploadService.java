package com.eaiman.multipart.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileUploadService {
    // this is for multiple file upload
    Flux<String> getLines(Flux<FilePart> filePartFlux);

    // this is for single file upload.
    Flux<String> getLines(Mono<FilePart> filePartMono);

    // this is for single file upload.
    Flux<String> getLines(FilePart filePart);

    // this is for both single and multiple file upload under `files` param key
    Flux<String> getLinesFromMap(Mono<MultiValueMap<String, Part>> filePartMap);
}
