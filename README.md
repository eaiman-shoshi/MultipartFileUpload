# MultipartFileUpload
https://medium.com/@eaimanshoshi/step-by-step-procedure-of-spring-webflux-multipart-file-upload-and-read-each-line-without-saving-it-6a12be64f6ee

This project is for tutorial purpose.

<h3>Steps</h3>
<ul>
<h5>Upload multiple file</h5>
<ol>
<li>Run the project</li>
<li>Open Postman to access the api of this project</li>
<li>Set url at `http://localhost:8080/multipart-file/upload-flux` and use POST method</li>
<li>From body tab select `form-data`</li>
<li>Select type `File` for a key and name the key as `files`</li>
<li>In value section choose a file from your PC by browsing to upload</li>
<li>Add files as many as you like to upload by following above two procedure</li>
</ol>

For better understanding see the image:
<a href="https://drive.google.com/open?id=1eWto_4TtMVBKaHGiNakvVZEq8Xwb4bfL" target="_blank">File-Upload-Flux</a>

<br />
<li>
<h5>Upload single file</h5>

<ol>
<li>Run the project</li>
<li>Open Postman to access the api of this project</li>
<li>Set url at<br />`http://localhost:8080/multipart-file/upload-mono` or<br />`http://localhost:8080/multipart-file/upload-filePart`<br />and use POST method</li>
<li>From body tab select `form-data`</li>
<li>Select type `File` for a key and name the key as `file`</li>
<li>In value section choose a file from your PC by browsing to upload</li>
</ol>

For better understanding see the image:
<a href="https://drive.google.com/open?id=1SZ9n5lh1K5dm4QjvRiQkGm_Rww9U3_tN" target="_blank">File-Upload-Mono or FilePart</a>

</ul>
