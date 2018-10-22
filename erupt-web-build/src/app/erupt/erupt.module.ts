import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DataService} from "./service/DataService";
import {HttpClientModule} from "@angular/common/http";
import {EditTypeComponent} from './edit-type/edit-type.component';
import {TagInputModule} from "ngx-chips";
import {FormsModule} from "@angular/forms";
import {ColorPickerModule} from "ngx-color-picker";
import {MatSelectModule, MatSlideToggleModule} from "@angular/material";
import {BrowserAnimationsModule, NoopAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    TagInputModule,
    ColorPickerModule,
    MatSlideToggleModule,
    MatSelectModule
  ],
  providers: [
    DataService
  ],
  exports: [
    EditTypeComponent
  ],
  declarations: [EditTypeComponent]
})
export class EruptModule {
}
