# erupt-file-center

A file center inside the admin: the upload directory, any extra folders and the S3 bucket, browsed in Finder-style columns with a preview of images, video, audio, PDF and text. Upload, new folder, rename, delete and download; copy a file's public link.

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-file-center</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

```yaml
erupt:
  file-center:
    roots:                         # extra local folders, by the name the page shows
      Reports: /data/reports
    stores:                        # any number of stores, local or S3, each with its own settings
      - name: Media
        type: s3
        bucket: media
        endpoint: https://oss-cn-hangzhou.aliyuncs.com   # MinIO, OSS, COS, R2: any S3 API
        region: cn-hangzhou
        access-key: ${MEDIA_AK}
        secret-key: ${MEDIA_SK}
        path-style: false
        prefix: public             # the store's root inside the bucket
        domain: https://cdn.example.com
      - name: Backups
        type: local
        path: /var/backups
    text-preview-bytes: 262144
```

## Stores

| Store | Where | Public link |
|---|---|---|
| Uploads | `erupt.upload-path`, what attachment fields write | `/erupt-attachment/...` |
| `erupt.file.roots` | any folder on the server | none, download through the page |
| S3 · bucket | the bucket attachments go to (`erupt.s3`), when erupt-data-s3 is on the classpath | the bucket URL or `erupt.s3.domain` |
| `erupt.file-center.stores` | any bucket or prefix with its own keys, or another local folder | the bucket URL or the store's `domain` |

Another store is a `FileStore` bean: implement it and it joins the list.

## Permissions

The page is one menu. Changing anything (upload, new folder, rename, delete) is the **Write** button under it, granted separately; without it the page is read only and the buttons are disabled.

## Page

Columns open to the right as folders are picked, like the Finder; the arrow keys move between them. Drop files on a column to upload there. The preview panel shows the file and its size, time and type, with open, download and copy-link actions. Every path is checked server side: nothing climbs out of a store.
