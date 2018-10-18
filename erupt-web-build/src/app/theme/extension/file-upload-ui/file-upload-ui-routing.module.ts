import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FileUploadUiComponent} from './file-upload-ui.component';

const routes: Routes = [
  {
    path: '',
    component: FileUploadUiComponent,
    data: {
      title: 'File Upload',
      icon: 'icon-layers',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - file upload',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FileUploadUiRoutingModule { }
