import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {FileUploader} from 'ng2-file-upload';
const URL = 'https://evening-anchorage-3159.herokuapp.com/api/';

@Component({
  selector: 'app-file-upload-ui',
  templateUrl: './file-upload-ui.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrls: ['./file-upload-ui.component.scss']
})
export class FileUploadUiComponent implements OnInit {
  uploader: FileUploader = new FileUploader({
    url: URL,
    isHTML5: true
  });
  hasBaseDropZoneOver = false;
  hasAnotherDropZoneOver = false;

  constructor() { }

  ngOnInit() {
  }

  fileOverBase(e: any): void {
    this.hasBaseDropZoneOver = e;
  }

  fileOverAnother(e: any): void {
    this.hasAnotherDropZoneOver = e;
  }

}
