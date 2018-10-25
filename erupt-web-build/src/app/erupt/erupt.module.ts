import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DataService} from "./service/DataService";
import {HttpClientModule} from "@angular/common/http";
import {EditTypeComponent} from './edit-type/edit-type.component';
import {TagInputModule} from "ngx-chips";
import {FormsModule} from "@angular/forms";
import {ColorPickerModule} from "ngx-color-picker";
import {MatCheckboxModule, MatSelectModule, MatSliderModule, MatSlideToggleModule} from "@angular/material";
import {BsDatepickerModule} from "ngx-bootstrap/datepicker";
import {defineLocale} from 'ngx-bootstrap/chronos';
import {zhCnLocale} from 'ngx-bootstrap/locale';
import {UiSwitchModule} from "ngx-ui-switch";
import { DataTableComponent } from './data-table/data-table.component';
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
defineLocale('zh-cn', zhCnLocale)

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    TagInputModule,
    ColorPickerModule,
    MatSlideToggleModule,
    MatSelectModule,
    BsDatepickerModule.forRoot(),
    MatSliderModule,
    UiSwitchModule,
    NgxDatatableModule,
    MatCheckboxModule
  ],
  providers: [
    DataService
  ],
  exports: [
    EditTypeComponent,
    DataTableComponent
  ],
  declarations: [EditTypeComponent, DataTableComponent]
})
export class EruptModule {
}
