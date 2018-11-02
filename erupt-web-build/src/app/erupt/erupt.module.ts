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
  MatSlideToggleModule, MatTableModule,
  MatDialogModule
} from "@angular/material";
import {UiSwitchModule} from "ngx-ui-switch";
import {DataTableComponent} from './data-table/data-table.component';
import {SharedModule} from "../shared/shared.module";


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
    MatDialogModule
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
