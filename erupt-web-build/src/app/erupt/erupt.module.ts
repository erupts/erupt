import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DataService} from "./service/DataService";
import {HttpClientModule} from "@angular/common/http";
import {EditTypeComponent} from './edit-type/edit-type.component';
import {TagInputModule} from "ngx-chips";
import {FormsModule} from "@angular/forms";
import {ColorPickerModule} from "ngx-color-picker";
import {
  MAT_DATE_LOCALE,
  MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule,
  MatNativeDateModule,
  MatRadioModule,
  MatSelectModule,
  MatSliderModule,
  MatSlideToggleModule, MatTableModule
} from "@angular/material";
import {BsDatepickerModule} from "ngx-bootstrap/datepicker";
import {defineLocale} from 'ngx-bootstrap/chronos';
import {zhCnLocale} from 'ngx-bootstrap/locale';
import {UiSwitchModule} from "ngx-ui-switch";
import {DataTableComponent} from './data-table/data-table.component';
import {SharedModule} from "../shared/shared.module";
defineLocale('zh-cn', zhCnLocale)


@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    SharedModule,
    HttpClientModule,
    TagInputModule,
    ColorPickerModule,
    MatSlideToggleModule,
    MatSelectModule,
    BsDatepickerModule.forRoot(),
    MatSliderModule,
    UiSwitchModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,

    MatDatepickerModule,
    MatIconModule,
    MatNativeDateModule,
    MatTableModule,
    MatRadioModule,

  ],
  providers: [
    DataService,
    {provide: MAT_DATE_LOCALE, useValue: 'zh'},
  ],
  exports: [
    EditTypeComponent,
    DataTableComponent
  ],
  declarations: [EditTypeComponent, DataTableComponent]
})
export class EruptModule {
}
