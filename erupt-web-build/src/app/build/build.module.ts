import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BuildRoutingModule} from "./build-routing.module";
import {EditComponent} from "./edit/edit.component";
import {SharedModule} from "../shared/shared.module";
import {EruptModule} from "../erupt/erupt.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    BuildRoutingModule,
    EruptModule,

  ],
  declarations: [EditComponent]
})
export class BuildModule {
}
