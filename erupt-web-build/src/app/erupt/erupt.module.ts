import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DataService} from "./service/DataService";
import {HttpClientModule} from "@angular/common/http";
import {EditTypeComponent} from './edit-type/edit-type.component';
import {UiSwitchModule} from "ng2-ui-switch/dist";
import {TagInputModule} from "ngx-chips";
import {FormsModule} from "@angular/forms";
import {ColorPickerModule} from "ngx-color-picker";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    UiSwitchModule,
    TagInputModule,
    ColorPickerModule
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
