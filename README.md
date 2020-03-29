# MultipartFileUpload
<strong>Blog link:</strong> <a href="https://medium.com/@eaimanshoshi/step-by-step-procedure-of-spring-webflux-multipart-file-upload-and-read-each-line-without-saving-it-6a12be64f6ee">Step by Step Procedure of Spring Webflux multipart file upload and read each line without saving it</a>

This project is for tutorial purpose.<br />

<h2>Description & Feature</h2>
<ul>
  <li>Shown 4 ways to upload file(s)</li>
  <li>Single file upload. Shown implementaion using <code>Mono < FilePart > </code> and <code>FilePart</code></li>
  <li>Multiple file upload. Shown implementaion using <code>Flux< FilePart ></code> and <code>Mono< MultiValueMap< String, Part >></code></li>
  <li>Postman collection is included in this project</li>
  <li>Two test files are included in this project</li>
</ul>
<br />

<h3>Steps</h3>
<ul>
  <li><h5>Upload multiple file</h5>
  
  <ol>
    <li>Run the project</li>
    <li>Open Postman to access the api of this project</li>
    <li>Set url at<br /><code>http://localhost:8080/multipart-file/upload-flux</code> or<br/><code>http://localhost:8080/multipart-file/upload-map</code> and use POST method</li>
    <li>From body tab select <code>form-data</code></li>
    <li>Select type <code>File</code> for a key and name the key as <code>files</code></li>
    <li>In value section choose a file from your PC by browsing to upload</li>
    <li>Add files as many as you like to upload by following above two procedure</li>
  </ol>
 

For better understanding see the collection or this image:
<a href="https://drive.google.com/open?id=1eWto_4TtMVBKaHGiNakvVZEq8Xwb4bfL" target="_blank">File-Upload-Flux</a>
</li>


<li><h5>Upload single file</h5>

  <ol>
    <li>Run the project</li>
    <li>Open Postman to access the api of this project</li>
    <li>Set url at<br /><code>http://localhost:8080/multipart-file/upload-mono</code> or<br /><code>http://localhost:8080/multipart-file/upload-filePart</code> or<br /><code>http://localhost:8080/multipart-file/upload-map</code> and use POST method</li>
    <li>From body tab select <code>form-data</code></li>
    <li>Select type <code>File</code> for a key and name the key as <code>files</code> if you used <code>upload-multiValueMap</code> url, otherwise <code>file</code></li>
    <li>In value section choose a file from your PC by browsing to upload</li>
  </ol>

For better understanding see the collection or this image:
<a href="https://drive.google.com/open?id=1SZ9n5lh1K5dm4QjvRiQkGm_Rww9U3_tN" target="_blank">File-Upload-Mono or FilePart</a>
</li>
</ul>

<br />
Author's Profiles:
<ul>
  <li><a href="https://www.linkedin.com/in/eaimanshoshi">LinkedIn</a></li>
  <li><a href="https://medium.com/@eaimanshoshi">Medium</a></li>
</ul>
